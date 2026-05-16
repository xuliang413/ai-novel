package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelChapterQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelChapterUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelIdForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelChapterVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelChapterDetailVO;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterEntity;
import net.lab1024.sa.admin.module.business.novel.service.NovelChapterService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 章节查询接口——章节写入由写作接口统一触发。
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

    /**
     * 查询章节详情（含正文）—— 返回专用 VO 而非 Entity，保证前端契约稳定
     */
    @Operation(summary = "查询章节详情")
    @PostMapping("/detail")
    public ResponseDTO<NovelChapterDetailVO> detail(@RequestBody @Valid NovelIdForm form) {
        NovelChapterEntity entity = novelChapterService.getById(form.getId());
        if (entity == null) {
            return ResponseDTO.userErrorParam("章节不存在");
        }
        NovelChapterDetailVO vo = new NovelChapterDetailVO();
        SmartBeanUtil.copyProperties(entity, vo);
        vo.setWordCount(entity.getContent() != null ? entity.getContent().length() : 0);
        return ResponseDTO.ok(vo);
    }

    /**
     * 编辑章节元信息 —— 标题、摘要、正文、状态。
     */
    @Operation(summary = "编辑章节信息")
    @PostMapping("/update")
    public ResponseDTO<Boolean> update(@RequestBody @Valid NovelChapterUpdateForm form) {
        return novelChapterService.update(form);
    }

    /**
     * 归档章节 —— 软删除。
     */
    @Operation(summary = "归档章节")
    @PostMapping("/archive")
    public ResponseDTO<Boolean> archive(@RequestBody @Valid NovelIdForm form) {
        return novelChapterService.archive(form.getId());
    }
}
