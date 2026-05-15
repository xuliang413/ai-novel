package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说角色实体。
 */
@Data
@TableName("t_novel_character")
public class NovelCharacterEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long characterId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 角色名称。
     */
    private String characterName;

    /**
     * 角色定位。
     */
    private String roleType;

    /**
     * 角色简介。
     */
    private String summary;

    /**
     * 角色当前状态。
     */
    private String currentStatus;

    /**
     * 逻辑删除标识。
     */
    private Boolean deletedFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
