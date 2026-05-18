package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 图谱概览 VO —— 项目下所有节点类型计数和关系统计
 *
 * @Author AI-Novel
 */
@Data
public class NovelGraphOverviewVO {

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "图谱总节点数")
    private Long nodeCount;

    @Schema(description = "图谱总关系数")
    private Long relationCount;

    @Schema(description = "角色节点数")
    private Long characterCount;

    @Schema(description = "地点节点数")
    private Long locationCount;

    @Schema(description = "线索节点数")
    private Long clueCount;

    @Schema(description = "章节节点数")
    private Long chapterCount;
}
