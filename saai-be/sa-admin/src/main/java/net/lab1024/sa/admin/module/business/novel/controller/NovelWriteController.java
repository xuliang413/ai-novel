package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelContentReviewPassForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelPatchBackForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelPatchConfirmForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelUndoForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelWriteMockForm;
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
 * M0 保留 mock 写作链路；M1 补齐安全写作闭环的最小接口。
 */
@RestController
@RequestMapping("/novel/write")
@Tag(name = "AI 小说 - 写作")
public class NovelWriteController {

    @Resource
    private NovelWriteService novelWriteService;

    /**
     * 模拟生成一章草稿，并保存章节与生成会话。
     */
    @Operation(summary = "模拟生成小说章节草稿")
    @PostMapping("/mock")
    public ResponseDTO<NovelChapterVO> writeMock(@RequestBody @Valid NovelWriteMockForm form) {
        return novelWriteService.writeMock(form);
    }

    /**
     * 启动 M1 mock 写作，返回 ChapterIntent、ContextPreview 与待审阅草稿。
     */
    @Operation(summary = "M1 启动 mock 写作")
    @PostMapping("/start")
    public ResponseDTO<NovelWriteDraftVO> startMock(@RequestBody @Valid NovelWriteStartForm form) {
        return novelWriteService.startMock(form);
    }

    /**
     * 正文审阅通过，并生成待确认 GraphPatch。
     */
    @Operation(summary = "正文审阅通过并生成 GraphPatch")
    @PostMapping("/content/pass")
    public ResponseDTO<NovelGraphPatchVO> passContentReview(@RequestBody @Valid NovelContentReviewPassForm form) {
        return novelWriteService.passContentReview(form);
    }

    /**
     * 用户确认 GraphPatch 后写入 Neo4j 并发布正文。
     */
    @Operation(summary = "确认 GraphPatch 并发布章节")
    @PostMapping("/patch/confirm")
    public ResponseDTO<NovelChapterVO> confirmPatch(@RequestBody @Valid NovelPatchConfirmForm form) {
        return novelWriteService.confirmPatch(form);
    }

    /**
     * 放弃候选 GraphPatch，返回正文审阅。
     */
    @Operation(summary = "返回正文审阅")
    @PostMapping("/patch/back")
    public ResponseDTO<NovelWriteRecoverVO> backToContentReview(@RequestBody @Valid NovelPatchBackForm form) {
        return novelWriteService.backToContentReview(form);
    }

    /**
     * 恢复最近一次或指定章节的写作状态。
     */
    @Operation(summary = "恢复写作状态")
    @PostMapping("/recover")
    public ResponseDTO<NovelWriteRecoverVO> recover(@RequestBody @Valid NovelWriteRecoverForm form) {
        return novelWriteService.recover(form);
    }

    /**
     * 撤销最近一次已确认图谱变更。
     */
    @Operation(summary = "撤销最近一次图谱变更")
    @PostMapping("/undo")
    public ResponseDTO<NovelUndoVO> undo(@RequestBody @Valid NovelUndoForm form) {
        return novelWriteService.undo(form);
    }
}
