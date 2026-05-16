package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 角色情绪枚举
 * 13种核心情绪, 约束AI描写角色的语气和行为。
 * 情绪强度(1~5)配合此枚举使用, 强度低是微表情, 强度高是崩溃/狂喜
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelEmotionEnum implements BaseEnum {

    /**
     * 愤怒
     */
    ANGER("ANGER", "愤怒"),

    /**
     * 恐惧
     */
    FEAR("FEAR", "恐惧"),

    /**
     * 坚定
     */
    DETERMINED("DETERMINED", "坚定"),

    /**
     * 绝望
     */
    DESPAIR("DESPAIR", "绝望"),

    /**
     * 喜悦
     */
    JOY("JOY", "喜悦"),

    /**
     * 悲伤
     */
    SADNESS("SADNESS", "悲伤"),

    /**
     * 冷静
     */
    CALM("CALM", "冷静"),

    /**
     * 怀疑
     */
    SUSPICIOUS("SUSPICIOUS", "怀疑"),

    /**
     * 羞愧
     */
    SHAME("SHAME", "羞愧"),

    /**
     * 骄傲
     */
    PRIDE("PRIDE", "骄傲"),

    /**
     * 希望
     */
    HOPE("HOPE", "希望"),

    /**
     * 悲痛
     */
    GRIEF("GRIEF", "悲痛"),

    /**
     * 焦虑
     */
    ANXIETY("ANXIETY", "焦虑");

    private final String value;

    private final String desc;
}
