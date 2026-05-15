package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 物品状态枚举。
 *
 * 物品状态变化往往是剧情事实，自动抽取时应进入 GraphPatch 等人确认。
 */
@AllArgsConstructor
@Getter
public enum NovelItemStatusEnum implements BaseEnum {

    /**
     * 完好可用。
     */
    INTACT("INTACT", "完好"),

    /**
     * 受损但可能仍可用。
     */
    DAMAGED("DAMAGED", "损坏"),

    /**
     * 丢失，当前持有人未知或没有持有人。
     */
    LOST("LOST", "丢失"),

    /**
     * 已毁坏，通常不可恢复。
     */
    DESTROYED("DESTROYED", "毁坏"),

    /**
     * 状态不明。
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
