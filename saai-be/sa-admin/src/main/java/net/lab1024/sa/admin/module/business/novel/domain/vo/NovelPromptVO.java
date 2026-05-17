package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 小说章节生成 Prompt 展示 VO。
 * <p>
 * 写作服务会把它传给 LLM Service；前端或日志也可以用它审阅本次真正发送给模型的系统提示词和用户提示词。
 *
 * @Author AI-Novel
 */
@Data
public class NovelPromptVO {

    /**
     * 项目ID，用来确认 Prompt 属于哪一本小说。
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 目标章节号。
     */
    @Schema(description = "目标章节号")
    private Integer chapterNumber;

    /**
     * 本章 POV 角色名。
     */
    @Schema(description = "本章POV角色名")
    private String pov;

    /**
     * System Prompt，主要承载身份设定、叙事规则、世界观、文风和字数约束。
     */
    @Schema(description = "System Prompt")
    private String systemPrompt;

    /**
     * User Prompt，主要承载本章任务、检索上下文、角色状态、线索和写作指令。
     */
    @Schema(description = "User Prompt")
    private String userPrompt;

    /**
     * System Prompt 的轻量 Token 估算值，当前按字符数估算。
     */
    @Schema(description = "System Prompt估算Token")
    private Integer systemEstimatedTokens;

    /**
     * User Prompt 的轻量 Token 估算值，当前按字符数估算。
     */
    @Schema(description = "User Prompt估算Token")
    private Integer userEstimatedTokens;

    /**
     * Prompt 总估算 Token 数，等于 System 与 User 的估算值之和。
     */
    @Schema(description = "Prompt总估算Token")
    private Integer estimatedTokens;

    /**
     * 本次组装使用的上下文预览，保留给写作会话快照和人工审阅。
     */
    @Schema(description = "上下文预览")
    private NovelRetrieveContextVO contextPreview;
}
