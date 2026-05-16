package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 写作连续打卡统计 VO —— 替代裸 Map
 *
 * streakDays=连续写作天数，monthDays=当月已写作天数，monthWords=当月总字数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakInfoVO {

    @Schema(description = "连续打卡天数")
    private Integer streakDays;

    @Schema(description = "当月写作天数")
    private Integer monthDays;

    @Schema(description = "当月总字数")
    private Integer monthWords;
}
