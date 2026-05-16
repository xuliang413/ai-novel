package net.lab1024.sa.admin.module.business.novel.ws;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 事件统一模型 —— 前端只需监听标准事件，不再散落 event 字符串
 *
 * 为什么需要统一事件结构：
 * 之前每个回调直接拼 event + data 的 Map，事件名散落在代码各处（"token"、"done"、"error"）。
 * 前端如果写死这些字符串，一旦加新事件或改名就要改前端。现在把事件名和字段都固定在枚举和 VO 里，
 * 前端按 eventType 做 switch 即可，新事件加进来也不会破坏已有逻辑。
 *
 * 每个事件固定携带：eventType（事件类型）、sessionId（会话 ID）、chapterId（章节 ID）、
 * requestId（请求追踪 ID）、timestamp（时间戳）、payload（业务数据）、message（人读消息）。
 *
 * 事件含义：
 * - sessionCreated   → 写作会话已创建，前端可以开始展示进度条
 * - intentParsed     → 意图解析完成，前端展示写作目标确认界面
 * - contextReady     → 上下文检索完成，前端展示上下文审阅面板
 * - contextConfirmed → 用户确认上下文，即将开始生成
 * - generationStarted→ LLM 开始生成，前端展示流式输出区域
 * - token            → 逐字推送，前端追加到正文末尾
 * - contentReady     → 正文生成完毕，前端展示正文审阅界面
 * - patchReady       → GraphPatch 已抽取，前端展示图谱变更确认面板
 * - patchApplied     → 图谱变更已写入，章节发布完成
 * - recoverDone      → 恢复操作完成
 * - canceled         → 用户取消生成
 * - failed           → 流程失败
 * - error            → 通用错误
 * - heartbeat        → 心跳保活（pong 是服务端回复 ping）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelWebSocketEventVO {

    @Schema(description = "事件类型", example = "token")
    private String eventType;

    @Schema(description = "写作会话 ID", example = "42")
    private Long sessionId;

    @Schema(description = "章节 ID", example = "15")
    private Long chapterId;

    @Schema(description = "请求追踪 ID（用于排错）", example = "req_abc123")
    private String requestId;

    @Schema(description = "时间戳（毫秒）", example = "1715000000000")
    private Long timestamp;

    @Schema(description = "业务负载数据（根据 eventType 不同而不同）")
    private Object payload;

    @Schema(description = "人读消息（错误提示/状态说明）", example = "生成成功")
    private String message;

    // ==================== 事件类型常量 —— 前端按这些值做 switch ====================

    public static final String EVENT_SESSION_CREATED = "sessionCreated";
    public static final String EVENT_INTENT_PARSED = "intentParsed";
    public static final String EVENT_CONTEXT_READY = "contextReady";
    public static final String EVENT_CONTEXT_CONFIRMED = "contextConfirmed";
    public static final String EVENT_GENERATION_STARTED = "generationStarted";
    public static final String EVENT_TOKEN = "token";
    public static final String EVENT_CONTENT_READY = "contentReady";
    public static final String EVENT_PATCH_READY = "patchReady";
    public static final String EVENT_PATCH_APPLIED = "patchApplied";
    public static final String EVENT_RECOVER_DONE = "recoverDone";
    public static final String EVENT_CANCELED = "canceled";
    public static final String EVENT_FAILED = "failed";
    public static final String EVENT_ERROR = "error";
    public static final String EVENT_HEARTBEAT = "heartbeat";

    // ==================== 工厂方法 ====================

    public static NovelWebSocketEventVO token(Long sessionId, String tokenText) {
        return builder()
                .eventType(EVENT_TOKEN).sessionId(sessionId)
                .timestamp(System.currentTimeMillis())
                .payload(tokenText).build();
    }

    public static NovelWebSocketEventVO done(Long sessionId, Long chapterId, Object result) {
        return builder()
                .eventType(EVENT_CONTENT_READY).sessionId(sessionId).chapterId(chapterId)
                .timestamp(System.currentTimeMillis())
                .payload(result).message("生成完成").build();
    }

    public static NovelWebSocketEventVO error(Long sessionId, String errorMessage) {
        return builder()
                .eventType(EVENT_ERROR).sessionId(sessionId)
                .timestamp(System.currentTimeMillis())
                .message(errorMessage).build();
    }

    public static NovelWebSocketEventVO cancelled(Long sessionId) {
        return builder()
                .eventType(EVENT_CANCELED).sessionId(sessionId)
                .timestamp(System.currentTimeMillis())
                .message("生成已取消").build();
    }

    public static NovelWebSocketEventVO failed(Long sessionId, String reason) {
        return builder()
                .eventType(EVENT_FAILED).sessionId(sessionId)
                .timestamp(System.currentTimeMillis())
                .message(reason).build();
    }

    public static NovelWebSocketEventVO patchReady(Long sessionId, Long chapterId, Object patch) {
        return builder()
                .eventType(EVENT_PATCH_READY).sessionId(sessionId).chapterId(chapterId)
                .timestamp(System.currentTimeMillis())
                .payload(patch).message("图谱变更已就绪").build();
    }

    public static NovelWebSocketEventVO patchApplied(Long sessionId, Long chapterId) {
        return builder()
                .eventType(EVENT_PATCH_APPLIED).sessionId(sessionId).chapterId(chapterId)
                .timestamp(System.currentTimeMillis())
                .message("图谱变更已应用").build();
    }

    public static NovelWebSocketEventVO sessionCreated(Long sessionId, Long chapterId) {
        return builder()
                .eventType(EVENT_SESSION_CREATED).sessionId(sessionId).chapterId(chapterId)
                .timestamp(System.currentTimeMillis())
                .message("写作会话已创建").build();
    }

    public static NovelWebSocketEventVO heartbeat() {
        return builder()
                .eventType(EVENT_HEARTBEAT)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
