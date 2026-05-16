package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说项目 实体类 —— 整个系统的顶层容器, 一本小说对应一个项目
 * 所有其他实体(角色/地点/线索/章节等)全部挂在项目下, 项目之间通过createUserId完全隔离
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_project")
public class NovelProjectEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目名称, 如"剑道独尊"
     */
    private String name;

    /**
     * 小说类型: XIANXIA/XUANHUAN/URBAN/HISTORY/SCIFI/MYSTERY/WUXIA/FANTASY
     * 影响默认叙事规则和世界观模板注入
     */
    private String genre;

    /**
     * 世界观概述, 自然语言描述这个世界的基本规则
     * 注入每章的写作Prompt, 让AI理解世界观约束(如"末法时代灵气稀薄,修仙者需依赖丹药")
     */
    private String worldBuilding;

    /**
     * 主角名, 快捷引用, 完整的角色信息在角色管理页
     */
    private String protagonistName;

    /**
     * 文风描述, 约束AI写作风格(如"白描克制冷峻,少用形容词"), 注入每章Prompt
     */
    private String styleDescription;

    /**
     * 目标平台: QIDIAN/FANQIE/ZONGHENG, 选平台时自动加载对应叙事规则红线
     */
    private String platform;

    /**
     * 目标总字数, 统计参考用, 质检不以此为准
     */
    private Integer targetTotalWords;

    /**
     * 每章目标字数, 质检基准(AI生成字数与此对比±20%)
     */
    private Integer targetChapterWords;

    /**
     * 上下文Token目标预算, 检索上下文时的软上限(默认6000)
     */
    private Integer tokenBudget;

    /**
     * 上下文Token硬上限, 无论如何不能超过(默认8000)
     */
    private Integer tokenHardLimit;

    /**
     * 项目状态: ACTIVE写作中/PAUSED已暂停/ARCHIVED已归档(不可见但可恢复)
     */
    private String status;

    /**
     * 归档标记: 0正常 1已归档(不物理删除)
     */
    private Boolean deletedFlag;

    /**
     * 创建用户ID, 所有项目查询强制过滤此字段实现用户隔离
     */
    private Long createUserId;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
