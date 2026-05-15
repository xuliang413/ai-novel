package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说章节返回对象。
 *
 * 前端展示章节列表、正文审阅和发布结果时都用它。
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

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
