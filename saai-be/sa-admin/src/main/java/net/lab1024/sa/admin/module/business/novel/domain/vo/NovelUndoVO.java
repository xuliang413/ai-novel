package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchModel;

/**
 * 图谱撤销结果。
 */
@Data
public class NovelUndoVO {

    private String operationBatchId;

    private Long chapterId;

    private Integer chapterNo;

    private String graphChangeStatus;

    private String chapterStatus;

    private NovelGraphPatchModel inversePatch;
}
