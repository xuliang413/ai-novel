package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * GraphPatch 确认表单。
 */
@Data
public class NovelPatchConfirmForm {

    @NotNull(message = "生成会话 ID 不能为空")
    private Long sessionId;

    /**
     * 用户勾选执行的操作 ID；为空时执行 Patch 中 selected=true 的操作。
     */
    private List<String> operationIds;
}
