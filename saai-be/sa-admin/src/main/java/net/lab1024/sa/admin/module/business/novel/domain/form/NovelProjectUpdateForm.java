package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

/**
 * 小说项目编辑表单。
 * <p>
 * 继承创建表单里的可编辑字段，额外带上项目ID和项目状态；归档动作单独走归档接口。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelProjectUpdateForm extends NovelProjectAddForm {

    /**
     * 项目ID，服务层会结合当前登录用户再次校验归属。
     */
    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 项目状态，编辑时允许在写作中和暂停之间切换，归档仍走单独接口。
     */
    @SchemaEnum(desc = "项目状态", value = NovelProjectStatusEnum.class)
    @CheckEnum(value = NovelProjectStatusEnum.class, required = true, message = "项目状态错误")
    private String status;
}
