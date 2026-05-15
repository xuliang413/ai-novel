package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说金手指实体。
 *
 * 金手指会影响角色能力边界，所以既存 MySQL，也同步到 Neo4j 供写作检索使用。
 */
@Data
@TableName("t_novel_cheat")
public class NovelCheatEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long cheatId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 金手指名称。
     */
    private String cheatName;

    /**
     * 金手指类型。
     */
    private String cheatType;

    /**
     * 金手指简介。
     */
    private String summary;

    /**
     * 来源。
     */
    private String origin;

    /**
     * 使用限制。
     */
    private String limitation;

    /**
     * 升级路径。
     */
    private String evolution;

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
