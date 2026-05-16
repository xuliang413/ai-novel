package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelLocationTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 编辑地点表单。
 */
@Data
public class NovelLocationUpdateForm {

    @NotNull(message = "地点ID不能为空")
    private Long locationId;

    @Length(max = 100, message = "地点名称最多 100 个字符")
    private String locationName;

    @SchemaEnum(value = NovelLocationTypeEnum.class, required = false)
    @CheckEnum(value = NovelLocationTypeEnum.class, message = "地点类型错误")
    @Length(max = 50, message = "地点类型最多 50 个字符")
    private String locationType;

    @Length(max = 2000, message = "地点简介最多 2000 个字符")
    private String summary;
}
