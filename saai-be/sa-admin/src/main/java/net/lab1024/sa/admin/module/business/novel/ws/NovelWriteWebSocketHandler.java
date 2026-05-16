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
 * 写作流式 WebSocket Handler（v2 —— 统一事件协议）
 *
 * 连接：ws://host/ws/novel/write?token=xxx
 * 客户端→服务端：{"action":"start","projectId":2,...}  启动生成
 * 客户端→服务端：{"action":"cancel"}                     取消生成
 * 客户端→服务端：{"action":"recover","sessionId":42}     断线重连恢复
 * 客户端→服务端：{"action":"ping"}                       心跳
 * 服务端→客户端统一为 NovelWebSocketEventVO，eventType 包括：
 * token | contentReady | canceled | failed | error | heartbeat 等。
 * 每个事件固定字段：eventType, sessionId, chapterId, timestamp, payload, message
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

    /**
     * 写作会话 ID 映射。key = wsSessionId, value = 当前写作 sessionId
     */
    private final Map<String, Long> activeWriteSessions = new ConcurrentHashMap<>();

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
        log.info("WebSocket 收到消息：wsSessionId={}, payload={}", session.getId(),
                payload.length() > 200 ? payload.substring(0, 200) + "..." : payload);

        try {
            JSONObject json = JSON.parseObject(payload);
            String action = json.getString("action");

            if ("ping".equals(action)) {
                sendEventQuiet(session, NovelWebSocketEventVO.heartbeat());
                return;
            }

            if ("cancel".equals(action)) {
                cancelGeneration(session);
                return;
            }

            if ("start".equals(action)) {
                if (cancelFlags.containsKey(session.getId())) {
                    sendEventQuiet(session, NovelWebSocketEventVO.error(null, "已有生成任务在进行中"));
                    return;
                }
                startGeneration(session, json);
                return;
            }

            if ("recover".equals(action)) {
                recoverSession(session, json);
                return;
            }

            sendEventQuiet(session, NovelWebSocketEventVO.error(null, "未知 action: " + action));
        } catch (Exception e) {
            log.error("WebSocket 消息处理失败", e);
            sendEventQuiet(session, NovelWebSocketEventVO.error(null, "消息格式错误: " + e.getMessage()));
        }
    }

    private void startGeneration(WebSocketSession session, JSONObject json) {
        NovelWriteStartForm form = json.toJavaObject(NovelWriteStartForm.class);
        if (form == null || form.getProjectId() == null) {
            sendEventQuiet(session, NovelWebSocketEventVO.error(null, "缺少 projectId"));
            return;
        }

        AtomicBoolean cancelFlag = new AtomicBoolean(false);
        cancelFlags.put(session.getId(), cancelFlag);

        novelWriteService.startStream(form,
                token -> {
                    if (cancelFlag.get()) return;
                    sendEventQuiet(session, NovelWebSocketEventVO.token(null, token));
                },
                result -> {
                    Long writeSessionId = result.getSessionId();
                    Long chapterId = result.getChapter() != null ? result.getChapter().getChapterId() : null;
                    activeWriteSessions.put(session.getId(), writeSessionId);
                    cleanup(session);
                    sendEventQuiet(session, NovelWebSocketEventVO.done(writeSessionId, chapterId, result));
                },
                errorMsg -> {
                    cleanup(session);
                    sendEventQuiet(session, NovelWebSocketEventVO.error(null, errorMsg));
                });
    }

    private void cancelGeneration(WebSocketSession session) {
        AtomicBoolean cancel = cancelFlags.get(session.getId());
        if (cancel != null) {
            cancel.set(true);
        }
        Long writeSessionId = activeWriteSessions.get(session.getId());
        sendEventQuiet(session, NovelWebSocketEventVO.cancelled(writeSessionId));
    }

    private void recoverSession(WebSocketSession session, JSONObject json) {
        Long recoverSessionId = json.getLong("sessionId");
        if (recoverSessionId == null) {
            sendEventQuiet(session, NovelWebSocketEventVO.error(null, "缺少 sessionId 参数"));
            return;
        }
        NovelWriteDraftVO result = novelWriteService.recoverStreamSession(recoverSessionId);
        if (result == null) {
            sendEventQuiet(session, NovelWebSocketEventVO.error(recoverSessionId, "会话不存在或已被清理"));
            return;
        }
        Long chapterId = result.getChapter() != null ? result.getChapter().getChapterId() : null;
        sendEventQuiet(session, NovelWebSocketEventVO.done(recoverSessionId, chapterId, result));
    }

    private void cleanup(WebSocketSession session) {
        cancelFlags.remove(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cancelFlags.remove(session.getId());
        authenticatedUsers.remove(session.getId());
        activeWriteSessions.remove(session.getId());
        log.info("WebSocket 写作连接已关闭：wsSessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket 传输错误：wsSessionId={}", session.getId(), exception);
        cancelFlags.remove(session.getId());
        authenticatedUsers.remove(session.getId());
        activeWriteSessions.remove(session.getId());
    }

    private void sendEvent(WebSocketSession session, NovelWebSocketEventVO event) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(JSON.toJSONString(event)));
        }
    }

    private void sendEventQuiet(WebSocketSession session, NovelWebSocketEventVO event) {
        try {
            sendEvent(session, event);
        } catch (IOException e) {
            log.warn("WebSocket 推送事件失败：wsSessionId={}, eventType={}", session.getId(), event.getEventType());
        }
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
