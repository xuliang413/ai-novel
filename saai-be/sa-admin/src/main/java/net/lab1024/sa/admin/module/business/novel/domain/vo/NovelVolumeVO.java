package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说卷展示 VO。
 * <p>
 * 返回卷序号、标题和概要，供卷管理页展示，也供后续写作上下文检索读取。
 *
 * @Author AI-Novel
 */
@Data
public class NovelVolumeVO {

    /**
     * 卷ID。
     */
    @Schema(description = "卷ID")
    private Long id;

    /**
     * 所属项目ID。
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 卷序号，从 1 开始。
     */
    @Schema(description = "卷序号")
    private Integer number;

    /**
     * 卷标题。
     */
    @Schema(description = "卷标题")
    private String title;

    /**
     * 卷概要，写作检索时会作为长程上下文注入 Prompt。
     */
    @Schema(description = "卷概要")
    private String summary;

    /**
     * 归档标记，true 表示卷已归档。
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
