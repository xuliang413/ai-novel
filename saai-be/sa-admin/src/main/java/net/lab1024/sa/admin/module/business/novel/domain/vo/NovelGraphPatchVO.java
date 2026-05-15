package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchModel;

/**
 * GraphPatch 审阅结果。
 *
 * 前端拿到它后进入“图谱变更确认页”：展示正向变更单和可撤销用的反向变更单。
 */
@Data
public class NovelGraphPatchVO {

    /**
     * 写作会话 ID。
     */
    private Long sessionId;

    /**
     * 当前会话状态，通常是 PATCH_REVIEW。
     */
    private String sessionStatus;

    /**
     * 正在确认图谱变更的章节。
     */
    private NovelChapterVO chapter;

    /**
     * 正向变更单，用户确认后会写入 Neo4j。
     */
    private NovelGraphPatchModel graphPatch;

    /**
     * 反向变更单，用于后续 undo。
     */
    private NovelGraphPatchModel inversePatch;
}
