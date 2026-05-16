package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用 ID 表单 —— detail / archive 等只需传 ID 的接口用
 */
@Data
public class NovelIdForm {

    @NotNull(message = "ID不能为空")
    private Long id;
}
