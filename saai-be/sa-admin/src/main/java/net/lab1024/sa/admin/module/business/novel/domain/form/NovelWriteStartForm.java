package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 写作启动表单。
 *
 * 前端点“开始写作”时传进来。这里的字段不会直接写正文，
 * 而是先被整理成 ChapterIntent，再进入检索和 Prompt 生成。
 */
@Data
public class NovelWriteStartForm {

    /**
     * 要写哪一本小说。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 指定章节序号；为空时自动使用下一章。
     */
    private Integer chapterNo;

    /**
     * 本章视角角色。
     *
     * 为空时后端自动使用项目主角或第一个角色。
     */
    private String pov;

    /**
     * 本章创作目标。
     *
     * 这是给 AI 的“本章任务”，不是最终章节摘要。
     */
    private String chapterGoal;

    /**
     * 希望参与本章的角色名称列表。
     *
     * 后端会用名称去匹配已有角色，匹配不到时回退默认候选。
     */
    private List<String> candidateCharacters;

    /**
     * 希望推进的线索名称列表。
     */
    private List<String> targetClues;

    /**
     * 候选场景/地点名称列表。
     */
    private List<String> candidateLocations;

    /**
     * 本次写作的临时额外要求。
     *
     * 例如“这一章不要揭露身份”。它只影响本次生成，不会变成长期叙事规则。
     */
    private List<String> extraInstructions;
}
