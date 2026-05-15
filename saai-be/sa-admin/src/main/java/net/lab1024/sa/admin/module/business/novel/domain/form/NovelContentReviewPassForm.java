package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 正文审阅通过表单。
 */
@Data
public class NovelContentReviewPassForm {

    @NotNull(message = "生成会话 ID 不能为空")
    private Long sessionId;

    @NotNull(message = "章节 ID 不能为空")
    private Long chapterId;

    /**
     * 用户审阅后修改的正文；为空则沿用当前草稿。
     */
    private String content;

    /**
     * 用户审阅后修改的摘要；为空则使用当前摘要。
     */
    private String summary;
}
