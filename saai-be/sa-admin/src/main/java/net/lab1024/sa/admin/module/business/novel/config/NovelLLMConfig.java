package net.lab1024.sa.admin.module.business.novel.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.dao.UserApiKeyDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.UserApiKeyEntity;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import net.lab1024.sa.base.module.support.apiencrypt.service.ApiEncryptService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 创建 LangChain4j 模型对象的工厂。
 *
 * 为什么不直接用 @Bean：
 * - API Key 可能没配，@Bean 会在启动时就报错
 * - 按需创建的话，Key 为空就返回 null，不影响启动
 *
 * 支持两种模式：普通模式（等完整结果）和流式模式（逐字推送）。
 */
@Slf4j
@Configuration
public class NovelLLMConfig {

    @Resource
    private NovelLLMProperties properties;

    @Resource
    private UserApiKeyDao userApiKeyDao;

    @Resource
    private ApiEncryptService apiEncryptService;

    public OpenAiChatModel createDeepseekModel() {
        String apiKey = decryptKey(UserApiKeyEntity::getDeepseekKey, "DeepSeek");
        if (StringUtils.isBlank(apiKey)) {
            return null;
        }
        NovelLLMProperties.DeepSeek cfg = properties.getDeepseek();
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(cfg.getApiUrl())
                .modelName(cfg.getModel())
                .maxTokens(cfg.getMaxTokens())
                .temperature(cfg.getTemperature())
                .timeout(Duration.ofSeconds(cfg.getTimeoutSeconds()))
                .build();
    }

    public OpenAiChatModel createTongyiModel() {
        String apiKey = decryptKey(UserApiKeyEntity::getQwenKey, "通义千问");
        if (StringUtils.isBlank(apiKey)) {
            return null;
        }
        NovelLLMProperties.Tongyi cfg = properties.getTongyi();
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(cfg.getApiUrl())
                .modelName(cfg.getModel())
                .maxTokens(cfg.getMaxTokens())
                .temperature(cfg.getTemperature())
                .timeout(Duration.ofSeconds(cfg.getTimeoutSeconds()))
                .build();
    }

    public OpenAiStreamingChatModel createDeepseekStreamingModel() {
        String apiKey = decryptKey(UserApiKeyEntity::getDeepseekKey, "DeepSeek");
        if (StringUtils.isBlank(apiKey)) {
            return null;
        }
        NovelLLMProperties.DeepSeek cfg = properties.getDeepseek();
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(cfg.getApiUrl())
                .modelName(cfg.getModel())
                .maxTokens(cfg.getMaxTokens())
                .temperature(cfg.getTemperature())
                .timeout(Duration.ofSeconds(cfg.getTimeoutSeconds()))
                .build();
    }

    public OpenAiStreamingChatModel createTongyiStreamingModel() {
        String apiKey = decryptKey(UserApiKeyEntity::getQwenKey, "通义千问");
        if (StringUtils.isBlank(apiKey)) {
            return null;
        }
        NovelLLMProperties.Tongyi cfg = properties.getTongyi();
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(cfg.getApiUrl())
                .modelName(cfg.getModel())
                .maxTokens(cfg.getMaxTokens())
                .temperature(cfg.getTemperature())
                .timeout(Duration.ofSeconds(cfg.getTimeoutSeconds()))
                .build();
    }

    /**
     * 从数据库读出 Key 并解密。
     *
     * 试解密，解出来了就用解密后的。解不出来但看着像明文（sk-开头或含 dashscope），
     * 就当它是明文直接用。都不是就返回空。
     */
    private String decryptKey(java.util.function.Function<UserApiKeyEntity, String> keyGetter, String label) {
        UserApiKeyEntity entity = userApiKeyDao.selectOne(new LambdaQueryWrapper<UserApiKeyEntity>()
                .eq(UserApiKeyEntity::getUserId, AdminRequestUtil.getRequestUserId())
                .last("limit 1"));
        if (entity == null) {
            log.info("UserApiKey 表无记录，{} 不可用", label);
            return "";
        }
        String encrypted = keyGetter.apply(entity);
        if (StringUtils.isBlank(encrypted)) {
            log.info("{} API Key 未配置", label);
            return "";
        }
        String decrypted = apiEncryptService.decrypt(encrypted);
        if (StringUtils.isNotBlank(decrypted)) {
            return decrypted;
        }
        if (encrypted.startsWith("sk-") || encrypted.contains("dashscope")) {
            log.info("{} API Key 似乎是明文，直接使用", label);
            return encrypted;
        }
        log.warn("{} API Key 解密失败，且不像明文Key，将以空值处理", label);
        return "";
    }
}
