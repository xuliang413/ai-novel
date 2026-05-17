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

    /**
     * 事件ID, 由数据库自增生成, 也是 Neo4j Event 节点的业务主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目ID, 所有事件查询都必须带上它做项目隔离
     */
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

    /**
     * 归档标记, false 表示正常可见, true 表示已归档
     */
    private Boolean deletedFlag;

    /**
     * 创建用户ID, 所有用户级查询都必须带上它做数据隔离
     */
    private Long createUserId;

    /**
     * 最后更新时间, 由数据库自动维护, 用于人工审阅事件最近变化
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间, 由数据库自动维护, 用于事件列表默认排序
     */
    private LocalDateTime createTime;
}
