package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 图谱变更日志状态。
 */
@AllArgsConstructor
@Getter
public enum NovelGraphChangeStatusEnum implements BaseEnum {

    APPLIED("APPLIED", "已应用"),
    UNDONE("UNDONE", "已撤销"),
    FAILED("FAILED", "失败");

    private final String value;

    private final String desc;
}
