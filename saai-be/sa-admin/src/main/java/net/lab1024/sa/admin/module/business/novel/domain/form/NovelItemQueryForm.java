package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.novel.constant.NovelItemStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelItemTypeEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说物品分页查询表单。
 * <p>
 * 查询条件只表达筛选意图，项目归属和当前用户隔离由服务层强制追加。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelItemQueryForm extends PageParam {

    /**
     * 所属项目ID，查询前会校验项目归属。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 物品名称搜索词，支持模糊匹配。
     */
    @Schema(description = "物品名称搜索词")
    @Length(max = 50, message = "物品名称搜索词最多50个字符")
    private String name;

    /**
     * 物品类型筛选。
     */
    @SchemaEnum(desc = "物品类型", value = NovelItemTypeEnum.class)
    @CheckEnum(value = NovelItemTypeEnum.class, required = false, message = "物品类型错误")
    private String type;

    /**
     * 物品状态筛选，状态本身由写作流程维护。
     */
    @SchemaEnum(desc = "物品状态", value = NovelItemStatusEnum.class)
    @CheckEnum(value = NovelItemStatusEnum.class, required = false, message = "物品状态错误")
    private String itemStatus;
}
