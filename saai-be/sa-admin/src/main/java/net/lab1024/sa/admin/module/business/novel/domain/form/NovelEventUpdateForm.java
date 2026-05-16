package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 编辑事件表单。
 *
 * 事件是已发生的剧情事实，编辑时主要改摘要和补充信息，一般不改 chapterOccurred。
 */
@Data
public class NovelEventUpdateForm {

    @NotNull(message = "事件ID不能为空")
    private Long eventId;

    @Length(max = 100, message = "事件名称最多 100 个字符")
    private String eventName;

    @Length(max = 2000, message = "事件摘要最多 2000 个字符")
    private String summary;

    /**
     * 发生章节序号 —— 一般不改，但允许手动修正
     */
    private Integer chapterOccurred;
}
