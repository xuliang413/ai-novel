package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 小说叙事规则创建表单。
 * <p>
 * 叙事规则是写作 System Prompt 的纯 MySQL 配置，不进入 Neo4j，用户可以随时维护全部字段。
 *
 * @Author AI-Novel
 */
@Data
public class NovelNarrativeRuleAddForm {

    /**
     * 所属项目ID，服务层会校验项目必须属于当前登录用户。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 规则名称，用于列表展示和人工审阅。
     */
    @Schema(description = "规则名称")
    @NotBlank(message = "规则名称不能为空")
    @Length(max = 200, message = "规则名称最多200个字符")
    private String name;

    /**
     * 自然语言规则内容，组装 System Prompt 时会按优先级注入。
     */
    @Schema(description = "自然语言规则内容")
    @NotBlank(message = "自然语言规则内容不能为空")
    private String content;

    /**
     * 优先级，范围 1~5；数字越大，在 System Prompt 中越靠前。
     */
    @Schema(description = "优先级")
    @NotNull(message = "优先级不能为空")
    @Min(value = 1, message = "优先级不能小于1")
    @Max(value = 5, message = "优先级不能大于5")
    private Integer priority;
}
