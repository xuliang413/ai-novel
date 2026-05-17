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

    /**
     * 存入数据库和接口传输使用的稳定物品状态编码。
     */
    private final String value;

    /**
     * 给人看的物品状态说明，用于 Swagger 枚举说明和前端展示。
     */
    private final String desc;
}
