package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

/**
 * 上下文预览条目。
 */
@Data
public class ContextPreviewItemModel {

    private Long id;

    private String name;

    private String type;

    private String summary;

    private String source;

    private Boolean required;

    private Integer priority;
}
