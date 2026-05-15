package net.lab1024.sa.admin.module.business.novel.domain.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectStatusEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说项目分页查询表单。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelProjectQueryForm extends PageParam {

    /**
     * 项目名称，支持模糊查询。
     */
    @Length(max = 100, message = "项目名称最多 100 个字符")
    private String projectName;

    /**
     * 项目状态。
     */
    @SchemaEnum(value = NovelProjectStatusEnum.class, required = false)
    @CheckEnum(value = NovelProjectStatusEnum.class, message = "项目状态错误")
    @Length(max = 30, message = "项目状态最多 30 个字符")
    private String status;
}
