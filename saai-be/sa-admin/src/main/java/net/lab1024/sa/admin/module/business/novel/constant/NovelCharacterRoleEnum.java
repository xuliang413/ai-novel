package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 角色定位枚举。
 *
 * 这是角色在故事结构里的位置，不是人物关系。
 */
@AllArgsConstructor
@Getter
public enum NovelCharacterRoleEnum implements BaseEnum {

    /**
     * 主角或核心视角人物。
     */
    PROTAGONIST("PROTAGONIST", "主角"),

    /**
     * 反派或主要对抗者。
     */
    ANTAGONIST("ANTAGONIST", "反派"),

    /**
     * 重要配角。
     */
    SUPPORTING("SUPPORTING", "配角"),

    /**
     * 临时或低戏份角色。
     */
    MINOR("MINOR", "路人或临时角色");

    /**
     * 持久化和接口传输使用的值。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;
}
