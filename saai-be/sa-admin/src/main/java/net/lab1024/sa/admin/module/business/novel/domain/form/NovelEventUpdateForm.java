package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小说事件编辑表单。
 * <p>
 * 继承事件创建表单里的字段，额外带上事件ID。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelEventUpdateForm extends NovelEventAddForm {

    /**
     * 事件ID，服务层会结合当前登录用户再次校验归属。
     */
    @Schema(description = "事件ID")
    @NotNull(message = "事件ID不能为空")
    private Long eventId;
}
