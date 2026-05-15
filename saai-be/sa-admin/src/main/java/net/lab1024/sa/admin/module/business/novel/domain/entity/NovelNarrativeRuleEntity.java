package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小说叙事规则实体。
 *
 * 叙事规则会进入写作 Prompt，是对模型的“写作边界”，不是剧情事实。
 */
@Data
@TableName("t_novel_narrative_rule")
public class NovelNarrativeRuleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long ruleId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 规则名称。
     */
    private String ruleName;

    /**
     * 规则类型，例如平台红线、文风、字数限制。
     */
    private String ruleType;

    /**
     * 规则内容。
     */
    private String ruleValue;

    /**
     * 优先级，数字越大越靠前进入 Prompt。
     */
    private Integer priority;

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
