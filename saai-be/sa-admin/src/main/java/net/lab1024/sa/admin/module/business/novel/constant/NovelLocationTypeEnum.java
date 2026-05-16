package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 地点类型枚举
 * CITY城市, VILLAGE村镇, BUILDING建筑, SECT宗门/门派,
 * WILDERNESS荒野, REALM秘境/小世界, BATTLEFIELD战场
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelLocationTypeEnum implements BaseEnum {

    /**
     * 城市
     */
    CITY("CITY", "城市"),

    /**
     * 村镇
     */
    VILLAGE("VILLAGE", "村镇"),

    /**
     * 建筑
     */
    BUILDING("BUILDING", "建筑"),

    /**
     * 宗门/门派
     */
    SECT("SECT", "宗门"),

    /**
     * 荒野
     */
    WILDERNESS("WILDERNESS", "荒野"),

    /**
     * 秘境/小世界
     */
    REALM("REALM", "秘境"),

    /**
     * 战场
     */
    BATTLEFIELD("BATTLEFIELD", "战场");

    private final String value;

    private final String desc;
}
