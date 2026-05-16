package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 物品类型枚举
 * WEAPON武器, ARMOR防具/护甲, TOOL工具/法器, CONSUMABLE消耗品(丹药/灵石),
 * TREASURE宝物/秘宝, DOCUMENT文书/秘笈, CURRENCY货币, OTHER其他
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelItemTypeEnum implements BaseEnum {

    /**
     * 武器
     */
    WEAPON("WEAPON", "武器"),

    /**
     * 防具/护甲
     */
    ARMOR("ARMOR", "防具"),

    /**
     * 工具/法器
     */
    TOOL("TOOL", "工具"),

    /**
     * 消耗品, 丹药/灵石等可消耗物品
     */
    CONSUMABLE("CONSUMABLE", "消耗品"),

    /**
     * 宝物/秘宝
     */
    TREASURE("TREASURE", "宝物"),

    /**
     * 文书/秘笈
     */
    DOCUMENT("DOCUMENT", "文书"),

    /**
     * 货币
     */
    CURRENCY("CURRENCY", "货币"),

    /**
     * 其他
     */
    OTHER("OTHER", "其他");

    private final String value;

    private final String desc;
}
