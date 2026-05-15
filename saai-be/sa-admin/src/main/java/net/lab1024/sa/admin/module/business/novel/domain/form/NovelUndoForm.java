package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 撤销最近一次图谱变更表单。
 */
@Data
public class NovelUndoForm {

    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;
}
