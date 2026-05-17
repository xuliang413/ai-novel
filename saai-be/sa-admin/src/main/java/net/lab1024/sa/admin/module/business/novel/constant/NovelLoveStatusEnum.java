package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * LOVES 关系状态枚举。
 * <p>
 * 用于描述角色之间爱慕关系当前处于单恋、双向或过去式，作为 Neo4j LOVES 边的 status 属性。
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelLoveStatusEnum implements BaseEnum {

    /**
     * 单恋
     */
    UNREQUITED("UNREQUITED", "单恋"),

    /**
     * 两情相悦
     */
    MUTUAL("MUTUAL", "两情相悦"),

    /**
     * 过去式, 曾经爱过但已结束
     */
    PAST("PAST", "过去式");

    /**
     * 枚举值，写入 MySQL 的 love_status，并同步到 Neo4j 的 status 属性。
     */
    private final String value;

    /**
     * 中文说明，用于管理页展示和接口文档说明。
     */
    private final String desc;
}
