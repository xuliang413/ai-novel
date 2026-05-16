package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说事件 实体类
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_event")
public class NovelEventEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 事件名称, 如"宗门大比""张三突破元婴"
     */
    private String name;

    /**
     * 事件描述
     */
    private String summary;

    /**
     * 发生章节号, 用户可手动补(回忆之前发生但没录的事件), 也可通过写作流程自动关联
     */
    private Integer chapterOccurred;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
