package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 小说卷编辑表单。
 * <p>
 * 继承创建表单中的可编辑字段，额外携带卷ID；章节归卷由后续章节管理或批量归入能力维护。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelVolumeUpdateForm extends NovelVolumeAddForm {

    /**
     * 卷ID，服务层会结合当前登录用户再次校验归属。
     */
    @Schema(description = "卷ID")
    @NotNull(message = "卷ID不能为空")
    private Long volumeId;
}
