package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 图谱变更状态枚举(graph_change_log)
 * APPLIED已执行, UNDONE已撤销(通过inversePatch), FAILED执行失败
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelGraphChangeStatusEnum implements BaseEnum {

    /**
     * 已执行, GraphPatch成功写入Neo4j
     */
    APPLIED("APPLIED", "已执行"),

    /**
     * 已撤销, 通过inversePatch撤销
     */
    UNDONE("UNDONE", "已撤销"),

    /**
     * 失败, GraphPatch执行失败
     */
    FAILED("FAILED", "执行失败");

    private final String value;

    private final String desc;
}
