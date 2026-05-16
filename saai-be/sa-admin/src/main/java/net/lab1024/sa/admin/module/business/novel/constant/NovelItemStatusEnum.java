package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 物品状态枚举
 * INTACT完好(默认), DAMAGED损坏, DESTROYED已摧毁, LOST遗失
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelItemStatusEnum implements BaseEnum {

    /**
     * 完好
     */
    INTACT("INTACT", "完好"),

    /**
     * 损坏
     */
    DAMAGED("DAMAGED", "损坏"),

    /**
     * 已摧毁
     */
    DESTROYED("DESTROYED", "已摧毁"),

    /**
     * 遗失
     */
    LOST("LOST", "遗失");

    private final String value;

    private final String desc;
}
