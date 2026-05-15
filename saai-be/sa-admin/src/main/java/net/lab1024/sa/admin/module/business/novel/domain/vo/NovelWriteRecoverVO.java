package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.domain.model.ChapterIntentModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ContentQualityCheckModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ContextPreviewModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchModel;

/**
 * 写作恢复结果。
 *
 * 它会尽量把用户离开页面前看到的内容还原出来：
 * 草稿、意图、上下文、质检、GraphPatch 都从 session 快照里恢复。
 */
@Data
public class NovelWriteRecoverVO {

    /**
     * 写作会话 ID。
     */
    private Long sessionId;

    /**
     * 当前会话状态。
     */
    private String sessionStatus;

    /**
     * 当前图谱操作批次 ID；如果还没进入 GraphPatch 阶段则为空。
     */
    private String operationBatchId;

    /**
     * 当前章节草稿或待发布章节。
     */
    private NovelChapterVO chapter;

    /**
     * 恢复出来的写作意图。
     */
    private ChapterIntentModel chapterIntent;

    /**
     * 恢复出来的上下文预览。
     */
    private ContextPreviewModel contextPreview;

    /**
     * 恢复出来的质检结果。
     */
    private ContentQualityCheckModel qualityCheck;

    /**
     * 待确认或已确认的正向 GraphPatch。
     */
    private NovelGraphPatchModel graphPatch;

    /**
     * 对应的反向 GraphPatch。
     */
    private NovelGraphPatchModel inversePatch;
}
