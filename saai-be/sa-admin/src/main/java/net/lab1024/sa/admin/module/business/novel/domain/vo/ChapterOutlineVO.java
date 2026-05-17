package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节细纲展示 VO。
 * <p>
 * 细纲只用于写作前规划和 ChapterIntent 注入，不进入 Neo4j。
 *
 * @Author AI-Novel
 */
@Data
public class ChapterOutlineVO {

    /**
     * 章节细纲ID。
     */
    @Schema(description = "章节细纲ID")
    private Long id;

    /**
     * 所属项目ID。
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 对应章节号，可超过当前写作进度。
     */
    @Schema(description = "对应章节号")
    private Integer chapterNumber;

    /**
     * 场景节拍，可能是 JSON 字符串，也可能是纯文本自然语言。
     */
    @Schema(description = "场景节拍")
    private String sceneBeats;

    /**
     * 细纲摘要。
     */
    @Schema(description = "细纲摘要")
    private String summary;

    /**
     * 归档标记，true 表示细纲已归档。
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
