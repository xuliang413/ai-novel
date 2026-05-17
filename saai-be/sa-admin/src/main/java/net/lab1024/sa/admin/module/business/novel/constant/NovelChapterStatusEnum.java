package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 章节状态枚举。
 * <p>
 * 用于标记章节从草稿、图谱确认到发布的写作流程状态，管理页按它筛选章节，写作引擎按它决定下一步动作。
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

    /**
     * 枚举值，写入 MySQL 的 status，并同步到 Neo4j Chapter 节点。
     */
    private final String value;

    /**
     * 中文说明，用于管理页展示和接口文档说明。
     */
    private final String desc;
}
