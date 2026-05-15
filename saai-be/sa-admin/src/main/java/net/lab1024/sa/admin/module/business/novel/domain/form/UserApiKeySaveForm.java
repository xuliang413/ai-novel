package net.lab1024.sa.admin.module.business.novel.domain.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 用户大模型 API Key 保存表单。
 *
 * 这是用户级配置，不是项目级配置。保存时会加密落库，返回给前端时只返回掩码。
 */
@Data
public class UserApiKeySaveForm {

    /**
     * DeepSeek API Key。传空字符串表示清空，不传则保持原值。
     */
    @Length(max = 1000, message = "DeepSeek API Key 最多 1000 个字符")
    private String deepseekKey;

    /**
     * 通义千问 API Key。传空字符串表示清空，不传则保持原值。
     */
    @Length(max = 1000, message = "通义千问 API Key 最多 1000 个字符")
    private String qwenKey;
}
