package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 新增小说线索表单。
 */
@Data
public class NovelClueAddForm {

    /**
     * 所属小说项目 ID。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 线索名称。
     */
    @NotBlank(message = "线索名称不能为空")
    @Length(max = 100, message = "线索名称最多 100 个字符")
    private String clueName;

    /**
     * 线索类型，取值来自 Clue.type 枚举。
     */
    @SchemaEnum(value = NovelClueTypeEnum.class, required = false)
    @CheckEnum(value = NovelClueTypeEnum.class, message = "线索类型错误")
    @Length(max = 50, message = "线索类型最多 50 个字符")
    private String clueType;

    /**
     * 线索状态；未填写时默认为 DORMANT。
     */
    @SchemaEnum(value = NovelClueStatusEnum.class, required = false)
    @CheckEnum(value = NovelClueStatusEnum.class, message = "线索状态错误")
    @Length(max = 30, message = "线索状态最多 30 个字符")
    private String clueStatus;

    /**
     * 线索简介。
     */
    @Size(max = 2000, message = "线索简介最多 2000 个字符")
    private String summary;
}
