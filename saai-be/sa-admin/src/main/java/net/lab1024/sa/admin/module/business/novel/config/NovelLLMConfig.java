package net.lab1024.sa.admin.module.business.novel.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.constant.NovelModelTypeEnum;
import net.lab1024.sa.admin.module.business.novel.dao.UserApiKeyDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.UserApiKeyEntity;
import net.lab1024.sa.base.module.support.apiencrypt.service.ApiEncryptService;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class NovelLLMConfig {

    @Resource
    private UserApiKeyDao userApiKeyDao;

    @Resource
    private ApiEncryptService apiEncryptService;

    @Resource
    private NovelLLMProperties llmProperties;

    public ChatLanguageModel createChatModel(Long userId) {
        UserApiKeyEntity key = getUserApiKey(userId, NovelModelTypeEnum.CHAT);
        if (key == null || key.getApiKey() == null) {
            log.warn("用户{}未配置CHAT(对话)模型的Key, 返回null将降级Mock", userId);
            return null;
        }
        String apiKey = apiEncryptService.decrypt(key.getApiKey());
        String url = key.getUrl() != null ? key.getUrl() : llmProperties.getChat().getDefaultUrl();
        String modelName = key.getModelName() != null ? key.getModelName() : llmProperties.getChat().getDefaultModel();
        double temperature = key.getTemperature() != null ? key.getTemperature().doubleValue() : 0.7;
        int maxTokens = key.getMaxTokens() != null ? key.getMaxTokens() : 4096;
        int timeout = key.getTimeout() != null ? key.getTimeout() : llmProperties.getChat().getDefaultTimeout();

        return OpenAiChatModel.builder()
                .baseUrl(url)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofMillis(timeout))
                .build();
    }

    public StreamingChatLanguageModel createStreamingChatModel(Long userId) {
        UserApiKeyEntity key = getUserApiKey(userId, NovelModelTypeEnum.CHAT);
        if (key == null || key.getApiKey() == null) {
            log.warn("用户{}未配置CHAT(对话)模型的Key, 返回null将降级Mock", userId);
            return null;
        }
        String apiKey = apiEncryptService.decrypt(key.getApiKey());
        String url = key.getUrl() != null ? key.getUrl() : llmProperties.getChat().getDefaultUrl();
        String modelName = key.getModelName() != null ? key.getModelName() : llmProperties.getChat().getDefaultModel();
        double temperature = key.getTemperature() != null ? key.getTemperature().doubleValue() : 0.7;
        int maxTokens = key.getMaxTokens() != null ? key.getMaxTokens() : 4096;
        int timeout = key.getTimeout() != null ? key.getTimeout() : llmProperties.getChat().getDefaultTimeout();

        return OpenAiStreamingChatModel.builder()
                .baseUrl(url)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofMillis(timeout))
                .build();
    }

    public EmbeddingModel createEmbeddingModel(Long userId) {
        UserApiKeyEntity key = getUserApiKey(userId, NovelModelTypeEnum.EMBEDDING);
        if (key == null || key.getApiKey() == null) {
            log.warn("用户{}未配置EMBEDDING(向量)模型的Key, 跳过向量化", userId);
            return null;
        }
        String apiKey = apiEncryptService.decrypt(key.getApiKey());
        String url = key.getUrl() != null ? key.getUrl() : llmProperties.getEmbedding().getDefaultUrl();
        String modelName = key.getModelName() != null ? key.getModelName() : llmProperties.getEmbedding().getDefaultModel();

        return OpenAiEmbeddingModel.builder()
                .baseUrl(url)
                .apiKey(apiKey)
                .modelName(modelName)
                .timeout(Duration.ofMillis(llmProperties.getEmbedding().getDefaultTimeout()))
                .build();
    }

    /**
     * 按模型用途查用户的API Key配置, 接受枚举杜绝硬编码拼写错误
     */
    private UserApiKeyEntity getUserApiKey(Long userId, NovelModelTypeEnum modelType) {
        return userApiKeyDao.selectOne(new LambdaQueryWrapper<UserApiKeyEntity>()
                .eq(UserApiKeyEntity::getUserId, userId)
                .eq(UserApiKeyEntity::getModelType, modelType.getValue())
                .eq(UserApiKeyEntity::getDeletedFlag, false));
    }
}
