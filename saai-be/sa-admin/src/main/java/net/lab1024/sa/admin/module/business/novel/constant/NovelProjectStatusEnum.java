package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 小说项目状态枚举。
 *
 * 控制项目是否还能继续写作，不等同于删除状态。
 */
@AllArgsConstructor
@Getter
public enum NovelProjectStatusEnum implements BaseEnum {

    /**
     * 正在写作中。
     */
    ACTIVE("ACTIVE", "写作中"),

    /**
     * 暂停写作，但项目仍保留。
     */
    PAUSED("PAUSED", "暂停"),

    /**
     * 归档，通常不再进入写作流程。
     */
    ARCHIVED("ARCHIVED", "归档");

    /**
     * 持久化和接口传输使用的值。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;
}
