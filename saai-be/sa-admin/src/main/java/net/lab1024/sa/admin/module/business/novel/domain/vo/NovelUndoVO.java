package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchModel;

/**
 * 图谱撤销结果。
 *
 * 撤销成功后前端用它告诉用户撤了哪一批、章节现在处于什么状态。
 */
@Data
public class NovelUndoVO {

    /**
     * 被撤销的操作批次 ID。
     */
    private String operationBatchId;

    /**
     * 关联章节 ID。
     */
    private Long chapterId;

    /**
     * 关联章节序号。
     */
    private Integer chapterNo;

    /**
     * 图谱变更日志状态，通常变为 UNDONE。
     */
    private String graphChangeStatus;

    /**
     * 章节状态，撤销后通常是 PENDING_GRAPH_UPDATE。
     */
    private String chapterStatus;

    /**
     * 实际执行的反向变更单。
     */
    private NovelGraphPatchModel inversePatch;
}
