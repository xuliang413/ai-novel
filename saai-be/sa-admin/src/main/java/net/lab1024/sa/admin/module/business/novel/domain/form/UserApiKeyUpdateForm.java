package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户API Key 更新表单
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserApiKeyUpdateForm extends UserApiKeyAddForm {

    @Schema(description = "记录ID")
    @NotNull(message = "ID不能为空")
    private Long id;
}
