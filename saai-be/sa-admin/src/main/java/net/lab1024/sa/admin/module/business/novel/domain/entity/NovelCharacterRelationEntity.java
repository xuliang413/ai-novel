package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说角色关系实体。
 * <p>
 * 这张表只存角色到角色的强语义关系: KNOWS、LOVES、HATES、IS_FAMILY_OF。
 * 角色与地点、物品、线索等关系由 GraphPatch 写作流程维护，不在资产管理页提供独立 CRUD。
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_character_relation")
public class NovelCharacterRelationEntity {

    /**
     * 主键ID，数据库自增。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目ID，用于 MySQL 与 Neo4j 的项目隔离。
     */
    private Long projectId;

    /**
     * 源角色ID，对应图谱关系的起点 Character。
     */
    private Long characterId;

    /**
     * 目标角色ID，对应图谱关系的终点 Character。
     */
    private Long targetCharacterId;

    /**
     * 关系大类: KNOWS 一般社交、LOVES 爱慕、HATES 仇恨、IS_FAMILY_OF 亲缘或师门。
     */
    private String relationType;

    /**
     * KNOWS 子类型: FRIEND 朋友、ALLY 盟友、RIVAL 竞争对手、ACQUAINTANCE 熟人、SUBORDINATE 下属、ENEMY 敌人。
     */
    private String knowsRelationType;

    /**
     * 爱慕状态，仅 relationType=LOVES 时有效: UNREQUITED 单恋、MUTUAL 两情相悦、PAST 过去式。
     */
    private String loveStatus;

    /**
     * 仇恨强度，仅 relationType=HATES 时有效，范围 1~5。
     */
    private Integer hateIntensity;

    /**
     * 亲缘或师门类型，仅 relationType=IS_FAMILY_OF 时有效。
     */
    private String familyType;

    /**
     * 归档标记，true 表示管理页不再展示，Neo4j 对应关系也应删除。
     */
    private Boolean deletedFlag;

    /**
     * 创建用户ID，用于 SmartAdmin 登录用户隔离。
     */
    private Long createUserId;

    /**
     * 最后更新时间，由数据库自动维护。
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间，由数据库自动维护。
     */
    private LocalDateTime createTime;
}
