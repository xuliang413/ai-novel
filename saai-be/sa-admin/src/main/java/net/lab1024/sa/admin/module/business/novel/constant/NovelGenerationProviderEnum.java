package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 章节生成供应商枚举。
 *
 * 用来记录本章到底是哪个“生成来源”写出来的，排查生成质量时很关键。
 */
@AllArgsConstructor
@Getter
public enum NovelGenerationProviderEnum implements BaseEnum {

    /**
     * 本地降级生成，不调用外部模型。
     */
    MOCK("MOCK", "本地模拟生成"),

    /**
     * DeepSeek。
     */
    DEEPSEEK("DEEPSEEK", "DeepSeek"),

    /**
     * 通义千问。
     */
    TONGYI("TONGYI", "通义千问");

    /**
     * 持久化和接口传输使用的值。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;
}
