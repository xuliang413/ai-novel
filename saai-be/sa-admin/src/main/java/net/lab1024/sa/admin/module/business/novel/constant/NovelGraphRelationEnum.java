package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 小说知识图谱关系类型枚举。
 *
 * 结构关系 CONTAINS + PREVIOUS，剧情关系 APPEARS_IN、ADVANCES。
 */
@AllArgsConstructor
@Getter
public enum NovelGraphRelationEnum implements BaseEnum {

    /**
     * 项目包含实体（Character/Location/Clue）。
     * 方案 §4.3：Project-[:CONTAINS]->Volume-[:CONTAINS]->Chapter。
     * Volume 未建时 Project-[:CONTAINS]->Chapter 直连。
     */
    CONTAINS("CONTAINS", "项目包含实体"),

    /**
     * 上一章 → 下一章。方案 §4.3：建立写作链路。
     */
    PREVIOUS("PREVIOUS", "上一章到下一章"),

    /**
     * 实体在章节中出场。
     */
    APPEARS_IN("APPEARS_IN", "实体在章节中出场"),

    /**
     * 章节推进线索。
     */
    ADVANCES("ADVANCES", "章节推进线索");

    private final String value;

    private final String desc;

    /**
     * 返回 Neo4j 关系类型名称。
     */
    public String type() {
        return value;
    }
}
