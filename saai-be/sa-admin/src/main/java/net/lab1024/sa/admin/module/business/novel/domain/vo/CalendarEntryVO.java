package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 写作日历条目 VO —— 替代裸 Map
 *
 * 前端日历热力图的单天数据。
 * date=日期（YYYY-MM-DD），wordCount=当天总字数，chapterCount=当天完成章节数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEntryVO {

    @Schema(description = "日期", example = "2026-05-16")
    private String date;

    @Schema(description = "当天字数", example = "4200")
    private Integer wordCount;

    @Schema(description = "当天完成章节数", example = "1")
    private Integer chapterCount;
}
