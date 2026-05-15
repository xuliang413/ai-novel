package net.lab1024.sa.admin.module.business.novel.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说项目列表返回对象。
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

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
