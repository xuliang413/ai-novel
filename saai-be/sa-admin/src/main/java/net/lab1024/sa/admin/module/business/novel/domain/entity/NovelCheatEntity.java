package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说金手指实体。
 * <p>
 * 金手指表示角色拥有的特殊能力、系统、随身空间或绑定物品；管理页维护设定字段，当前副作用阶段由写作流程推进。
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_cheat")
public class NovelCheatEntity {

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
     * 金手指名称，例如“万倍悟性”“老爷爷戒指”。
     */
    private String name;

    /**
     * 金手指类型，对应 NovelCheatTypeEnum 的枚举值。
     */
    private String type;

    /**
     * 金手指描述，记录能力效果和叙事定位。
     */
    private String summary;

    /**
     * 金手指来源，说明获得方式和背后设定。
     */
    private String origin;

    /**
     * 金手指限制或副作用，用来约束写作时的能力边界。
     */
    private String limitation;

    /**
     * 金手指进化或升级路径，用于规划后续能力成长。
     */
    private String evolution;

    /**
     * 当前副作用阶段，仅通过写作流程修改，追踪能力代价的渐进演化。
     */
    private String currentStage;

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
