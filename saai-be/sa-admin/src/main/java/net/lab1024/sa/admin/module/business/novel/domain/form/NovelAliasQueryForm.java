package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.novel.constant.NovelAliasTypeEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说马甲分页查询表单。
 * <p>
 * 查询条件只表达筛选意图，服务层统一追加项目归属、用户隔离和未归档条件。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelAliasQueryForm extends PageParam {

    /**
     * 所属项目ID，查询前会校验项目归属。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 马甲名称搜索词，支持模糊匹配。
     */
    @Schema(description = "马甲名称搜索词")
    @Length(max = 50, message = "马甲名称搜索词最多50个字符")
    private String name;

    /**
     * 马甲类型筛选。
     */
    @SchemaEnum(desc = "马甲类型", value = NovelAliasTypeEnum.class)
    @CheckEnum(value = NovelAliasTypeEnum.class, required = false, message = "马甲类型错误")
    private String type;

    /**
     * 是否已被识破筛选，true 表示只看已经暴露的马甲。
     */
    @Schema(description = "是否已被识破")
    private Boolean revealed;
}
