package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.domain.model.ChapterIntentModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ContentQualityCheckModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ContextPreviewModel;

/**
 * 写作启动结果。
 *
 * 前端拿到它后进入“正文审阅页”：展示草稿、写作意图、上下文预览和质检提醒。
 */
@Data
public class NovelWriteDraftVO {

    /**
     * 写作会话 ID，后续正文审阅通过、恢复状态都要带它。
     */
    private Long sessionId;

    /**
     * 当前会话状态，通常是 CONTENT_REVIEW。
     */
    private String sessionStatus;

    /**
     * 生成出来的章节草稿。
     */
    private NovelChapterVO chapter;

    /**
     * 本章写作意图。
     */
    private ChapterIntentModel chapterIntent;

    /**
     * 写作前的上下文预览。
     */
    private ContextPreviewModel contextPreview;

    /**
     * 自动质检提醒。
     */
    private ContentQualityCheckModel qualityCheck;
}
