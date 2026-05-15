package net.lab1024.sa.admin.module.business.novel.ws;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelWriteStartForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelWriteDraftVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelWriteService;
import net.lab1024.sa.admin.module.system.login.domain.RequestEmployee;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 写作流式 WebSocket Handler。
 *
 * 协议：
 * 连接：ws://host/ws/novel/write?token=xxx
 * 客户端→服务端：{"action":"start","projectId":2,...}  启动生成
 * 客户端→服务端：{"action":"cancel"}                     取消生成
 * 服务端→客户端：{"event":"token","data":"..."}          逐字推送
 * 服务端→客户端：{"event":"done","data":{...}}           生成完成
 * 服务端→客户端：{"event":"error","data":"..."}          出错
 */
@Slf4j
@Component
public class NovelWriteWebSocketHandler extends TextWebSocketHandler {

    private final NovelWriteService novelWriteService;

    /**
     * 正在生成中的 session 取消标记。
     * key = WebSocket session id, value = cancel flag
     */
    private final Map<String, AtomicBoolean> cancelFlags = new ConcurrentHashMap<>();

    /**
     * 已鉴权的用户 ID 缓存。key = WebSocket session id, value = userId
     */
    private final Map<String, Long> authenticatedUsers = new ConcurrentHashMap<>();

    public NovelWriteWebSocketHandler(NovelWriteService novelWriteService) {
        this.novelWriteService = novelWriteService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            close(session, CloseStatus.BAD_DATA, "Missing URI");
            return;
        }

        String query = uri.getQuery();
        if (query == null || !query.contains("token=")) {
            close(session, CloseStatus.POLICY_VIOLATION, "Missing token");
            return;
        }

        String token = extractParam(query, "token");
        if (token == null) {
            close(session, CloseStatus.POLICY_VIOLATION, "Missing token");
            return;
        }

        try {
            String loginId = (String) StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                close(session, CloseStatus.POLICY_VIOLATION, "Invalid token");
                return;
            }
            authenticatedUsers.put(session.getId(), extractUserId(loginId));
            log.info("WebSocket 写作连接已建立：sessionId={}, loginId={}", session.getId(), loginId);
        } catch (Exception e) {
            log.error("WebSocket 鉴权失败", e);
            close(session, CloseStatus.POLICY_VIOLATION, "Auth failed");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();

        // WebSocket 线程不在 HTTP 请求上下文里，需要手动把当前用户注入 ThreadLocal，
        // 这样下游的 AdminRequestUtil.getRequestUserId() 才能拿到正确的 userId
        Long userId = authenticatedUsers.get(session.getId());
        if (userId != null) {
            RequestEmployee requestUser = new RequestEmployee();
            requestUser.setEmployeeId(userId);
            SmartRequestUtil.setRequestUser(requestUser);
        }
        try {
            processMessage(session, payload);
        } finally {
            if (userId != null) {
                SmartRequestUtil.remove();
            }
        }
    }

    private void processMessage(WebSocketSession session, String payload) {
        log.info("WebSocket 收到消息：sessionId={}, payload={}", session.getId(),
                payload.length() > 200 ? payload.substring(0, 200) + "..." : payload);

        try {
            JSONObject json = JSON.parseObject(payload);

            if ("cancel".equals(json.getString("action"))) {
                AtomicBoolean cancel = cancelFlags.get(session.getId());
                if (cancel != null) {
                    cancel.set(true);
                }
                sendJson(session, "event", "cancelled", "data", "已取消");
                return;
            }

            if ("start".equals(json.getString("action"))) {
                if (cancelFlags.containsKey(session.getId())) {
                    sendJson(session, "event", "error", "data", "已有生成任务在进行中");
                    return;
                }
                startGeneration(session, json);
                return;
            }

            sendJson(session, "event", "error", "data", "未知 action: " + json.getString("action"));
        } catch (Exception e) {
            log.error("WebSocket 消息处理失败", e);
            try {
                sendJson(session, "event", "error", "data", "消息格式错误: " + e.getMessage());
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }

    private void startGeneration(WebSocketSession session, JSONObject json) {
        NovelWriteStartForm form = json.toJavaObject(NovelWriteStartForm.class);
        if (form == null || form.getProjectId() == null) {
            try {
                sendJson(session, "event", "error", "data", "缺少 projectId");
            } catch (IOException e) {
                log.error("WebSocket 发送错误失败", e);
            }
            return;
        }

        AtomicBoolean cancelFlag = new AtomicBoolean(false);
        cancelFlags.put(session.getId(), cancelFlag);

        novelWriteService.startStream(form,
                token -> {
                    if (cancelFlag.get()) return;
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(
                                    JSON.toJSONString(Map.of("event", "token", "data", token))));
                        }
                    } catch (IOException e) {
                        log.warn("WebSocket 推送 token 失败：sessionId={}", session.getId());
                        cancelFlag.set(true);
                    }
                },
                result -> {
                    cancelFlags.remove(session.getId());
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(
                                    JSON.toJSONString(Map.of("event", "done", "data", result))));
                        }
                    } catch (IOException e) {
                        log.error("WebSocket 推送 done 失败", e);
                    }
                },
                errorMsg -> {
                    cancelFlags.remove(session.getId());
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(
                                    JSON.toJSONString(Map.of("event", "error", "data", errorMsg))));
                        }
                    } catch (IOException e) {
                        log.error("WebSocket 推送 error 失败", e);
                    }
                });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cancelFlags.remove(session.getId());
        authenticatedUsers.remove(session.getId());
        log.info("WebSocket 写作连接已关闭：sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket 传输错误：sessionId={}", session.getId(), exception);
        cancelFlags.remove(session.getId());
        authenticatedUsers.remove(session.getId());
    }

    private void sendJson(WebSocketSession session, String... keyValues) throws IOException {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        session.sendMessage(new TextMessage(JSON.toJSONString(map)));
    }

    private void close(WebSocketSession session, CloseStatus status, String reason) {
        try {
            session.close(new CloseStatus(status.getCode(), reason));
        } catch (IOException e) {
            log.error("关闭 WebSocket 连接失败", e);
        }
    }

    private String extractParam(String query, String key) {
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && pair[0].equals(key)) {
                return pair[1];
            }
        }
        return null;
    }

    /**
     * 从 Sa-Token loginId（格式"admin_employee:1"）中提取 userId。
     */
    private Long extractUserId(String loginId) {
        int colon = loginId.lastIndexOf(':');
        return Long.parseLong(colon < 0 ? loginId : loginId.substring(colon + 1));
    }
}
