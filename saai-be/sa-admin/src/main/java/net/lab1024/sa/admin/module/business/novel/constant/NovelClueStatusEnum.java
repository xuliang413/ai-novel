package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 线索状态枚举。
 */
@AllArgsConstructor
@Getter
public enum NovelClueStatusEnum implements BaseEnum {

    DORMANT("DORMANT", "未激活"),
    ACTIVE("ACTIVE", "推进中"),
    RESOLVED("RESOLVED", "已解决");

    private final String value;

    private final String desc;
}
