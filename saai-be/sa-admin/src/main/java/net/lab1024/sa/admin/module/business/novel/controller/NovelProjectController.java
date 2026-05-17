package net.lab1024.sa.admin.module.business.novel.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelProjectAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelProjectQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelProjectUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelProjectVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelProjectService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小说项目管理接口。
 * <p>
 * 这里负责承接 SmartAdmin 登录上下文，把当前用户ID交给服务层做强制数据隔离。
 *
 * @Author AI-Novel
 */
@RestController
@Tag(name = AdminSwaggerTagConst.Business.NOVEL_PROJECT)
public class NovelProjectController {

    /**
     * 小说项目服务。
     */
    @Resource
    private NovelProjectService novelProjectService;

    /**
     * 分页查询当前用户的小说项目。
     *
     * @param queryForm 查询条件
     * @return 项目分页结果
     */
    @Operation(summary = "分页查询小说项目 @author AI-Novel")
    @PostMapping("/novel/project/page/query")
    @SaCheckPermission("novel:project:query")
    public ResponseDTO<PageResult<NovelProjectVO>> queryByPage(@RequestBody @Valid NovelProjectQueryForm queryForm) {
        return novelProjectService.queryByPage(queryForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询当前用户的项目详情。
     *
     * @param projectId 项目ID
     * @return 项目详情
     */
    @Operation(summary = "查询小说项目详情 @author AI-Novel")
    @GetMapping("/novel/project/get/{projectId}")
    @SaCheckPermission("novel:project:query")
    public ResponseDTO<NovelProjectVO> getDetail(@PathVariable Long projectId) {
        return novelProjectService.getDetail(projectId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 创建当前用户的小说项目。
     *
     * @param addForm 创建表单
     * @return 创建结果
     */
    @Operation(summary = "创建小说项目 @author AI-Novel")
    @PostMapping("/novel/project/create")
    @SaCheckPermission("novel:project:add")
    public ResponseDTO<String> createProject(@RequestBody @Valid NovelProjectAddForm addForm) {
        return novelProjectService.createProject(addForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 编辑当前用户的小说项目。
     *
     * @param updateForm 编辑表单
     * @return 编辑结果
     */
    @Operation(summary = "编辑小说项目 @author AI-Novel")
    @PostMapping("/novel/project/update")
    @SaCheckPermission("novel:project:update")
    public ResponseDTO<String> updateProject(@RequestBody @Valid NovelProjectUpdateForm updateForm) {
        return novelProjectService.updateProject(updateForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 归档当前用户的小说项目。
     *
     * @param projectId 项目ID
     * @return 归档结果
     */
    @Operation(summary = "归档小说项目 @author AI-Novel")
    @GetMapping("/novel/project/archive/{projectId}")
    @SaCheckPermission("novel:project:archive")
    public ResponseDTO<String> archiveProject(@PathVariable Long projectId) {
        return novelProjectService.archiveProject(projectId, SmartRequestUtil.getRequestUserId());
    }
}
