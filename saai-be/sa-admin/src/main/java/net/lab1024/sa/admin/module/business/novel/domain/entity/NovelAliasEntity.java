package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说马甲实体。
 *
 * 存角色的隐藏身份、账号、称号、伪装等。它在图谱里可通过 HAS_ALIAS / KNOWS_ALIAS 表达。
 */
@Data
@TableName("t_novel_alias")
public class NovelAliasEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long aliasId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 马甲名称。
     */
    private String aliasName;

    /**
     * 马甲类型，例如假身份、线上身份、分身。
     */
    private String aliasType;

    /**
     * 使用场景，说明这个马甲通常什么时候出现。
     */
    private String aliasContext;

    /**
     * 马甲简介。
     */
    private String summary;

    /**
     * 是否已经暴露。
     */
    private Boolean revealed;

    /**
     * 已识破该马甲的角色列表。
     */
    private String revealedTo;

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
