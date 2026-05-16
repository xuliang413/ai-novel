package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节详情 VO —— 替代直接返回 NovelChapterEntity
 *
 * 为什么不能直接返回 Entity：
 * Entity 是持久层结构，字段变化（比如加了 deletedFlag）会直接暴露给前端。
 * 前端不应该关心数据是怎么存的，只需要知道自己要展示什么。
 * 用 VO 隔离开：后端表结构随便改，VO 字段不变，前端契约就稳定。
 *
 * 如果以后需要加"所在卷名""上一章 ID"等展示字段，
 * 只需在 VO 里加，不影响数据库。
 */
@Data
public class NovelChapterDetailVO {

    @Schema(description = "章节ID")
    private Long chapterId;

    @Schema(description = "所属项目ID")
    private Long projectId;

    @Schema(description = "所属卷ID")
    private Long volumeId;

    @Schema(description = "章节序号")
    private Integer chapterNo;

    @Schema(description = "章节标题")
    private String title;

    @Schema(description = "章节摘要")
    private String summary;

    @Schema(description = "章节正文")
    private String content;

    @Schema(description = "章节状态（DRAFT/PUBLISHED等）")
    private String status;

    @Schema(description = "最近一次生成会话ID")
    private Long generationSessionId;

    @Schema(description = "正文字数")
    private Integer wordCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
