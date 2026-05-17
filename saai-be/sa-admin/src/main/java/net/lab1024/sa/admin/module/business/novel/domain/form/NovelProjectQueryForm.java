package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectGenreEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectPlatformEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectStatusEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说项目分页查询表单。
 * <p>
 * 查询条件只描述用户想筛选什么，当前用户隔离条件由服务层强制追加。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelProjectQueryForm extends PageParam {

    /**
     * 项目名称搜索词，支持模糊匹配。
     */
    @Schema(description = "项目名称搜索词")
    @Length(max = 50, message = "项目名称搜索词最多50个字符")
    private String name;

    /**
     * 小说类型筛选。
     */
    @SchemaEnum(desc = "小说类型", value = NovelProjectGenreEnum.class)
    @CheckEnum(value = NovelProjectGenreEnum.class, required = false, message = "小说类型错误")
    private String genre;

    /**
     * 目标平台筛选。
     */
    @SchemaEnum(desc = "目标平台", value = NovelProjectPlatformEnum.class)
    @CheckEnum(value = NovelProjectPlatformEnum.class, required = false, message = "目标平台错误")
    private String platform;

    /**
     * 项目状态筛选，默认列表通常只查 ACTIVE/PAUSED。
     */
    @SchemaEnum(desc = "项目状态", value = NovelProjectStatusEnum.class)
    @CheckEnum(value = NovelProjectStatusEnum.class, required = false, message = "项目状态错误")
    private String status;

    /**
     * 是否包含已归档项目；为空或 false 时只查未归档项目。
     */
    @Schema(description = "是否包含已归档项目")
    private Boolean includeArchived;
}
