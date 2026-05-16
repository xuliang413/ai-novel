package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节细纲 实体类
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_chapter_outline")
public class ChapterOutlineEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 对应章节号, 可超过当前写作进度(提前规划)
     */
    private Integer chapterNumber;

    /**
     * 场景节拍, JSON存场景列表或纯文本自然语言
     */
    private String sceneBeats;

    /**
     * 细纲摘要
     */
    private String summary;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
