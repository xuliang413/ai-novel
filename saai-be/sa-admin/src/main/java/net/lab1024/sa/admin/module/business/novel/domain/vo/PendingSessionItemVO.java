package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 仪表盘待处理会话 VO —— 替代裸 Map
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingSessionItemVO {

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "章节序号")
    private Integer chapterNo;

    @Schema(description = "会话状态")
    private String status;

    @Schema(description = "生成来源")
    private String provider;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
