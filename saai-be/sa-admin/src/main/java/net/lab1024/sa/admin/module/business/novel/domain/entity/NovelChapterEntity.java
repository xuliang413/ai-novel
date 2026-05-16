package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说章节 实体类 —— 写作的基本单元
 * 正文存MySQL, 摘要/标题/POV同步Neo4j用于检索
 * 状态机: DRAFT→PENDING_GRAPH_CONFIRM→PUBLISHED(或PENDING_GRAPH_UPDATE待重试)
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_chapter")
public class NovelChapterEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 所属卷ID, 可选, 未关联卷时为null
     */
    private Long volumeId;

    /**
     * 章节序号, 全局递增, 决定章节间的PREVIOUS链
     */
    private Integer chapterNumber;

    /**
     * 章节标题
     */
    private String title;

    /**
     * 章节摘要, 300字以内, 存入Neo4j Chapter节点用于检索
     */
    private String summary;

    /**
     * 章节正文, 全文存MySQL, 不存Neo4j
     */
    private String content;

    /**
     * POV视角人物名, 生成时约束AI从谁的视角叙事
     */
    private String pov;

    /**
     * 正文字数
     */
    private Integer wordCount;

    /**
     * 章节状态: DRAFT草稿/PENDING_GRAPH_CONFIRM等图谱确认/PUBLISHED已发布/PENDING_GRAPH_UPDATE图谱写入失败待重试/INTERRUPTED_DRAFT中断草稿
     */
    private String status;

    /**
     * 章节摘要向量化(JSON数组), Embedding模型输出, 未配向量Key时为空
     */
    private String embedding;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
