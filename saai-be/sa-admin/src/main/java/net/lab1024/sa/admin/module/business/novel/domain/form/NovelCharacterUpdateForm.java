package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterRoleEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 编辑角色表单。
 *
 * 与 AddForm 不同：UpdateForm 必须带 id，且字段都可选（只更新非空字段）。
 * 这是常见的"表单编辑"模式——前端只传用户改过的字段，后端做增量更新。
 */
@Data
public class NovelCharacterUpdateForm {

    @NotNull(message = "角色ID不能为空")
    private Long characterId;

    @Length(max = 100, message = "角色名称最多 100 个字符")
    private String characterName;

    @SchemaEnum(value = NovelCharacterRoleEnum.class, required = false)
    @CheckEnum(value = NovelCharacterRoleEnum.class, message = "角色定位错误")
    @Length(max = 50, message = "角色定位最多 50 个字符")
    private String roleType;

    @Length(max = 2000, message = "角色简介最多 2000 个字符")
    private String summary;

    @SchemaEnum(value = NovelCharacterStatusEnum.class, required = false)
    @CheckEnum(value = NovelCharacterStatusEnum.class, message = "角色当前状态错误")
    @Length(max = 100, message = "角色当前状态最多 100 个字符")
    private String currentStatus;
}
