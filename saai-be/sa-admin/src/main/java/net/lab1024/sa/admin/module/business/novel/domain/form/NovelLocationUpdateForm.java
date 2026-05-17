package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小说地点编辑表单。
 * <p>
 * 继承地点创建表单中的可编辑设定字段，额外带上地点ID。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelLocationUpdateForm extends NovelLocationAddForm {

    /**
     * 地点ID，服务层会结合当前登录用户再次校验归属。
     */
    @Schema(description = "地点ID")
    @NotNull(message = "地点ID不能为空")
    private Long locationId;
}
