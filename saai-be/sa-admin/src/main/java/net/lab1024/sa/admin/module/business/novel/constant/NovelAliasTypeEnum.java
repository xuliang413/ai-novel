package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 马甲类型枚举。
 * <p>
 * 用于马甲创建、编辑和查询时的类型校验，也帮助写作检索判断这个身份属于线上身份、伪装身份、第二人格还是其他类型。
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelAliasTypeEnum implements BaseEnum {

    /**
     * 网络身份，例如论坛马甲、社交账号、暗网账号。
     */
    ONLINE_IDENTITY("ONLINE_IDENTITY", "网络身份"),

    /**
     * 伪装身份，例如易容、化名、临时身份。
     */
    DISGUISE("DISGUISE", "伪装身份"),

    /**
     * 第二人格或分身身份，例如精神分裂人格、外化分身。
     */
    ALTER_EGO("ALTER_EGO", "第二人格"),

    /**
     * 其他类型，保留给暂时无法归类的特殊身份。
     */
    OTHER("OTHER", "其他");

    /**
     * 枚举值，写入数据库和接口请求时使用。
     */
    private final String value;

    /**
     * 中文说明，展示给管理页和接口文档阅读者。
     */
    private final String desc;
}
