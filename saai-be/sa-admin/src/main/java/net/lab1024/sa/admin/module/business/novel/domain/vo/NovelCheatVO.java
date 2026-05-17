package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCheatTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 小说金手指展示 VO。
 * <p>
 * 返回管理页设定字段和写作流程维护出的当前副作用阶段，方便作者检查能力边界是否仍然合理。
 *
 * @Author AI-Novel
 */
@Data
public class NovelCheatVO {

    /**
     * 金手指ID。
     */
    @Schema(description = "金手指ID")
    private Long id;

    /**
     * 所属项目ID。
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 金手指名称。
     */
    @Schema(description = "金手指名称")
    private String name;

    /**
     * 金手指类型。
     */
    @SchemaEnum(desc = "金手指类型", value = NovelCheatTypeEnum.class)
    private String type;

    /**
     * 金手指描述。
     */
    @Schema(description = "金手指描述")
    private String summary;

    /**
     * 金手指来源。
     */
    @Schema(description = "金手指来源")
    private String origin;

    /**
     * 金手指限制或副作用。
     */
    @Schema(description = "金手指限制或副作用")
    private String limitation;

    /**
     * 金手指进化路径。
     */
    @Schema(description = "金手指进化路径")
    private String evolution;

    /**
     * 当前副作用阶段，由写作流程推进后回写。
     */
    @Schema(description = "当前副作用阶段")
    private String currentStage;

    /**
     * 归档标记，true 表示金手指已归档。
     */
    @Schema(description = "归档标记")
    private Boolean deletedFlag;

    /**
     * 创建用户ID。
     */
    @Schema(description = "创建用户ID")
    private Long createUserId;

    /**
     * 最后更新时间。
     */
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
