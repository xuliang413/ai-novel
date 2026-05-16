package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 章节上下文预览 VO —— 生成正文前展示给用户审阅的上下文材料
 *
 * 为什么需要上下文审阅：
 * AI 生成正文时会从 Neo4j 检索前文摘要、角色状态、线索进度等作为 Prompt 上下文。
 * 如果检索出来的材料不对（比如漏了关键角色、多了一条不相关的线索），生成质量会很差。
 * 所以在"检索完→送入 LLM"之间插入一个人工审阅环节：用户看到检索结果，可以排除不想用的、
 * 补充遗漏的，确认后再让 AI 生成正文。
 *
 * 前端展示流程：
 * 1. 用户输入写作目标 → 系统检索上下文 → 返回 ChapterContextPreviewVO
 * 2. 前端展示各板块（前文摘要、角色、地点、线索等）
 * 3. 用户可以排除某些项（点击去掉），可以重新检索
 * 4. 确认后前端把 selectedContextIds 发回给后端
 * 5. 后端只用被选中的上下文注入 Prompt 并开始 LLM 生成
 *
 * 低置信度提示：
 * warnings 字段里会列出潜在问题，比如"角色A已死亡3章但仍在候选列表"。
 * 这是给用户的提醒，不是硬阻断。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterContextPreviewVO {

    @Schema(description = "本次写作目标（用户输入 + 系统解析的意图）")
    private ContextIntent intent;

    @Schema(description = "前文摘要 —— 最近几章的故事回顾")
    private List<ContextItem> previousChapters;

    @Schema(description = "相关角色 —— 本章可能用到的人物及其状态")
    private List<ContextItem> characters;

    @Schema(description = "相关地点 —— 本章可能用到的场景")
    private List<ContextItem> locations;

    @Schema(description = "相关线索 —— 当前在推进或本章应该涉及的剧情线")
    private List<ContextItem> clues;

    @Schema(description = "相关物品/道具 —— 角色当前持有或本章可能出现的")
    private List<ContextItem> items;

    @Schema(description = "叙事规则 —— 写入 Prompt 的写作约束")
    private List<ContextItem> rules;

    @Schema(description = "低置信度提示/冲突警告")
    private List<String> warnings;

    @Schema(description = "用户确认要带入生成的上下文 ID 列表 —— 前端勾选后传回")
    private List<String> selectedContextIds;

    @Schema(description = "本次上下文的 Token 预算", example = "8000")
    private Integer tokenBudget;

    @Schema(description = "已使用的 Token 估算", example = "5200")
    private Integer tokenUsed;

    /**
     * 本次写作意图
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextIntent {

        @Schema(description = "目标章节序号", example = "15")
        private Integer targetChapterNo;

        @Schema(description = "章节建议标题", example = "剑意初成")
        private String suggestedTitle;

        @Schema(description = "写作目标描述 —— 用户原始输入", example = "叶尘在华山之巅悟出第一剑")
        private String goalDescription;

        @Schema(description = "系统解析的写作要点")
        private List<String> keyPoints;

        @Schema(description = "建议字数", example = "4000")
        private Integer suggestedWordCount;

        @Schema(description = "POV 角色名称", example = "叶尘")
        private String povCharacter;
    }

    /**
     * 单条上下文项 —— 上述各板块的通用结构
     *
     * 每一条都有唯一 id 用于前端勾选/取消，有置信度和来源让用户判断要不要相信。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextItem {

        @Schema(description = "上下文项唯一 ID —— 前端用这个 id 标记选中/排除", example = "char_5")
        private String contextId;

        @Schema(description = "实体类型", example = "CHARACTER")
        private String entityType;

        @Schema(description = "实体 MySQL ID")
        private Long entityId;

        @Schema(description = "实体名称", example = "叶尘")
        private String name;

        @Schema(description = "摘要/状态描述", example = "当前位于长安城，状态：受伤")
        private String summary;

        @Schema(description = "检索置信度", example = "HIGH")
        private String confidence;

        @Schema(description = "来源证据 —— 为什么检索系统选了这条上下文",
                example = "本章写作目标中提及该角色")
        private String source;

        @Schema(description = "关联章节序号 —— 最后一次出现或相关的章节")
        private Integer relatedChapterNo;

        @Schema(description = "是否默认选中", example = "true")
        private Boolean selected;

        @Schema(description = "优先级 —— 数字越大越重要，用于排序展示", example = "10")
        private Integer priority;
    }
}
