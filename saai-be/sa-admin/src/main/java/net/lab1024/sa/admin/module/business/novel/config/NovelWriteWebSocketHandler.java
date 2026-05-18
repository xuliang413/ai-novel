package net.lab1024.sa.admin.module.business.novel.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelPromptVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelWriteSessionVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelLLMService;
import net.lab1024.sa.admin.module.business.novel.service.NovelPromptService;
import net.lab1024.sa.admin.module.business.novel.service.NovelWriteService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * 写作流式推送 WebSocket 处理器
 * <p>
 * 前端发送JSON消息控制写作流程: {"action":"start","projectId":1,"chapterNumber":5,"chapterGoal":"..."}
 * 后端逐token推送JSON: {"type":"token","data":"文"} 和 {"type":"complete","title":"...","sessionId":1}
 * 断线后后端继续生成完成，P1阶段通过/recover接口拉回完整结果。
 *
 * @Author AI-Novel
 */
@Slf4j
@Component
public class NovelWriteWebSocketHandler extends TextWebSocketHandler {

    /** JSON序列化工具, 用于解析前端指令和构建推送消息 */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private NovelPromptService novelPromptService;

    @Resource
    private NovelLLMService novelLLMService;

    @Resource
    private NovelWriteService novelWriteService;

    /**
     * WebSocket连接建立后, 等待前端发送start指令
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        log.info("WebSocket连接建立, sessionId={}, userId={}", session.getId(), userId);
    }

    /**
     * 处理前端消息 —— 接收JSON格式的写作指令。
     * <p>
     * 前端发送 {"action":"start","projectId":...} 触发流式写作。
     * 后续可能扩展 {"action":"recover","sessionId":...} 用于断线拉回。
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("未认证"));
            return;
        }

        String payload = message.getPayload();
        log.info("收到WebSocket消息, userId={}, payload={}", userId, payload);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = objectMapper.readValue(payload, Map.class);
            String action = (String) msg.get("action");

            if ("start".equals(action)) {
                handleStartWriting(session, msg, userId);
            } else if ("recover".equals(action)) {
                handleRecover(session, msg, userId);
            } else {
                sendError(session, "未知action: " + action);
            }
        } catch (Exception e) {
            log.error("解析WebSocket消息失败, userId={}", userId, e);
            sendError(session, "消息格式错误: " + e.getMessage());
        }
    }

    /**
     * 开始流式写作。
     * <p>
     * 组装Prompt后通过LangChain4j流式API逐token推送到前端。
     * 生成完成后发送complete事件(含标题/摘要/sessionId), 供前端跳转审阅页。
     */
    private void handleStartWriting(WebSocketSession session, Map<String, Object> msg, Long userId) {
        Long projectId = toLong(msg.get("projectId"));
        Integer chapterNumber = toInt(msg.get("chapterNo"));
        String chapterGoal = (String) msg.get("chapterGoal");
        String pov = (String) msg.get("pov");

        if (projectId == null || chapterNumber == null) {
            sendError(session, "缺少projectId或chapterNo");
            return;
        }

        try {
            ResponseDTO<NovelPromptVO> promptResult = novelPromptService.buildPrompt(
                    projectId, chapterNumber, chapterGoal, userId);
            if (!promptResult.getOk()) {
                sendError(session, "Prompt组装失败: " + promptResult.getMsg());
                return;
            }
            NovelPromptVO promptVO = promptResult.getData();

            StringBuilder buffer = new StringBuilder();

            sendEvent(session, "started", String.format(
                    "{\"chapterNumber\":%d}", chapterNumber));

            novelLLMService.generateChapterStream(
                    promptVO.getSystemPrompt(),
                    promptVO.getUserPrompt(),
                    userId,
                    new StreamingResponseHandler<AiMessage>() {
                        @Override
                        public void onNext(String token) {
                            try {
                                if (session.isOpen()) {
                                    buffer.append(token);
                                    String escaped = token
                                            .replace("\\", "\\\\")
                                            .replace("\"", "\\\"")
                                            .replace("\n", "\\n")
                                            .replace("\r", "\\r")
                                            .replace("\t", "\\t");
                                    sendEvent(session, "token", "\"" + escaped + "\"");
                                }
                            } catch (Exception e) {
                                log.error("推送token失败", e);
                            }
                        }

                        @Override
                        public void onComplete(Response<AiMessage> response) {
                            String fullText = response.content().text();
                            NovelWriteSessionVO sessionVO = null;
                            try {
                                sessionVO = novelWriteService.onStreamWriteComplete(
                                        projectId, chapterNumber, fullText,
                                        pov, chapterGoal, "DEEPSEEK", userId);

                                sendEvent(session, "complete", String.format(
                                        "{\"sessionId\":%d,\"title\":\"%s\",\"wordCount\":%d,\"chapterNumber\":%d,\"status\":\"%s\"}",
                                        sessionVO.getSessionId(),
                                        sessionVO.getTitle() != null ? sessionVO.getTitle().replace("\"", "\\\"") : "",
                                        fullText.length(),
                                        chapterNumber,
                                        sessionVO.getStatus()));

                                log.info("流式写作完成并持久化 sessionId={}, wordCount={}",
                                        sessionVO.getSessionId(), fullText.length());
                            } catch (Exception e) {
                                log.error("流式写作完成后持久化失败, chapterNumber={}", chapterNumber, e);
                                sendError(session, "写作完成但保存失败: " + e.getMessage());
                            }
                        }

                        @Override
                        public void onError(Throwable error) {
                            log.error("流式写作异常, chapterNumber={}", chapterNumber, error);
                            sendError(session, "生成失败: " + error.getMessage());
                        }
                    });

        } catch (Exception e) {
            log.error("流式写作启动失败, projectId={}, chapterNumber={}", projectId, chapterNumber, e);
            sendError(session, "写作启动失败: " + e.getMessage());
        }
    }

    /**
     * 断线恢复 —— 通过sessionId查询已持久化的写作会话。
     * P1阶段实现完整版，当前为基础骨架。
     */
    private void handleRecover(WebSocketSession session, Map<String, Object> msg, Long userId) {
        Long sessionId = toLong(msg.get("sessionId"));
        if (sessionId == null) {
            sendError(session, "缺少sessionId");
            return;
        }
        NovelWriteSessionVO vo = novelWriteService.querySession(sessionId, userId).getData();
        if (vo != null) {
            sendEvent(session, "recover", String.format(
                    "{\"sessionId\":%d,\"status\":\"%s\",\"wordCount\":%d}",
                    sessionId,
                    vo.getStatus() != null ? vo.getStatus() : "UNKNOWN",
                    vo.getWordCount() != null ? vo.getWordCount() : 0));
        } else {
            sendError(session, "会话不存在或无权访问");
        }
    }

    /**
     * 连接关闭时清理资源。
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        log.info("WebSocket连接关闭, sessionId={}, userId={}, status={}", session.getId(), userId, status);
    }

    /**
     * 发送事件类型的JSON消息到前端。
     */
    private void sendEvent(WebSocketSession session, String type, String data) {
        try {
            String json = String.format("{\"type\":\"%s\",\"data\":%s}", type, data);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error("发送WebSocket事件失败, type={}", type, e);
        }
    }

    /**
     * 发送错误消息到前端。
     */
    private void sendError(WebSocketSession session, String error) {
        try {
            String escaped = error.replace("\"", "\\\"");
            String json = String.format("{\"type\":\"error\",\"data\":\"%s\"}", escaped);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error("发送WebSocket错误消息失败", e);
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(value.toString()); } catch (NumberFormatException e) { return null; }
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); } catch (NumberFormatException e) { return null; }
    }
}
