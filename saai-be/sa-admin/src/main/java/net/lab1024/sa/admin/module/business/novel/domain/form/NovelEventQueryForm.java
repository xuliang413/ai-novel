package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.domain.PageParam;
import org.hibernate.validator.constraints.Length;

/**
 * 小说事件分页查询表单。
 * <p>
 * 查询条件只表达筛选意图，项目归属和当前用户隔离由服务层强制追加。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelEventQueryForm extends PageParam {

    /**
     * 所属项目ID，查询前会校验项目归属。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 事件名称搜索词，支持模糊匹配。
     */
    @Schema(description = "事件名称搜索词")
    @Length(max = 50, message = "事件名称搜索词最多50个字符")
    private String name;

    /**
     * 发生章节号筛选。
     */
    @Schema(description = "发生章节号")
    private Integer chapterOccurred;
}
