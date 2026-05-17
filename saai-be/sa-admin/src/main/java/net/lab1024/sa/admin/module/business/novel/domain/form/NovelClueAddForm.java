package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueSubTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueToneEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说线索创建表单。
 * <p>
 * 线索管理页只维护长期规划类设定字段，进展摘要、揭露程度和当前阶段由写作流程 GraphPatch 推进。
 *
 * @Author AI-Novel
 */
@Data
public class NovelClueAddForm {

    /**
     * 所属项目ID，服务层会校验项目必须属于当前登录用户。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 线索名称，用于列表、图谱节点和写作检索。
     */
    @Schema(description = "线索名称")
    @NotBlank(message = "线索名称不能为空")
    @Length(max = 200, message = "线索名称最多200个字符")
    private String name;

    /**
     * 线索类型，区分主线、支线和暗线。
     */
    @SchemaEnum(desc = "线索类型", value = NovelClueTypeEnum.class)
    @CheckEnum(value = NovelClueTypeEnum.class, required = true, message = "线索类型错误")
    private String type;

    /**
     * 线索子类型，决定写作前是否持续进入 Prompt。
     */
    @SchemaEnum(desc = "线索子类型", value = NovelClueSubTypeEnum.class)
    @CheckEnum(value = NovelClueSubTypeEnum.class, required = true, message = "线索子类型错误")
    private String subType;

    /**
     * 线索完整描述，记录长期规划和核心悬念。
     */
    @Schema(description = "线索完整描述")
    private String description;

    /**
     * 优先级 1~5，多条活跃线索竞争上下文预算时使用。
     */
    @Schema(description = "优先级")
    @Min(value = 1, message = "优先级不能小于1")
    @Max(value = 5, message = "优先级不能大于5")
    private Integer priority;

    /**
     * 计划收束章节号，只作为规划参考，不强制剧情必须在该章解决。
     */
    @Schema(description = "计划收束章节号")
    @Min(value = 1, message = "计划收束章节号必须大于0")
    private Integer targetChapter;

    /**
     * 情绪基调，用于约束线索相关段落的语气。
     */
    @SchemaEnum(desc = "情绪基调", value = NovelClueToneEnum.class)
    @CheckEnum(value = NovelClueToneEnum.class, required = false, message = "线索情绪基调错误")
    private String tone;
}
