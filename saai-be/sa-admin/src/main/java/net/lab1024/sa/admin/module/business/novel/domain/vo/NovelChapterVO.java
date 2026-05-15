package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说章节返回对象。
 */
@Data
public class NovelChapterVO {

    /**
     * 章节 ID。
     */
    private Long chapterId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 章节序号。
     */
    private Integer chapterNo;

    /**
     * 章节标题。
     */
    private String title;

    /**
     * 章节摘要。
     */
    private String summary;

    /**
     * 章节正文。
     */
    private String content;

    /**
     * 章节状态。
     */
    private String status;

    /**
     * 最近一次生成会话 ID。
     */
    private Long generationSessionId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
