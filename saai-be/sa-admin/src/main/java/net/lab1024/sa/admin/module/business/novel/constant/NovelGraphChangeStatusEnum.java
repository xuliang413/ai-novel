package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 图谱变更日志状态。
 *
 * 对应 GraphChangeLogEntity，主要服务确认和撤销。
 */
@AllArgsConstructor
@Getter
public enum NovelGraphChangeStatusEnum implements BaseEnum {

    /**
     * 正向 GraphPatch 已经写入 Neo4j。
     */
    APPLIED("APPLIED", "已应用"),

    /**
     * 已执行 inversePatch 撤销。
     */
    UNDONE("UNDONE", "已撤销"),

    /**
     * 执行或撤销失败。
     */
    FAILED("FAILED", "失败");

    /**
     * 持久化和接口传输使用的值。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;
}
