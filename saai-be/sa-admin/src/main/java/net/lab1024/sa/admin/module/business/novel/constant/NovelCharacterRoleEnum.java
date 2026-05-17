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

    /**
     * 存入数据库和接口传输使用的稳定角色定位编码。
     */
    private final String value;

    /**
     * 给人看的角色定位说明，用于 Swagger 枚举说明和前端展示。
     */
    private final String desc;
}
