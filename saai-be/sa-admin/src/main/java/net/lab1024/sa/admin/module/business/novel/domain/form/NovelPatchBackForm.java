package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 从 Patch 确认返回正文审阅。
 *
 * 用户发现变更单不对时用它回到正文审阅页，重新改正文再生成 Patch。
 */
@Data
public class NovelPatchBackForm {

    /**
     * 要回退的写作会话 ID。
     */
    @NotNull(message = "生成会话 ID 不能为空")
    private Long sessionId;
}
