package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 地点类型枚举。
 *
 * 地点类型会影响场景写法，也方便后续按城市、建筑、秘境等维度检索。
 */
@AllArgsConstructor
@Getter
public enum NovelLocationTypeEnum implements BaseEnum {

    /**
     * 城市。
     */
    CITY("CITY", "城市"),

    /**
     * 村镇。
     */
    VILLAGE("VILLAGE", "村镇"),

    /**
     * 建筑或室内地点。
     */
    BUILDING("BUILDING", "建筑"),

    /**
     * 宗门、组织驻地。
     */
    SECT("SECT", "宗门或组织驻地"),

    /**
     * 荒野、野外区域。
     */
    WILDERNESS("WILDERNESS", "荒野"),

    /**
     * 秘境、异世界、特殊空间。
     */
    REALM("REALM", "秘境或世界"),

    /**
     * 战场。
     */
    BATTLEFIELD("BATTLEFIELD", "战场");

    /**
     * 持久化和接口传输使用的值。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;
}
