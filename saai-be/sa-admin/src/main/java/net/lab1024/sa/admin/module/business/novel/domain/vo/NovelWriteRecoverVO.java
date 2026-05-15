package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.domain.model.ChapterIntentModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ContentQualityCheckModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ContextPreviewModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchModel;

/**
 * 写作恢复结果。
 */
@Data
public class NovelWriteRecoverVO {

    private Long sessionId;

    private String sessionStatus;

    private String operationBatchId;

    private NovelChapterVO chapter;

    private ChapterIntentModel chapterIntent;

    private ContextPreviewModel contextPreview;

    private ContentQualityCheckModel qualityCheck;

    private NovelGraphPatchModel graphPatch;

    private NovelGraphPatchModel inversePatch;
}
