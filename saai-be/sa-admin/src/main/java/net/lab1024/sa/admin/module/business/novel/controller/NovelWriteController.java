package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelContentReviewPassForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelPatchBackForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelPatchConfirmForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelUndoForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelWriteRecoverForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelWriteStartForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelChapterVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelGraphPatchVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelUndoVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelWriteDraftVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelWriteRecoverVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelWriteService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 小说写作接口。
 *
 * 写作闭环：写作启动 → 正文审阅 → 图谱变更确认 → 发布 → 撤销。
 * 流式写作走 WebSocket：ws://host/ws/novel/write?token=xxx
 */
@RestController
@RequestMapping("/novel/write")
@Tag(name = "AI 小说 - 写作")
public class NovelWriteController {

    @Resource
    private NovelWriteService novelWriteService;

    /**
     * 启动写作——自动判断用 DeepSeek 还是通义千问，都没有就用降级模式。
     */
    @Operation(summary = "启动写作")
    @PostMapping("/start")
    public ResponseDTO<NovelWriteDraftVO> start(@RequestBody @Valid NovelWriteStartForm form) {
        return novelWriteService.start(form);
    }

    /**
     * 正文审阅通过，生成待确认的图谱变更单。
     */
    @Operation(summary = "正文审阅通过并生成图谱变更单")
    @PostMapping("/content/pass")
    public ResponseDTO<NovelGraphPatchVO> passContentReview(@RequestBody @Valid NovelContentReviewPassForm form) {
        return novelWriteService.passContentReview(form);
    }

    /**
     * 确认图谱变更单，写入 Neo4j 并发布章节。
     */
    @Operation(summary = "确认图谱变更并发布章节")
    @PostMapping("/patch/confirm")
    public ResponseDTO<NovelChapterVO> confirmPatch(@RequestBody @Valid NovelPatchConfirmForm form) {
        return novelWriteService.confirmPatch(form);
    }

    /**
     * 放弃本次图谱变更，返回正文审阅。
     */
    @Operation(summary = "返回正文审阅")
    @PostMapping("/patch/back")
    public ResponseDTO<NovelWriteRecoverVO> backToContentReview(@RequestBody @Valid NovelPatchBackForm form) {
        return novelWriteService.backToContentReview(form);
    }

    /**
     * 恢复某个章节的写作状态。
     */
    @Operation(summary = "恢复写作状态")
    @PostMapping("/recover")
    public ResponseDTO<NovelWriteRecoverVO> recover(@RequestBody @Valid NovelWriteRecoverForm form) {
        return novelWriteService.recover(form);
    }

    /**
     * 撤销最近一次图谱变更。只撤图谱不删正文。
     */
    @Operation(summary = "撤销最近一次图谱变更")
    @PostMapping("/undo")
    public ResponseDTO<NovelUndoVO> undo(@RequestBody @Valid NovelUndoForm form) {
        return novelWriteService.undo(form);
    }

    /**
     * 流式写作入口——走 WebSocket，不在 HTTP 里生成。
     */
    @Operation(summary = "WebSocket 流式写作")
    @PostMapping("/start/stream")
    public ResponseDTO<String> startStream() {
        return ResponseDTO.ok("请使用 WebSocket 连接：ws://localhost:11024/ws/novel/write?token=<your_token>");
    }
}
