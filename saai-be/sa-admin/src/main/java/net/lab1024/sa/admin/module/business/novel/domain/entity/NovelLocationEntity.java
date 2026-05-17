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

    /**
     * 地点ID, 由数据库自增生成, 也是 Neo4j Location 节点的业务主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目ID, 所有地点查询都必须带上它做项目隔离
     */
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

    /**
     * 归档标记, false 表示正常可见, true 表示已归档
     */
    private Boolean deletedFlag;

    /**
     * 创建用户ID, 所有用户级查询都必须带上它做数据隔离
     */
    private Long createUserId;

    /**
     * 最后更新时间, 由数据库自动维护, 用于人工审阅地点最近变化
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间, 由数据库自动维护, 用于地点列表默认排序
     */
    private LocalDateTime createTime;
}
