package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 小说线索 实体类 —— 贯穿多章的叙事线, 系统中最核心的实体
 * PLOT_THREAD占Token进Prompt持续推进, FORESHADOWING伏笔在休眠期不占Token不进Prompt
 * 生命周期: DORMANT(休眠)→ACTIVE(活跃)→RESOLVED(已收束)
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_clue")
public class NovelClueEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 线索名称
     */
    private String name;

    /**
     * 线索类型: MAIN主线/SUB支线/HIDDEN暗线(读者暂时不知道)
     */
    private String type;

    /**
     * 子类型: PLOT_THREAD线索(占Token进Prompt)/FORESHADOWING伏笔(休眠期不进Prompt)
     */
    private String subType;

    /**
     * 完整描述, 设定属性, 管理页随时可改
     */
    private String description;

    /**
     * 优先级 1~5, 多条ACTIVE线索抢Prompt预算时高优先级优先
     */
    private Integer priority;

    /**
     * 计划收束章节号, 规划参考不强制执行
     */
    private Integer targetChapter;

    /**
     * 情绪基调: TRAGIC/TENSE/ROMANTIC/HEROIC/MYSTERIOUS/DARK, 约束AI写相关段落时的语气
     */
    private String tone;

    // ===== 以下为动态属性, 仅通过写作流程修改 =====

    /**
     * 当前进展摘要, 自然语言, 每章推进后更新
     */
    private String summary;

    /**
     * 揭露程度 0~1, 0.2=刚刚暗示, 0.8=真相快大白了
     */
    private BigDecimal revealLevel;

    /**
     * 当前阶段, 自然语言描述(如"线索碎片""真相浮现""证据确凿"), 和揭露程度正交
     */
    private String currentStage;

    /**
     * 线索状态: DORMANT休眠/ACTIVE活跃/RESOLVED已收束, 决定检索行为
     */
    private String clueStatus;

    /**
     * 最后一次提醒的章节号, DORMANT伏笔回溯提醒后记录, 防止重复提醒
     */
    private Integer lastAlertedChapter;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
