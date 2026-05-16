package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelAliasTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 编辑马甲表单。
 *
 * revealed 字段确认后不可轻易回退，因为暴露马甲是不可逆剧情节点。
 */
@Data
public class NovelAliasUpdateForm {

    @NotNull(message = "马甲ID不能为空")
    private Long aliasId;

    @Length(max = 100, message = "马甲名称最多 100 个字符")
    private String aliasName;

    @SchemaEnum(value = NovelAliasTypeEnum.class, required = false)
    @CheckEnum(value = NovelAliasTypeEnum.class, message = "马甲类型错误")
    @Length(max = 50, message = "马甲类型最多 50 个字符")
    private String aliasType;

    @Length(max = 500, message = "使用场景最多 500 个字符")
    private String aliasContext;

    @Length(max = 2000, message = "马甲简介最多 2000 个字符")
    private String summary;

    /**
     * 是否已暴露 —— 高风险字段，操作前确认
     */
    private Boolean revealed;

    @Length(max = 500, message = "已识破角色列表最多 500 个字符")
    private String revealedTo;
}
