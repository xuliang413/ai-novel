package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelNarrativeRuleTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 编辑叙事规则表单。
 *
 * 规则修改后，已生成的 Prompt 不会自动重新拼装，只影响后续写作。
 */
@Data
public class NovelNarrativeRuleUpdateForm {

    @NotNull(message = "规则ID不能为空")
    private Long ruleId;

    @Length(max = 100, message = "规则名称最多 100 个字符")
    private String ruleName;

    @SchemaEnum(value = NovelNarrativeRuleTypeEnum.class, required = false)
    @CheckEnum(value = NovelNarrativeRuleTypeEnum.class, message = "规则类型错误")
    @Length(max = 50, message = "规则类型最多 50 个字符")
    private String ruleType;

    @Length(max = 2000, message = "规则内容最多 2000 个字符")
    private String ruleValue;

    /**
     * 优先级 —— 数字越大越靠前进入 Prompt
     */
    private Integer priority;
}
