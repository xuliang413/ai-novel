package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说物品实体。
 *
 * 物品可以被角色持有、转移、损坏或销毁。相关变化通常需要 GraphPatch 审阅。
 */
@Data
@TableName("t_novel_item")
public class NovelItemEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long itemId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 物品名称。
     */
    private String itemName;

    /**
     * 物品类型。
     */
    private String itemType;

    /**
     * 物品状态。
     */
    private String itemStatus;

    /**
     * 物品简介。
     */
    private String summary;

    /**
     * 逻辑删除标识。
     */
    private Boolean deletedFlag;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
