package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户API Key 实体类 —— 按模型用途分类, 每种类型一条记录
 * 一个用户最多3条: CHAT(对话)/EMBEDDING(向量)/RERANK(重排)
 * 各自独立配置 url + Key + 模型名, 完全解耦
 * API Key AES加密存储, 前端显示为 sk-****
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_user_api_key")
public class UserApiKeyEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID, 一个用户每种modelType只能配一条(uk_user_model_type保证)
     */
    private Long userId;

    /**
     * 模型用途: CHAT(对话)/EMBEDDING(向量)/RERANK(重排)
     * 按功能分类而非按提供商, 用户可为对话用DeepSeek、向量用通义、Rerank用另一家
     */
    private String modelType;

    /**
     * API地址, 用户可配以切换兼容OpenAI接口的任意提供商
     * 为空时使用系统默认值(DeepSeek/通义DashScope)
     */
    private String url;

    /**
     * API Key, AES加密存储, 前端展示脱敏
     */
    private String apiKey;

    /**
     * 模型名称, 如deepseek-chat/qwen3-embedding/qwen3-rerank
     */
    private String modelName;

    /**
     * 提供商描述(可选), 如DeepSeek/通义千问, 仅前端展示用不参与逻辑
     */
    private String providerName;

    /**
     * 生成温度 0~1, 控制输出随机性(仅CHAT类型使用)
     */
    private BigDecimal temperature;

    /**
     * 最大Token数(仅CHAT类型使用)
     */
    private Integer maxTokens;

    /**
     * 超时毫秒数
     */
    private Integer timeout;

    /**
     * 删除标记
     */
    private Boolean deletedFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
