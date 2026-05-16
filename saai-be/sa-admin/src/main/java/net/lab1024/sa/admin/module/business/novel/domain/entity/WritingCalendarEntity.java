package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 写作日历 实体类 —— 按天记录章节和字数
 * 写作日历页面展示每天写了哪些章节 (UNIQUE: projectId + writeDate)
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_writing_calendar")
public class WritingCalendarEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 写作日期
     */
    private LocalDate writeDate;

    /**
     * 当天生成的章节号列表(JSON数组)
     */
    private String chaptersWritten;

    /**
     * 当天总字数
     */
    private Integer totalWords;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
