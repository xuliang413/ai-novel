package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 叙事规则类型枚举。
 *
 * 叙事规则会进入 Prompt，优先级高的规则会更靠前，约束模型不要跑偏。
 */
@AllArgsConstructor
@Getter
public enum NovelNarrativeRuleTypeEnum implements BaseEnum {

    /**
     * 平台红线，例如不能写的内容边界。
     */
    PLATFORM_REDLINE("PLATFORM_REDLINE", "平台红线"),

    /**
     * 字数限制。
     */
    WORD_COUNT("WORD_COUNT", "字数限制"),

    /**
     * 文风要求。
     */
    STYLE("STYLE", "文风"),

    /**
     * 明确禁止项。
     */
    FORBIDDEN("FORBIDDEN", "禁止项"),

    /**
     * 视角规则。
     */
    POV("POV", "视角规则"),

    /**
     * 世界观约束。
     */
    WORLD_SETTING("WORLD_SETTING", "世界观约束"),

    /**
     * 其他规则。
     */
    OTHER("OTHER", "其他");

    /**
     * 持久化和接口传输使用的值。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;
}
