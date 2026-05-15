package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelNarrativeRuleTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 新增小说叙事规则表单。
 *
 * 叙事规则不是剧情事实，而是写作时必须遵守的边界，例如平台红线、文风、视角规则。
 */
@Data
public class NovelNarrativeRuleAddForm {

    /**
     * 所属小说项目 ID。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 规则名称，方便后台列表识别。
     */
    @NotBlank(message = "规则名称不能为空")
    @Length(max = 100, message = "规则名称最多 100 个字符")
    private String ruleName;

    @SchemaEnum(value = NovelNarrativeRuleTypeEnum.class, required = true)
    @CheckEnum(value = NovelNarrativeRuleTypeEnum.class, message = "叙事规则类型错误")
    @NotBlank(message = "规则类型不能为空")
    @Length(max = 50, message = "规则类型最多 50 个字符")
    private String ruleType;

    /**
     * 规则内容。
     *
     * 这段文字会进入 Prompt，所以要写得像给作者看的要求，而不是只写代码枚举。
     */
    @NotBlank(message = "规则内容不能为空")
    @Length(max = 2000, message = "规则内容最多 2000 个字符")
    private String ruleValue;

    /**
     * 规则优先级。
     *
     * 数字越大越靠前进入 Prompt；为空时服务层默认给 3。
     */
    private Integer priority;
}
