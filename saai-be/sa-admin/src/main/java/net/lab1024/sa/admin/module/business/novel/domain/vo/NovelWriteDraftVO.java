package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.domain.model.ChapterIntentModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ContentQualityCheckModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ContextPreviewModel;

/**
 * 写作启动结果。
 */
@Data
public class NovelWriteDraftVO {

    private Long sessionId;

    private String sessionStatus;

    private NovelChapterVO chapter;

    private ChapterIntentModel chapterIntent;

    private ContextPreviewModel contextPreview;

    private ContentQualityCheckModel qualityCheck;
}
