package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说事件展示 VO。
 * <p>
 * 事件用于标记故事关键节点，当前阶段只返回基础描述和发生章节。
 *
 * @Author AI-Novel
 */
@Data
public class NovelEventVO {

    /**
     * 事件ID。
     */
    @Schema(description = "事件ID")
    private Long id;

    /**
     * 所属项目ID。
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 事件名称。
     */
    @Schema(description = "事件名称")
    private String name;

    /**
     * 事件描述。
     */
    @Schema(description = "事件描述")
    private String summary;

    /**
     * 发生章节号。
     */
    @Schema(description = "发生章节号")
    private Integer chapterOccurred;

    /**
     * 归档标记，true 表示事件已归档。
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
