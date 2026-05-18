package net.lab1024.sa.admin.module.business.novel.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelDashboardVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelDashboardService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小说仪表盘接口 —— 项目统计概览。
 * <p>
 * 提供项目级字数/章节/角色/线索统计, 全部通过MySQL COUNT聚合查询。
 * 所有查询强制过滤 create_user_id 实现用户隔离。
 *
 * @Author AI-Novel
 */
@RestController
@Tag(name = AdminSwaggerTagConst.Business.NOVEL_DASHBOARD)
public class NovelDashboardController {

    @Resource
    private NovelDashboardService novelDashboardService;

    /**
     * 获取项目仪表盘统计数据。
     *
     * @param projectId 项目ID
     * @return 仪表盘VO, 包含字数/章节数/角色数/线索数/地点数
     */
    @Operation(summary = "获取项目仪表盘统计 @author AI-Novel")
    @GetMapping("/novel/dashboard/{projectId}")
    @SaCheckPermission("novel:dashboard:query")
    public ResponseDTO<NovelDashboardVO> getDashboard(@PathVariable Long projectId) {
        return novelDashboardService.getDashboard(projectId, SmartRequestUtil.getRequestUserId());
    }
}
