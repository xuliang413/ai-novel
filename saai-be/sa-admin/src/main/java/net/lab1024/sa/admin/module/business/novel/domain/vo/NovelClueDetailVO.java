package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 线索详情 VO
 *
 * 线索是小说最重要的结构性元素之一。详情页除了表字段还要展示：这条线索被哪些章节推进过、
 * 关联了哪些角色、和哪些其他线索有交叉或触发关系。前端线索面板可根据这些数据画出线索推进图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelClueDetailVO {

    @Schema(description = "线索ID")
    private Long clueId;
    @Schema(description = "所属项目ID")
    private Long projectId;
    @Schema(description = "线索名称")
    private String clueName;
    @Schema(description = "线索类型")
    private String clueType;
    @Schema(description = "线索状态")
    private String clueStatus;
    @Schema(description = "线索简介")
    private String summary;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "推进章节列表 —— 哪些章节推进了这条线索")
    private List<AdvanceBrief> advanceChapters;
    @Schema(description = "关联角色 —— 哪些角色知道或参与了这条线索")
    private List<CharacterBrief> relatedCharacters;
    @Schema(description = "触发事件 —— 哪些事件触发了这条线索")
    private List<EventBrief> triggerEvents;
    @Schema(description = "交叉线索 —— 和哪些其他线索有交集")
    private List<IntersectClueBrief> intersectingClues;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdvanceBrief {
        @Schema(description = "章节ID")
        private Long chapterId;
        @Schema(description = "章节序号")
        private Integer chapterNo;
        @Schema(description = "章节标题")
        private String chapterTitle;
        @Schema(description = "本章推进了多少", example = "叶尘发现了神秘玉佩的来历")
        private String advanceDescription;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharacterBrief {
        @Schema(description = "角色ID")
        private Long characterId;
        @Schema(description = "角色名称")
        private String characterName;
        @Schema(description = "关联方式", example = "KNOWS_ABOUT")
        private String relationType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventBrief {
        @Schema(description = "事件ID")
        private Long eventId;
        @Schema(description = "事件名称")
        private String eventName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IntersectClueBrief {
        @Schema(description = "交叉线索ID")
        private Long clueId;
        @Schema(description = "交叉线索名称")
        private String clueName;
        @Schema(description = "交叉说明")
        private String intersectDescription;
    }
}
