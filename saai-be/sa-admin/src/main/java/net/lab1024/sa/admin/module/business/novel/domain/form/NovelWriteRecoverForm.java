package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 写作状态恢复表单。
 */
@Data
public class NovelWriteRecoverForm {

    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 为空时恢复该项目最近一次会话。
     */
    private Integer chapterNo;
}
