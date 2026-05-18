package net.lab1024.sa.admin.module.business.novel.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.novel.dao.NovelProjectDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelCharacterNetworkVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelClueHistoryVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelGraphOverviewVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelGraphService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 小说图谱查询接口 —— 项目概览/角色关系网/线索推进历史。
 * <p>
 * 所有查询入口先通过MySQL校验项目归属, 再委托NovelGraphService执行Neo4j只读Cypher并直接返回VO。
 *
 * @Author AI-Novel
 */
@RestController
@Tag(name = AdminSwaggerTagConst.Business.NOVEL_GRAPH)
public class NovelGraphController {

    @Resource
    private NovelGraphService novelGraphService;

    @Resource
    private NovelProjectDao novelProjectDao;

    /**
     * 获取项目图谱概览 —— 各类型节点数量和关系统计。
     */
    @Operation(summary = "获取项目图谱概览 @author AI-Novel")
    @GetMapping("/novel/graph/overview/{projectId}")
    @SaCheckPermission("novel:graph:query")
    public ResponseDTO<NovelGraphOverviewVO> getGraphOverview(@PathVariable Long projectId) {
        Long userId = SmartRequestUtil.getRequestUserId();
        ResponseDTO<String> validateResult = validateProjectOwnership(projectId, userId);
        if (!validateResult.getOk()) {
            return ResponseDTO.error(validateResult);
        }
        return ResponseDTO.ok(novelGraphService.queryGraphOverview(projectId));
    }

    /**
     * 获取角色关系网 —— nodes + edges 结构, 前端 @antv/g6 直接渲染。
     */
    @Operation(summary = "获取角色关系网 @author AI-Novel")
    @GetMapping("/novel/graph/character-network/{projectId}")
    @SaCheckPermission("novel:graph:query")
    public ResponseDTO<NovelCharacterNetworkVO> getCharacterNetwork(@PathVariable Long projectId) {
        Long userId = SmartRequestUtil.getRequestUserId();
        ResponseDTO<String> validateResult = validateProjectOwnership(projectId, userId);
        if (!validateResult.getOk()) {
            return ResponseDTO.error(validateResult);
        }
        return ResponseDTO.ok(novelGraphService.queryCharacterNetwork(projectId));
    }

    /**
     * 获取线索推进历史 —— 指定线索被哪些章节推进的时间线。
     */
    @Operation(summary = "获取线索推进历史 @author AI-Novel")
    @GetMapping("/novel/graph/clue-history/{projectId}/{clueId}")
    @SaCheckPermission("novel:graph:query")
    public ResponseDTO<NovelClueHistoryVO> getClueHistory(@PathVariable Long projectId, @PathVariable Long clueId) {
        Long userId = SmartRequestUtil.getRequestUserId();
        ResponseDTO<String> validateResult = validateProjectOwnership(projectId, userId);
        if (!validateResult.getOk()) {
            return ResponseDTO.error(validateResult);
        }
        return ResponseDTO.ok(novelGraphService.queryClueHistory(projectId, clueId));
    }

    /**
     * 通过MySQL校验当前用户是否拥有该项目。
     */
    private ResponseDTO<String> validateProjectOwnership(Long projectId, Long userId) {
        if (userId == null) {
            return ResponseDTO.userErrorParam("未获取到当前登录用户");
        }
        NovelProjectEntity project = novelProjectDao.selectById(projectId);
        if (project == null || !Objects.equals(project.getCreateUserId(), userId)) {
            return ResponseDTO.userErrorParam("项目不存在或无权访问");
        }
        return ResponseDTO.ok();
    }
}
