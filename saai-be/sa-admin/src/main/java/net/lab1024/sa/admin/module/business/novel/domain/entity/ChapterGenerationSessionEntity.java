package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节生成会话 实体类 —— 写作流程的"黑匣子"
 * 每次写作创建一个会话, 绑定ChapterIntent/上下文快照/GraphPatch/操作批次ID
 * 状态机: IDLE→GENERATING→CONTENT_REVIEW→PATCH_REVIEW→SUCCESS(详见过渡枚举NovelGenerationStatusEnum)
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_chapter_generation_session")
public class ChapterGenerationSessionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 关联章节ID, 生成完成后回写
     */
    private Long chapterId;

    /**
     * 章节号
     */
    private Integer chapterNumber;

    /**
     * 会话状态, 见 NovelGenerationStatusEnum
     */
    private String status;

    /**
     * 写作意图快照(JSON), 记录ChapterIntent组装结果
     */
    private String chapterIntentJson;

    /**
     * 上下文检索快照(JSON), 记录检索到的实体和Token分配
     */
    private String contextSnapshotJson;

    /**
     * 使用的LLM提供商: DEEPSEEK/TONGYI/MOCK
     */
    private String provider;

    /**
     * 提示词摘要, 记录发送给LLM的prompt概要
     */
    private String promptSummary;

    /**
     * 候选GraphPatch(JSON), AI抽取的图谱变更
     */
    private String graphPatchJson;

    /**
     * 逆向操作(JSON), 用于撤销功能
     */
    private String inversePatchJson;

    /**
     * 操作批次ID, 幂等保护: 执行前查此ID是否已写入
     */
    private String operationBatchId;

    /**
     * 结果摘要或失败原因
     */
    private String resultSummary;

    /**
     * 重试次数, 失败后默认重试3次再降级Mock
     */
    private Integer retryCount;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
