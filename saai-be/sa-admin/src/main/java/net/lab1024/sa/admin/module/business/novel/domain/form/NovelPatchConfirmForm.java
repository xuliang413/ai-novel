package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * GraphPatch 确认表单。
 *
 * 用户在“图谱变更确认页”提交。只有这一步之后，变更才真正写进 Neo4j。
 */
@Data
public class NovelPatchConfirmForm {

    /**
     * 本次写作会话 ID。
     */
    @NotNull(message = "生成会话 ID 不能为空")
    private Long sessionId;

    /**
     * 用户勾选执行的操作 ID；为空时执行 Patch 中 selected=true 的操作。
     */
    private List<String> operationIds;
}
