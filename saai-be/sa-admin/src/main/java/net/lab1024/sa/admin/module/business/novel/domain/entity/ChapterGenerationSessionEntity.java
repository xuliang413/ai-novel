package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 章节生成会话实体。
 *
 * 每次调用写作接口都会记录一次会话，便于后续恢复、排错和审阅。
 */
@Data
@TableName("t_chapter_generation_session")
public class ChapterGenerationSessionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long sessionId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 生成后绑定的章节 ID。
     *
     * start 阶段保存草稿后会回填；如果生成中断，可能暂时为空。
     */
    private Long chapterId;

    /**
     * 章节序号。
     */
    private Integer chapterNo;

    /**
     * 生成供应商，例如 MOCK、DEEPSEEK、TONGYI。
     *
     * 排查“为什么这章不像模型写的”时先看这里。
     */
    private String provider;

    /**
     * 生成状态。
     *
     * 这就是写作状态机：CONTENT_REVIEW -> PATCH_REVIEW -> APPLYING_PATCH -> SUCCESS。
     */
    private String status;

    /**
     * 提示词快照。
     *
     * 目前只存轻量摘要，不存完整 Prompt，避免日志过大和敏感信息扩散。
     */
    private String promptSnapshot;

    /**
     * ChapterIntent JSON。
     *
     * 恢复会话时用它还原“这一章原本打算怎么写”。
     */
    private String intentJson;

    /**
     * 上下文快照。
     *
     * 这是前端上下文预览的保存版，刷新页面也能看到当时参考了什么。
     */
    private String contextSnapshot;

    /**
     * 正文审阅质检 JSON。
     *
     * 保存字数、POV 是否出现、章末提醒等轻量检查结果。
     */
    private String contentReviewJson;

    /**
     * 待确认 GraphPatch JSON。
     *
     * 正文审阅通过后生成，用户确认前不会写入 Neo4j。
     */
    private String graphPatchJson;

    /**
     * 待确认 inversePatch JSON。
     *
     * 和 graphPatch 同时生成，真正执行后会再过滤成“实际执行项”的反向补丁。
     */
    private String inversePatchJson;

    /**
     * 图谱操作批次 ID。
     */
    private String operationBatchId;

    /**
     * 生成结果摘要。
     *
     * 只截一小段正文，方便列表排查，不承担完整正文存储。
     */
    private String resultExcerpt;

    /**
     * 失败原因。
     *
     * AI 调用或图谱写入失败时记录，用于前端提示和后台排查。
     */
    private String errorMessage;

    /**
     * 创建人用户 ID。
     */
    private Long createUserId;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
