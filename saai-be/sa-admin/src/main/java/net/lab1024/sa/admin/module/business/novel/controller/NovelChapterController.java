package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelChapterQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelChapterVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelChapterService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 小说章节接口。
 *
 * M0 只提供章节查询，章节写入由写作接口统一触发。
 */
@RestController
@RequestMapping("/novel/chapter")
@Tag(name = "AI 小说 - 章节")
public class NovelChapterController {

    @Resource
    private NovelChapterService novelChapterService;

    /**
     * 分页查询项目下的章节列表。
     */
    @Operation(summary = "分页查询小说章节")
    @PostMapping("/query")
    public ResponseDTO<PageResult<NovelChapterVO>> queryChapter(@RequestBody @Valid NovelChapterQueryForm queryForm) {
        return novelChapterService.query(queryForm);
    }
}
