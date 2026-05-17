package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterRoleEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelEmotionEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGoalStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小说角色展示 VO。
 * <p>
 * 列表和详情会展示设定字段以及写作流程维护出的动态字段，但动态字段不通过普通编辑表单修改。
 *
 * @Author AI-Novel
 */
@Data
public class NovelCharacterVO {

    /**
     * 角色ID。
     */
    @Schema(description = "角色ID")
    private Long id;

    /**
     * 所属项目ID。
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 角色名称。
     */
    @Schema(description = "角色名称")
    private String name;

    /**
     * 角色定位。
     */
    @SchemaEnum(desc = "角色定位", value = NovelCharacterRoleEnum.class)
    private String roleType;

    /**
     * 基础描述。
     */
    @Schema(description = "基础描述")
    private String description;

    /**
     * 当前目标，由写作流程 GraphPatch 维护。
     */
    @Schema(description = "当前目标")
    private String currentGoal;

    /**
     * 目标完成度，范围 0~1。
     */
    @Schema(description = "目标完成度")
    private BigDecimal goalProgress;

    /**
     * 目标状态。
     */
    @SchemaEnum(desc = "目标状态", value = NovelGoalStatusEnum.class)
    private String goalStatus;

    /**
     * 当前主导情绪。
     */
    @SchemaEnum(desc = "当前主导情绪", value = NovelEmotionEnum.class)
    private String currentEmotion;

    /**
     * 情绪强度，范围 1~5。
     */
    @Schema(description = "情绪强度")
    private Integer emotionIntensity;

    /**
     * 次生情绪，用于记录表层和深层情绪差异。
     */
    @SchemaEnum(desc = "次生情绪", value = NovelEmotionEnum.class)
    private String secondaryEmotion;

    /**
     * 战力或境界描述。
     */
    @Schema(description = "战力或境界")
    private String powerLevel;

    /**
     * 当前存活状态。
     */
    @SchemaEnum(desc = "当前存活状态", value = NovelCharacterStatusEnum.class)
    private String currentStatus;

    /**
     * 归档标记，true 表示角色已归档。
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
