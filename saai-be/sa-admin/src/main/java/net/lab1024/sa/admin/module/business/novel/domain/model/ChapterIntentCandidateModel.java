package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

/**
 * ChapterIntent 候选实体。
 *
 * 一个候选实体可以是角色、地点或线索。它把“实体是谁”和“本章有多重要”放在一起，
 * 方便后面排序、预览和拼 Prompt。
 */
@Data
public class ChapterIntentCandidateModel {

    /**
     * 候选实体在 MySQL 里的主键。
     *
     * 如果是 AI 临时建议的新实体，未来也可能为空；当前流程主要使用已有实体。
     */
    private Long id;

    /**
     * 展示名称。
     *
     * 例如角色名、地点名、线索名。检索 Neo4j 时也常用名称匹配。
     */
    private String name;

    /**
     * 业务类型。
     *
     * 对角色是 PROTAGONIST / SUPPORTING 这类定位；
     * 对线索是 MAIN / SUB / HIDDEN；
     * 对地点是 CITY / BUILDING 等。
     */
    private String type;

    /**
     * 候选来源。
     *
     * 例如 USER_OR_PROJECT 表示来自用户选择或项目默认候选，便于排查“它为什么进了 Prompt”。
     */
    private String source;

    /**
     * 是否强候选。
     *
     * true 表示这一章尽量要考虑它；false 只是可参考，不要求一定写进去。
     */
    private Boolean required;

    /**
     * 优先级。
     *
     * 数字越小越靠前。当前服务默认按候选顺序赋值，前 3 个 usually required。
     */
    private Integer priority;
}
