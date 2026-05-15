package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 撤销最近一次图谱变更表单。
 *
 * 这里只传项目 ID，因为撤销目标是该项目最近一次 APPLIED 的 GraphChangeLog。
 */
@Data
public class NovelUndoForm {

    /**
     * 要撤销哪本小说的最近一次图谱变更。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;
}
