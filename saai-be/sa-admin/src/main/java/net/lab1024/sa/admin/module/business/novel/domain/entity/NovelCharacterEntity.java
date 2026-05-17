package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小说角色 实体类
 * 设定属性(名称/定位/描述)管理页随时可改, 状态属性(情绪/目标/战力/存活状态)仅通过写作流程GraphPatch审阅修改
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_character")
public class NovelCharacterEntity {

    /**
     * 角色ID, 由数据库自增生成, 也是 Neo4j Character 节点的业务主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目ID, 所有角色查询都必须带上它做项目隔离
     */
    private Long projectId;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色定位: PROTAGONIST主角/ANTAGONIST反派/SUPPORTING重要配角/MINOR次要角色
     */
    private String roleType;

    /**
     * 基础描述(外貌/性格等常驻信息), 管理页随时可改
     */
    private String description;

    // ===== 以下为动态属性, 仅通过写作流程GraphPatch审阅修改 =====

    /**
     * 当前目标, 自然语言描述角色现在想干什么
     */
    private String currentGoal;

    /**
     * 目标完成度 0~1, 数值化方便检索时排序和截断判断
     */
    private BigDecimal goalProgress;

    /**
     * 目标状态: IN_PROGRESS进行中/ACHIEVED已达成/ABANDONED已放弃/DIVERTED已转向
     */
    private String goalStatus;

    /**
     * 当前主导情绪, 约束AI描写角色的语气和行为
     */
    private String currentEmotion;

    /**
     * 情绪强度 1~5, 区分微表情和崩溃/狂喜
     */
    private Integer emotionIntensity;

    /**
     * 次生情绪, 表层+深层并存(如表面坚定实则恐惧)
     */
    private String secondaryEmotion;

    /**
     * 战力/境界, 自然语言不用枚举(不同世界观体系完全不同)
     */
    private String powerLevel;

    /**
     * 存活状态: ACTIVE活跃/INACTIVE暂离/DEAD死亡(不进候选池)/MISSING失踪/UNKNOWN未知
     */
    private String currentStatus;

    /**
     * 归档标记, false 表示正常可见, true 表示已归档
     */
    private Boolean deletedFlag;

    /**
     * 创建用户ID, 所有用户级查询都必须带上它做数据隔离
     */
    private Long createUserId;

    /**
     * 最后更新时间, 由数据库自动维护, 用于人工审阅角色最近变化
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间, 由数据库自动维护, 用于角色列表默认排序
     */
    private LocalDateTime createTime;
}
