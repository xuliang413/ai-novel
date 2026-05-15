package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 正文自动质检结果。
 */
@Data
public class ContentQualityCheckModel {

    private Integer wordCount;

    private String pov;

    private Boolean povMentioned;

    private Boolean hasChapterEnding;

    private List<String> newEntityHints = new ArrayList<>();

    private List<String> warnings = new ArrayList<>();
}
