package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 章节状态枚举。
 *
 * 章节不是一生成就发布，它要先过正文审阅，再过图谱变更确认。
 */
@AllArgsConstructor
@Getter
public enum NovelChapterStatusEnum implements BaseEnum {

    /**
     * 草稿，正文可继续改。
     */
    DRAFT("DRAFT", "草稿"),

    /**
     * 正文过了，正在等用户确认图谱变更。
     */
    PENDING_GRAPH_CONFIRM("PENDING_GRAPH_CONFIRM", "待确认图谱变更"),

    /**
     * 正文存在，但图谱还没同步好，通常出现在写图谱失败或撤销后。
     */
    PENDING_GRAPH_UPDATE("PENDING_GRAPH_UPDATE", "待同步图谱"),

    /**
     * 已发布，正文和图谱都完成确认。
     */
    PUBLISHED("PUBLISHED", "已发布"),

    /**
     * 生成被打断时留下的草稿。
     */
    INTERRUPTED_DRAFT("INTERRUPTED_DRAFT", "中断草稿");

    /**
     * 持久化和接口传输使用的值。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;
}
