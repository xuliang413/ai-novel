package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 马甲类型枚举。
 *
 * 用来区分“这个身份为什么是另一个身份”，方便后续写作检索和马甲暴露逻辑。
 */
@AllArgsConstructor
@Getter
public enum NovelAliasTypeEnum implements BaseEnum {

    /**
     * 假身份，例如主角伪造的姓名或履历。
     */
    FALSE_IDENTITY("FALSE_IDENTITY", "假身份"),

    /**
     * 线上身份，例如论坛账号、直播账号。
     */
    ONLINE_IDENTITY("ONLINE_IDENTITY", "线上身份"),

    /**
     * 伪装身份，例如换装、易容后的外在身份。
     */
    DISGUISE("DISGUISE", "伪装身份"),

    /**
     * 称号，例如江湖名号、官方封号。
     */
    TITLE("TITLE", "称号"),

    /**
     * 分身或化身。
     */
    AVATAR("AVATAR", "分身"),

    /**
     * 暂时无法归类的马甲。
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
