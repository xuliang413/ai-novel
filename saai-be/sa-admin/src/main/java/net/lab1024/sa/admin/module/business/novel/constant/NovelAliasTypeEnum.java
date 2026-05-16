package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 马甲类型枚举
 * ONLINE_IDENTITY网络身份(论坛马甲/社交账号), DISGUISE乔装身份(易容/化名),
 * ALTER_EGO第二人格/分身, OTHER其他
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelAliasTypeEnum implements BaseEnum {

    /**
     * 网络身份, 论坛马甲/社交账号
     */
    ONLINE_IDENTITY("ONLINE_IDENTITY", "网络身份"),

    /**
     * 乔装身份, 易容/化名
     */
    DISGUISE("DISGUISE", "乔装身份"),

    /**
     * 第二人格/分身
     */
    ALTER_EGO("ALTER_EGO", "第二人格"),

    /**
     * 其他
     */
    OTHER("OTHER", "其他");

    private final String value;

    private final String desc;
}
