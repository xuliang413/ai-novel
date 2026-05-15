package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 地点类型枚举。
 */
@AllArgsConstructor
@Getter
public enum NovelLocationTypeEnum implements BaseEnum {

    CITY("CITY", "城市"),
    VILLAGE("VILLAGE", "村镇"),
    BUILDING("BUILDING", "建筑"),
    SECT("SECT", "宗门或组织驻地"),
    WILDERNESS("WILDERNESS", "荒野"),
    REALM("REALM", "秘境或世界"),
    BATTLEFIELD("BATTLEFIELD", "战场");

    private final String value;

    private final String desc;
}
