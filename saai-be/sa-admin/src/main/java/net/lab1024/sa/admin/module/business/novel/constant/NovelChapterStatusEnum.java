package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 章节状态枚举
 * DRAFT草稿(AI生成完待审阅), PENDING_GRAPH_CONFIRM等图谱确认,
 * PUBLISHED已发布(图谱更新完成), PENDING_GRAPH_UPDATE图谱写入失败待重试,
 * INTERRUPTED_DRAFT生成中断的草稿
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelChapterStatusEnum implements BaseEnum {

    /**
     * 草稿, AI生成完待审阅
     */
    DRAFT("DRAFT", "草稿"),

    /**
     * 等图谱确认, GraphPatch审阅中
     */
    PENDING_GRAPH_CONFIRM("PENDING_GRAPH_CONFIRM", "等图谱确认"),

    /**
     * 已发布, 图谱更新完成
     */
    PUBLISHED("PUBLISHED", "已发布"),

    /**
     * 图谱更新待重试, Neo4j写入失败
     */
    PENDING_GRAPH_UPDATE("PENDING_GRAPH_UPDATE", "图谱更新待重试"),

    /**
     * 中断草稿, 生成过程被中断
     */
    INTERRUPTED_DRAFT("INTERRUPTED_DRAFT", "中断草稿");

    private final String value;

    private final String desc;
}
