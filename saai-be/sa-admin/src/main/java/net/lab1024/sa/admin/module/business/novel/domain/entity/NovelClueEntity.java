package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说线索实体。
 */
@Data
@TableName("t_novel_clue")
public class NovelClueEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long clueId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 线索名称。
     */
    private String clueName;

    /**
     * 线索类型。
     */
    private String clueType;

    /**
     * 线索状态。
     */
    private String clueStatus;

    /**
     * 线索简介。
     */
    private String summary;

    /**
     * 逻辑删除标识。
     */
    private Boolean deletedFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
