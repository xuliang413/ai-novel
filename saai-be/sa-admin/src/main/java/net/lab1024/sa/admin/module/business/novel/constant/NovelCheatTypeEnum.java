package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 金手指类型枚举。
 *
 * 用来告诉写作模型“这个优势属于哪种能力边界”，避免把它写成万能钥匙。
 */
@AllArgsConstructor
@Getter
public enum NovelCheatTypeEnum implements BaseEnum {

    /**
     * 主动或被动能力。
     */
    ABILITY("ABILITY", "能力"),

    /**
     * 系统类外挂。
     */
    SYSTEM("SYSTEM", "系统"),

    /**
     * 依附在物品上的金手指。
     */
    ITEM("ITEM", "物品"),

    /**
     * 血脉或天赋来源。
     */
    BLOODLINE("BLOODLINE", "血脉"),

    /**
     * 信息差或知识类优势。
     */
    KNOWLEDGE("KNOWLEDGE", "知识"),

    /**
     * 暂时无法归类的金手指。
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
