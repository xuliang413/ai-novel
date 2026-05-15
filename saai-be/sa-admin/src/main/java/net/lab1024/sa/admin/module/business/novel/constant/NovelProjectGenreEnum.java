package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 小说类型枚举。
 */
@AllArgsConstructor
@Getter
public enum NovelProjectGenreEnum implements BaseEnum {

    XIANXIA("XIANXIA", "仙侠"),
    XUANHUAN("XUANHUAN", "玄幻"),
    URBAN("URBAN", "都市"),
    HISTORY("HISTORY", "历史"),
    SCIFI("SCIFI", "科幻"),
    MYSTERY("MYSTERY", "悬疑"),
    WUXIA("WUXIA", "武侠"),
    FANTASY("FANTASY", "奇幻");

    private final String value;

    private final String desc;
}
