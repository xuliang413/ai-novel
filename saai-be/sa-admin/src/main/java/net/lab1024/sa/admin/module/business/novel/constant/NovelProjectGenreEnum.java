package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 小说项目类型枚举
 * 用于项目创建时选择小说流派, 不同流派影响默认叙事规则和世界观模板注入
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelProjectGenreEnum implements BaseEnum {

    /**
     * 仙侠
     */
    XIANXIA("XIANXIA", "仙侠"),

    /**
     * 玄幻
     */
    XUANHUAN("XUANHUAN", "玄幻"),

    /**
     * 都市
     */
    URBAN("URBAN", "都市"),

    /**
     * 历史
     */
    HISTORY("HISTORY", "历史"),

    /**
     * 科幻
     */
    SCIFI("SCIFI", "科幻"),

    /**
     * 悬疑
     */
    MYSTERY("MYSTERY", "悬疑"),

    /**
     * 武侠
     */
    WUXIA("WUXIA", "武侠"),

    /**
     * 奇幻
     */
    FANTASY("FANTASY", "奇幻");

    /**
     * 存入数据库和接口传输使用的稳定类型编码。
     */
    private final String value;

    /**
     * 给人看的类型说明，用于 Swagger 枚举说明和前端展示。
     */
    private final String desc;
}
