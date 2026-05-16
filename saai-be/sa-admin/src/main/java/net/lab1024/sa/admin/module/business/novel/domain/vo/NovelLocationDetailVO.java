package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 地点详情 VO
 *
 * 除了 MySQL 表字段外，还从图谱查：哪些章节在这个地点发生、当前有哪些角色在这里、
 * 关联了哪些事件。这样前端地点页面能画出"地点-人物-事件"的局部关系图。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelLocationDetailVO {

    @Schema(description = "地点ID")
    private Long locationId;
    @Schema(description = "所属项目ID")
    private Long projectId;
    @Schema(description = "地点名称")
    private String locationName;
    @Schema(description = "地点类型")
    private String locationType;
    @Schema(description = "地点简介")
    private String summary;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "出现章节列表")
    private List<AppearanceBrief> appearances;
    @Schema(description = "当前在场的角色")
    private List<PresentCharacterBrief> presentCharacters;
    @Schema(description = "关联事件")
    private List<EventBrief> relatedEvents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppearanceBrief {
        @Schema(description = "章节ID")
        private Long chapterId;
        @Schema(description = "章节序号")
        private Integer chapterNo;
        @Schema(description = "章节标题")
        private String chapterTitle;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PresentCharacterBrief {
        @Schema(description = "角色ID")
        private Long characterId;
        @Schema(description = "角色名称")
        private String characterName;
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
}
