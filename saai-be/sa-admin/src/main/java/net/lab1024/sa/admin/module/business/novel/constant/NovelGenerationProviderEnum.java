package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 章节生成供应商枚举。
 */
@AllArgsConstructor
@Getter
public enum NovelGenerationProviderEnum implements BaseEnum {

    MOCK("MOCK", "本地模拟生成"),
    DEEPSEEK("DEEPSEEK", "DeepSeek"),
    TONGYI("TONGYI", "通义千问");

    private final String value;

    private final String desc;
}
