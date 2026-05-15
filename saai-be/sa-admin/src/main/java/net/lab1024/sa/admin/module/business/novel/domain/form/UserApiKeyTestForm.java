package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 用户 API Key 连接状态检测表单。
 *
 * 这里只选择供应商，不传明文 Key。
 */
@Data
public class UserApiKeyTestForm {

    /**
     * 供应商：DEEPSEEK 或 TONGYI。
     */
    @NotBlank(message = "供应商不能为空")
    @Length(max = 30, message = "供应商最多 30 个字符")
    private String provider;
}
