package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 小说知识图谱关系类型枚举。
 *
 * M0 维护 Project 到基础实体的拥有关系，M1 扩展出场、推进线索等剧情关系。
 */
@AllArgsConstructor
@Getter
public enum NovelGraphRelationEnum implements BaseEnum {

    /**
     * 项目拥有角色。
     */
    HAS_CHARACTER("HAS_CHARACTER", "项目拥有角色"),

    /**
     * 项目拥有地点。
     */
    HAS_LOCATION("HAS_LOCATION", "项目拥有地点"),

    /**
     * 项目拥有线索。
     */
    HAS_CLUE("HAS_CLUE", "项目拥有线索"),

    /**
     * 项目拥有章节。
     */
    HAS_CHAPTER("HAS_CHAPTER", "项目拥有章节"),

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
