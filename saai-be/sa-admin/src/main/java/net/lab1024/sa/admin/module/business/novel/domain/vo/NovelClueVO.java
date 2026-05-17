package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueSubTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueToneEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小说线索展示 VO。
 * <p>
 * 返回长期设定字段和写作流程维护出来的动态进展，前端用它同时展示线索规划和当前推进情况。
 *
 * @Author AI-Novel
 */
@Data
public class NovelClueVO {

    /**
     * 线索ID。
     */
    @Schema(description = "线索ID")
    private Long id;

    /**
     * 所属项目ID。
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 线索名称。
     */
    @Schema(description = "线索名称")
    private String name;

    /**
     * 线索类型。
     */
    @SchemaEnum(desc = "线索类型", value = NovelClueTypeEnum.class)
    private String type;

    /**
     * 线索子类型。
     */
    @SchemaEnum(desc = "线索子类型", value = NovelClueSubTypeEnum.class)
    private String subType;

    /**
     * 线索完整描述。
     */
    @Schema(description = "线索完整描述")
    private String description;

    /**
     * 优先级 1~5。
     */
    @Schema(description = "优先级")
    private Integer priority;

    /**
     * 计划收束章节号。
     */
    @Schema(description = "计划收束章节号")
    private Integer targetChapter;

    /**
     * 情绪基调。
     */
    @SchemaEnum(desc = "情绪基调", value = NovelClueToneEnum.class)
    private String tone;

    /**
     * 当前进展摘要，由写作流程更新。
     */
    @Schema(description = "当前进展摘要")
    private String summary;

    /**
     * 揭露程度，范围 0~1。
     */
    @Schema(description = "揭露程度")
    private BigDecimal revealLevel;

    /**
     * 当前阶段，用自然语言描述线索推进到哪里。
     */
    @Schema(description = "当前阶段")
    private String currentStage;

    /**
     * 线索生命周期状态。
     */
    @SchemaEnum(desc = "线索状态", value = NovelClueStatusEnum.class)
    private String clueStatus;

    /**
     * 最后一次提醒的章节号，用于避免伏笔重复提醒。
     */
    @Schema(description = "最后提醒章节号")
    private Integer lastAlertedChapter;

    /**
     * 归档标记，true 表示线索已归档。
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
