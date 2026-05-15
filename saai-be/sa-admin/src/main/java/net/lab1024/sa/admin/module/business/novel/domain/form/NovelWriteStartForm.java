package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * M1 mock 写作启动表单。
 */
@Data
public class NovelWriteStartForm {

    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 指定章节序号；为空时自动使用下一章。
     */
    private Integer chapterNo;

    private String pov;

    private String chapterGoal;

    private List<String> candidateCharacters;

    private List<String> targetClues;

    private List<String> candidateLocations;

    private List<String> extraInstructions;
}
