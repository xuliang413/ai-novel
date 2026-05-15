package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 线索类型枚举。
 */
@AllArgsConstructor
@Getter
public enum NovelClueTypeEnum implements BaseEnum {

    MAIN("MAIN", "主线"),
    SUB("SUB", "支线"),
    HIDDEN("HIDDEN", "暗线");

    private final String value;

    private final String desc;
}
