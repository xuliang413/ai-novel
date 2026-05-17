package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说叙事规则展示 VO。
 * <p>
 * 返回规则内容和优先级，供管理页审阅，也供后续 Prompt 组装模块直接读取。
 *
 * @Author AI-Novel
 */
@Data
public class NovelNarrativeRuleVO {

    /**
     * 叙事规则ID。
     */
    @Schema(description = "叙事规则ID")
    private Long id;

    /**
     * 所属项目ID。
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 规则名称。
     */
    @Schema(description = "规则名称")
    private String name;

    /**
     * 自然语言规则内容。
     */
    @Schema(description = "自然语言规则内容")
    private String content;

    /**
     * 优先级，数字越大，组装 System Prompt 时越靠前。
     */
    @Schema(description = "优先级")
    private Integer priority;

    /**
     * 归档标记，true 表示规则已归档。
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
