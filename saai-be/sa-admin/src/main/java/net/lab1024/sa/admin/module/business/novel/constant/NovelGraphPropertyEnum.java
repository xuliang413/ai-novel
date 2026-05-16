package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Neo4j系统属性名常量
 * 定义图谱节点和关系中使用的系统字段名, 避免硬编码
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelGraphPropertyEnum {

    /**
     * 项目ID, 所有节点必带, 用于项目隔离
     */
    PROJECT_ID("projectId", "项目ID"),

    /**
     * 归档标记, true表示已归档不出现在检索中
     */
    ARCHIVED("archived", "归档标记"),

    /**
     * 节点在MySQL中对应的业务主键ID
     */
    ENTITY_ID("entityId", "业务主键ID"),

    /**
     * 实体名称, 冗余存储便于Cypher展示
     */
    NAME("name", "名称"),

    /**
     * 章节号
     */
    CHAPTER_NUMBER("chapterNumber", "章节号"),

    /**
     * 存活/实体状态
     */
    CURRENT_STATUS("currentStatus", "当前状态"),

    /**
     * 线索状态: DORMANT/ACTIVE/RESOLVED
     */
    CLUE_STATUS("clueStatus", "线索状态"),

    /**
     * 当前情绪
     */
    CURRENT_EMOTION("currentEmotion", "当前情绪"),

    /**
     * 当前位置关系属性
     */
    SINCE_CHAPTER("sinceChapter", "自某章起"),

    /**
     * 进度/揭露程度
     */
    REVEAL_LEVEL("revealLevel", "揭露程度"),

    /**
     * 摘要
     */
    SUMMARY("summary", "摘要"),

    /**
     * POV视角人物名
     */
    POV("pov", "视角人物"),

    /**
     * 类型
     */
    TYPE("type", "类型"),

    /**
     * 向量化embedding
     */
    EMBEDDING("embedding", "向量");

    /**
     * 属性名, 对应Neo4j节点属性key
     */
    private final String propertyName;

    /**
     * 中文描述
     */
    private final String desc;
}
