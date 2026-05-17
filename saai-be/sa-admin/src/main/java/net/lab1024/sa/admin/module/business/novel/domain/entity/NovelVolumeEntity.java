package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说卷实体。
 * <p>
 * 卷把长篇小说切成较大的叙事段落，卷概要会在写作检索阶段注入 Prompt，帮助 AI 把握当前大段方向。
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_volume")
public class NovelVolumeEntity {

    /**
     * 主键ID，由 MySQL 自增生成。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目ID，用于隔离不同小说项目的数据。
     */
    private Long projectId;

    /**
     * 卷序号，从 1 开始，决定卷列表和检索排序。
     */
    private Integer number;

    /**
     * 卷标题，例如“第一卷：少年游”。
     */
    private String title;

    /**
     * 卷概要，自然语言描述当前卷的大方向，写作时注入 Prompt。
     */
    private String summary;

    /**
     * 归档标记，true 表示已归档，普通查询默认排除。
     */
    private Boolean deletedFlag;

    /**
     * 创建用户ID，用于 SmartAdmin 登录用户维度的数据隔离。
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
