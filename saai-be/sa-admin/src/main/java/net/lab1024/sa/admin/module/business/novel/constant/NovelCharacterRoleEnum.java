package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 角色定位枚举
 * PROTAGONIST主角(故事核心, POV默认来源), ANTAGONIST反派(主要冲突对手),
 * SUPPORTING重要配角(有独立剧情线), MINOR次要角色(功能性出场)
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelCharacterRoleEnum implements BaseEnum {

    /**
     * 主角, 故事核心, POV默认来源
     */
    PROTAGONIST("PROTAGONIST", "主角"),

    /**
     * 反派, 主要冲突对手
     */
    ANTAGONIST("ANTAGONIST", "反派"),

    /**
     * 重要配角, 有独立剧情线
     */
    SUPPORTING("SUPPORTING", "重要配角"),

    /**
     * 次要角色, 功能性出场
     */
    MINOR("MINOR", "次要角色");

    private final String value;

    private final String desc;
}
