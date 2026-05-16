package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 打卡结果 VO
 *
 * 打卡后返回：是否成功、打卡日期和连续写作统计。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinResultVO {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "打卡日期", example = "2026-05-16")
    private String date;

    @Schema(description = "连续写作统计")
    private StreakInfoVO streakInfo;
}
