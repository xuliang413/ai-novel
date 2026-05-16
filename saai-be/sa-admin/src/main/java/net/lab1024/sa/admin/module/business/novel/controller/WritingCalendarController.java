package net.lab1024.sa.admin.module.business.novel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.dao.WritingCalendarDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.WritingCalendarEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelIdForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.CalendarEntryVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.CheckinResultVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.StreakInfoVO;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 写作日历接口 —— 打卡记录、连续写作天数、月度统计
 *
 * 打卡规则：
 * 每次用户发布章节（确认 GraphPatch 成功后）自动触发一次打卡，也可以手动调用。
 * 同一天同一项目多次打卡只更新字数和章节数，不重复创建记录。
 * 前端用日历热力图展示：颜色深浅 → 字数多少。
 */
@RestController
@RequestMapping("/novel/write/calendar")
@Tag(name = "AI 小说 - 写作日历")
public class WritingCalendarController {

    @Resource
    private WritingCalendarDao writingCalendarDao;

    /**
     * 打卡表单 —— 支持手动打卡（如今天用外部编辑器写了字）
     */
    @Data
    public static class CheckinForm {
        @NotNull(message = "项目ID不能为空")
        private Long projectId;
        private Integer wordCount;
        private Integer chapterCount;
    }

    /**
     * 每日或查询日期范围内的写作打卡记录
     *
     * 返回指定月份的所有打卡记录，前端用日历热力图渲染。
     */
    @Data
    public static class CalendarQueryForm {
        @NotNull(message = "项目ID不能为空")
        private Long projectId;
        private Integer year;
        private Integer month;
    }

    /**
     * 打卡 —— 记录今天的写作量
     *
     * 同一项目同一天重复打卡时，字数累加（取最大值），章节数累加。
     */
    @Operation(summary = "写作打卡")
    @PostMapping("/checkin")
    public ResponseDTO<CheckinResultVO> checkin(@RequestBody @Valid CheckinForm form) {
        Long userId = AdminRequestUtil.getRequestUserId();
        LocalDate today = LocalDate.now();

        WritingCalendarEntity existing = writingCalendarDao.selectOne(new LambdaQueryWrapper<WritingCalendarEntity>()
                .eq(WritingCalendarEntity::getUserId, userId)
                .eq(WritingCalendarEntity::getProjectId, form.getProjectId())
                .eq(WritingCalendarEntity::getCalendarDate, today));

        if (existing != null) {
            existing.setWordCount(Math.max(existing.getWordCount() != null ? existing.getWordCount() : 0,
                    form.getWordCount() != null ? form.getWordCount() : 0));
            if (form.getChapterCount() != null) {
                existing.setChapterCount((existing.getChapterCount() != null ? existing.getChapterCount() : 0)
                        + form.getChapterCount());
            }
            writingCalendarDao.updateById(existing);
        } else {
            WritingCalendarEntity entity = new WritingCalendarEntity();
            entity.setUserId(userId);
            entity.setProjectId(form.getProjectId());
            entity.setCalendarDate(today);
            entity.setWordCount(form.getWordCount() != null ? form.getWordCount() : 0);
            entity.setChapterCount(form.getChapterCount() != null ? form.getChapterCount() : 0);
            writingCalendarDao.insert(entity);
        }

        return ResponseDTO.ok(CheckinResultVO.builder()
                .success(true)
                .date(today.toString())
                .streakInfo(buildStreakInfo(userId, form.getProjectId()))
                .build());
    }

    /**
     * 查询月度打卡记录 —— 前端日历热力图的数据来源
     *
     * 返回每天的字数和章节数，天粒度。
     */
    @Operation(summary = "查询月度写作日历")
    @PostMapping("/query")
    public ResponseDTO<List<CalendarEntryVO>> query(@RequestBody @Valid CalendarQueryForm form) {
        Long userId = AdminRequestUtil.getRequestUserId();
        int year = form.getYear() != null ? form.getYear() : LocalDate.now().getYear();
        int month = form.getMonth() != null ? form.getMonth() : LocalDate.now().getMonthValue();
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<WritingCalendarEntity> records = writingCalendarDao.selectList(new LambdaQueryWrapper<WritingCalendarEntity>()
                .eq(WritingCalendarEntity::getUserId, userId)
                .eq(WritingCalendarEntity::getProjectId, form.getProjectId())
                .between(WritingCalendarEntity::getCalendarDate, start, end)
                .orderByAsc(WritingCalendarEntity::getCalendarDate));

        List<CalendarEntryVO> list = new ArrayList<>();
        for (WritingCalendarEntity r : records) {
            list.add(CalendarEntryVO.builder()
                    .date(r.getCalendarDate().toString())
                    .wordCount(r.getWordCount())
                    .chapterCount(r.getChapterCount())
                    .build());
        }
        return ResponseDTO.ok(list);
    }

    /**
     * 连续写作天数统计 —— 前端卡片展示
     *
     * 逻辑：从今天往前数，遇到没有打卡记录的日期就停。
     */
    @Operation(summary = "连续写作统计")
    @PostMapping("/streak")
    public ResponseDTO<StreakInfoVO> streak(@RequestBody @Valid NovelIdForm form) {
        Long userId = AdminRequestUtil.getRequestUserId();
        return ResponseDTO.ok(buildStreakInfo(userId, form.getId()));
    }

    private StreakInfoVO buildStreakInfo(Long userId, Long projectId) {
        LocalDate today = LocalDate.now();

        // 连续天数：从今天往前数
        int streak = 0;
        for (int i = 0; i < 365; i++) {
            LocalDate date = today.minusDays(i);
            Long count = writingCalendarDao.selectCount(new LambdaQueryWrapper<WritingCalendarEntity>()
                    .eq(WritingCalendarEntity::getUserId, userId)
                    .eq(WritingCalendarEntity::getProjectId, projectId)
                    .eq(WritingCalendarEntity::getCalendarDate, date));
            if (count != null && count > 0) {
                streak++;
            } else {
                break;
            }
        }

        // 当月总天数和字数
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today;
        List<WritingCalendarEntity> monthRecords = writingCalendarDao.selectList(new LambdaQueryWrapper<WritingCalendarEntity>()
                .eq(WritingCalendarEntity::getUserId, userId)
                .eq(WritingCalendarEntity::getProjectId, projectId)
                .between(WritingCalendarEntity::getCalendarDate, monthStart, monthEnd));
        int monthDays = monthRecords.size();
        int monthWords = monthRecords.stream().mapToInt(r -> r.getWordCount() != null ? r.getWordCount() : 0).sum();

        return StreakInfoVO.builder()
                .streakDays(streak)
                .monthDays(monthDays)
                .monthWords(monthWords)
                .build();
    }
}
