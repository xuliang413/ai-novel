package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 写作意图——这一章视角谁、想写什么、要推进哪些线索。
 *
 * 它不是数据库实体，而是一次写作前整理出来的“任务说明”。
 * 后续检索上下文、拼 Prompt、生成质检和恢复会话都会用它。
 */
@Data
public class ChapterIntentModel {

    /**
     * 小说项目 ID。
     *
     * 主要用于恢复会话和排查日志，真正查项目详情还是走 NovelProjectEntity。
     */
    private Long projectId;

    /**
     * 本次要写第几章。
     *
     * 用户不传时，NovelWriteService 会自动取当前项目的下一章。
     */
    private Integer chapterNo;

    /**
     * POV 视角角色。
     *
     * 可以理解成“这一章主要跟着谁看世界”。为空时会依次取项目主角、第一位角色兜底。
     */
    private String pov;

    /**
     * 本章目标。
     *
     * 例如“让主角发现密室线索”“完成第一次宗门冲突”。
     * 模型写作时会把它放在 Prompt 的靠前位置。
     */
    private String chapterGoal;

    /**
     * 本章候选角色。
     *
     * 不是说这些角色一定全部出场，而是告诉检索和模型“优先考虑这几个人”。
     */
    private List<ChapterIntentCandidateModel> candidateCharacters = new ArrayList<>();

    /**
     * 本章希望推进的线索。
     *
     * 写作前用于检索线索状态，写作后也会影响 GraphPatch 兜底生成。
     */
    private List<ChapterIntentCandidateModel> targetClues = new ArrayList<>();

    /**
     * 本章候选地点。
     *
     * 让模型知道场景可能在哪里发生，也给上下文预览页展示。
     */
    private List<ChapterIntentCandidateModel> candidateLocations = new ArrayList<>();

    /**
     * 用户临时补充的写作要求。
     *
     * 例如“这一章不要揭开真相”“加强压迫感”。这些指令不会落库成长期设定。
     */
    private List<String> extraInstructions = new ArrayList<>();
}
