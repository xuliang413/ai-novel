package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说写作日志实体。
 *
 * 这是统计和排查用的流水账，不参与写作状态机判断。
 */
@Data
@TableName("t_writing_log")
public class WritingLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long writingLogId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 关联章节 ID。
     */
    private Long chapterId;

    /**
     * 章节序号。
     */
    private Integer chapterNo;

    /**
     * 正文字数。
     */
    private Integer wordCount;

    /**
     * 粗略 token 使用量。
     */
    private Integer tokenUsed;

    /**
     * 写作是否成功。
     */
    private Boolean success;

    /**
     * 本次使用的生成供应商。
     */
    private String provider;

    /**
     * 创建人用户 ID。
     */
    private Long createUserId;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
