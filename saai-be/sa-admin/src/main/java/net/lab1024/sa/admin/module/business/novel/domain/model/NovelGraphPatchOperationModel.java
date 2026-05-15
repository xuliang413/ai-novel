package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

/**
 * GraphPatch 单个白名单业务操作。
 */
@Data
public class NovelGraphPatchOperationModel {

    private String operationId;

    private String operationType;

    private String targetType;

    private Long targetId;

    private String targetName;

    private String beforeStatus;

    private String afterStatus;

    private String beforeSummary;

    private String afterSummary;

    private String confidence;

    private String validationStatus;

    private Boolean selected;

    private String evidence;

    private String reason;
}
