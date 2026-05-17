package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelItemStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelItemTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 小说物品展示 VO。
 * <p>
 * 返回物品设定字段和写作流程维护出的数量、状态，便于资产页查看物品当前事实。
 *
 * @Author AI-Novel
 */
@Data
public class NovelItemVO {

    /**
     * 物品ID。
     */
    @Schema(description = "物品ID")
    private Long id;

    /**
     * 所属项目ID。
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 物品名称。
     */
    @Schema(description = "物品名称")
    private String name;

    /**
     * 物品类型。
     */
    @SchemaEnum(desc = "物品类型", value = NovelItemTypeEnum.class)
    private String type;

    /**
     * 物品描述。
     */
    @Schema(description = "物品描述")
    private String summary;

    /**
     * 物品数量，可消耗物品才会有值。
     */
    @Schema(description = "物品数量")
    private Integer quantity;

    /**
     * 物品状态。
     */
    @SchemaEnum(desc = "物品状态", value = NovelItemStatusEnum.class)
    private String itemStatus;

    /**
     * 归档标记，true 表示物品已归档。
     */
    @Schema(description = "归档标记")
    private Boolean deletedFlag;

    /**
     * 创建用户ID。
     */
    @Schema(description = "创建用户ID")
    private Long createUserId;

    /**
     * 最后更新时间。
     */
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
