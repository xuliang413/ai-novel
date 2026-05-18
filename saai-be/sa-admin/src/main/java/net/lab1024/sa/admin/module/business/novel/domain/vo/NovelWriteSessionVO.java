package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 写作会话 VO —— 展示一次章节生成任务的完整状态
 * 包括状态机当前阶段、生成内容、质检结果和下一步可用操作
 *
 * @Author AI-Novel
 */
@Data
public class NovelWriteSessionVO {

    /**
     * 会话ID, 对应 chapter_generation_session 表主键
     */
    @Schema(description = "会话ID")
    private Long sessionId;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 目标章节号
     */
    @Schema(description = "目标章节号")
    private Integer chapterNumber;

    /**
     * 会话状态: GENERATING/CONTENT_REVIEW/PATCH_REVIEW/PENDING_GRAPH_UPDATE/SUCCESS/INTERRUPTED/FAILED
     */
    @Schema(description = "会话状态")
    private String status;

    /**
     * 生成的章节标题
     */
    @Schema(description = "章节标题")
    private String title;

    /**
     * 生成的章节摘要, 前300字
     */
    @Schema(description = "章节摘要")
    private String summary;

    /**
     * 生成的章节正文全文
     */
    @Schema(description = "章节正文")
    private String content;

    /**
     * 生成字符数
     */
    @Schema(description = "生成字符数")
    private Integer wordCount;

    /**
     * 使用的LLM提供商, 如DEEPSEEK/MOCK
     */
    @Schema(description = "使用的LLM提供商")
    private String provider;

    /**
     * 质检旁注: 字数检测/视角一致性/新实体提醒等
     */
    @Schema(description = "质检旁注列表")
    private java.util.List<String> qualityNotes;

    /**
     * 候选GraphPatch列表, 进入PATCH_REVIEW状态后填充
     */
    @Schema(description = "候选GraphPatch列表")
    private java.util.List<NovelGraphPatchVO> patches;

    /**
     * 重试次数, 自动降级Mock触发阈值=3
     */
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 会话创建时间
     */
    @Schema(description = "会话创建时间")
    private LocalDateTime createTime;

    /**
     * 下一步可用操作: START/CONTENT_REVIEW_PASS/CONTENT_REVIEW_EDIT/PATCH_CONFIRM/PATCH_BACK/RETRY/PUBLISH
     */
    @Schema(description = "下一步可用操作列表")
    private java.util.List<String> availableActions;

    /**
     * GraphPatch VO —— 单一图谱变更项
     */
    @Data
    public static class NovelGraphPatchVO {
        /** 操作类型编码, 对应NovelGraphPatchOperationTypeEnum */
        @Schema(description = "操作类型编码")
        private String operationType;

        /** 操作中文描述 */
        @Schema(description = "操作描述")
        private String operationDesc;

        /** 涉及的角色名 */
        @Schema(description = "涉及的角色名")
        private String characterName;

        /** 变更前的值, 用于审阅页面对比 */
        @Schema(description = "变更前的值")
        private String beforeValue;

        /** 变更后的值, LLM抽取结果 */
        @Schema(description = "变更后的值")
        private String afterValue;

        /** LLM抽取的置信度 0~1 */
        @Schema(description = "置信度")
        private Float confidence;

        /** 风险等级: LOW/HIGH, 高风险默认不勾选 */
        @Schema(description = "风险等级")
        private String riskLevel;

        /** 是否被用户勾选确认(审阅阶段使用) */
        @Schema(description = "是否已确认")
        private Boolean confirmed;
    }
}
