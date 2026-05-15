package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 模拟生成章节表单。
 */
@Data
public class NovelWriteMockForm {

    /**
     * 所属小说项目 ID。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 指定章节序号；为空时自动使用下一章。
     */
    private Integer chapterNo;
}
