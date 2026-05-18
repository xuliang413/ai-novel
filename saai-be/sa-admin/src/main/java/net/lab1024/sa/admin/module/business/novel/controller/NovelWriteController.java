package net.lab1024.sa.admin.module.business.novel.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelWriteStartForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelPromptVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelWriteSessionVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelWriteService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小说写作引擎接口 —— 对接前端写作面板, 实现AI续写的完整交互闭环
 * <p>
 * 写作流程三个确认点: 上下文审阅(默认跳过,截断时强制) → 正文审阅(不可跳过) → GraphPatch审阅(不可跳过)
 * 所有接口均校验当前用户的项目归属, 单章节互斥防止并发写作。
 *
 * @Author AI-Novel
 */
@RestController
@Tag(name = AdminSwaggerTagConst.Business.NOVEL_WRITE)
public class NovelWriteController {

    @Resource
    private NovelWriteService novelWriteService;

    /**
     * 开始写作 —— 发起一章的AI续写任务（阻塞式）。
     * <p>
     * 阻塞等待LLM生成完成后返回正文, 进入CONTENT_REVIEW状态。
     * 流式模式请使用 POST /novel/write/start-stream 获取Prompt后通过WebSocket续写。
     *
     * @param form 写作请求参数(项目ID/章节号/POV/写作方向)
     * @return 写作会话(含生成正文)
     */
    @Operation(summary = "开始AI续写章节(阻塞式) @author AI-Novel")
    @PostMapping("/novel/write/start")
    @SaCheckPermission("novel:write:start")
    public ResponseDTO<NovelWriteSessionVO> startWrite(@RequestBody @Valid NovelWriteStartForm form) {
        return novelWriteService.startWrite(form, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 获取流式写作Prompt —— 组装System/User Prompt供前端通过WebSocket续写。
     * <p>
     * 返回Prompt后前端连线 ws://host/ws/novel/write?token=xxx 发送 {"action":"start","projectId":...,"chapterNo":...,"chapterGoal":"...","pov":"..."}
     *
     * @param form 写作请求参数
     * @return System Prompt + User Prompt
     */
    @Operation(summary = "获取流式写作Prompt @author AI-Novel")
    @PostMapping("/novel/write/start-stream")
    @SaCheckPermission("novel:write:start")
    public ResponseDTO<NovelPromptVO> startWriteStream(@RequestBody @Valid NovelWriteStartForm form) {
        return novelWriteService.buildWritePrompt(form, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 正文审阅通过 —— 用户确认生成内容, 触发GraphPatch抽取。
     * <p>
     * 用户可携带编辑后的正文(form.editedContent), 替代AI原始生成内容。
     * 审阅通过后进入PATCH_REVIEW状态。
     *
     * @param sessionId 写作会话ID
     * @param form 可选携带编辑后的正文
     * @return 更新后的会话状态和候选GraphPatch列表
     */
    @Operation(summary = "正文审阅通过 @author AI-Novel")
    @PostMapping("/novel/write/content-review-pass/{sessionId}")
    @SaCheckPermission("novel:write:review")
    public ResponseDTO<NovelWriteSessionVO> contentReviewPass(
            @PathVariable Long sessionId,
            @RequestBody NovelWriteStartForm form) {
        return novelWriteService.contentReviewPass(sessionId, form, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 退回正文审阅 —— 用户想放弃当前GraphPatch, 重新编辑正文或重写。
     * <p>
     * 回到CONTENT_REVIEW状态, 丢弃当前候选GraphPatch。
     *
     * @param sessionId 写作会话ID
     * @return 更新后的会话状态
     */
    @Operation(summary = "退回正文审阅(放弃当前Patch) @author AI-Novel")
    @PostMapping("/novel/write/patch-back/{sessionId}")
    @SaCheckPermission("novel:write:review")
    public ResponseDTO<NovelWriteSessionVO> patchBack(@PathVariable Long sessionId) {
        return novelWriteService.patchBack(sessionId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 确认GraphPatch并发布章节。
     * <p>
     * 用户审阅/修改后确认Patch → 白名单执行器写Neo4j → 章节发布 → 写日志。
     * Neo4j失败时标记PENDING_GRAPH_UPDATE状态, 允许稍后重试。
     *
     * @param sessionId 写作会话ID
     * @param confirmedPatches 用户确认后的Patch列表(可被编辑/剔除高风险项)
     * @return 发布结果
     */
    @Operation(summary = "确认GraphPatch并发布章节 @author AI-Novel")
    @PostMapping("/novel/write/patch-confirm/{sessionId}")
    @SaCheckPermission("novel:write:publish")
    public ResponseDTO<NovelWriteSessionVO> patchConfirm(
            @PathVariable Long sessionId,
            @RequestBody List<NovelWriteSessionVO.NovelGraphPatchVO> confirmedPatches) {
        return novelWriteService.patchConfirm(sessionId, confirmedPatches, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 撤销最近一次已执行的图谱变更。
     * <p>
     * 只撤图谱不删正文日志, 只撤全部操作不支持单条撤销。
     *
     * @param projectId 项目ID
     * @return 撤销结果
     */
    @Operation(summary = "撤销最近一次图谱变更 @author AI-Novel")
    @PostMapping("/novel/write/undo/{projectId}")
    @SaCheckPermission("novel:write:undo")
    public ResponseDTO<String> undoGraphChanges(@PathVariable Long projectId) {
        return novelWriteService.undoGraphChanges(projectId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询写作会话状态。
     * <p>
     * 用于前端轮询或WebSocket重连后获取最新状态。
     *
     * @param sessionId 写作会话ID
     * @return 会话完整状态
     */
    @Operation(summary = "查询写作会话状态 @author AI-Novel")
    @GetMapping("/novel/write/session/{sessionId}")
    public ResponseDTO<NovelWriteSessionVO> querySession(@PathVariable Long sessionId) {
        return novelWriteService.querySession(sessionId, SmartRequestUtil.getRequestUserId());
    }
}
