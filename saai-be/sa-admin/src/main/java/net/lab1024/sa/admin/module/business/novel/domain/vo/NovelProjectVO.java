package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说项目列表返回对象。
 *
 * 这是管理页看到的项目摘要，不包含大段章节内容。
 */
@Data
public class NovelProjectVO {

    /**
     * 项目 ID。
     */
    private Long projectId;

    /**
     * 项目名称。
     */
    private String projectName;

    /**
     * 小说类型。
     */
    private String genre;

    /**
     * 项目简介。
     */
    private String summary;

    /**
     * 主角名称。
     */
    private String protagonist;

    /**
     * 目标字数。
     */
    private Integer targetWords;

    /**
     * 项目状态。
     */
    private String status;

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
