package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelItemTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelItemStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 编辑物品表单。
 */
@Data
public class NovelItemUpdateForm {

    @NotNull(message = "物品ID不能为空")
    private Long itemId;

    @Length(max = 100, message = "物品名称最多 100 个字符")
    private String itemName;

    @SchemaEnum(value = NovelItemTypeEnum.class, required = false)
    @CheckEnum(value = NovelItemTypeEnum.class, message = "物品类型错误")
    @Length(max = 50, message = "物品类型最多 50 个字符")
    private String itemType;

    @SchemaEnum(value = NovelItemStatusEnum.class, required = false)
    @CheckEnum(value = NovelItemStatusEnum.class, message = "物品状态错误")
    @Length(max = 50, message = "物品状态最多 50 个字符")
    private String itemStatus;

    @Length(max = 2000, message = "物品简介最多 2000 个字符")
    private String summary;
}
