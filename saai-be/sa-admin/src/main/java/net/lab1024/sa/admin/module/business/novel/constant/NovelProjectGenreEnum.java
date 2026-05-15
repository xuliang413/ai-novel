package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 小说类型枚举。
 *
 * 主要给写作 Prompt 提供题材方向，不是强约束。
 */
@AllArgsConstructor
@Getter
public enum NovelProjectGenreEnum implements BaseEnum {

    /**
     * 仙侠。
     */
    XIANXIA("XIANXIA", "仙侠"),

    /**
     * 玄幻。
     */
    XUANHUAN("XUANHUAN", "玄幻"),

    /**
     * 都市。
     */
    URBAN("URBAN", "都市"),

    /**
     * 历史。
     */
    HISTORY("HISTORY", "历史"),

    /**
     * 科幻。
     */
    SCIFI("SCIFI", "科幻"),

    /**
     * 悬疑。
     */
    MYSTERY("MYSTERY", "悬疑"),

    /**
     * 武侠。
     */
    WUXIA("WUXIA", "武侠"),

    /**
     * 奇幻。
     */
    FANTASY("FANTASY", "奇幻");

    /**
     * 持久化和接口传输使用的值。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;
}
