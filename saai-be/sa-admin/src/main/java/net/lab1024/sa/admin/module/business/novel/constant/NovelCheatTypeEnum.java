package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 金手指类型枚举
 * ABILITY能力型(如万倍悟性/读心术), ITEM_BOUND物品绑定型(如老爷爷戒指/系统面板),
 * SPACE空间型(如随身空间/洞天福地), SYSTEM系统型(如系统面板/任务系统)
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelCheatTypeEnum implements BaseEnum {

    /**
     * 能力型, 如万倍悟性/读心术
     */
    ABILITY("ABILITY", "能力型"),

    /**
     * 物品绑定型, 如老爷爷戒指
     */
    ITEM_BOUND("ITEM_BOUND", "物品绑定型"),

    /**
     * 空间型, 如随身空间/洞天福地
     */
    SPACE("SPACE", "空间型"),

    /**
     * 系统型, 如系统面板/任务系统
     */
    SYSTEM("SYSTEM", "系统型");

    private final String value;

    private final String desc;
}
