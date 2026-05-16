package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Neo4j图关系类型枚举
 * 定义所有29种关系类型及其方向(A->B), 用于Cypher白名单模板拼接
 * 关系方向定义: FROM为起始节点类型, TO为目标节点类型
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelGraphRelationEnum {

    // ===== 结构层关系 =====
    /** 项目包含卷 */
    CONTAINS("CONTAINS", "包含", "Project", "Volume"),
    /** 章节前任下一章 */
    PREVIOUS("PREVIOUS", "前一章", "Chapter", "Chapter"),

    // ===== 出场关系(6种实体均可出场) =====
    /** 出场: 角色在某章出场 */
    APPEARS_IN("APPEARS_IN", "出场", "*", "Chapter"),

    // ===== 角色间关系 =====
    /** 一般社交: 认识某人 */
    KNOWS("KNOWS", "认识", "Character", "Character"),
    /** 爱慕: 单向 */
    LOVES("LOVES", "爱慕", "Character", "Character"),
    /** 仇恨: 单向 */
    HATES("HATES", "仇恨", "Character", "Character"),
    /** 亲缘/师门 */
    IS_FAMILY_OF("IS_FAMILY_OF", "亲缘", "Character", "Character"),

    // ===== 角色位置 =====
    /** 当前位置 */
    CURRENTLY_AT("CURRENTLY_AT", "位于", "Character", "Location"),

    // ===== 角色持有/参与/驱动 =====
    /** 持有物品 */
    POSSESSES("POSSESSES", "持有", "Character", "Item"),
    /** 参与事件 */
    PARTICIPATES_IN("PARTICIPATES_IN", "参与", "Character", "Event"),
    /** 主动推动线索 */
    DRIVES("DRIVES", "推动线索", "Character", "Clue"),
    /** 被动知情线索 */
    KNOWS_ABOUT("KNOWS_ABOUT", "知情线索", "Character", "Clue"),
    /** 拥有金手指 */
    HAS_CHEAT("HAS_CHEAT", "拥有金手指", "Character", "Cheat"),
    /** 拥有马甲身份 */
    HAS_ALIAS("HAS_ALIAS", "拥有马甲", "Character", "Alias"),

    // ===== 线索关系 =====
    /** 章节推进线索 */
    ADVANCES("ADVANCES", "推进线索", "Chapter", "Clue"),
    /** 线索牵连角色 */
    INVOLVES("INVOLVES", "牵连角色", "Clue", "Character"),
    /** 两条线索交汇 */
    INTERSECTS("INTERSECTS", "线索交汇", "Clue", "Clue"),
    /** 线索属于某卷 */
    BELONGS_TO("BELONGS_TO", "属于卷", "Clue", "Volume"),
    /** 线索关联金手指 */
    RELATES_TO("RELATES_TO", "关联金手指", "Clue", "Cheat"),

    // ===== 事件关系 =====
    /** 事件触发线索 */
    TRIGGERS("TRIGGERS", "触发线索", "Event", "Clue"),

    // ===== 物品关系 =====
    /** 物品碎片/部件属于主物品 */
    ITEM_OF("ITEM_OF", "属于物品", "Item", "Item"),

    // ===== 金手指关系 =====
    /** 金手指绑定于物品 */
    BOUND_TO("BOUND_TO", "绑定物品", "Cheat", "Item"),

    // ===== 马甲关系 =====
    /** 某人知道马甲背后是谁 */
    KNOWS_ALIAS("KNOWS_ALIAS", "识破马甲", "Character", "Alias");

    /** 关系类型名, 用于Cypher [r:TYPE] */
    private final String type;

    /** 中文描述 */
    private final String desc;

    /** 起始节点类型, *表示任意实体类型 */
    private final String fromNode;

    /** 目标节点类型 */
    private final String toNode;
}
