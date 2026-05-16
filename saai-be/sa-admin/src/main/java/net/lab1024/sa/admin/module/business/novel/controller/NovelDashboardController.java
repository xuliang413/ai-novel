package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.business.novel.domain.entity.GraphChangeLogEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.WritingLogEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelIdForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.*;
import net.lab1024.sa.admin.module.business.novel.service.NovelDashboardService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工作台仪表盘接口 —— 项目总览、进度统计、待处理事项
 *
 * Controller 只做路由和参数适配，数据聚合下沉到 NovelDashboardService。
 */
@RestController
@RequestMapping("/novel/dashboard")
@Tag(name = "AI 小说 - 仪表盘")
public class NovelDashboardController {

    @Resource
    private NovelDashboardService dashboardService;

    @Operation(summary = "项目资产统计")
    @PostMapping("/asset-summary")
    public ResponseDTO<AssetSummaryVO> assetSummary(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(dashboardService.assetSummary(form.getId()));
    }

    @Operation(summary = "章节写作进度")
    @PostMapping("/chapter-progress")
    public ResponseDTO<List<ChapterProgressItemVO>> chapterProgress(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(dashboardService.chapterProgress(form.getId()));
    }

    @Operation(summary = "最近写作日志")
    @PostMapping("/recent-logs")
    public ResponseDTO<List<WritingLogEntity>> recentLogs(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(dashboardService.recentLogs(form.getId()));
    }

    @Operation(summary = "最近图谱变更")
    @PostMapping("/recent-patches")
    public ResponseDTO<List<GraphChangeLogEntity>> recentPatches(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(dashboardService.recentPatches(form.getId()));
    }

    @Operation(summary = "待处理会话")
    @PostMapping("/pending-sessions")
    public ResponseDTO<List<PendingSessionItemVO>> pendingSessions(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(dashboardService.pendingSessions(form.getId()));
    }

    @Operation(summary = "项目总字数")
    @PostMapping("/total-words")
    public ResponseDTO<TotalWordCountVO> totalWords(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(dashboardService.totalWords(form.getId()));
    }
}
