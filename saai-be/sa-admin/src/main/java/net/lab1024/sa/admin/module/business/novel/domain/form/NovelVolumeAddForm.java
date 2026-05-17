package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 小说卷创建表单。
 * <p>
 * 卷用于给长篇小说切分大段落，卷概要会在写作检索阶段注入 Prompt。
 *
 * @Author AI-Novel
 */
@Data
public class NovelVolumeAddForm {

    /**
     * 所属项目ID，服务层会校验项目必须属于当前登录用户。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 卷序号，从 1 开始，用于确定卷列表和写作检索顺序。
     */
    @Schema(description = "卷序号")
    @NotNull(message = "卷序号不能为空")
    @Min(value = 1, message = "卷序号必须大于0")
    private Integer number;

    /**
     * 卷标题，例如“第一卷：少年游”。
     */
    @Schema(description = "卷标题")
    @NotBlank(message = "卷标题不能为空")
    @Length(max = 255, message = "卷标题最多255个字符")
    private String title;

    /**
     * 卷概要，描述这一卷的大方向，后续写章节时会作为长程上下文注入。
     */
    @Schema(description = "卷概要")
    private String summary;
}
