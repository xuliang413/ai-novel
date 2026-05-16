package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 编辑线索表单。
 */
@Data
public class NovelClueUpdateForm {

    @NotNull(message = "线索ID不能为空")
    private Long clueId;

    @Length(max = 100, message = "线索名称最多 100 个字符")
    private String clueName;

    @SchemaEnum(value = NovelClueTypeEnum.class, required = false)
    @CheckEnum(value = NovelClueTypeEnum.class, message = "线索类型错误")
    @Length(max = 50, message = "线索类型最多 50 个字符")
    private String clueType;

    @SchemaEnum(value = NovelClueStatusEnum.class, required = false)
    @CheckEnum(value = NovelClueStatusEnum.class, message = "线索状态错误")
    @Length(max = 50, message = "线索状态最多 50 个字符")
    private String clueStatus;

    @Length(max = 2000, message = "线索简介最多 2000 个字符")
    private String summary;
}
