package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说叙事规则 实体类
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_narrative_rule")
public class NovelNarrativeRuleEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 规则名称, 如"禁止血腥描写""每章必须3000字"
     */
    private String name;

    /**
     * 自然语言规则内容, 拼System Prompt时直接注入, 不进Neo4j纯MySQL存
     */
    private String content;

    /**
     * 优先级 1~5, 拼System Prompt时按priority降序排列, 红线永远在最前面
     */
    private Integer priority;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
