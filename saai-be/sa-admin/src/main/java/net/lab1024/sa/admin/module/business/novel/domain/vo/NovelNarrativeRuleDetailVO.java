package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 叙事规则详情 VO
 *
 * 叙事规则不是剧情数据，而是对 AI 模型的"写作边界约束"——比如"每章 3000-5000 字"
 * "不使用第一人称""避免过度描写战斗细节"。规则有优先级（数字越大越靠前），在 Prompt 拼装
 * 时按优先级排列，让最重要的规则最先被模型看到。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelNarrativeRuleDetailVO {

    @Schema(description = "规则ID")
    private Long ruleId;
    @Schema(description = "所属项目ID")
    private Long projectId;
    @Schema(description = "规则名称")
    private String ruleName;
    @Schema(description = "规则类型")
    private String ruleType;
    @Schema(description = "规则内容")
    private String ruleValue;
    @Schema(description = "优先级 —— 数字越大越靠前进入 Prompt", example = "10")
    private Integer priority;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
