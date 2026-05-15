package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 正文自动质检结果。
 *
 * 这不是“判定文章好坏”的系统，只是把明显风险提前摆出来，
 * 让作者在正文审阅页能更快发现问题。
 */
@Data
public class ContentQualityCheckModel {

    /**
     * 正文字数。
     *
     * 当前按去掉空白后的字符数粗略计算，主要用于提示“太短/太长”。
     */
    private Integer wordCount;

    /**
     * 本章期望 POV。
     *
     * 来自 ChapterIntent，方便前端把质检结果和写作意图一起展示。
     */
    private String pov;

    /**
     * POV 角色是否在正文中出现。
     *
     * 这里只做简单字符串检测，不代表真正的叙事视角判断。
     */
    private Boolean povMentioned;

    /**
     * 是否检测到章末推进入口。
     *
     * 目前是关键词级别的轻量检测，用于提醒人工复核。
     */
    private Boolean hasChapterEnding;

    /**
     * 正文中疑似新实体的提示。
     *
     * 预留字段：以后可以把“正文出现但资产库没有”的人名、地点、物品放进来。
     */
    private List<String> newEntityHints = new ArrayList<>();

    /**
     * 质检警告。
     *
     * 例如字数偏短、POV 未出现、章末没有明显推进。
     */
    private List<String> warnings = new ArrayList<>();
}
