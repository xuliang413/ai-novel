package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说卷实体。
 *
 * 用来承载卷级结构。当前主要用于资产管理和图谱节点，后续可绑定章节细纲。
 */
@Data
@TableName("t_novel_volume")
public class NovelVolumeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long volumeId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 卷序号。
     */
    private Integer volumeNo;

    /**
     * 卷标题。
     */
    private String volumeTitle;

    /**
     * 卷概要。
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
