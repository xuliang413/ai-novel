package net.lab1024.sa.admin.module.business.novel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM配置属性 —— 系统默认值
 * 仅存放各模型用途的默认url和模型名, 用户可在t_user_api_key中覆盖
 *
 * @Author AI-Novel
 */
@Data
@Component
@ConfigurationProperties(prefix = "novel.llm")
public class NovelLLMProperties {

    /**
     * 对话模型默认配置
     */
    private ChatDefaults chat = new ChatDefaults();

    /**
     * 向量模型默认配置
     */
    private EmbeddingDefaults embedding = new EmbeddingDefaults();

    /**
     * 重排模型默认配置
     */
    private RerankDefaults rerank = new RerankDefaults();

    @Data
    public static class ChatDefaults {
        private String defaultUrl = "https://api.deepseek.com";
        private String defaultModel = "deepseek-chat";
        private int defaultTimeout = 60000;
    }

    @Data
    public static class EmbeddingDefaults {
        private String defaultUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private String defaultModel = "qwen3-embedding";
        private int defaultTimeout = 30000;
    }

    @Data
    public static class RerankDefaults {
        private String defaultUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private String defaultModel = "qwen3-rerank";
        private int defaultTimeout = 30000;
    }
}
