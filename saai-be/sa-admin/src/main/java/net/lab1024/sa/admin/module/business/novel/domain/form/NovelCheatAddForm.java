package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCheatTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说金手指创建表单。
 * <p>
 * 管理页只维护名称、类型、来源、限制和升级路径；当前副作用阶段由写作流程 GraphPatch 推进，创建时由服务端给安全默认值。
 *
 * @Author AI-Novel
 */
@Data
public class NovelCheatAddForm {

    /**
     * 所属项目ID，服务层会校验项目必须属于当前登录用户。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 金手指名称，用于资产列表、图谱节点和写作上下文检索。
     */
    @Schema(description = "金手指名称")
    @NotBlank(message = "金手指名称不能为空")
    @Length(max = 200, message = "金手指名称最多200个字符")
    private String name;

    /**
     * 金手指类型，用来区分能力型、物品绑定型、空间型和系统型。
     */
    @SchemaEnum(desc = "金手指类型", value = NovelCheatTypeEnum.class)
    @CheckEnum(value = NovelCheatTypeEnum.class, required = true, message = "金手指类型错误")
    private String type;

    /**
     * 金手指描述，记录能力效果、叙事定位和读者可感知的表现。
     */
    @Schema(description = "金手指描述")
    private String summary;

    /**
     * 金手指来源，说明主角如何获得它以及背后的设定根因。
     */
    @Schema(description = "金手指来源")
    private String origin;

    /**
     * 金手指限制或副作用，作为写作时避免能力失控的约束材料。
     */
    @Schema(description = "金手指限制或副作用")
    private String limitation;

    /**
     * 金手指进化路径，描述后续升级、解锁或代价增强的方向。
     */
    @Schema(description = "金手指进化路径")
    private String evolution;
}
