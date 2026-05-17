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

    /**
     * 物品ID, 由数据库自增生成, 也是 Neo4j Item 节点的业务主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目ID, 所有物品查询都必须带上它做项目隔离
     */
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

    /**
     * 归档标记, false 表示正常可见, true 表示已归档
     */
    private Boolean deletedFlag;

    /**
     * 创建用户ID, 所有用户级查询都必须带上它做数据隔离
     */
    private Long createUserId;

    /**
     * 最后更新时间, 由数据库自动维护, 用于人工审阅物品最近变化
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间, 由数据库自动维护, 用于物品列表默认排序
     */
    private LocalDateTime createTime;
}
