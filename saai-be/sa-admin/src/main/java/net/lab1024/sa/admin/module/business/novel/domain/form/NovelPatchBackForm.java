package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 从 Patch 确认返回正文审阅。
 */
@Data
public class NovelPatchBackForm {

    @NotNull(message = "生成会话 ID 不能为空")
    private Long sessionId;
}
