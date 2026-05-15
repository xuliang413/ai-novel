package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 写作前的上下文预览——让用户看到 AI 即将参考哪些信息。
 *
 * 它面向前端展示，不直接写数据库。作用是让人先看一眼：
 * “这一章 AI 准备拿哪些角色、线索、地点做参考？”
 */
@Data
public class ContextPreviewModel {

    /**
     * 小说项目 ID。
     */
    private Long projectId;

    /**
     * 章节序号。
     */
    private Integer chapterNo;

    /**
     * 项目摘要。
     *
     * 给审阅人快速确认 AI 是否拿到了正确作品背景。
     */
    private String projectSummary;

    /**
     * 角色上下文卡片。
     */
    private List<ContextPreviewItemModel> characterCards = new ArrayList<>();

    /**
     * 线索上下文卡片。
     */
    private List<ContextPreviewItemModel> clueCards = new ArrayList<>();

    /**
     * 地点上下文卡片。
     */
    private List<ContextPreviewItemModel> locationCards = new ArrayList<>();

    /**
     * 粗略 token 估算。
     *
     * 用来提示上下文体量，不是最终模型计费值。
     */
    private Integer estimatedTokens;

    /**
     * 被裁掉的上下文条数。
     *
     * 当前主流程里通常为 0，后续做智能裁剪时会用到。
     */
    private Integer truncatedItems;
}
