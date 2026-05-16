package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Neo4j节点标签枚举
 * 11种节点类型Label: Project/Volume/Chapter/Character/Location/Clue/Item/Event/Cheat/Alias/NarrativeRule
 * 标签只表达实体类型, 不拼项目ID, 项目归属通过projectId属性表达
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelGraphNodeEnum {

    /**
     * 项目节点
     */
    Project("Project", "项目"),

    /**
     * 卷节点
     */
    Volume("Volume", "卷"),

    /**
     * 章节节点
     */
    Chapter("Chapter", "章节"),

    /**
     * 角色节点
     */
    Character("Character", "角色"),

    /**
     * 地点节点
     */
    Location("Location", "地点"),

    /**
     * 线索节点
     */
    Clue("Clue", "线索"),

    /**
     * 物品节点
     */
    Item("Item", "物品"),

    /**
     * 事件节点
     */
    Event("Event", "事件"),

    /**
     * 金手指节点
     */
    Cheat("Cheat", "金手指"),

    /**
     * 马甲节点
     */
    Alias("Alias", "马甲"),

    /**
     * 叙事规则节点
     */
    NarrativeRule("NarrativeRule", "叙事规则");

    /**
     * Neo4j标签名
     */
    private final String label;

    /**
     * 中文描述
     */
    private final String desc;
}
