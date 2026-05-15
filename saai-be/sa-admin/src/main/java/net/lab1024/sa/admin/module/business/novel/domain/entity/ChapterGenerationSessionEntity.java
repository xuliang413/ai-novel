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
     */
    private Long chapterId;

    /**
     * 章节序号。
     */
    private Integer chapterNo;

    /**
     * 生成供应商，例如 MOCK、DEEPSEEK。
     */
    private String provider;

    /**
     * 生成状态。
     */
    private String status;

    /**
     * 提示词快照。
     */
    private String promptSnapshot;

    /**
     * ChapterIntent JSON。
     */
    private String intentJson;

    /**
     * 上下文快照。
     */
    private String contextSnapshot;

    /**
     * 正文审阅质检 JSON。
     */
    private String contentReviewJson;

    /**
     * 待确认 GraphPatch JSON。
     */
    private String graphPatchJson;

    /**
     * 待确认 inversePatch JSON。
     */
    private String inversePatchJson;

    /**
     * 图谱操作批次 ID。
     */
    private String operationBatchId;

    /**
     * 生成结果摘要。
     */
    private String resultExcerpt;

    /**
     * 失败原因。
     */
    private String errorMessage;

    /**
     * 创建人用户 ID。
     */
    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
