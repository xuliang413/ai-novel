package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 物品类型枚举。
 *
 * 类型越明确，写作检索越容易判断它在剧情里应该怎么用。
 */
@AllArgsConstructor
@Getter
public enum NovelItemTypeEnum implements BaseEnum {

    /**
     * 武器。
     */
    WEAPON("WEAPON", "武器"),

    /**
     * 法宝或超自然器物。
     */
    ARTIFACT("ARTIFACT", "法宝"),

    /**
     * 信物、令牌、凭证。
     */
    TOKEN("TOKEN", "信物"),

    /**
     * 文书、卷宗、信件。
     */
    DOCUMENT("DOCUMENT", "文书"),

    /**
     * 丹药或普通药品。
     */
    MEDICINE("MEDICINE", "丹药或药品"),

    /**
     * 资源、材料、货币类。
     */
    RESOURCE("RESOURCE", "资源"),

    /**
     * 其他暂未分类物品。
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
