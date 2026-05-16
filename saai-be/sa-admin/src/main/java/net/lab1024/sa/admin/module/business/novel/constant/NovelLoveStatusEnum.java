package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 爱慕状态枚举(LOVES关系)
 * UNREQUITED单恋, MUTUAL两情相悦, PAST过去式
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

    private final String value;

    private final String desc;
}
