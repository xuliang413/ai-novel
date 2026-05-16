package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说地点 实体类
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_location")
public class NovelLocationEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 地点名称
     */
    private String name;

    /**
     * 地点类型: CITY城市/VILLAGE村镇/BUILDING建筑/SECT宗门/WILDERNESS荒野/REALM秘境/BATTLEFIELD战场
     */
    private String type;

    /**
     * 自然语言描述
     */
    private String summary;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
