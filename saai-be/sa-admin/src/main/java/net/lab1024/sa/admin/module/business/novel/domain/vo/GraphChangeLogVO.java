package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * GraphPatch 变更历史 VO —— 前端变更面板的数据行
 *
 * 数据来源：graph_change_log 表，每次 GraphPatch 执行/撤销/失败都会写入一条。
 * 前端用这个 VO 展示变更历史列表，每一行包含操作批次 ID、状态、影响的章节和操作摘要。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphChangeLogVO {

    @Schema(description = "日志ID")
    private Long changeLogId;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "章节ID")
    private Long chapterId;

    @Schema(description = "章节序号")
    private Integer chapterNo;

    @Schema(description = "操作批次ID（关联确认/撤销）")
    private String operationBatchId;

    @Schema(description = "操作状态：APPLIED/UNDONE/FAILED")
    private String status;

    @Schema(description = "失败原因（成功时为空）")
    private String errorMessage;

    @Schema(description = "正向变更操作数")
    private Integer patchOperationCount;

    @Schema(description = "反向变更操作数（用于撤销）")
    private Integer inverseOperationCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
