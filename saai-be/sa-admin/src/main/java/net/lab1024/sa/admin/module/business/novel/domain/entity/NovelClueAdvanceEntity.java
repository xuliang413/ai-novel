package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 线索推进记录 实体类 —— 每章推进线索的记录, 从GraphPatch ADVANCES操作同步过来
 * 用于线索停滞提醒: 查出ACTIVE线索最近一次ADVANCES的章节号对比当前进度
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_clue_advance")
public class NovelClueAdvanceEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 线索ID
     */
    private Long clueId;

    /**
     * 推进章节ID
     */
    private Long chapterId;

    /**
     * 章节号, 冗余便于查询
     */
    private Integer chapterNumber;

    /**
     * 推进到什么程度, 自然语言描述
     */
    private String progressDescription;

    /**
     * 推进后的揭露程度
     */
    private BigDecimal revealLevel;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
