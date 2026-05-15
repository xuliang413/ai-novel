package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 角色状态枚举。
 *
 * 这里表达的是角色在故事世界里的状态，不等同于系统归档字段 archived。
 */
@AllArgsConstructor
@Getter
public enum NovelCharacterStatusEnum implements BaseEnum {

    ACTIVE("ACTIVE", "活跃"),
    INACTIVE("INACTIVE", "暂离"),
    DEAD("DEAD", "死亡"),
    MISSING("MISSING", "失踪"),
    UNKNOWN("UNKNOWN", "未知");

    private final String value;

    private final String desc;
}
