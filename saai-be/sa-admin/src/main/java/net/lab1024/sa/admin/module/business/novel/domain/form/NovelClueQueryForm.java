package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueSubTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueTypeEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说线索分页查询表单。
 * <p>
 * 查询条件只表达筛选意图，项目归属和当前用户隔离由服务层强制追加。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelClueQueryForm extends PageParam {

    /**
     * 所属项目ID，查询前会校验项目归属。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 线索名称搜索词，支持模糊匹配。
     */
    @Schema(description = "线索名称搜索词")
    @Length(max = 50, message = "线索名称搜索词最多50个字符")
    private String name;

    /**
     * 线索类型筛选。
     */
    @SchemaEnum(desc = "线索类型", value = NovelClueTypeEnum.class)
    @CheckEnum(value = NovelClueTypeEnum.class, required = false, message = "线索类型错误")
    private String type;

    /**
     * 线索子类型筛选。
     */
    @SchemaEnum(desc = "线索子类型", value = NovelClueSubTypeEnum.class)
    @CheckEnum(value = NovelClueSubTypeEnum.class, required = false, message = "线索子类型错误")
    private String subType;

    /**
     * 线索生命周期状态筛选。
     */
    @SchemaEnum(desc = "线索状态", value = NovelClueStatusEnum.class)
    @CheckEnum(value = NovelClueStatusEnum.class, required = false, message = "线索状态错误")
    private String clueStatus;
}
