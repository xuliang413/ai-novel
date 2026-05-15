package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户大模型 API Key 返回对象。
 *
 * 注意：这里绝不返回明文 Key，只返回是否已配置、掩码和本地检测状态。
 */
@Data
public class UserApiKeyVO {

    /**
     * 是否配置 DeepSeek Key。
     */
    private Boolean hasDeepseekKey;

    /**
     * 是否配置通义千问 Key。
     */
    private Boolean hasQwenKey;

    /**
     * DeepSeek Key 掩码，例如 sk-***abcd。
     */
    private String deepseekMasked;

    /**
     * 通义千问 Key 掩码。
     */
    private String qwenMasked;

    /**
     * DeepSeek 本地检测状态。
     */
    private String deepseekStatus;

    /**
     * 通义千问本地检测状态。
     */
    private String qwenStatus;

    /**
     * 最近更新时间。
     */
    private LocalDateTime updateTime;
}
