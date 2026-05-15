package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 小说项目状态枚举。
 */
@AllArgsConstructor
@Getter
public enum NovelProjectStatusEnum implements BaseEnum {

    ACTIVE("ACTIVE", "写作中"),
    PAUSED("PAUSED", "暂停"),
    ARCHIVED("ARCHIVED", "归档");

    private final String value;

    private final String desc;
}
