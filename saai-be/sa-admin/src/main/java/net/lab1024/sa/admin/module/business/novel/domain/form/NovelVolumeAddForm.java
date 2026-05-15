package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 新增小说卷表单。
 *
 * 卷用于把长篇小说分段管理，后续可以接章节细纲和卷级目标。
 */
@Data
public class NovelVolumeAddForm {

    /**
     * 所属小说项目 ID。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 卷序号，例如 1、2、3。
     */
    @NotNull(message = "卷序号不能为空")
    private Integer volumeNo;

    /**
     * 卷标题。
     */
    @NotBlank(message = "卷标题不能为空")
    @Length(max = 200, message = "卷标题最多 200 个字符")
    private String volumeTitle;

    @Size(max = 2000, message = "卷概要最多 2000 个字符")
    private String summary;
}
