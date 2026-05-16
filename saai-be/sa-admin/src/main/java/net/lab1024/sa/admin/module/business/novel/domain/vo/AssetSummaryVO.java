package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目资产统计 VO —— 替换 assetSummary 接口返回的裸 Map
 *
 * 每种资产类型的计数都有明确的类型和名称，前端契约稳定。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetSummaryVO {

    @Schema(description = "角色数")
    private Integer characterCount;

    @Schema(description = "地点数")
    private Integer locationCount;

    @Schema(description = "线索数")
    private Integer clueCount;

    @Schema(description = "物品数")
    private Integer itemCount;

    @Schema(description = "事件数")
    private Integer eventCount;

    @Schema(description = "金手指数")
    private Integer cheatCount;

    @Schema(description = "马甲数")
    private Integer aliasCount;

    @Schema(description = "叙事规则数")
    private Integer ruleCount;

    @Schema(description = "卷数")
    private Integer volumeCount;

    @Schema(description = "章节数")
    private Integer chapterCount;

    @Schema(description = "项目总字数")
    private Integer totalWords;
}
