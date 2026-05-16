package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 正文审阅通过表单。
 *
 * 用户在正文审阅页点“通过”时提交。提交后不会立刻发布章节，
 * 而是进入 GraphPatch 生成和确认阶段。
 */
@Data
public class NovelContentReviewPassForm {

    /**
     * 本次写作会话 ID。
     *
     * 后端靠它找到 intent、上下文快照和生成状态。
     */
    @NotNull(message = "生成会话 ID 不能为空")
    private Long sessionId;

    /**
     * 本次审阅的章节 ID。
     */
    @NotNull(message = "章节 ID 不能为空")
    private Long chapterId;

    /**
     * 用户审阅后修改的正文；为空则沿用当前草稿。
     */
    private String content;

    /**
     * 用户审阅后修改的标题；为空则沿用当前标题。
     *
     * 前端写作工作台允许用户在正文审阅页直接改标题，然后一次性提交。
     * 如果不同时支持标题编辑，前端就需要单独调 /novel/chapter/update，破坏单步提交体验。
     */
    private String title;

    /**
     * 用户审阅后修改的摘要；为空则使用当前摘要。
     */
    private String summary;
}
