package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色关系 实体类 —— 只存KNOWS/LOVES/HATES/IS_FAMILY_OF四种关系
 * KNOWS的relationType枚举严格校验(不在白名单内拒绝写入)
 * 其他关系中间表(character_location等)不提供独立CRUD, 仅通过GraphPatch写入
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_character_relation")
public class NovelCharacterRelationEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 源角色ID
     */
    private Long characterId;

    /**
     * 目标角色ID
     */
    private Long targetCharacterId;

    /**
     * 关系大类: KNOWS一般社交/LOVES爱慕/HATES仇恨/IS_FAMILY_OF亲缘
     */
    private String relationType;

    /**
     * KNOWS子类型: FRIEND朋友/ALLY盟友/RIVAL竞争对手/ACQUAINTANCE熟人/SUBORDINATE下属/ENEMY敌人
     * 枚举严格校验, 不在白名单内拒绝写入
     */
    private String knowsRelationType;

    /**
     * 爱慕状态: UNREQUITED单恋/MUTUAL两情相悦/PAST过去式
     */
    private String loveStatus;

    /**
     * 仇恨强度 1~5
     */
    private Integer hateIntensity;

    /**
     * 亲缘类型: FATHER/MOTHER/BROTHER/SISTER/SON/DAUGHTER/COUSIN/SPOUSE/MASTER/DISCIPLE
     */
    private String familyType;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
