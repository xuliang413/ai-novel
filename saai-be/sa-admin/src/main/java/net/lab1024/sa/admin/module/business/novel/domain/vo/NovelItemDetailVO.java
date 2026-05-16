package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物品详情 VO
 *
 * 物品在小说里可以持有、转移、损坏、修复、绑定金手指。详情页除了表字段还要展示：
 * 当前持有者是谁（可能为空，表示无主）、之前在哪些章节出场、是否绑定了金手指。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelItemDetailVO {

    @Schema(description = "物品ID")
    private Long itemId;
    @Schema(description = "所属项目ID")
    private Long projectId;
    @Schema(description = "物品名称")
    private String itemName;
    @Schema(description = "物品类型")
    private String itemType;
    @Schema(description = "物品状态")
    private String itemStatus;
    @Schema(description = "物品简介")
    private String summary;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "当前持有者（可能为空）")
    private HolderBrief currentHolder;
    @Schema(description = "出场章节列表")
    private List<AppearanceBrief> appearances;
    @Schema(description = "绑定的金手指（如果有）")
    private List<CheatBrief> boundCheats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HolderBrief {
        @Schema(description = "角色ID")
        private Long characterId;
        @Schema(description = "角色名称")
        private String characterName;
    }

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
    public static class CheatBrief {
        @Schema(description = "金手指ID")
        private Long cheatId;
        @Schema(description = "金手指名称")
        private String cheatName;
    }
}
