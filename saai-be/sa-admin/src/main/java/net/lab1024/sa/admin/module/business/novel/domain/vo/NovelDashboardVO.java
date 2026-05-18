package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 仪表盘统计 VO —— 项目级字数/章节/角色/线索/地点总览
 *
 * @Author AI-Novel
 */
@Data
public class NovelDashboardVO {

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "项目状态")
    private String projectStatus;

    @Schema(description = "目标总字数, 统计参考")
    private Integer targetTotalWords;

    @Schema(description = "章节总数(含草稿)")
    private Long totalChapters;

    @Schema(description = "已发布章节数")
    private Long publishedChapters;

    @Schema(description = "草稿章节数")
    private Long draftChapters;

    @Schema(description = "已发布章节总字数")
    private Long totalWords;

    @Schema(description = "角色数量")
    private Long characterCount;

    @Schema(description = "线索数量")
    private Long clueCount;

    @Schema(description = "地点数量")
    private Long locationCount;
}
