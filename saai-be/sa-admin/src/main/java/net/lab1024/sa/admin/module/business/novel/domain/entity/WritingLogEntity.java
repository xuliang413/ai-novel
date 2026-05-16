package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 写作日志 实体类 —— 每章生成后自动记录字数/Token消耗/耗时
 * 纯写入, 不做修改, 用于仪表盘统计和成本分析
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_writing_log")
public class WritingLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 关联会话ID
     */
    private Long sessionId;

    /**
     * 章节ID
     */
    private Long chapterId;

    /**
     * 章节号
     */
    private Integer chapterNumber;

    /**
     * 生成字数
     */
    private Integer wordCount;

    /**
     * Prompt送入的Token数
     */
    private Integer promptTokens;

    /**
     * AI生成的Token数
     */
    private Integer completionTokens;

    /**
     * 生成耗时(毫秒)
     */
    private Long durationMs;

    /**
     * 使用的LLM提供商
     */
    private String provider;

    /**
     * 使用的模型名称
     */
    private String modelName;

    /**
     * 写作时间
     */
    private LocalDateTime writeTime;

    private Long createUserId;

    private LocalDateTime createTime;
}
