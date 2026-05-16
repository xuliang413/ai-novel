package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelModelTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户API Key 展示VO, 不含加密后的原始Key
 *
 * @Author AI-Novel
 */
@Data
public class UserApiKeyVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @SchemaEnum(desc = "模型用途", value = NovelModelTypeEnum.class)
    private String modelType;

    @Schema(description = "API地址")
    private String url;

    @Schema(description = "API Key, 已脱敏 sk-****xxxx")
    private String apiKey;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "提供商描述")
    private String providerName;

    @Schema(description = "生成温度")
    private BigDecimal temperature;

    @Schema(description = "最大Token数")
    private Integer maxTokens;

    @Schema(description = "超时毫秒数")
    private Integer timeout;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
