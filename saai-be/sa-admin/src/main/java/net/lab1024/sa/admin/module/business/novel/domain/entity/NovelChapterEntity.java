package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说章节实体。
 * <p>
 * 章节是写作的基本单元。正文全文只存 MySQL；标题、摘要、POV、字数等检索字段同步到 Neo4j Chapter 节点。
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_chapter")
public class NovelChapterEntity {

    /**
     * 主键ID，数据库自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目ID，用于 MySQL 与 Neo4j 的项目隔离。
     */
    private Long projectId;

    /**
     * 所属卷ID，可选，未归入某卷时为空。
     */
    private Long volumeId;

    /**
     * 章节序号，项目内全局递增，决定章节间的 PREVIOUS 链。
     */
    private Integer chapterNumber;

    /**
     * 章节标题，会同步到 Neo4j Chapter 节点。
     */
    private String title;

    /**
     * 章节摘要，建议 300 字以内，会同步到 Neo4j Chapter 节点用于上下文检索。
     */
    private String summary;

    /**
     * 章节正文，全文只存 MySQL，不写入 Neo4j。
     */
    private String content;

    /**
     * POV 视角人物名，生成时约束 AI 从谁的视角叙事。
     */
    private String pov;

    /**
     * 正文字数，由服务层根据 content 计算，写作日志也会使用。
     */
    private Integer wordCount;

    /**
     * 章节状态: DRAFT 草稿、PENDING_GRAPH_CONFIRM 等图谱确认、PUBLISHED 已发布、
     * PENDING_GRAPH_UPDATE 图谱写入失败待重试、INTERRUPTED_DRAFT 中断草稿。
     */
    private String status;

    /**
     * 章节摘要向量化结果，通常是 JSON 数组；未配置向量模型 Key 或未向量化时为空。
     */
    private String embedding;

    /**
     * 归档标记，true 表示管理页不再展示。
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
