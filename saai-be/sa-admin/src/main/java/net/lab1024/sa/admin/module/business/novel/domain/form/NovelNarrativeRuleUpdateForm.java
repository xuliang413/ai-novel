package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小说叙事规则编辑表单。
 * <p>
 * 继承创建表单中的全部可编辑字段，额外携带规则ID。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelNarrativeRuleUpdateForm extends NovelNarrativeRuleAddForm {

    /**
     * 叙事规则ID，服务层会结合当前登录用户再次校验归属。
     */
    @Schema(description = "叙事规则ID")
    @NotNull(message = "叙事规则ID不能为空")
    private Long ruleId;
}
