package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

/**
 * ChapterIntent 候选实体。
 */
@Data
public class ChapterIntentCandidateModel {

    private Long id;

    private String name;

    private String type;

    private String source;

    private Boolean required;

    private Integer priority;
}
