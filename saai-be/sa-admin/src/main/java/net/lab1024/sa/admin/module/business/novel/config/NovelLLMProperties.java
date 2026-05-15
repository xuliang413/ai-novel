package net.lab1024.sa.admin.module.business.novel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "novel.llm")
public class NovelLLMProperties {

    private String defaultProvider = "MOCK";

    private DeepSeek deepseek = new DeepSeek();

    private Tongyi tongyi = new Tongyi();

    @Data
    public static class DeepSeek {
        private String apiUrl = "https://api.deepseek.com/v1";
        private String model = "deepseek-v4-pro";
        private int maxTokens = 4096;
        private double temperature = 0.9;
        private int timeoutSeconds = 180;
    }

    @Data
    public static class Tongyi {
        private String apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private String model = "qwen-plus";
        private int maxTokens = 4096;
        private double temperature = 0.9;
        private int timeoutSeconds = 180;
    }
}
