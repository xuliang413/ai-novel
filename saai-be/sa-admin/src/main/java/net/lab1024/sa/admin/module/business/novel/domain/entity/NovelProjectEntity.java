package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说项目实体。
 *
 * 一本小说的根记录。项目节点是 MySQL 和 Neo4j 之间最重要的隔离边界。
 */
@Data
@TableName("t_novel_project")
public class NovelProjectEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
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
     * 逻辑删除标识。
     */
    private Boolean deletedFlag;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
