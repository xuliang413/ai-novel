package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelItemStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelItemTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 新增小说物品表单。
 *
 * 物品既可以是普通道具，也可以是推动剧情的关键证据或资源。
 */
@Data
public class NovelItemAddForm {

    /**
     * 所属小说项目 ID。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 物品名称。
     */
    @NotBlank(message = "物品名称不能为空")
    @Length(max = 100, message = "物品名称最多 100 个字符")
    private String itemName;

    @SchemaEnum(value = NovelItemTypeEnum.class, required = false)
    @CheckEnum(value = NovelItemTypeEnum.class, message = "物品类型错误")
    @Length(max = 50, message = "物品类型最多 50 个字符")
    private String itemType;

    /**
     * 物品状态。
     *
     * 如果不传，服务层会默认为 INTACT。
     */
    @SchemaEnum(value = NovelItemStatusEnum.class, required = false)
    @CheckEnum(value = NovelItemStatusEnum.class, message = "物品状态错误")
    @Length(max = 50, message = "物品状态最多 50 个字符")
    private String itemStatus;

    @Size(max = 2000, message = "物品简介最多 2000 个字符")
    private String summary;
}
