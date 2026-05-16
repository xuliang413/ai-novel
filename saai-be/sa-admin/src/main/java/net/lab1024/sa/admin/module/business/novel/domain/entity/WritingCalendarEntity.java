package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 写作打卡日历实体 —— 记录作者每天的写作量和完成情况
 *
 * 为什么单独建表而不是复用 writing_log：
 * writing_log 记录的是"每次调用 LLM 的结果"，一天可能多次调用，粒度太细。
 * 写作日历要的是"今天写了多少字、完成了几个章"，按天聚合。
 * 两者是不同粒度的数据，合在一起会让查询变慢、语义模糊。
 */
@Data
@TableName("t_writing_calendar")
public class WritingCalendarEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID —— 来自 SmartAdmin 的登录用户
     */
    private Long userId;

    /**
     * 项目 ID —— 按项目独立记录打卡
     */
    private Long projectId;

    /**
     * 打卡日期 —— 一天一条
     */
    private LocalDate calendarDate;

    /**
     * 当日新增字数 —— 由写作日志聚合得出
     */
    private Integer wordCount;

    /**
     * 当日完成章节数 —— 只有 PUBLISHED 状态的章节才算
     */
    private Integer chapterCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
