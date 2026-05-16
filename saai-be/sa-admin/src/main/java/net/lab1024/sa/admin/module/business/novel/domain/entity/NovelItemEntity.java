package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说物品 实体类
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_item")
public class NovelItemEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 物品名称, 如"断魂刀""筑基丹"
     */
    private String name;

    /**
     * 物品类型: WEAPON武器/ARMOR防具/TOOL工具/CONSUMABLE消耗品/TREASURE宝物/DOCUMENT文书/CURRENCY货币/OTHER其他
     */
    private String type;

    /**
     * 物品描述
     */
    private String summary;

    /**
     * 数量, 可消耗物品才填(丹药/灵石), 唯一物品不填(断魂刀), 仅通过写作流程GraphPatch扣减, 下限为0
     */
    private Integer quantity;

    /**
     * 物品状态: INTACT完好/DAMAGED损坏/DESTROYED摧毁/LOST遗失, 仅通过写作流程修改
     */
    private String itemStatus;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
