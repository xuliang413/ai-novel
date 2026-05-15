package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说事件实体。
 *
 * 事件是“已经发生过的剧情事实”，后续可连接参与角色、触发线索、发生章节。
 */
@Data
@TableName("t_novel_event")
public class NovelEventEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long eventId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 事件名称。
     */
    private String eventName;

    /**
     * 事件摘要。
     */
    private String summary;

    /**
     * 事件发生章节。
     */
    private Integer chapterOccurred;

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
