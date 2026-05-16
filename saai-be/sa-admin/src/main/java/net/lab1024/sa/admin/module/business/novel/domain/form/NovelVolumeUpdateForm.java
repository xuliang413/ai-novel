package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 编辑卷表单。
 */
@Data
public class NovelVolumeUpdateForm {

    @NotNull(message = "卷ID不能为空")
    private Long volumeId;

    @Length(max = 100, message = "卷标题最多 100 个字符")
    private String volumeTitle;

    @Length(max = 2000, message = "卷概要最多 2000 个字符")
    private String summary;

    /**
     * 卷序号 —— 调整后可改变导航顺序
     */
    private Integer volumeNo;
}
