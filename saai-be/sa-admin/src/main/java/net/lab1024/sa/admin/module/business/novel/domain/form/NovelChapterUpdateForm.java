package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 小说章节编辑表单。
 * <p>
 * 管理页只编辑章节内容和可选卷归属；章节号、状态流转和图谱确认由写作流程维护。
 *
 * @Author AI-Novel
 */
@Data
public class NovelChapterUpdateForm {

    /**
     * 章节ID，服务层会结合当前登录用户再次校验归属。
     */
    @Schema(description = "章节ID")
    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    /**
     * 所属项目ID，不能通过编辑章节移动到其他项目。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 所属卷ID，可为空；不为空时必须属于同一项目和同一用户。
     */
    @Schema(description = "所属卷ID")
    private Long volumeId;

    /**
     * 章节标题，会同步到 Neo4j Chapter 节点用于检索和展示。
     */
    @Schema(description = "章节标题")
    private String title;

    /**
     * 章节摘要，建议 300 字以内，会同步到 Neo4j Chapter 节点。
     */
    @Schema(description = "章节摘要")
    @Size(max = 300, message = "章节摘要不能超过300字")
    private String summary;

    /**
     * 章节正文，只保存到 MySQL，不写入 Neo4j。
     */
    @Schema(description = "章节正文")
    private String content;

    /**
     * POV 视角人物名，会同步到 Neo4j Chapter 节点辅助后续上下文检索。
     */
    @Schema(description = "POV视角人物名")
    private String pov;
}
