package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * GraphPatch 确认表单。
 *
 * 用户在"图谱变更确认页"提交。只有这一步之后，变更才真正写进 Neo4j。
 *
 * 冲突解决机制：
 * 当某条操作被标为 CONFLICT 时（比如用户在审阅页停了很久，期间图谱已被其他操作改了），
 * 前端会展示冲突详情。用户对每条冲突可以做三种选择：
 * - SKIP：跳过此操作，保留图谱当前状态
 * - FORCE：强制用我的版本覆盖图谱（谨慎使用）
 * - REVIEW：回退到正文审阅，重新抽取 GraphPatch
 *
 * conflictResolutions 的 key 是 operationId，value 是上述三种选择之一。
 * 如果没有传 conflictResolutions 但有 CONFLICT 操作，后端默认按 SKIP 处理。
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

    /**
     * 冲突解决策略 —— key=operationId, value=SKIP|FORCE|REVIEW
     *
     * 如果 value=REVIEW，整个确认流程回退到正文审阅阶段，不执行任何图谱写入。
     */
    private Map<String, String> conflictResolutions;
}
