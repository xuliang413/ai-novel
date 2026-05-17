package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 小说项目状态枚举
 * ACTIVE为正常写作中, PAUSED为暂时搁置, ARCHIVED为已归档(不可见但可恢复)
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelProjectStatusEnum implements BaseEnum {

    /**
     * 写作中
     */
    ACTIVE("ACTIVE", "写作中"),

    /**
     * 已暂停
     */
    PAUSED("PAUSED", "已暂停"),

    /**
     * 已归档, 不可见但可恢复
     */
    ARCHIVED("ARCHIVED", "已归档");

    /**
     * 存入数据库和接口传输使用的稳定状态编码。
     */
    private final String value;

    /**
     * 给人看的状态说明，用于 Swagger 枚举说明和前端展示。
     */
    private final String desc;
}
