package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说地点实体。
 */
@Data
@TableName("t_novel_location")
public class NovelLocationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long locationId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 地点名称。
     */
    private String locationName;

    /**
     * 地点类型。
     */
    private String locationType;

    /**
     * 地点简介。
     */
    private String summary;

    /**
     * 逻辑删除标识。
     */
    private Boolean deletedFlag;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
