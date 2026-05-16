package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 小说模块操作结果 VO
 *
 * 为什么不用 ResponseDTO 直接返回：
 * ResponseDTO 是全局通用的"成功/失败"响应壳，适合查询接口。但小说模块里有很多写操作
 * （确认 GraphPatch、撤销、恢复等）不只是成功或失败，还带回了统计信息、告警提示、
 * 请求追踪 ID。这些信息如果塞进 ResponseDTO.data 里会让结构嵌套太深，不如直接
 * 提供一个专用 VO，让前端写操作面板能拿到完整的操作反馈。
 *
 * 使用场景：
 * - GraphPatch 确认：返回新增/修改了多少节点和关系，哪些被阻断
 * - 撤销/恢复：返回回退了多少条操作，是否有异常
 * - 批量资产操作：返回成功/失败明细
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelOperationResultVO {

    /**
     * 操作是否整体成功
     *
     * 注意：success=true 不代表没有警告。比如 GraphPatch 确认成功了，但可能有
     * 一条高风险操作被自动跳过，这个会在 warnings 里体现。
     */
    @Schema(description = "操作是否整体成功", example = "true")
    private Boolean success;

    /**
     * 错误码 —— 失败时从这里取
     *
     * 值来自 NovelErrorCodeEnum，比如 40502=PATCH_VALIDATION_FAILED。
     */
    @Schema(description = "错误码，失败时不为空", example = "40502")
    private Integer errorCode;

    /**
     * 错误描述 —— 给人看
     */
    @Schema(description = "错误描述", example = "图谱变更校验不通过")
    private String errorMessage;

    /**
     * 请求追踪 ID —— 排查问题时用这个 ID 在日志里搜索
     *
     * 每一次操作都会生成一个唯一的 requestId，写入日志和数据库。
     * 如果用户反馈"操作失败了"，你只需要让他提供这个 requestId 就能快速定位。
     */
    @Schema(description = "请求追踪ID，用于排错", example = "req_abc123def456")
    private String requestId;

    /**
     * 操作批次 ID —— 关联 graph_change_log 的 operationBatchId
     *
     * GraphPatch 确认后会生成 batchId，撤销也用同一个 batchId。
     */
    @Schema(description = "操作批次ID，关联图谱变更日志", example = "batch_20260515_001")
    private String operationBatchId;

    /**
     * 变更统计 —— 本次操作影响了多少节点和关系
     */
    @Schema(description = "变更统计")
    private ChangeSummaryVO changeSummary;

    /**
     * 警告信息列表 —— 不影响操作成功的提示
     *
     * 比如某些高风险操作被自动跳过、某些关系因为 before 值冲突被忽略等。
     * 前端应在前端面板里醒目展示。
     */
    @Schema(description = "警告信息列表")
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    /**
     * 变更统计子 VO
     *
     * 一次写操作（尤其是 GraphPatch）可能同时创建、修改、删除多项节点和关系。
     * 前端需要知道"本次操作到底改了什么"，这个统计结构就是为此准备的。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeSummaryVO {

        @Schema(description = "新增节点数", example = "3")
        private int createdNodes;

        @Schema(description = "更新节点数", example = "1")
        private int updatedNodes;

        @Schema(description = "归档节点数", example = "0")
        private int archivedNodes;

        @Schema(description = "新增关系数", example = "5")
        private int createdRelations;

        @Schema(description = "删除关系数", example = "0")
        private int deletedRelations;

        @Schema(description = "被阻断的操作数", example = "1")
        private int blockedOperations;

        @Schema(description = "被跳过的操作数（低置信度/高风险自动跳过）", example = "0")
        private int skippedOperations;

        /**
         * 是否有任何变更发生
         *
         * 如果所有操作都被阻断/跳过，这里为 false，前端可以提示用户
         * "本次没有发生任何变更"。
         */
        public boolean hasAnyChange() {
            return createdNodes > 0 || updatedNodes > 0 || archivedNodes > 0
                    || createdRelations > 0 || deletedRelations > 0;
        }
    }
}
