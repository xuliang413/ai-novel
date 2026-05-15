package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 角色定位枚举。
 */
@AllArgsConstructor
@Getter
public enum NovelCharacterRoleEnum implements BaseEnum {

    PROTAGONIST("PROTAGONIST", "主角"),
    ANTAGONIST("ANTAGONIST", "反派"),
    SUPPORTING("SUPPORTING", "配角"),
    MINOR("MINOR", "路人或临时角色");

    private final String value;

    private final String desc;
}
