package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.novel.constant.NovelChapterStatusEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

/**
 * 小说章节分页查询表单。
 * <p>
 * 查询表单只表达筛选条件，项目归属、当前用户隔离和未归档条件由服务层强制追加。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelChapterQueryForm extends PageParam {

    /**
     * 所属项目ID，服务层会校验项目必须属于当前登录用户。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 所属卷ID筛选，不传表示查询项目下全部章节。
     */
    @Schema(description = "所属卷ID")
    private Long volumeId;

    /**
     * 章节序号筛选，不传表示不过滤具体章节。
     */
    @Schema(description = "章节序号")
    private Integer chapterNumber;

    /**
     * 章节标题模糊搜索，不传表示不过滤标题。
     */
    @Schema(description = "章节标题")
    private String title;

    /**
     * 章节状态筛选，用于区分草稿、待图谱确认、已发布等阶段。
     */
    @SchemaEnum(desc = "章节状态", value = NovelChapterStatusEnum.class)
    @CheckEnum(value = NovelChapterStatusEnum.class, required = false, message = "章节状态错误")
    private String status;
}
