package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 章节细纲编辑表单。
 * <p>
 * 继承创建表单中的规划字段，额外携带细纲ID；更新时仍会校验项目归属。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChapterOutlineUpdateForm extends ChapterOutlineAddForm {

    /**
     * 章节细纲ID，服务层会结合当前登录用户再次校验归属。
     */
    @Schema(description = "章节细纲ID")
    @NotNull(message = "章节细纲ID不能为空")
    private Long outlineId;
}
