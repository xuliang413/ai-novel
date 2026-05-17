package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelChapterStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 小说章节展示 VO。
 * <p>
 * 正文只来自 MySQL；Neo4j Chapter 节点只同步标题、摘要、POV、字数、状态等检索字段。
 *
 * @Author AI-Novel
 */
@Data
public class NovelChapterVO {

    /**
     * 章节ID。
     */
    @Schema(description = "章节ID")
    private Long id;

    /**
     * 所属项目ID。
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 所属卷ID，为空表示暂未归入某一卷。
     */
    @Schema(description = "所属卷ID")
    private Long volumeId;

    /**
     * 章节序号，项目内全局递增。
     */
    @Schema(description = "章节序号")
    private Integer chapterNumber;

    /**
     * 章节标题。
     */
    @Schema(description = "章节标题")
    private String title;

    /**
     * 章节摘要，供章节列表和图谱检索使用。
     */
    @Schema(description = "章节摘要")
    private String summary;

    /**
     * 章节正文，全文只保存在 MySQL。
     */
    @Schema(description = "章节正文")
    private String content;

    /**
     * POV 视角人物名。
     */
    @Schema(description = "POV视角人物名")
    private String pov;

    /**
     * 正文字数，由服务层根据 content 计算。
     */
    @Schema(description = "正文字数")
    private Integer wordCount;

    /**
     * 章节状态，用于写作流程和图谱确认流程。
     */
    @SchemaEnum(desc = "章节状态", value = NovelChapterStatusEnum.class)
    private String status;

    /**
     * 章节摘要向量，未配置向量模型或未向量化时为空。
     */
    @Schema(description = "章节摘要向量")
    private String embedding;

    /**
     * 归档标记，true 表示章节已归档。
     */
    @Schema(description = "归档标记")
    private Boolean deletedFlag;

    /**
     * 创建用户ID。
     */
    @Schema(description = "创建用户ID")
    private Long createUserId;

    /**
     * 最后更新时间。
     */
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
