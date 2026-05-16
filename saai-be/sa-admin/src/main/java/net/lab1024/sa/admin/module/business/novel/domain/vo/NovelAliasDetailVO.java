package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 马甲详情 VO
 *
 * 马甲是角色的隐藏身份、小号、伪装。详情页展示马甲自身信息，以及由谁持有、谁已经识破了它。
 * 马甲是否暴露会影响剧情走向，所以"已识破角色列表"是重要的前端展示字段。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelAliasDetailVO {

    @Schema(description = "马甲ID")
    private Long aliasId;
    @Schema(description = "所属项目ID")
    private Long projectId;
    @Schema(description = "马甲名称")
    private String aliasName;
    @Schema(description = "马甲类型")
    private String aliasType;
    @Schema(description = "使用场景")
    private String aliasContext;
    @Schema(description = "马甲简介")
    private String summary;
    @Schema(description = "是否已暴露")
    private Boolean revealed;
    @Schema(description = "已识破该马甲的角色名称列表（前端可直接展示）")
    private String revealedTo;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "持有者列表 —— 哪些角色使用这个马甲")
    private List<HolderBrief> holders;
    @Schema(description = "已识破该马甲的角色详情列表")
    private List<KnowerBrief> knowers;
    @Schema(description = "出场章节")
    private List<AppearanceBrief> appearances;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HolderBrief {
        @Schema(description = "角色ID")
        private Long characterId;
        @Schema(description = "角色名称")
        private String characterName;
        @Schema(description = "创建马甲的章节序号")
        private Integer createdInChapter;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowerBrief {
        @Schema(description = "角色ID")
        private Long characterId;
        @Schema(description = "角色名称")
        private String characterName;
        @Schema(description = "识破章节序号")
        private Integer sinceChapter;
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
}
