package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.novel.constant.NovelChapterStatusEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说章节分页查询表单。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelChapterQueryForm extends PageParam {

    /**
     * 所属小说项目 ID。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 章节状态。
     */
    @SchemaEnum(value = NovelChapterStatusEnum.class, required = false)
    @CheckEnum(value = NovelChapterStatusEnum.class, message = "章节状态错误")
    @Length(max = 30, message = "章节状态最多 30 个字符")
    private String status;
}
