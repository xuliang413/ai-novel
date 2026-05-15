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

    /**
     * 当前在主线中活跃，可以正常参与写作检索。
     */
    ACTIVE("ACTIVE", "活跃"),

    /**
     * 暂时离开舞台，通常不优先进入候选角色。
     */
    INACTIVE("INACTIVE", "暂离"),

    /**
     * 已死亡，后续出场需要特别谨慎。
     */
    DEAD("DEAD", "死亡"),

    /**
     * 失踪，适合悬疑或寻找剧情。
     */
    MISSING("MISSING", "失踪"),

    /**
     * 状态未知。
     */
    UNKNOWN("UNKNOWN", "未知");

    /**
     * 持久化和接口传输使用的值。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;
}
