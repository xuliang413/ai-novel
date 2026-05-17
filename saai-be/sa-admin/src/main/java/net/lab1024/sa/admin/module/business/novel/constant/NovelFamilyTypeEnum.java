package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * IS_FAMILY_OF 关系类型枚举。
 * <p>
 * 用于描述角色之间的血缘、婚姻和师门关系，作为 Neo4j IS_FAMILY_OF 边的 familyType 属性。
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

    /**
     * 枚举值，写入 MySQL 的 family_type，并同步到 Neo4j 的 familyType 属性。
     */
    private final String value;

    /**
     * 中文说明，用于管理页展示和接口文档说明。
     */
    private final String desc;
}
