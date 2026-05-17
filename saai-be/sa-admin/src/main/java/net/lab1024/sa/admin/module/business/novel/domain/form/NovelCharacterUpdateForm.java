package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小说角色编辑表单。
 * <p>
 * 继承角色创建表单中的设定字段，额外带上角色ID；不允许通过编辑接口修改动态状态字段。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelCharacterUpdateForm extends NovelCharacterAddForm {

    /**
     * 角色ID，服务层会结合当前登录用户再次校验归属。
     */
    @Schema(description = "角色ID")
    @NotNull(message = "角色ID不能为空")
    private Long characterId;
}
