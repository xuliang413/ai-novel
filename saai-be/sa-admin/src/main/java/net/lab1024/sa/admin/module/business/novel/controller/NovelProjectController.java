package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelIdForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelProjectAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelProjectQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelProjectUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelProjectVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelProjectService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目接口——角色、地点、线索、章节都挂在项目下。
 */
@RestController
@RequestMapping("/novel/project")
@Tag(name = "AI 小说 - 项目")
public class NovelProjectController {

    @Resource
    private NovelProjectService novelProjectService;

    /**
     * 新建小说项目，并同步创建 Neo4j Project 节点。
     */
    @Operation(summary = "新建小说项目")
    @PostMapping("/add")
    public ResponseDTO<Long> addProject(@RequestBody @Valid NovelProjectAddForm addForm) {
        return novelProjectService.add(addForm);
    }

    /**
     * 分页查询小说项目列表。
     */
    @Operation(summary = "分页查询小说项目")
    @PostMapping("/query")
    public ResponseDTO<PageResult<NovelProjectVO>> queryProject(@RequestBody @Valid NovelProjectQueryForm queryForm) {
        return novelProjectService.query(queryForm);
    }

    /**
     * 查询项目详情。
     */
    @Operation(summary = "查询项目详情")
    @PostMapping("/detail")
    public ResponseDTO<NovelProjectEntity> detail(@RequestBody @Valid NovelIdForm form) {
        return novelProjectService.detail(form.getId());
    }

    /**
     * 编辑项目 —— 项目名称、类型、简介、主角、目标字数。
     *
     * 注意：不通过编辑接口修改 status，归档单独走 /archive。
     */
    @Operation(summary = "编辑项目信息")
    @PostMapping("/update")
    public ResponseDTO<Boolean> update(@RequestBody @Valid NovelProjectUpdateForm form) {
        return novelProjectService.update(form);
    }

    /**
     * 归档项目 —— 软删除，标记状态为 ARCHIVED。
     */
    @Operation(summary = "归档项目")
    @PostMapping("/archive")
    public ResponseDTO<Boolean> archive(@RequestBody @Valid NovelIdForm form) {
        return novelProjectService.archive(form.getId());
    }
}
