package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 小说写作前上下文检索预览 VO。
 * <p>
 * 这个对象不是最终 Prompt，而是给写作引擎和人工审阅看的结构化上下文：包括卷概要、上一章摘要、章节意图、候选角色、地点、线索、关系和 Token 裁剪结果。
 *
 * @Author AI-Novel
 */
@Data
public class NovelRetrieveContextVO {

    /**
     * 项目ID，用来让调用方确认这份上下文属于哪一本小说。
     */
    @Schema(description = "项目ID")
    private Long projectId;

    /**
     * 目标章节号，表示本次上下文是为哪一章准备的。
     */
    @Schema(description = "目标章节号")
    private Integer chapterNumber;

    /**
     * 本章 POV 角色名，优先来自当前章，其次来自项目主角名，最后从 PROTAGONIST 角色兜底。
     */
    @Schema(description = "本章POV角色名")
    private String pov;

    /**
     * 本章写作意图，优先来自章节细纲摘要，其次来自调用方传入的用户方向。
     */
    @Schema(description = "本章写作意图")
    private String chapterIntent;

    /**
     * 用户临时写作方向，保留原始输入，方便后续人工审阅 Prompt 来源。
     */
    @Schema(description = "用户临时写作方向")
    private String userDirection;

    /**
     * 当前章所属卷概要；章节未归卷时为空。
     */
    @Schema(description = "当前章所属卷概要")
    private TextBlockVO volumeSummary;

    /**
     * 上一章摘要；第一章或上一章不存在时为空。
     */
    @Schema(description = "上一章摘要")
    private TextBlockVO previousChapter;

    /**
     * 当前章细纲摘要；没有细纲时为空。
     */
    @Schema(description = "当前章细纲摘要")
    private TextBlockVO outline;

    /**
     * 写作候选角色，已经排除死亡角色，并按 POV、上一章出场、角色定位排序。
     */
    @Schema(description = "写作候选角色")
    private List<CharacterCardVO> characters = new ArrayList<>();

    /**
     * 写作候选地点，优先包含 POV 当前地点；首章无候选时兜底项目全量地点。
     */
    @Schema(description = "写作候选地点")
    private List<LocationCardVO> locations = new ArrayList<>();

    /**
     * 写作候选线索，只包含写前需要推进的活跃主线线索。
     */
    @Schema(description = "写作候选线索")
    private List<ClueCardVO> clues = new ArrayList<>();

    /**
     * 候选角色之间的关键关系，用来辅助写作时保持人物关系连续。
     */
    @Schema(description = "候选角色之间的关键关系")
    private List<RelationCardVO> relations = new ArrayList<>();

    /**
     * Token 统计与裁剪信息，记录本次上下文是否被压缩或丢弃内容。
     */
    @Schema(description = "Token统计与裁剪信息")
    private TokenStatsVO tokenStats;

    /**
     * 兜底说明，记录本次检索为什么跳过某些阶段或启用兜底。
     */
    @Schema(description = "兜底说明")
    private List<String> fallbackNotes = new ArrayList<>();

    /**
     * 通用文本块 VO，用来承载卷概要、上一章摘要和细纲摘要。
     */
    @Data
    public static class TextBlockVO {

        /**
         * 文本来源类型，如 VOLUME、PREVIOUS_CHAPTER、OUTLINE。
         */
        @Schema(description = "文本来源类型")
        private String sourceType;

        /**
         * 来源业务ID；上一章使用章节ID，卷使用卷ID，细纲使用细纲ID。
         */
        @Schema(description = "来源业务ID")
        private Long sourceId;

        /**
         * 标题，用于人工审阅时快速识别这段文本。
         */
        @Schema(description = "标题")
        private String title;

        /**
         * 摘要正文，进入 Prompt 前会参与 Token 预算裁剪。
         */
        @Schema(description = "摘要正文")
        private String summary;

        /**
         * 当前详情级别，FULL 表示完整卡片，SHORT 表示已被压缩。
         */
        @Schema(description = "当前详情级别")
        private String detailLevel;

        /**
         * 估算 Token 数，使用当前阶段的轻量字符数估算法。
         */
        @Schema(description = "估算Token数")
        private Integer estimatedTokens;
    }

    /**
     * 角色候选卡片 VO。
     */
    @Data
    public static class CharacterCardVO {

        /**
         * 角色ID。
         */
        @Schema(description = "角色ID")
        private Long id;

        /**
         * 角色名。
         */
        @Schema(description = "角色名")
        private String name;

        /**
         * 角色定位，如 PROTAGONIST、SUPPORTING。
         */
        @Schema(description = "角色定位")
        private String roleType;

        /**
         * 基础描述，来自角色设定。
         */
        @Schema(description = "基础描述")
        private String description;

        /**
         * 当前目标，来自写作流程维护的动态状态。
         */
        @Schema(description = "当前目标")
        private String currentGoal;

        /**
         * 当前情绪，帮助写作时保持人物状态。
         */
        @Schema(description = "当前情绪")
        private String currentEmotion;

        /**
         * 候选来源，如 POV、PREVIOUS_CHAPTER、ROLE_PRIORITY。
         */
        @Schema(description = "候选来源")
        private String source;

        /**
         * 排序权重，数值越小越靠前。
         */
        @Schema(description = "排序权重")
        private Integer priority;

        /**
         * 当前详情级别，FULL 表示完整卡片，SHORT 表示已被压缩。
         */
        @Schema(description = "当前详情级别")
        private String detailLevel;

        /**
         * 估算 Token 数，使用当前阶段的轻量字符数估算法。
         */
        @Schema(description = "估算Token数")
        private Integer estimatedTokens;
    }

    /**
     * 地点候选卡片 VO。
     */
    @Data
    public static class LocationCardVO {

        /**
         * 地点ID。
         */
        @Schema(description = "地点ID")
        private Long id;

        /**
         * 地点名。
         */
        @Schema(description = "地点名")
        private String name;

        /**
         * 地点类型，如 SECT、CITY、BUILDING。
         */
        @Schema(description = "地点类型")
        private String type;

        /**
         * 地点摘要，帮助写作时保持空间设定。
         */
        @Schema(description = "地点摘要")
        private String summary;

        /**
         * 候选来源，如 CURRENT_LOCATION、FIRST_CHAPTER_FULL_POOL。
         */
        @Schema(description = "候选来源")
        private String source;

        /**
         * 排序权重，数值越小越靠前。
         */
        @Schema(description = "排序权重")
        private Integer priority;

        /**
         * 当前详情级别，FULL 表示完整卡片，SHORT 表示已被压缩。
         */
        @Schema(description = "当前详情级别")
        private String detailLevel;

        /**
         * 估算 Token 数，使用当前阶段的轻量字符数估算法。
         */
        @Schema(description = "估算Token数")
        private Integer estimatedTokens;
    }

    /**
     * 线索候选卡片 VO。
     */
    @Data
    public static class ClueCardVO {

        /**
         * 线索ID。
         */
        @Schema(description = "线索ID")
        private Long id;

        /**
         * 线索名。
         */
        @Schema(description = "线索名")
        private String name;

        /**
         * 线索子类型，当前写前检索主要使用 PLOT_THREAD。
         */
        @Schema(description = "线索子类型")
        private String subType;

        /**
         * 线索描述，来自长期设定。
         */
        @Schema(description = "线索描述")
        private String description;

        /**
         * 当前推进摘要，来自写作流程维护的动态状态。
         */
        @Schema(description = "当前推进摘要")
        private String summary;

        /**
         * 线索优先级，数值越高越优先进入上下文。
         */
        @Schema(description = "线索优先级")
        private Integer priority;

        /**
         * 当前详情级别，FULL 表示完整卡片，SHORT 表示已被压缩。
         */
        @Schema(description = "当前详情级别")
        private String detailLevel;

        /**
         * 估算 Token 数，使用当前阶段的轻量字符数估算法。
         */
        @Schema(description = "估算Token数")
        private Integer estimatedTokens;
    }

    /**
     * 角色关系卡片 VO。
     */
    @Data
    public static class RelationCardVO {

        /**
         * 关系ID。
         */
        @Schema(description = "关系ID")
        private Long id;

        /**
         * 源角色ID。
         */
        @Schema(description = "源角色ID")
        private Long characterId;

        /**
         * 源角色名。
         */
        @Schema(description = "源角色名")
        private String characterName;

        /**
         * 目标角色ID。
         */
        @Schema(description = "目标角色ID")
        private Long targetCharacterId;

        /**
         * 目标角色名。
         */
        @Schema(description = "目标角色名")
        private String targetCharacterName;

        /**
         * 关系类型，如 KNOWS、LOVES、HATES、IS_FAMILY_OF。
         */
        @Schema(description = "关系类型")
        private String relationType;

        /**
         * 关系补充说明，用来保存子类型、爱慕状态、仇恨强度或亲缘类型。
         */
        @Schema(description = "关系补充说明")
        private String relationDetail;

        /**
         * 当前详情级别，FULL 表示完整卡片，SHORT 表示已被压缩。
         */
        @Schema(description = "当前详情级别")
        private String detailLevel;

        /**
         * 估算 Token 数，使用当前阶段的轻量字符数估算法。
         */
        @Schema(description = "估算Token数")
        private Integer estimatedTokens;
    }

    /**
     * Token 统计与裁剪结果 VO。
     */
    @Data
    public static class TokenStatsVO {

        /**
         * 项目配置的上下文软预算。
         */
        @Schema(description = "上下文软预算")
        private Integer tokenBudget;

        /**
         * 项目配置的上下文硬上限。
         */
        @Schema(description = "上下文硬上限")
        private Integer tokenHardLimit;

        /**
         * 当前上下文估算 Token 数。
         */
        @Schema(description = "当前上下文估算Token数")
        private Integer estimatedTokens;

        /**
         * 是否发生过压缩或丢弃。
         */
        @Schema(description = "是否发生过压缩或丢弃")
        private Boolean truncated;

        /**
         * 被压缩成 SHORT 详情级别的卡片数量。
         */
        @Schema(description = "被压缩卡片数量")
        private Integer shortenedCount;

        /**
         * 因硬上限被丢弃的低优先级卡片数量。
         */
        @Schema(description = "被丢弃卡片数量")
        private Integer discardedCount;
    }
}
