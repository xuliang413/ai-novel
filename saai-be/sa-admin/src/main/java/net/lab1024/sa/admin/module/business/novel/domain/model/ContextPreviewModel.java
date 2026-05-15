package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 写作前的上下文预览——让用户看到 AI 即将参考哪些信息。
 */
@Data
public class ContextPreviewModel {

    private Long projectId;

    private Integer chapterNo;

    private String projectSummary;

    private List<ContextPreviewItemModel> characterCards = new ArrayList<>();

    private List<ContextPreviewItemModel> clueCards = new ArrayList<>();

    private List<ContextPreviewItemModel> locationCards = new ArrayList<>();

    private Integer estimatedTokens;

    private Integer truncatedItems;
}
