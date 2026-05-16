package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 小说项目工作台详情 VO —— 前端首页一个请求拿全所有概览数据
 *
 * 设计思路：
 * 传统做法是前端分别调"项目详情""卷列表""章节列表""资产统计""最近会话"等五六个接口，
 * 不仅请求多、Loading 多，而且各数据之间的时间一致性很难保证。现在把项目工作台需要的
 * 所有数据打包成一个 VO，一次请求全部返回，前端工作台首页一个接口搞定。
 *
 * 数据来源（后端在 Service 里聚合）：
 * - projectInfo    → t_novel_project
 * - volumes        → t_novel_volume + t_novel_chapter
 * - assetSummary   → COUNT(*) from 各资产表
 * - recentSessions → t_chapter_generation_session ORDER BY created_at DESC LIMIT 5
 * - recentLogs     → t_writing_log ORDER BY created_at DESC LIMIT 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelWorkbenchProjectVO {

    /**
     * 项目基本信息
     */
    @Schema(description = "项目基本信息")
    private WorkbenchProjectInfo projectInfo;

    /**
     * 卷与章节树 —— 前端左侧导航的数据来源
     *
     * 按卷分组、卷内按章节序号升序排列。
     * 如果项目没有分卷（legacy 数据），会有一个"默认卷"兜底。
     */
    @Schema(description = "卷与章节列表")
    private List<WorkbenchVolumeBrief> volumes;

    /**
     * 资产统计概览 —— 前端仪表盘数字卡片的数据来源
     *
     * 每种资产类型一个数字，让作者一眼看到"我有多少个角色、多少条线索"。
     */
    @Schema(description = "资产统计概览")
    private AssetCountSummary assetSummary;

    /**
     * 最近写作会话 —— 最近 5 条
     *
     * 前端展示"上次写到哪了"和快速继续入口。
     */
    @Schema(description = "最近写作会话")
    private List<SessionBrief> recentSessions;

    /**
     * 最近写作日志 —— 最近 5 条
     *
     * 前端展示"最近写了多少字""有没有失败"。
     */
    @Schema(description = "最近写作日志")
    private List<WritingLogBrief> recentLogs;

    // ==================== 内嵌 VO 定义 ====================

    /**
     * 工作台项目概要 —— 比 NovelProjectVO 精简，只放工作台需要的关键字段
     *
     * 前端项目卡片展示：书名、类型、状态、主角、写作进度。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkbenchProjectInfo {

        @Schema(description = "项目ID", example = "1")
        private Long projectId;

        @Schema(description = "项目名称/书名", example = "剑道独尊")
        private String projectName;

        @Schema(description = "小说类型", example = "武侠")
        private String genre;

        @Schema(description = "项目简介", example = "一个少年持剑走天涯的故事")
        private String summary;

        @Schema(description = "主角名称", example = "叶尘")
        private String protagonist;

        @Schema(description = "目标字数", example = "1000000")
        private Integer targetWords;

        @Schema(description = "当前总字数", example = "250000")
        private Integer currentWords;

        @Schema(description = "项目状态", example = "ACTIVE")
        private String status;

        @Schema(description = "总章节数（含草稿）", example = "42")
        private Integer totalChapters;

        @Schema(description = "已发布章节数", example = "30")
        private Integer publishedChapters;

        @Schema(description = "创建时间")
        private LocalDateTime createdAt;

        @Schema(description = "最近更新时间")
        private LocalDateTime updatedAt;
    }

    /**
     * 工作台卷概要 —— 导航树里的一级节点
     *
     * 一个项目下可以有多个卷，每个卷下面挂着若干章节。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkbenchVolumeBrief {

        @Schema(description = "卷ID", example = "1")
        private Long volumeId;

        @Schema(description = "卷序号", example = "1")
        private Integer volumeNo;

        @Schema(description = "卷标题", example = "第一卷：少年出山")
        private String volumeTitle;

        @Schema(description = "该卷下的章节列表")
        private List<WorkbenchChapterBrief> chapters;
    }

    /**
     * 工作台章节概要 —— 导航树里的二级节点
     *
     * 不返回正文，只返回章节头部信息，减小传输体积。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkbenchChapterBrief {

        @Schema(description = "章节ID", example = "1")
        private Long chapterId;

        @Schema(description = "章节序号", example = "1")
        private Integer chapterNo;

        @Schema(description = "章节标题", example = "第一章：剑出华山")
        private String title;

        @Schema(description = "章节摘要（前端可做 tooltip 预览）", example = "叶尘在华山之巅悟出第一剑")
        private String summary;

        @Schema(description = "章节状态", example = "PUBLISHED")
        private String status;

        @Schema(description = "正文字数（用于进度统计）", example = "4500")
        private Integer wordCount;

        @Schema(description = "最近更新时间")
        private LocalDateTime updatedAt;
    }

    /**
     * 资产数量统计 —— 工作台仪表盘数字卡片
     *
     * 所有计数字段为 null 时表示尚未加载（与 0 区分）。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetCountSummary {

        @Schema(description = "角色数量", example = "15")
        private Integer characterCount;

        @Schema(description = "地点数量", example = "8")
        private Integer locationCount;

        @Schema(description = "线索数量", example = "5")
        private Integer clueCount;

        @Schema(description = "物品数量", example = "12")
        private Integer itemCount;

        @Schema(description = "事件数量", example = "20")
        private Integer eventCount;

        @Schema(description = "金手指数量", example = "2")
        private Integer cheatCount;

        @Schema(description = "马甲数量", example = "3")
        private Integer aliasCount;

        @Schema(description = "叙事规则数量", example = "4")
        private Integer narrativeRuleCount;

        @Schema(description = "卷数量", example = "3")
        private Integer volumeCount;

        /**
         * 所有资产总数 —— 前端可以不调这个，自己把上面加起来也行
         */
        public int getTotal() {
            return (characterCount == null ? 0 : characterCount)
                    + (locationCount == null ? 0 : locationCount)
                    + (clueCount == null ? 0 : clueCount)
                    + (itemCount == null ? 0 : itemCount)
                    + (eventCount == null ? 0 : eventCount)
                    + (cheatCount == null ? 0 : cheatCount)
                    + (aliasCount == null ? 0 : aliasCount)
                    + (narrativeRuleCount == null ? 0 : narrativeRuleCount);
        }
    }

    /**
     * 写作会话概要 —— "上次写到哪了"
     *
     * 从 t_chapter_generation_session 取最新 5 条，给前端快速继续入口。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionBrief {

        @Schema(description = "会话ID", example = "42")
        private Long sessionId;

        @Schema(description = "章节ID", example = "15")
        private Long chapterId;

        @Schema(description = "章节序号", example = "15")
        private Integer chapterNo;

        @Schema(description = "章节标题", example = "第十五章：剑意初成")
        private String chapterTitle;

        @Schema(description = "会话状态", example = "SUCCESS")
        private String status;

        @Schema(description = "本次生成字数", example = "4200")
        private Integer wordCount;

        @Schema(description = "操作批次ID（用于排错追踪）", example = "batch_20260515_001")
        private String operationBatchId;

        @Schema(description = "会话创建时间")
        private LocalDateTime createdAt;
    }

    /**
     * 写作日志概要 —— "最近写得怎么样"
     *
     * 从 t_writing_log 取最新 5 条，前端展示写作动态。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WritingLogBrief {

        @Schema(description = "日志ID", example = "100")
        private Long logId;

        @Schema(description = "章节序号", example = "15")
        private Integer chapterNo;

        @Schema(description = "本章字数", example = "4200")
        private Integer wordCount;

        @Schema(description = "消耗 Token 数", example = "3500")
        private Integer tokenUsed;

        @Schema(description = "是否成功", example = "true")
        private Boolean success;

        @Schema(description = "失败原因（成功时为空）", example = "LLM 返回格式异常")
        private String failReason;

        @Schema(description = "创建时间")
        private LocalDateTime createdAt;
    }
}
