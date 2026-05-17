package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小说金手指编辑表单。
 * <p>
 * 继承创建表单中的设定字段，额外携带金手指ID；当前副作用阶段不从普通编辑入口修改。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelCheatUpdateForm extends NovelCheatAddForm {

    /**
     * 金手指ID，服务层会结合当前登录用户再次校验归属。
     */
    @Schema(description = "金手指ID")
    @NotNull(message = "金手指ID不能为空")
    private Long cheatId;
}
