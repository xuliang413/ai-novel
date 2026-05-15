package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 章节细纲实体。
 *
 * 当前预留给后续“先生成细纲、再写正文”的流程。
 */
@Data
@TableName("t_chapter_outline")
public class ChapterOutlineEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long outlineId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 章节序号。
     */
    private Integer chapterNo;

    /**
     * 场景列表 JSON。
     *
     * 可以存每个场景的目标、地点、出场角色，后续再结构化拆表。
     */
    private String scenesJson;

    /**
     * 本章细纲概要。
     */
    private String summary;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
