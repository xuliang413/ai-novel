package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件详情 VO
 *
 * 事件是已经发生的剧情事实。详情页展示事件本身的信息，以及它在图谱里的关联：
 * 发生在哪个章节、有哪些角色参与、发生地点、影响了哪些线索。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelEventDetailVO {

    @Schema(description = "事件ID")
    private Long eventId;
    @Schema(description = "所属项目ID")
    private Long projectId;
    @Schema(description = "事件名称")
    private String eventName;
    @Schema(description = "事件摘要")
    private String summary;
    @Schema(description = "发生章节序号")
    private Integer chapterOccurred;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "参与角色")
    private List<CharacterBrief> participants;
    @Schema(description = "发生地点")
    private LocationBrief location;
    @Schema(description = "影响的线索")
    private List<ClueBrief> affectedClues;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharacterBrief {
        @Schema(description = "角色ID")
        private Long characterId;
        @Schema(description = "角色名称")
        private String characterName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationBrief {
        @Schema(description = "地点ID")
        private Long locationId;
        @Schema(description = "地点名称")
        private String locationName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClueBrief {
        @Schema(description = "线索ID")
        private Long clueId;
        @Schema(description = "线索名称")
        private String clueName;
    }
}
