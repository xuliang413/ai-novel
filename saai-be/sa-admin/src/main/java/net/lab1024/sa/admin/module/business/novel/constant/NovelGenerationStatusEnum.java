package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 章节生成会话状态枚举。
 *
 * 这是写作流程的状态机，排查“卡在哪一步”时优先看 t_chapter_generation_session.status。
 */
@AllArgsConstructor
@Getter
public enum NovelGenerationStatusEnum implements BaseEnum {

    /**
     * AI 正在生成正文。
     */
    GENERATING("GENERATING", "生成中"),

    /**
     * 正文已经生成，等待用户审阅。
     */
    CONTENT_REVIEW("CONTENT_REVIEW", "正文审阅"),

    /**
     * 正在从正文抽取 GraphPatch。
     */
    EXTRACTING_PATCH("EXTRACTING_PATCH", "抽取图谱变更"),

    /**
     * GraphPatch 已生成，等待用户确认。
     */
    PATCH_REVIEW("PATCH_REVIEW", "图谱变更确认"),

    /**
     * 用户已确认，正在写 Neo4j。
     */
    APPLYING_PATCH("APPLYING_PATCH", "执行图谱变更"),

    /**
     * 正文发布和图谱写入都成功。
     */
    SUCCESS("SUCCESS", "生成成功"),

    /**
     * 生成失败。
     */
    FAILED("FAILED", "生成失败"),

    /**
     * 流式生成被打断。
     */
    INTERRUPTED("INTERRUPTED", "生成中断");

    /**
     * 持久化和接口传输使用的值。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;
}
