package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchModel;

/**
 * GraphPatch 审阅结果。
 */
@Data
public class NovelGraphPatchVO {

    private Long sessionId;

    private String sessionStatus;

    private NovelChapterVO chapter;

    private NovelGraphPatchModel graphPatch;

    private NovelGraphPatchModel inversePatch;
}
