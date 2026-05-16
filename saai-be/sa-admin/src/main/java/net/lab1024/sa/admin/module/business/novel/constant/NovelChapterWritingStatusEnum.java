package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 章节写作状态机枚举
 *
 * 这是写作工作台页面流转的依据。每一次写作会话从 IDLE 开始，经过意图解析、上下文审阅、
 * LLM 生成、正文审阅、图谱变更确认，最终到达 SUCCESS（发布完成）或失败/取消终止。
 *
 * 状态迁移规则（哪些状态能跳哪些）：
 * IDLE               → INTENT_PARSED               （用户输入写作目标）
 * INTENT_PARSED      → CONTEXT_REVIEWING           （系统检索上下文）
 * CONTEXT_REVIEWING  → CONTEXT_CONFIRMED           （用户确认上下文）
 * CONTEXT_CONFIRMED  → GENERATING                  （开始调用 LLM）
 * GENERATING         → CONTENT_REVIEWING / FAILED / CANCELED
 * CONTENT_REVIEWING  → PATCH_PENDING               （正文通过，抽取 GraphPatch）
 * PATCH_PENDING      → PATCH_APPLIED / CONTENT_REVIEWING （确认图谱 / 返回审阅）
 * PATCH_APPLIED      → SUCCESS                     （发布完成）
 * *_ → RECOVERED                                   （通过 /recover 恢复会话）
 * *_ → FAILED                                      （任何步骤都可能失败）
 * GENERATING         → CANCELED                    （用户主动取消）
 *
 * 单章节互斥规则：
 * 同一 chapter 同一时间只允许一个非终态会话（IDLE 不算），防止并发写作把数据冲乱。
 *
 * 取代了旧的 NovelGenerationStatusEnum，那个只覆盖了生成-发布后半段，
 * 现在把意图解析和上下文审阅也纳入标准状态机。
 */
@AllArgsConstructor
@Getter
public enum NovelChapterWritingStatusEnum implements BaseEnum {

    /**
     * 初始状态 —— 还没有开始写作会话，也没有活跃的会话。
     */
    IDLE("IDLE", "空闲"),

    /**
     * 用户已输入写作目标，系统已解析意图。
     * 前端展示"确认写作目标"界面。
     */
    INTENT_PARSED("INTENT_PARSED", "意图已解析"),

    /**
     * 系统正在检索上下文，或已完成检索等待用户审阅。
     * 前端展示上下文审阅面板。
     */
    CONTEXT_REVIEWING("CONTEXT_REVIEWING", "上下文审阅中"),

    /**
     * 用户已确认上下文，可以开始生成正文。
     */
    CONTEXT_CONFIRMED("CONTEXT_CONFIRMED", "上下文已确认"),

    /**
     * LLM 正在生成正文（流式输出中）。
     */
    GENERATING("GENERATING", "生成中"),

    /**
     * 正文已生成完毕，等待用户审阅。
     */
    CONTENT_REVIEWING("CONTENT_REVIEWING", "正文审阅中"),

    /**
     * GraphPatch 已抽取，等待用户确认图谱变更。
     */
    PATCH_PENDING("PATCH_PENDING", "待确认图谱变更"),

    /**
     * 图谱变更已写入 Neo4j，章节发布完成。终态。
     */
    SUCCESS("SUCCESS", "已完成"),

    /**
     * 已通过 /undo 撤回图谱变更。需要 /recover 恢复。
     */
    RECOVERED("RECOVERED", "已回退"),

    /**
     * 用户主动取消。终态。
     */
    CANCELED("CANCELED", "已取消"),

    /**
     * 流程失败。终态（可通过 /recover 尝试恢复）。
     */
    FAILED("FAILED", "生成失败");

    private final String value;
    private final String desc;

    public static final java.util.Set<String> TERMINAL_STATUSES = java.util.Set.of(
            SUCCESS.getValue(),
            CANCELED.getValue(),
            FAILED.getValue()
    );

    public boolean isTerminal() {
        return TERMINAL_STATUSES.contains(this.getValue());
    }

    public boolean isActive() {
        return !isTerminal() && this != IDLE;
    }
}
