package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 新增小说事件表单。
 *
 * 事件用于记录“故事世界里发生过的事实”，不是章节正文全文。
 */
@Data
public class NovelEventAddForm {

    /**
     * 所属小说项目 ID。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 事件名称。
     */
    @NotBlank(message = "事件名称不能为空")
    @Length(max = 100, message = "事件名称最多 100 个字符")
    private String eventName;

    @Size(max = 2000, message = "事件简介最多 2000 个字符")
    private String summary;

    /**
     * 事件发生章节。
     *
     * 为空表示暂时只做世界观事件，尚未绑定具体章节。
     */
    private Integer chapterOccurred;
}
