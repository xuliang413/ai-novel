package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * M1 写前上下文预览。
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
