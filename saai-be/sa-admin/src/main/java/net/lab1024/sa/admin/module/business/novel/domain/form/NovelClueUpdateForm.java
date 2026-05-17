package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小说线索编辑表单。
 * <p>
 * 继承线索创建表单里的设定字段，额外带上线索ID；动态进展字段不允许从普通编辑入口修改。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelClueUpdateForm extends NovelClueAddForm {

    /**
     * 线索ID，服务层会结合当前登录用户再次校验归属。
     */
    @Schema(description = "线索ID")
    @NotNull(message = "线索ID不能为空")
    private Long clueId;
}
