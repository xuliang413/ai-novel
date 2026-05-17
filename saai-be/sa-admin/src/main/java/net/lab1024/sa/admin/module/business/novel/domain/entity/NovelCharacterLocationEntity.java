package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色当前位置实体类。
 * <p>
 * 这张表是 Neo4j `CURRENTLY_AT` 关系在 MySQL 侧的审计补充, 只由写作流程里的 GraphPatch 写入。
 * 管理页不直接改当前位置, 避免手动状态和图谱事实出现冲突。
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_character_location")
public class NovelCharacterLocationEntity {

    /**
     * 主键ID, 由数据库自增生成。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目ID, 所有查询必须带上它做项目隔离。
     */
    private Long projectId;

    /**
     * 角色ID, 对应 t_novel_character.id。
     */
    private Long characterId;

    /**
     * 地点ID, 对应 t_novel_location.id。
     */
    private Long locationId;

    /**
     * 地点名称冗余字段, 用于审阅页和日志展示, 避免每次展示都回表查询地点名。
     */
    private String locationName;

    /**
     * 角色移动到该地点的章节号, 便于追踪当前位置是在哪一章形成的。
     */
    private Integer enteredInChapter;

    /**
     * 是否当前所在地: true 表示当前事实, false 表示历史位置记录。
     */
    private Boolean currentFlag;

    /**
     * 归档标记, false 表示有效, true 表示已归档。
     */
    private Boolean deletedFlag;

    /**
     * 创建用户ID, 所有用户级查询必须带上它做数据隔离。
     */
    private Long createUserId;

    /**
     * 更新时间, 由数据库自动维护。
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间, 由数据库自动维护。
     */
    private LocalDateTime createTime;
}
