package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCheatTypeEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说金手指分页查询表单。
 * <p>
 * 查询条件只表达筛选意图，项目归属、当前用户隔离和归档过滤由服务层强制追加。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelCheatQueryForm extends PageParam {

    /**
     * 所属项目ID，查询前会校验项目归属。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 金手指名称搜索词，支持模糊匹配。
     */
    @Schema(description = "金手指名称搜索词")
    @Length(max = 50, message = "金手指名称搜索词最多50个字符")
    private String name;

    /**
     * 金手指类型筛选。
     */
    @SchemaEnum(desc = "金手指类型", value = NovelCheatTypeEnum.class)
    @CheckEnum(value = NovelCheatTypeEnum.class, required = false, message = "金手指类型错误")
    private String type;
}
