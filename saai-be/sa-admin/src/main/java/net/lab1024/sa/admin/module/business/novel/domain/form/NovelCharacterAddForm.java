package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterRoleEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说角色创建表单。
 * <p>
 * 只开放管理页允许人工维护的设定字段；目标、情绪、战力等动态字段只能由写作流程 GraphPatch 修改。
 *
 * @Author AI-Novel
 */
@Data
public class NovelCharacterAddForm {

    /**
     * 所属项目ID，服务层会校验项目必须属于当前登录用户。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 角色名称，用于角色列表、图谱节点和写作提示词中的人物识别。
     */
    @Schema(description = "角色名称")
    @NotBlank(message = "角色名称不能为空")
    @Length(max = 100, message = "角色名称最多100个字符")
    private String name;

    /**
     * 角色定位，决定候选角色排序和写作时的关注权重。
     */
    @SchemaEnum(desc = "角色定位", value = NovelCharacterRoleEnum.class)
    @CheckEnum(value = NovelCharacterRoleEnum.class, required = true, message = "角色定位错误")
    private String roleType;

    /**
     * 基础描述，记录外貌、性格、身份等长期稳定设定。
     */
    @Schema(description = "基础描述")
    private String description;
}
