package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘章节进度 VO —— 替代裸 Map
 *
 * 每个字段的类型和含义在此处明确，前端契约稳定。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterProgressItemVO {

    @Schema(description = "章节ID")
    private Long chapterId;

    @Schema(description = "章节序号")
    private Integer chapterNo;

    @Schema(description = "章节标题")
    private String title;

    @Schema(description = "章节状态")
    private String status;

    @Schema(description = "正文字数")
    private Integer wordCount;
}
