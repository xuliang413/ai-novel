package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节细纲实体。
 * <p>
 * 细纲用于提前规划某一章的场景节拍，只存 MySQL，不进入 Neo4j。
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_chapter_outline")
public class ChapterOutlineEntity {

    /**
     * 主键ID，数据库自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目ID，用于用户隔离和写作时定位细纲。
     */
    private Long projectId;

    /**
     * 对应章节号，可超过当前写作进度，支持提前规划后续章节。
     */
    private Integer chapterNumber;

    /**
     * 场景节拍，可以是 JSON 场景列表，也可以是纯文本自然语言。
     */
    private String sceneBeats;

    /**
     * 细纲摘要，用于列表扫读和 ChapterIntent 注入。
     */
    private String summary;

    /**
     * 归档标记，true 表示细纲已归档。
     */
    private Boolean deletedFlag;

    /**
     * 创建用户ID，用于 SmartAdmin 登录用户隔离。
     */
    private Long createUserId;

    /**
     * 最后更新时间，由数据库自动维护。
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间，由数据库自动维护。
     */
    private LocalDateTime createTime;
}
