package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 章节细纲创建表单。
 * <p>
 * 细纲可以提前规划任意章节号，内容可以是 JSON 场景节拍，也可以是纯文本自然语言。
 *
 * @Author AI-Novel
 */
@Data
public class ChapterOutlineAddForm {

    /**
     * 所属项目ID，服务层会校验项目必须属于当前登录用户。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 对应章节号，可超过当前写作进度。
     */
    @Schema(description = "对应章节号")
    @NotNull(message = "对应章节号不能为空")
    @Min(value = 1, message = "对应章节号必须大于0")
    private Integer chapterNumber;

    /**
     * 场景节拍，支持 JSON 字符串或纯文本，写作时会注入 ChapterIntent。
     */
    @Schema(description = "场景节拍")
    private String sceneBeats;

    /**
     * 细纲摘要，用于列表快速扫读和写作上下文提示。
     */
    @Schema(description = "细纲摘要")
    private String summary;
}
