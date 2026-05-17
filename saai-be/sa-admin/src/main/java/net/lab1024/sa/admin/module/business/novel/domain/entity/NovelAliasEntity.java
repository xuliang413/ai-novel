package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说马甲实体。
 * <p>
 * 马甲表示角色对外使用的隐藏身份；管理页维护身份设定，是否被识破属于高风险剧情事实，由写作流程确认后修改。
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_alias")
public class NovelAliasEntity {

    /**
     * 主键ID，由 MySQL 自增生成。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目ID，用于隔离不同小说项目的数据。
     */
    private Long projectId;

    /**
     * 马甲名称，例如“暗影”“夜天子”。
     */
    private String name;

    /**
     * 马甲类型，对应 NovelAliasTypeEnum 的枚举值。
     */
    private String type;

    /**
     * 使用场景描述，说明该身份通常在什么剧情或环境下出现。
     */
    private String aliasContext;

    /**
     * 马甲描述，记录外显身份、人设特征和与真实身份的差异。
     */
    private String summary;

    /**
     * 是否已被识破，仅通过写作流程修改。
     */
    private Boolean revealed;

    /**
     * 被谁识破，通常记录角色名称或角色ID摘要。
     */
    private String revealedTo;

    /**
     * 归档标记，true 表示已归档，普通查询默认排除。
     */
    private Boolean deletedFlag;

    /**
     * 创建用户ID，用于 SmartAdmin 登录用户维度的数据隔离。
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
