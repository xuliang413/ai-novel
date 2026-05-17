package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小说角色关系编辑表单。
 * <p>
 * 继承创建表单中的关系字段，额外携带关系ID；更新时如果起止角色或关系大类变化，会重建 Neo4j 关系边。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelCharacterRelationUpdateForm extends NovelCharacterRelationAddForm {

    /**
     * 角色关系ID，服务层会结合当前登录用户再次校验归属。
     */
    @Schema(description = "角色关系ID")
    @NotNull(message = "角色关系ID不能为空")
    private Long relationId;
}
