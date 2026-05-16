package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 亲缘类型枚举(IS_FAMILY_OF关系)
 * 涵盖血缘亲属和师门关系: FATHER/MOTHER/BROTHER/SISTER/SON/DAUGHTER/COUSIN/SPOUSE/MASTER/DISCIPLE
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelFamilyTypeEnum implements BaseEnum {

    /**
     * 父亲
     */
    FATHER("FATHER", "父亲"),

    /**
     * 母亲
     */
    MOTHER("MOTHER", "母亲"),

    /**
     * 兄弟
     */
    BROTHER("BROTHER", "兄弟"),

    /**
     * 姐妹
     */
    SISTER("SISTER", "姐妹"),

    /**
     * 儿子
     */
    SON("SON", "儿子"),

    /**
     * 女儿
     */
    DAUGHTER("DAUGHTER", "女儿"),

    /**
     * 堂亲/表亲
     */
    COUSIN("COUSIN", "堂表亲"),

    /**
     * 配偶
     */
    SPOUSE("SPOUSE", "配偶"),

    /**
     * 师父
     */
    MASTER("MASTER", "师父"),

    /**
     * 弟子
     */
    DISCIPLE("DISCIPLE", "弟子");

    private final String value;

    private final String desc;
}
