package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说章节实体。
 */
@Data
@TableName("t_novel_chapter")
public class NovelChapterEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
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
