package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 图谱变更单。
 */
@Data
public class NovelGraphPatchModel {

    private String patchId;

    private String operationBatchId;

    private Long projectId;

    private Long chapterId;

    private Integer chapterNo;

    private String status;

    private List<NovelGraphPatchOperationModel> operations = new ArrayList<>();

    private List<String> warnings = new ArrayList<>();
}
