package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 金手指详情 VO
 *
 * 金手指在网文里通常决定了主角的能力天花板。详情页展示金手指的基本属性（来源、限制、升级路径），
 * 以及它被谁持有、绑定了哪些物品。这些关联信息从图谱查询。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelCheatDetailVO {

    @Schema(description = "金手指ID")
    private Long cheatId;
    @Schema(description = "所属项目ID")
    private Long projectId;
    @Schema(description = "金手指名称")
    private String cheatName;
    @Schema(description = "金手指类型")
    private String cheatType;
    @Schema(description = "金手指简介")
    private String summary;
    @Schema(description = "来源")
    private String origin;
    @Schema(description = "使用限制")
    private String limitation;
    @Schema(description = "升级路径")
    private String evolution;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "持有者列表")
    private List<HolderBrief> holders;
    @Schema(description = "绑定的物品")
    private List<ItemBrief> boundItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HolderBrief {
        @Schema(description = "角色ID")
        private Long characterId;
        @Schema(description = "角色名称")
        private String characterName;
        @Schema(description = "获得章节序号")
        private Integer acquiredInChapter;
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
    }
}
