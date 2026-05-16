package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色详情 VO
 *
 * 不只返回表字段，还从 Neo4j 查询角色的周边关系信息，让前端角色面板一次拿到所有关联数据。
 * 周边信息包括：该角色在哪些章节出场、和其他角色是什么关系、当前在哪个地点、持有哪些物品、
 * 知道哪些线索。这些数据来自图谱查询，不是 MySQL 表字段。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelCharacterDetailVO {

    @Schema(description = "角色ID")
    private Long characterId;
    @Schema(description = "所属项目ID")
    private Long projectId;
    @Schema(description = "角色名称")
    private String characterName;
    @Schema(description = "角色定位")
    private String roleType;
    @Schema(description = "角色简介")
    private String summary;
    @Schema(description = "当前状态")
    private String currentStatus;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "出场章节列表")
    private List<AppearanceBrief> appearances;
    @Schema(description = "与其他角色的关系")
    private List<RelationBrief> relations;
    @Schema(description = "当前位置")
    private LocationBrief currentLocation;
    @Schema(description = "持有物品")
    private List<ItemBrief> heldItems;
    @Schema(description = "已知线索")
    private List<ClueBrief> knownClues;

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
    public static class RelationBrief {
        @Schema(description = "目标角色ID")
        private Long targetCharacterId;
        @Schema(description = "目标角色名称")
        private String targetCharacterName;
        @Schema(description = "关系类型", example = "KNOWS")
        private String relationType;
        @Schema(description = "关系说明", example = "师徒")
        private String description;
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
    public static class ItemBrief {
        @Schema(description = "物品ID")
        private Long itemId;
        @Schema(description = "物品名称")
        private String itemName;
        @Schema(description = "物品状态")
        private String itemStatus;
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
        @Schema(description = "线索状态")
        private String clueStatus;
    }
}
