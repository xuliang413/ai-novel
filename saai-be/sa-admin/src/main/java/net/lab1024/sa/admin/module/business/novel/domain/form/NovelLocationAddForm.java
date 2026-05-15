package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelLocationTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 新增小说地点表单。
 */
@Data
public class NovelLocationAddForm {

    /**
     * 所属小说项目 ID。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 地点名称。
     */
    @NotBlank(message = "地点名称不能为空")
    @Length(max = 100, message = "地点名称最多 100 个字符")
    private String locationName;

    /**
     * 地点类型，取值来自 Location.type 枚举。
     */
    @SchemaEnum(value = NovelLocationTypeEnum.class, required = false)
    @CheckEnum(value = NovelLocationTypeEnum.class, message = "地点类型错误")
    @Length(max = 50, message = "地点类型最多 50 个字符")
    private String locationType;

    /**
     * 地点简介。
     */
    @Size(max = 2000, message = "地点简介最多 2000 个字符")
    private String summary;
}
