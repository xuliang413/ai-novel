package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 角色存活状态枚举
 * ACTIVE正常出场, INACTIVE暂离剧情, DEAD已死亡(不进候选池),
 * MISSING失踪(悬疑刚需, 仍然可能出场), UNKNOWN状态未知
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelCharacterStatusEnum implements BaseEnum {

    /**
     * 活跃, 正常出场进候选池
     */
    ACTIVE("ACTIVE", "活跃"),

    /**
     * 暂离, 暂时不在剧情中
     */
    INACTIVE("INACTIVE", "暂离"),

    /**
     * 死亡, 不进候选池
     */
    DEAD("DEAD", "死亡"),

    /**
     * 失踪, 悬疑刚需, 仍可能出场
     */
    MISSING("MISSING", "失踪"),

    /**
     * 未知
     */
    UNKNOWN("UNKNOWN", "未知");

    /**
     * 存入数据库和接口传输使用的稳定角色状态编码。
     */
    private final String value;

    /**
     * 给人看的角色状态说明，用于 Swagger 枚举说明和前端展示。
     */
    private final String desc;
}
