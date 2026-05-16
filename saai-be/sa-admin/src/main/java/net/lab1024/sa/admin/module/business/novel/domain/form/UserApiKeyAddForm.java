package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelModelTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

/**
 * 用户API Key 添加表单
 *
 * @Author AI-Novel
 */
@Data
public class UserApiKeyAddForm {

    @SchemaEnum(desc = "模型用途", value = NovelModelTypeEnum.class)
    @CheckEnum(value = NovelModelTypeEnum.class, required = true, message = "模型用途错误")
    private String modelType;

    @Schema(description = "API地址, 不填使用系统默认")
    @Length(max = 500, message = "URL最多500字符")
    private String url;

    @Schema(description = "API Key")
    @NotBlank(message = "API Key不能为空")
    @Length(max = 500, message = "Key最多500字符")
    private String apiKey;

    @Schema(description = "模型名称")
    @Length(max = 100, message = "模型名最多100字符")
    private String modelName;

    @Schema(description = "提供商描述, 仅展示用")
    @Length(max = 50, message = "提供商描述最多50字符")
    private String providerName;

    @Schema(description = "生成温度 0~1, 仅CHAT类型使用")
    private BigDecimal temperature;

    @Schema(description = "最大Token数, 仅CHAT类型使用")
    private Integer maxTokens;

    @Schema(description = "超时毫秒数")
    private Integer timeout;
}
