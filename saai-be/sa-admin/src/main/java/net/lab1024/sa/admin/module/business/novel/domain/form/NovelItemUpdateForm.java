package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小说物品编辑表单。
 * <p>
 * 继承物品创建表单里的设定字段，额外带上物品ID；数量和状态不从普通编辑入口修改。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelItemUpdateForm extends NovelItemAddForm {

    /**
     * 物品ID，服务层会结合当前登录用户再次校验归属。
     */
    @Schema(description = "物品ID")
    @NotNull(message = "物品ID不能为空")
    private Long itemId;
}
