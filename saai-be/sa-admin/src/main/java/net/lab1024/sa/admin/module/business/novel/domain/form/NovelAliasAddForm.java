package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelAliasTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 新增小说马甲表单。
 *
 * 马甲是“角色对外展示的另一层身份”。它可能没暴露，也可能只被部分角色识破。
 */
@Data
public class NovelAliasAddForm {

    /**
     * 所属小说项目 ID。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 马甲名称，例如“青衣客”“论坛账号X”。
     */
    @NotBlank(message = "马甲名称不能为空")
    @Length(max = 100, message = "马甲名称最多 100 个字符")
    private String aliasName;

    @SchemaEnum(value = NovelAliasTypeEnum.class, required = false)
    @CheckEnum(value = NovelAliasTypeEnum.class, message = "马甲类型错误")
    @Length(max = 50, message = "马甲类型最多 50 个字符")
    private String aliasType;

    /**
     * 使用场景。
     *
     * 说明这个马甲通常在哪里、什么时候使用，写作检索时可以帮模型避免乱用。
     */
    @NotBlank(message = "使用场景不能为空")
    @Length(max = 200, message = "使用场景最多 200 个字符")
    private String aliasContext;

    @Size(max = 2000, message = "马甲简介最多 2000 个字符")
    private String summary;

    /**
     * 是否已经暴露。
     */
    private Boolean revealed;

    /**
     * 已识破该马甲的角色列表。
     *
     * 当前用文本存储，后续可拆成 KNOWS_ALIAS 关系。
     */
    @Length(max = 500, message = "识破角色列表最多 500 个字符")
    private String revealedTo;
}
