package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 线索推进历史 VO —— 一条线索被哪些章节推进的记录时间线
 *
 * @Author AI-Novel
 */
@Data
public class NovelClueHistoryVO {

    @Schema(description = "线索ID")
    private Long clueId;

    @Schema(description = "线索名称")
    private String clueName;

    @Schema(description = "线索当前状态: DORMANT/ACTIVE/RESOLVED")
    private String clueStatus;

    @Schema(description = "推进记录列表, 按章节号升序")
    private List<AdvanceRecord> history;

    /**
     * 单次推进记录 —— 某章对这条线索做了什么推进
     */
    @Data
    public static class AdvanceRecord {

        @Schema(description = "推进章节号")
        private Integer chapterNumber;

        @Schema(description = "章节标题")
        private String chapterTitle;

        @Schema(description = "推进描述, 自然语言")
        private String progressDescription;

        @Schema(description = "推进后的揭露程度 0~1")
        private BigDecimal revealLevel;

        @Schema(description = "推进记录创建时间")
        private LocalDateTime createTime;
    }
}
