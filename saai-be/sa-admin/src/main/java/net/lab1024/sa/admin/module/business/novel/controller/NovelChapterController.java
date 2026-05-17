package net.lab1024.sa.admin.module.business.novel.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.novel.domain.form.ChapterOutlineAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.ChapterOutlineQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.ChapterOutlineUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelChapterQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelChapterUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.ChapterOutlineVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelChapterVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelChapterService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小说章节管理接口。
 * <p>
 * 负责章节列表、详情、编辑和章节细纲 CRUD，所有接口都通过 SmartAdmin 登录上下文做用户隔离。
 *
 * @Author AI-Novel
 */
@RestController
@Tag(name = AdminSwaggerTagConst.Business.NOVEL_CHAPTER)
public class NovelChapterController {

    /**
     * 小说章节服务。
     */
    @Resource
    private NovelChapterService novelChapterService;

    /**
     * 分页查询章节。
     *
     * @param queryForm 查询条件
     * @return 章节分页结果
     */
    @Operation(summary = "分页查询小说章节 @author AI-Novel")
    @PostMapping("/novel/chapter/page/query")
    @SaCheckPermission("novel:chapter:query")
    public ResponseDTO<PageResult<NovelChapterVO>> queryChapterByPage(@RequestBody @Valid NovelChapterQueryForm queryForm) {
        return novelChapterService.queryChapterByPage(queryForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询章节详情。
     *
     * @param chapterId 章节ID
     * @return 章节详情
     */
    @Operation(summary = "查询小说章节详情 @author AI-Novel")
    @GetMapping("/novel/chapter/get/{chapterId}")
    @SaCheckPermission("novel:chapter:query")
    public ResponseDTO<NovelChapterVO> getChapterDetail(@PathVariable Long chapterId) {
        return novelChapterService.getChapterDetail(chapterId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 编辑章节。
     *
     * @param updateForm 编辑表单
     * @return 编辑结果
     */
    @Operation(summary = "编辑小说章节 @author AI-Novel")
    @PostMapping("/novel/chapter/update")
    @SaCheckPermission("novel:chapter:update")
    public ResponseDTO<String> updateChapter(@RequestBody @Valid NovelChapterUpdateForm updateForm) {
        return novelChapterService.updateChapter(updateForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 分页查询章节细纲。
     *
     * @param queryForm 查询条件
     * @return 章节细纲分页结果
     */
    @Operation(summary = "分页查询章节细纲 @author AI-Novel")
    @PostMapping("/novel/chapter/outline/page/query")
    @SaCheckPermission("novel:chapter:query")
    public ResponseDTO<PageResult<ChapterOutlineVO>> queryOutlineByPage(@RequestBody @Valid ChapterOutlineQueryForm queryForm) {
        return novelChapterService.queryOutlineByPage(queryForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询章节细纲详情。
     *
     * @param outlineId 章节细纲ID
     * @return 章节细纲详情
     */
    @Operation(summary = "查询章节细纲详情 @author AI-Novel")
    @GetMapping("/novel/chapter/outline/get/{outlineId}")
    @SaCheckPermission("novel:chapter:query")
    public ResponseDTO<ChapterOutlineVO> getOutlineDetail(@PathVariable Long outlineId) {
        return novelChapterService.getOutlineDetail(outlineId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 创建章节细纲。
     *
     * @param addForm 创建表单
     * @return 创建结果
     */
    @Operation(summary = "创建章节细纲 @author AI-Novel")
    @PostMapping("/novel/chapter/outline/create")
    @SaCheckPermission("novel:chapter:add")
    public ResponseDTO<String> createOutline(@RequestBody @Valid ChapterOutlineAddForm addForm) {
        return novelChapterService.createOutline(addForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 编辑章节细纲。
     *
     * @param updateForm 编辑表单
     * @return 编辑结果
     */
    @Operation(summary = "编辑章节细纲 @author AI-Novel")
    @PostMapping("/novel/chapter/outline/update")
    @SaCheckPermission("novel:chapter:update")
    public ResponseDTO<String> updateOutline(@RequestBody @Valid ChapterOutlineUpdateForm updateForm) {
        return novelChapterService.updateOutline(updateForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 归档章节细纲。
     *
     * @param outlineId 章节细纲ID
     * @return 归档结果
     */
    @Operation(summary = "归档章节细纲 @author AI-Novel")
    @GetMapping("/novel/chapter/outline/archive/{outlineId}")
    @SaCheckPermission("novel:chapter:archive")
    public ResponseDTO<String> archiveOutline(@PathVariable Long outlineId) {
        return novelChapterService.archiveOutline(outlineId, SmartRequestUtil.getRequestUserId());
    }
}
