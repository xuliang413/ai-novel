package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 角色目标状态枚举
 * IN_PROGRESS正在推进(默认), ACHIEVED已达成, ABANDONED已放弃, DIVERTED已转向(原目标被新目标取代)
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelGoalStatusEnum implements BaseEnum {

    /**
     * 进行中
     */
    IN_PROGRESS("IN_PROGRESS", "进行中"),

    /**
     * 已达成
     */
    ACHIEVED("ACHIEVED", "已达成"),

    /**
     * 已放弃
     */
    ABANDONED("ABANDONED", "已放弃"),

    /**
     * 已转向, 原目标被新目标取代
     */
    DIVERTED("DIVERTED", "已转向");

    private final String value;

    private final String desc;
}
