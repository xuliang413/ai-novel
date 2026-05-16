package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * GraphPatch 预览 VO —— 前端图谱变更确认面板的完整数据
 *
 * 为什么需要这个 VO：
 * 之前的 NovelGraphPatchVO 直接包装了 NovelGraphPatchModel，前端拿到的数据太"后端化"——
 * 没有风险等级、没有校验结果、没有汇总统计，前端只能自己数、自己判断。
 * 现在把 GraphPatch 预览做成一个独立的、前端友好的结构，包含汇总统计和每条操作的完整审阅信息。
 *
 * 前端使用流程：
 * 1. 拿到 GraphPatchPreviewVO，先看 summary，了解总共多少条、多少高风险、多少被阻断
 * 2. 遍历 operations，每条展示为一行，勾选/取消
 * 3. 高风险条目默认不勾选（前端根据 riskLevel 判断），用户可以手动勾选
 * 4. 被阻断的条目（validationStatus=BLOCKED）无法勾选
 * 5. 确认后提交勾选的 operationId 列表给后端
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelGraphPatchPreviewVO {

    @Schema(description = "写作会话ID")
    private Long sessionId;

    @Schema(description = "章节ID")
    private Long chapterId;

    @Schema(description = "章节序号")
    private Integer chapterNo;

    @Schema(description = "章节标题")
    private String chapterTitle;

    @Schema(description = "操作批次ID")
    private String operationBatchId;

    @Schema(description = "汇总统计")
    private PatchSummary summary;

    @Schema(description = "每条操作的详细审阅信息")
    private List<PatchOperationItem> operations;

    // ==================== 内嵌 VO ====================

    /**
     * 图谱变更汇总统计
     *
     * 让前端确认按钮之前先看清：一共多少条、哪些有风险、哪些被拦截了。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatchSummary {

        @Schema(description = "操作总数")
        private int totalOperations;

        @Schema(description = "高风险操作数")
        private int highRiskCount;

        @Schema(description = "中风险操作数")
        private int mediumRiskCount;

        @Schema(description = "低风险操作数")
        private int lowRiskCount;

        @Schema(description = "被阻断操作数（校验不通过，无法执行）")
        private int blockedCount;

        @Schema(description = "需要人工确认的操作数（高风险 + 校验警告）")
        private int needManualReviewCount;

        @Schema(description = "受影响资产数 —— 唯一节点数")
        private int affectedAssetCount;
    }

    /**
     * 单条图谱变更操作的预览项
     *
     * 每一条就是确认面板上的一行。包含了后端对这条操作的完整判断：
     * 它要做什么、风险多大、来源是什么、有没有冲突、该不该默认勾选。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatchOperationItem {

        @Schema(description = "操作ID，前端用这个 id 标记勾选/取消")
        private String operationId;

        @Schema(description = "操作类型枚举值", example = "MOVE_CHARACTER")
        private String operationType;

        @Schema(description = "操作类型中文名", example = "移动角色位置")
        private String operationName;

        @Schema(description = "风险等级", example = "HIGH")
        private String riskLevel;

        @Schema(description = "目标实体类型", example = "CHARACTER")
        private String targetType;

        @Schema(description = "目标实体ID（MySQL 主键）")
        private Long targetId;

        @Schema(description = "目标实体名称", example = "叶尘")
        private String targetName;

        @Schema(description = "变更前快照 —— 修改前的值", example = "青云城")
        private String beforeSnapshot;

        @Schema(description = "变更后快照 —— 修改后的值", example = "长安城")
        private String afterSnapshot;

        @Schema(description = "操作来源证据 —— AI 从正文哪句话抽出了这条操作", example = "叶尘离开青云城，踏上了前往长安的官道。")
        private String evidence;

        @Schema(description = "操作原因 —— AI 为什么建议执行这个操作", example = "原文明确描写角色移动")
        private String reason;

        @Schema(description = "置信度", example = "HIGH")
        private String confidence;

        @Schema(description = "是否默认勾选 —— 后端建议，前端可以覆盖")
        private Boolean selected;

        @Schema(description = "校验状态", example = "PASSED")
        private String validationStatus;

        @Schema(description = "校验信息 —— 校验失败/警告时的人读说明", example = "叶尘在第8章已经在长安城，无需再次移动")
        private String validationMessage;

        @Schema(description = "阻断原因 —— 为什么这条操作被拦截", example = "与第8章已确认状态冲突")
        private String blockedReason;

        @Schema(description = "关联章节序号 —— 抽取出处的章节")
        private Integer relatedChapterNo;

        @Schema(description = "扩展属性（辅助展示用）")
        private Map<String, Object> extraProperties;

        /**
         * 校验状态常量
         */
        public static final String VALIDATION_PASSED = "PASSED";
        public static final String VALIDATION_WARNING = "WARNING";
        public static final String VALIDATION_BLOCKED = "BLOCKED";
        public static final String VALIDATION_PENDING = "PENDING";
    }
}
