package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 小说事件创建表单。
 * <p>
 * 事件用于标记故事关键节点，发生章节可以由用户手动补录，也可以后续由写作流程自动关联。
 *
 * @Author AI-Novel
 */
@Data
public class NovelEventAddForm {

    /**
     * 所属项目ID，服务层会校验项目必须属于当前登录用户。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 事件名称，用于列表、图谱节点和写作检索。
     */
    @Schema(description = "事件名称")
    @NotBlank(message = "事件名称不能为空")
    @Length(max = 200, message = "事件名称最多200个字符")
    private String name;

    /**
     * 事件描述，记录事件经过、影响和剧情意义。
     */
    @Schema(description = "事件描述")
    private String summary;

    /**
     * 发生章节号，可为空；补录历史事件时用户可以手动填写。
     */
    @Schema(description = "发生章节号")
    @Min(value = 1, message = "发生章节号必须大于0")
    private Integer chapterOccurred;
}
