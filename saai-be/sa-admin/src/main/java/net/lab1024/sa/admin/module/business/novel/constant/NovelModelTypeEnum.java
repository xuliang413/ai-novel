package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 模型用途类型枚举 —— 按功能分类, 而非按提供商
 * 每个用户每种类型一条配置记录, 各自独立配置 URL + Key + 模型名
 * 实现BaseEnum以支持@CheckEnum和@SchemaEnum注解校验
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelModelTypeEnum implements BaseEnum {

    /**
     * 对话模型 —— 写作/续写/流式生成
     * 当前默认走 DeepSeek, 但用户可通过url字段切换其他兼容OpenAI接口的提供商
     */
    CHAT("CHAT", "对话模型"),

    /**
     * 向量模型 —— 章节摘要向量化, 用于语义相似检索
     * 当前默认走通义千问 qwen3-embedding
     */
    EMBEDDING("EMBEDDING", "向量模型"),

    /**
     * 重排模型 —— 对检索结果精排
     * 当前默认走通义千问 qwen3-rerank
     */
    RERANK("RERANK", "重排模型");

    /**
     * 类型编码, 存入DB的model_type字段
     */
    private final String value;

    /**
     * 中文描述
     */
    private final String desc;
}
