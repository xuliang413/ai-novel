package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 小说目标平台枚举。
 * <p>
 * 用于项目创建和编辑时标记作品面向的平台，后续叙事规则、禁忌词、章节节奏等平台差异都可以从这里分流。
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelProjectPlatformEnum implements BaseEnum {

    /**
     * 起点中文网，适合长篇网文节奏和升级线。
     */
    QIDIAN("QIDIAN", "起点"),

    /**
     * 番茄小说，适合强节奏、强钩子的免费阅读平台。
     */
    FANQIE("FANQIE", "番茄"),

    /**
     * 纵横中文网，适合传统男频长篇和类型化叙事。
     */
    ZONGHENG("ZONGHENG", "纵横");

    /**
     * 存入数据库和前端表单提交使用的稳定编码。
     */
    private final String value;

    /**
     * 给人看的平台名称，用于 Swagger 枚举说明和页面展示。
     */
    private final String desc;
}
