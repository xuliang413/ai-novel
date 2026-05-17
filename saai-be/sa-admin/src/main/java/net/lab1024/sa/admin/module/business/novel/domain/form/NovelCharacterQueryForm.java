package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterRoleEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterStatusEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说角色分页查询表单。
 * <p>
 * 查询条件只表达筛选意图，项目归属和当前用户隔离由服务层强制追加。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelCharacterQueryForm extends PageParam {

    /**
     * 所属项目ID，查询前会校验项目归属。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 角色名称搜索词，支持模糊匹配。
     */
    @Schema(description = "角色名称搜索词")
    @Length(max = 50, message = "角色名称搜索词最多50个字符")
    private String name;

    /**
     * 角色定位筛选。
     */
    @SchemaEnum(desc = "角色定位", value = NovelCharacterRoleEnum.class)
    @CheckEnum(value = NovelCharacterRoleEnum.class, required = false, message = "角色定位错误")
    private String roleType;

    /**
     * 当前存活状态筛选，状态本身由写作流程维护。
     */
    @SchemaEnum(desc = "当前存活状态", value = NovelCharacterStatusEnum.class)
    @CheckEnum(value = NovelCharacterStatusEnum.class, required = false, message = "角色状态错误")
    private String currentStatus;
}
