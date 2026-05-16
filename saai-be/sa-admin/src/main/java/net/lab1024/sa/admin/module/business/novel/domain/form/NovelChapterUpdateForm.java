package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelChapterStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 编辑章节表单。
 *
 * 章节正文较长，前端通常会分"标题/摘要/状态"和"正文编辑"两个界面。
 * 这里只覆盖元信息编辑；正文更新走写作流程，不通过批量编辑接口。
 */
@Data
public class NovelChapterUpdateForm {

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;

    @Length(max = 200, message = "章节标题最多 200 个字符")
    private String title;

    @Length(max = 2000, message = "章节摘要最多 2000 个字符")
    private String summary;

    @Length(max = 65535, message = "章节正文超出长度限制")
    private String content;

    @SchemaEnum(value = NovelChapterStatusEnum.class, required = false)
    @CheckEnum(value = NovelChapterStatusEnum.class, message = "章节状态错误")
    @Length(max = 50, message = "章节状态最多 50 个字符")
    private String status;
}
