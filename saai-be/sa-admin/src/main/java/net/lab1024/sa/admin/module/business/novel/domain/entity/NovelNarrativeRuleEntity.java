package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说叙事规则实体。
 * <p>
 * 叙事规则是写作 System Prompt 的纯 MySQL 配置，不进入 Neo4j；用户可以随时修改全部字段。
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_narrative_rule")
public class NovelNarrativeRuleEntity {

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
     * 规则名称，例如“平台红线”“每章字数要求”。
     */
    private String name;

    /**
     * 自然语言规则内容，组装 System Prompt 时直接注入。
     */
    private String content;

    /**
     * 优先级，范围 1~5；数字越大，组装 Prompt 时越靠前。
     */
    private Integer priority;

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
