package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 写作状态恢复表单。
 *
 * 前端刷新或用户回到写作页时使用，用来找回未完成的审阅/确认状态。
 */
@Data
public class NovelWriteRecoverForm {

    /**
     * 小说项目 ID。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 为空时恢复该项目最近一次会话。
     */
    private Integer chapterNo;
}
