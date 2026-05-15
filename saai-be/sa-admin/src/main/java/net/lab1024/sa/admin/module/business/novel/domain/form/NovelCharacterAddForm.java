package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterRoleEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 新增小说角色表单。
 */
@Data
public class NovelCharacterAddForm {

    /**
     * 所属小说项目 ID。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 角色名称。
     */
    @NotBlank(message = "角色名称不能为空")
    @Length(max = 100, message = "角色名称最多 100 个字符")
    private String characterName;

    /**
     * 角色定位，取值来自 Character.role 枚举。
     */
    @SchemaEnum(value = NovelCharacterRoleEnum.class, required = false)
    @CheckEnum(value = NovelCharacterRoleEnum.class, message = "角色定位错误")
    @Length(max = 50, message = "角色定位最多 50 个字符")
    private String roleType;

    /**
     * 角色简介。
     */
    @Size(max = 2000, message = "角色简介最多 2000 个字符")
    private String summary;

    /**
     * 角色当前状态；未填写时默认为 ACTIVE。
     */
    @SchemaEnum(value = NovelCharacterStatusEnum.class, required = false)
    @CheckEnum(value = NovelCharacterStatusEnum.class, message = "角色当前状态错误")
    @Length(max = 100, message = "角色当前状态最多 100 个字符")
    private String currentStatus;
}
