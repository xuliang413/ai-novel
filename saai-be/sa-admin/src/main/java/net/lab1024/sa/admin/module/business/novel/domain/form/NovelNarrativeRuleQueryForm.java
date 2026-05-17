package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.domain.PageParam;
import org.hibernate.validator.constraints.Length;

/**
 * 小说叙事规则分页查询表单。
 * <p>
 * 查询条件只表达筛选意图，服务层会强制追加项目、用户和未归档条件。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelNarrativeRuleQueryForm extends PageParam {

    /**
     * 所属项目ID，查询前会校验项目归属。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 规则名称搜索词，支持模糊匹配。
     */
    @Schema(description = "规则名称搜索词")
    @Length(max = 50, message = "规则名称搜索词最多50个字符")
    private String name;

    /**
     * 优先级筛选，范围 1~5。
     */
    @Schema(description = "优先级")
    @Min(value = 1, message = "优先级不能小于1")
    @Max(value = 5, message = "优先级不能大于5")
    private Integer priority;
}
