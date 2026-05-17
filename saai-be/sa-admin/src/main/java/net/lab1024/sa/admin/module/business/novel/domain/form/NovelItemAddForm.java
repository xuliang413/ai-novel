package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelItemTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说物品创建表单。
 * <p>
 * 管理页只维护名称、类型和描述；数量扣减和物品状态变化由写作流程 GraphPatch 审阅后写入。
 *
 * @Author AI-Novel
 */
@Data
public class NovelItemAddForm {

    /**
     * 所属项目ID，服务层会校验项目必须属于当前登录用户。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 物品名称，用于列表、图谱节点和写作检索。
     */
    @Schema(description = "物品名称")
    @NotBlank(message = "物品名称不能为空")
    @Length(max = 200, message = "物品名称最多200个字符")
    private String name;

    /**
     * 物品类型，用于区分武器、防具、文书、消耗品等。
     */
    @SchemaEnum(desc = "物品类型", value = NovelItemTypeEnum.class)
    @CheckEnum(value = NovelItemTypeEnum.class, required = true, message = "物品类型错误")
    private String type;

    /**
     * 物品描述，记录外观、来历、功能或剧情用途。
     */
    @Schema(description = "物品描述")
    private String summary;
}
