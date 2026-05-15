package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;
/**
 * 小说知识图谱节点类型枚举。
 *
 * 覆盖技术方案 §4.2 定义的 11 类业务节点，保证标签名不散落各处。
 */
@AllArgsConstructor
@Getter
public enum NovelGraphNodeEnum implements BaseEnum {

    /**
     * 项目节点，对应一本小说或一个创作项目。
     */
    PROJECT("Project", "项目节点"),

    /**
     * 卷节点。
     */
    VOLUME("Volume", "卷节点"),

    /**
     * 章节节点，只保存摘要和状态，不保存正文。
     */
    CHAPTER("Chapter", "章节节点"),

    /**
     * 角色节点。
     */
    CHARACTER("Character", "角色节点"),

    /**
     * 地点节点。
     */
    LOCATION("Location", "地点节点"),

    /**
     * 线索节点，也可承载伏笔。
     */
    CLUE("Clue", "线索节点"),

    /**
     * 物品节点。
     */
    ITEM("Item", "物品节点"),

    /**
     * 事件节点。
     */
    EVENT("Event", "事件节点"),

    /**
     * 金手指节点。
     */
    CHEAT("Cheat", "金手指节点"),

    /**
     * 马甲节点。
     */
    ALIAS("Alias", "马甲节点"),

    /**
     * 叙事规则节点。
     */
    NARRATIVE_RULE("NarrativeRule", "叙事规则节点");

    /**
     * Neo4j 节点标签名。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;

    /**
     * 返回 Neo4j 标签名称。
     */
    public String label() {
        return value;
    }
}
