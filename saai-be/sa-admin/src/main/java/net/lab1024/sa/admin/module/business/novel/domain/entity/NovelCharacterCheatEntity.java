package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色金手指关联 实体类 —— 记录角色在第几章获得金手指
 * 仅通过写作流程GraphPatch写入, 不提供独立CRUD
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_character_cheat")
public class NovelCharacterCheatEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 角色ID
     */
    private Long characterId;

    /**
     * 金手指ID
     */
    private Long cheatId;

    /**
     * 在第几章获得, Neo4j同步(HAS_CHEAT.acquiredInChapter)
     */
    private Integer acquiredInChapter;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
