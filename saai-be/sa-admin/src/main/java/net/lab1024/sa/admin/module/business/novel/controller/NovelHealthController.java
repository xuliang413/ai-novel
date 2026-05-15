package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelHealthVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelHealthService;
import net.lab1024.sa.base.common.annoation.NoNeedLogin;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 小说健康检查接口。
 *
 * 用于确认 MySQL、Redis、Neo4j 三类中间件是否可用，是 M0 联调的第一步。
 */
@RestController
@RequestMapping("/novel")
@Tag(name = "AI 小说 - 健康检查")
public class NovelHealthController {

    @Resource
    private NovelHealthService novelHealthService;

    /**
     * 检查小说模块依赖的中间件状态。
     */
    @Operation(summary = "AI 小说中间件健康检查")
    @GetMapping("/health")
    @NoNeedLogin
    public ResponseDTO<NovelHealthVO> health() {
        return ResponseDTO.ok(novelHealthService.check());
    }
}
