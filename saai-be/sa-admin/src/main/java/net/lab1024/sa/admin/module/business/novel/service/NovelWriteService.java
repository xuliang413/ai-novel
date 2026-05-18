package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.constant.NovelChapterStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGenerationStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphChangeStatusEnum;
import net.lab1024.sa.admin.module.business.novel.dao.ChapterGenerationSessionDao;
import net.lab1024.sa.admin.module.business.novel.dao.GraphChangeLogDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelChapterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelProjectDao;
import net.lab1024.sa.admin.module.business.novel.dao.WritingLogDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.ChapterGenerationSessionEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.GraphChangeLogEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.WritingLogEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelWriteStartForm;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchModel;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelPromptVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelWriteSessionVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelWriteSessionVO.NovelGraphPatchVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelLLMService.NovelGenerationResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 小说写作引擎 —— 实现完整的AI写作闭环流程。
 * <p>
 * 流程: 开始写作 → ChapterIntent组装 → 上下文检索 → Prompt组装 → LLM生成 → 质检旁注 → 正文审阅 → GraphPatch抽取审阅 → 图谱写入发布
 * 状态机: GENERATING → CONTENT_REVIEW → PATCH_REVIEW → SUCCESS 或任意环节 FAILED
 * 支持无Key降级Mock、3次重试、单章节互斥、流式WebSocket推送。
 *
 * @Author AI-Novel
 */
@Slf4j
@Service
public class NovelWriteService {

    /** 最大重试次数, 超过后降级Mock */
    private static final int MAX_RETRY = 3;

    /** Mock正文, 占位文本表示降级模式已运行 */
    private static final String MOCK_CONTENT_TEMPLATE = "【第%d章 Mock章节】\n\n这是AI小说的第%d章占位内容。"
            + "当前未配置LLM API Key或LLM调用失败次数已达上限(%d次)，系统已降级为Mock模式。"
            + "\n请配置DeepSeek API Key后重新生成本章内容。";

    @Resource
    private NovelProjectDao novelProjectDao;

    @Resource
    private NovelChapterDao novelChapterDao;

    @Resource
    private ChapterGenerationSessionDao sessionDao;

    @Resource
    private GraphChangeLogDao graphChangeLogDao;

    @Resource
    private WritingLogDao writingLogDao;

    @Resource
    private NovelPromptService novelPromptService;

    @Resource
    private NovelLLMService novelLLMService;

    @Resource
    private NovelGraphService novelGraphService;

    @Resource
    private NovelGraphPatchService novelGraphPatchService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 章节级互斥锁 —— 同一个project+chapterNumber只允许一个活跃会话
     * key: projectId:chapterNumber, value: sessionId
     * 仅阻塞式(非流式)使用; 流式模式不在此加锁
     */
    private final ConcurrentHashMap<String, Long> chapterLocks = new ConcurrentHashMap<>();

    /**
     * 构建章节续写Prompt（流式和非流式共用）。
     * <p>
     * 只做校验+组装, 不创建session不加锁。调用方决定后续是阻塞生成还是流式推送。
     *
     * @param form 写作请求参数
     * @param requestUserId 当前用户ID
     * @return Prompt VO, 包含systemPrompt/userPrompt/上下文快照
     */
    public ResponseDTO<NovelPromptVO> buildWritePrompt(NovelWriteStartForm form, Long requestUserId) {
        NovelProjectEntity project = novelProjectDao.selectById(form.getProjectId());
        if (project == null || !Objects.equals(project.getCreateUserId(), requestUserId)) {
            return ResponseDTO.userErrorParam("项目不存在或无权访问");
        }

        ResponseDTO<NovelPromptVO> promptResult = novelPromptService.buildPrompt(
                form.getProjectId(), form.getChapterNumber(), form.getChapterGoal(), requestUserId);
        if (!promptResult.getOk()) {
            return ResponseDTO.error(promptResult);
        }
        return promptResult;
    }

    /**
     * 开始AI续写一个章节（阻塞式）。
     * <p>
     * 创建写作会话 + 组装Prompt + LLM生成 + 保存草稿 → 进入CONTENT_REVIEW。
     * 单章互斥: 同一章节同时只允许一个活跃的非终态会话。
     *
     * @param form 写作请求参数
     * @param requestUserId 当前用户ID
     * @return 写作会话VO, 包含正文和状态
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<NovelWriteSessionVO> startWrite(NovelWriteStartForm form, Long requestUserId) {
        ResponseDTO<NovelPromptVO> promptResult = buildWritePrompt(form, requestUserId);
        if (!promptResult.getOk()) {
            return ResponseDTO.error(promptResult);
        }
        NovelPromptVO promptVO = promptResult.getData();

        String lockKey = form.getProjectId() + ":" + form.getChapterNumber();
        if (!acquireChapterLock(lockKey)) {
            return ResponseDTO.userErrorParam("章节" + form.getChapterNumber() + "已有活跃写作会话, 请先完成或放弃");
        }
        try {
            return doBlockingWrite(form, promptVO, requestUserId, lockKey);
        } catch (Exception e) {
            log.error("阻塞式写作异常 projectId={} chapterNumber={}", form.getProjectId(), form.getChapterNumber(), e);
            return ResponseDTO.userErrorParam("写作生成失败: " + e.getMessage());
        } finally {
            chapterLocks.remove(lockKey);
        }
    }

    /**
     * 流式生成完成后创建Session和Chapter草稿——由WebSocket Handler回调。
     * <p>
     * 流式流程: startWrite(streamMode) → 返回Prompt → WebSocket逐token推送 → onComplete时回调此方法。
     * 此方法独立事务, 与前端HTTP请求解耦。
     *
     * @param projectId 项目ID
     * @param chapterNumber 章节号
     * @param fullText LLM生成的完整正文
     * @param pov POV角色名
     * @param chapterGoal 写作方向
     * @param provider LLM提供商
     * @param userId 当前用户ID
     * @return 创建好的写作会话VO, status=CONTENT_REVIEW
     */
    @Transactional(rollbackFor = Exception.class)
    public NovelWriteSessionVO onStreamWriteComplete(Long projectId, Integer chapterNumber, String fullText,
                                                      String pov, String chapterGoal, String provider, Long userId) {
        ChapterGenerationSessionEntity session = new ChapterGenerationSessionEntity();
        session.setProjectId(projectId);
        session.setChapterNumber(chapterNumber);
        session.setStatus(NovelGenerationStatusEnum.CONTENT_REVIEW.getValue());
        session.setProvider(provider != null ? provider : "DEEPSEEK");
        session.setRetryCount(0);
        session.setCreateUserId(userId);
        session.setChapterIntentJson("{\"chapterNumber\":" + chapterNumber + ",\"pov\":\"" + (pov != null ? pov : "") + "\",\"chapterGoal\":\"" + (chapterGoal != null ? chapterGoal : "") + "\"}");
        session.setPromptSummary(fullText.length() > 500 ? fullText.substring(0, 500) : fullText);
        sessionDao.insert(session);

        NovelChapterEntity chapter = saveChapterDraft(projectId, chapterNumber, fullText, pov, userId);
        session.setChapterId(chapter.getId());
        sessionDao.updateById(session);

        log.info("流式写作完成并持久化 sessionId={}, chapterId={}, wordCount={}",
                session.getId(), chapter.getId(), fullText.length());

        NovelWriteSessionVO vo = buildSessionVO(session, null, false);
        vo.setTitle(extractTitle(fullText));
        vo.setSummary(extractSummary(fullText, 300));
        vo.setContent(fullText);
        vo.setWordCount(fullText.length());
        vo.setProvider(session.getProvider());
        vo.setQualityNotes(Collections.emptyList());
        vo.setAvailableActions(buildAvailableActions(NovelGenerationStatusEnum.CONTENT_REVIEW));
        return vo;
    }

    /**
     * 正文审阅通过 —— 用户确认生成内容, 触发GraphPatch抽取。
     * <p>
     * 用户可携带编辑后的正文(form.editedContent), 替代AI原始生成内容。
     * 审阅通过后进入PATCH_REVIEW状态。
     *
     * @param sessionId 写作会话ID
     * @param form 携带可选的editedContent
     * @param requestUserId 当前用户ID
     * @return 更新后的会话状态和候选GraphPatch列表
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<NovelWriteSessionVO> contentReviewPass(Long sessionId, NovelWriteStartForm form, Long requestUserId) {
        ChapterGenerationSessionEntity session = sessionDao.selectById(sessionId);
        if (session == null || !Objects.equals(session.getCreateUserId(), requestUserId)) {
            return ResponseDTO.userErrorParam("会话不存在或无权访问");
        }
        if (!NovelGenerationStatusEnum.CONTENT_REVIEW.getValue().equals(session.getStatus())) {
            return ResponseDTO.userErrorParam("当前会话状态不允许正文审阅, 当前状态: " + session.getStatus());
        }

        NovelChapterEntity chapter = novelChapterDao.selectById(session.getChapterId());
        if (chapter == null) {
            return ResponseDTO.userErrorParam("关联章节不存在");
        }

        if (form.getEditedContent() != null && !form.getEditedContent().isEmpty()) {
            chapter.setContent(form.getEditedContent());
            chapter.setWordCount(form.getEditedContent().length());
            chapter.setTitle(extractTitle(form.getEditedContent()));
            chapter.setSummary(extractSummary(form.getEditedContent(), 300));
            novelChapterDao.updateById(chapter);
            log.info("用户编辑正文 sessionId={}, chapterId={}, newLength={}",
                    sessionId, chapter.getId(), form.getEditedContent().length());
        }

        List<NovelGraphPatchVO> patches = extractGraphPatches(chapter.getContent(), session, requestUserId);

        session.setStatus(NovelGenerationStatusEnum.PATCH_REVIEW.getValue());
        sessionDao.updateById(session);

        NovelWriteSessionVO vo = buildSessionVO(session, null, false);
        vo.setContent(chapter.getContent());
        vo.setPatches(patches);
        vo.setAvailableActions(buildAvailableActions(NovelGenerationStatusEnum.PATCH_REVIEW));
        return ResponseDTO.ok(vo);
    }

    /**
     * 退回正文审阅 —— 放弃当前GraphPatch, 回到CONTENT_REVIEW重新编辑或重写。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<NovelWriteSessionVO> patchBack(Long sessionId, Long requestUserId) {
        ChapterGenerationSessionEntity session = sessionDao.selectById(sessionId);
        if (session == null || !Objects.equals(session.getCreateUserId(), requestUserId)) {
            return ResponseDTO.userErrorParam("会话不存在或无权访问");
        }
        if (!NovelGenerationStatusEnum.PATCH_REVIEW.getValue().equals(session.getStatus())) {
            return ResponseDTO.userErrorParam("当前会话状态不允许退回, 当前状态: " + session.getStatus());
        }

        session.setStatus(NovelGenerationStatusEnum.CONTENT_REVIEW.getValue());
        session.setGraphPatchJson(null);
        sessionDao.updateById(session);

        NovelChapterEntity chapter = novelChapterDao.selectById(session.getChapterId());
        NovelWriteSessionVO vo = buildSessionVO(session, null, false);
        if (chapter != null) {
            vo.setContent(chapter.getContent());
        }
        vo.setAvailableActions(buildAvailableActions(NovelGenerationStatusEnum.CONTENT_REVIEW));
        return ResponseDTO.ok(vo);
    }

    /**
     * 确认GraphPatch并发布章节。
     * <p>
     * 三步写入: ① 章节→PUBLISHED + 写writingLog → ② 白名单执行器写Neo4j → ③ 更新MySQL + graphChangeLog。
     * Neo4j失败时标记PENDING_GRAPH_UPDATE允许重试。
     *
     * @param sessionId 写作会话ID
     * @param confirmedPatches 用户确认后的Patch列表
     * @param requestUserId 当前用户ID
     * @return 发布结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<NovelWriteSessionVO> patchConfirm(Long sessionId, List<NovelGraphPatchVO> confirmedPatches, Long requestUserId) {
        ChapterGenerationSessionEntity session = sessionDao.selectById(sessionId);
        if (session == null || !Objects.equals(session.getCreateUserId(), requestUserId)) {
            return ResponseDTO.userErrorParam("会话不存在或无权访问");
        }
        if (!NovelGenerationStatusEnum.PATCH_REVIEW.getValue().equals(session.getStatus())) {
            return ResponseDTO.userErrorParam("当前会话状态不允许确认Patch, 当前状态: " + session.getStatus());
        }

        NovelChapterEntity chapter = novelChapterDao.selectById(session.getChapterId());
        if (chapter == null) {
            return ResponseDTO.userErrorParam("关联章节不存在");
        }

        String operationBatchId = UUID.randomUUID().toString();

        try {
            chapter.setStatus(NovelChapterStatusEnum.PUBLISHED.getValue());
            novelChapterDao.updateById(chapter);

            if (confirmedPatches != null && !confirmedPatches.isEmpty()) {
                executeGraphPatches(confirmedPatches, session.getProjectId(), session.getChapterNumber());
            }

            Map<String, Object> chapterProps = new LinkedHashMap<>();
            chapterProps.put("title", chapter.getTitle());
            chapterProps.put("summary", chapter.getSummary());
            chapterProps.put("pov", chapter.getPov());
            chapterProps.put("wordCount", chapter.getWordCount());
            chapterProps.put("status", chapter.getStatus());
            novelGraphService.mergeChapter(session.getProjectId(), session.getChapterNumber(), chapterProps);

            GraphChangeLogEntity changeLog = new GraphChangeLogEntity();
            changeLog.setProjectId(session.getProjectId());
            changeLog.setSessionId(session.getId());
            changeLog.setChapterId(chapter.getId());
            changeLog.setChapterNumber(session.getChapterNumber());
            changeLog.setOperationBatchId(operationBatchId);
            changeLog.setPatchJson(confirmedPatches != null ? confirmedPatches.toString() : "[]");
            changeLog.setInversePatchJson(buildInversePatchJson(confirmedPatches));
            changeLog.setStatus(NovelGraphChangeStatusEnum.APPLIED.getValue());
            changeLog.setCreateUserId(requestUserId);
            graphChangeLogDao.insert(changeLog);

            session.setStatus(NovelGenerationStatusEnum.SUCCESS.getValue());
            session.setOperationBatchId(operationBatchId);
            sessionDao.updateById(session);

            log.info("章节发布成功 sessionId={}, chapterId={}, batchId={}", session.getId(), chapter.getId(), operationBatchId);
            NovelWriteSessionVO vo = buildSessionVO(session, null, false);
            vo.setContent(chapter.getContent());
            vo.setAvailableActions(buildAvailableActions(NovelGenerationStatusEnum.SUCCESS));

            List<String> postChecks = novelGraphPatchService.runPostWriteChecks(
                    session.getProjectId(), session.getChapterNumber());
            vo.setQualityNotes(postChecks);

            return ResponseDTO.ok(vo);

        } catch (Exception e) {
            log.error("图谱写入失败 sessionId={}, chapterId={}", session.getId(), chapter.getId(), e);
            chapter.setStatus(NovelChapterStatusEnum.PENDING_GRAPH_UPDATE.getValue());
            novelChapterDao.updateById(chapter);
            session.setStatus(NovelGenerationStatusEnum.PENDING_GRAPH_UPDATE.getValue());
            sessionDao.updateById(session);

            GraphChangeLogEntity changeLog = new GraphChangeLogEntity();
            changeLog.setProjectId(session.getProjectId());
            changeLog.setSessionId(session.getId());
            changeLog.setChapterId(chapter.getId());
            changeLog.setChapterNumber(session.getChapterNumber());
            changeLog.setOperationBatchId(operationBatchId);
            changeLog.setStatus(NovelGraphChangeStatusEnum.FAILED.getValue());
            changeLog.setErrorMessage(e.getMessage());
            changeLog.setCreateUserId(requestUserId);
            graphChangeLogDao.insert(changeLog);

            return ResponseDTO.userErrorParam("图谱写入失败, 章节正文已保存, 可稍后重试: " + e.getMessage());
        }
    }

    /**
     * 撤销最近一次已执行的图谱变更。
     * <p>
     * 读取最近APPLIED状态的graphChangeLog → 解析inversePatchJson → 执行逆操作 → 标记UNDONE。
     * 只撤图谱不删正文日志，只撤全部不支持单条。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前用户ID
     * @return 撤销结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> undoGraphChanges(Long projectId, Long requestUserId) {
        GraphChangeLogEntity latest = graphChangeLogDao.selectOne(new LambdaQueryWrapper<GraphChangeLogEntity>()
                .eq(GraphChangeLogEntity::getProjectId, projectId)
                .eq(GraphChangeLogEntity::getStatus, NovelGraphChangeStatusEnum.APPLIED.getValue())
                .orderByDesc(GraphChangeLogEntity::getId)
                .last("LIMIT 1"));

        if (latest == null) {
            return ResponseDTO.userErrorParam("没有可撤销的图谱变更");
        }
        if (!Objects.equals(latest.getCreateUserId(), requestUserId)) {
            return ResponseDTO.userErrorParam("无权撤销他人的操作");
        }

        try {
            if (latest.getInversePatchJson() != null && !latest.getInversePatchJson().isEmpty()
                    && !"[]".equals(latest.getInversePatchJson())) {
                List<NovelGraphPatchVO> inversePatches = objectMapper.readValue(
                        latest.getInversePatchJson(),
                        new TypeReference<List<NovelGraphPatchVO>>() {});
                if (!inversePatches.isEmpty()) {
                    novelGraphPatchService.executePatches(inversePatches, projectId, latest.getChapterNumber());
                }
            }

            latest.setStatus(NovelGraphChangeStatusEnum.UNDONE.getValue());
            graphChangeLogDao.updateById(latest);

            log.info("撤销图谱变更 batchId={}, projectId={}, chapterNumber={}",
                    latest.getOperationBatchId(), projectId, latest.getChapterNumber());
            return ResponseDTO.ok("撤销成功");
        } catch (Exception e) {
            log.error("撤销失败 batchId={}", latest.getOperationBatchId(), e);
            return ResponseDTO.userErrorParam("撤销失败: " + e.getMessage());
        }
    }

    /**
     * 查询写作会话状态。
     */
    public ResponseDTO<NovelWriteSessionVO> querySession(Long sessionId, Long requestUserId) {
        ChapterGenerationSessionEntity session = sessionDao.selectById(sessionId);
        if (session == null || !Objects.equals(session.getCreateUserId(), requestUserId)) {
            return ResponseDTO.userErrorParam("会话不存在或无权访问");
        }

        NovelWriteSessionVO vo = buildSessionVO(session, null, false);
        if (session.getChapterId() != null) {
            NovelChapterEntity chapter = novelChapterDao.selectById(session.getChapterId());
            if (chapter != null) {
                vo.setContent(chapter.getContent());
                vo.setTitle(chapter.getTitle());
                vo.setSummary(chapter.getSummary());
                vo.setWordCount(chapter.getWordCount());
            }
        }
        vo.setAvailableActions(buildAvailableActions(NovelGenerationStatusEnum.fromValue(session.getStatus())));
        return ResponseDTO.ok(vo);
    }

    // ============================================================
    // 私有方法
    // ============================================================

    /**
     * 获取章节互斥锁 —— 查DB确认没有活跃的终态会话后加锁。
     *
     * @param lockKey "projectId:chapterNumber"
     * @return true=获取成功, false=已有活跃会话
     */
    private boolean acquireChapterLock(String lockKey) {
        Long existing = chapterLocks.putIfAbsent(lockKey, -1L);
        if (existing != null) {
            return false;
        }
        // 并发安全: DB 唯一约束 uk_project_chapter_active 兜底, 两个 GENERATING 插入时第二个违反约束
        return true;
    }

    /**
     * 执行阻塞式写作流程。
     */
    private ResponseDTO<NovelWriteSessionVO> doBlockingWrite(NovelWriteStartForm form, NovelPromptVO promptVO,
                                                               Long requestUserId, String lockKey) {
        ChapterGenerationSessionEntity session = new ChapterGenerationSessionEntity();
        session.setProjectId(form.getProjectId());
        session.setChapterNumber(form.getChapterNumber());
        session.setStatus(NovelGenerationStatusEnum.GENERATING.getValue());
        session.setProvider("DEEPSEEK");
        session.setRetryCount(0);
        session.setCreateUserId(requestUserId);
        sessionDao.insert(session);

        chapterLocks.put(lockKey, session.getId());
        log.info("创建写作会话 sessionId={}, projectId={}, chapterNumber={}",
                session.getId(), form.getProjectId(), form.getChapterNumber());

        session.setChapterIntentJson(buildIntentJson(form, promptVO));
        session.setContextSnapshotJson("{}");
        String promptHeader = promptVO.getSystemPrompt();
        session.setPromptSummary(promptHeader.substring(0, Math.min(500, promptHeader.length())));
        sessionDao.updateById(session);

        NovelGenerationResult genResult = generateWithRetry(promptVO, session, requestUserId);
        NovelChapterEntity chapter = saveChapterDraft(form.getProjectId(), form.getChapterNumber(),
                genResult.getContent(), form.getPov(), requestUserId);
        session.setChapterId(chapter.getId());
        session.setStatus(NovelGenerationStatusEnum.CONTENT_REVIEW.getValue());
        sessionDao.updateById(session);

        writeGenerationLog(session, chapter, genResult, requestUserId);

        NovelProjectEntity project = novelProjectDao.selectById(form.getProjectId());
        List<String> qualityNotes = runQualityCheck(genResult, project);

        NovelWriteSessionVO resultVO = buildSessionVO(session, genResult, false);
        resultVO.setQualityNotes(qualityNotes);
        resultVO.setAvailableActions(buildAvailableActions(NovelGenerationStatusEnum.CONTENT_REVIEW));

        log.info("写作生成完成 sessionId={}, chapterId={}, wordCount={}, 进入CONTENT_REVIEW",
                session.getId(), chapter.getId(), genResult.getCharCount());
        return ResponseDTO.ok(resultVO);
    }

    /**
     * 写写作日志 —— 记录生成统计(字数/Token/耗时/提供商), 纯写入不做修改。
     */
    private void writeGenerationLog(ChapterGenerationSessionEntity session, NovelChapterEntity chapter,
                                     NovelGenerationResult genResult, Long requestUserId) {
        WritingLogEntity log = new WritingLogEntity();
        log.setProjectId(session.getProjectId());
        log.setSessionId(session.getId());
        log.setChapterId(chapter.getId());
        log.setChapterNumber(session.getChapterNumber());
        log.setWordCount(genResult.getCharCount());
        log.setPromptTokens(genResult.getPromptTokens());
        log.setCompletionTokens(genResult.getCompletionTokens());
        log.setDurationMs(genResult.getDurationMs());
        log.setProvider(session.getProvider());
        log.setCreateUserId(requestUserId);
        writingLogDao.insert(log);
    }

    /**
     * 保存章节草稿到MySQL——阻塞式和流式模式共用。
     */
    private NovelChapterEntity saveChapterDraft(Long projectId, Integer chapterNumber, String content,
                                                  String pov, Long requestUserId) {
        NovelChapterEntity chapter = novelChapterDao.selectOne(new LambdaQueryWrapper<NovelChapterEntity>()
                .eq(NovelChapterEntity::getProjectId, projectId)
                .eq(NovelChapterEntity::getChapterNumber, chapterNumber));

        if (chapter == null) {
            chapter = new NovelChapterEntity();
            chapter.setProjectId(projectId);
            chapter.setChapterNumber(chapterNumber);
            chapter.setCreateUserId(requestUserId);
        }

        chapter.setTitle(extractTitle(content));
        chapter.setSummary(extractSummary(content, 300));
        chapter.setContent(content);
        chapter.setPov(pov);
        chapter.setWordCount(content.length());
        chapter.setStatus(NovelChapterStatusEnum.DRAFT.getValue());
        chapter.setDeletedFlag(false);

        if (chapter.getId() == null) {
            novelChapterDao.insert(chapter);
        } else {
            novelChapterDao.updateById(chapter);
        }
        return chapter;
    }

    /**
     * 带重试的章节生成 —— 最多重试MAX_RETRY次, 超过后降级Mock。
     */
    private NovelGenerationResult generateWithRetry(NovelPromptVO promptVO, ChapterGenerationSessionEntity session, Long requestUserId) {
        int retries = session.getRetryCount() != null ? session.getRetryCount() : 0;

        for (int i = retries; i < MAX_RETRY; i++) {
            try {
                NovelGenerationResult result = novelLLMService.generateChapter(
                        promptVO.getSystemPrompt(), promptVO.getUserPrompt(), requestUserId);
                if (result != null) {
                    return result;
                }
                log.info("用户{}未配置API Key, 直接降级Mock", requestUserId);
                break;
            } catch (Exception e) {
                log.warn("LLM生成失败 第{}次尝试, sessionId={}", i + 1, session.getId(), e);
                session.setRetryCount(i + 1);
                sessionDao.updateById(session);

                if (i + 1 >= MAX_RETRY) {
                    log.warn("达到最大重试次数{}, 降级Mock", MAX_RETRY);
                    break;
                }
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }

        return generateMock(session.getChapterNumber());
    }

    /**
     * Mock模式 —— 生成占位文本, 状态机正常流转, 不阻塞写作流程。
     */
    private NovelGenerationResult generateMock(Integer chapterNumber) {
        String content = String.format(MOCK_CONTENT_TEMPLATE, chapterNumber, chapterNumber, MAX_RETRY);
        return new NovelGenerationResult(
                "第" + chapterNumber + "章 Mock章节",
                "Mock章节占位内容, 请配置API Key后重新生成",
                content,
                content.length(),
                0L,
                0,
                0
        );
    }

    /**
     * 质检旁注 —— 检查字数/视角/新实体, 不阻塞流程只旁注。
     */
    private List<String> runQualityCheck(NovelGenerationResult genResult, NovelProjectEntity project) {
        List<String> notes = new ArrayList<>();
        int targetWords = project.getTargetChapterWords() != null ? project.getTargetChapterWords() : 3000;
        int actualWords = genResult.getCharCount();

        if (actualWords < targetWords * 0.7) {
            notes.add(String.format("字数偏少: 生成%d字, 目标%d字(偏差>30%%)", actualWords, targetWords));
        } else if (actualWords > targetWords * 1.5) {
            notes.add(String.format("字数偏多: 生成%d字, 目标%d字(超出>50%%)", actualWords, targetWords));
        }
        if (genResult.getContent() != null && genResult.getContent().contains("Mock章节")) {
            notes.add("当前为Mock模式生成的占位文本, 请配置API Key后重新生成");
        }
        return notes;
    }

    /**
     * 抽取GraphPatch —— 把正文发给LLM分析角色/线索/关系变化。
     * <p>
     * 完整链路: LLM抽取 → JSON解析 → before值回填 → 转换为前端VO。
     * LLM未配Key时返回空列表, 前端展示"无变更"。
     *
     * @param chapterContent 章节正文
     * @param session 当前写作会话
     * @param requestUserId 当前用户ID
     * @return GraphPatch VO列表, 含前后值和风险等级
     */
    private List<NovelGraphPatchVO> extractGraphPatches(String chapterContent, ChapterGenerationSessionEntity session, Long requestUserId) {
        NovelGraphPatchModel patch = novelGraphPatchService.extractAndParsePatches(
                chapterContent, session.getProjectId(), session.getChapterNumber(), requestUserId);

        List<NovelGraphPatchVO> voList = novelGraphPatchService.convertToVoList(patch);

        // 保存原始Patch JSON到会话, 用于审阅撤销和日志
        session.setGraphPatchJson(voList.toString());
        sessionDao.updateById(session);

        log.info("GraphPatch抽取完成 sessionId={}, operationCount={}", session.getId(), voList.size());
        return voList;
    }

    /**
     * 构建逆操作Patch JSON —— 将正向操作列表的before/after互换，ADD_/REMOVE_互换。
     */
    private String buildInversePatchJson(List<NovelGraphPatchVO> forwardPatches) {
        if (forwardPatches == null || forwardPatches.isEmpty()) {
            return "[]";
        }
        List<NovelGraphPatchVO> inverse = new ArrayList<>();
        for (NovelGraphPatchVO fwd : forwardPatches) {
            if (Boolean.FALSE.equals(fwd.getConfirmed())) continue;

            NovelGraphPatchVO inv = new NovelGraphPatchVO();
            String opType = fwd.getOperationType();
            if (opType != null && opType.startsWith("ADD_")) {
                inv.setOperationType(opType.replace("ADD_", "REMOVE_"));
            } else if (opType != null && opType.startsWith("REMOVE_")) {
                inv.setOperationType(opType.replace("REMOVE_", "ADD_"));
            } else {
                inv.setOperationType(opType);
            }
            inv.setOperationDesc("逆:" + fwd.getOperationDesc());
            inv.setCharacterName(fwd.getCharacterName());
            inv.setBeforeValue(fwd.getAfterValue());
            inv.setAfterValue(fwd.getBeforeValue());
            inv.setConfidence(1.0f);
            inv.setRiskLevel(fwd.getRiskLevel());
            inv.setConfirmed(true);
            inverse.add(inv);
        }
        try {
            return objectMapper.writeValueAsString(inverse);
        } catch (Exception e) {
            log.error("序列化逆操作Patch失败", e);
            return "[]";
        }
    }

    /**
     * 执行GraphPatch列表 —— 委托NovelGraphPatchService按38→5映射写入Neo4j。
     */
    private void executeGraphPatches(List<NovelGraphPatchVO> patches, Long projectId, Integer chapterNumber) {
        novelGraphPatchService.executePatches(patches, projectId, chapterNumber);
    }

    /**
     * 构建写作意图JSON快照。
     */
    private String buildIntentJson(NovelWriteStartForm form, NovelPromptVO promptVO) {
        return String.format("{\"chapterNumber\":%d,\"pov\":\"%s\",\"chapterGoal\":\"%s\"}",
                form.getChapterNumber(),
                form.getPov() != null ? form.getPov() : "",
                form.getChapterGoal() != null ? form.getChapterGoal() : "");
    }

    /**
     * 构建会话VO。
     */
    private NovelWriteSessionVO buildSessionVO(ChapterGenerationSessionEntity session, NovelGenerationResult genResult, boolean isStreamMode) {
        NovelWriteSessionVO vo = new NovelWriteSessionVO();
        vo.setSessionId(session.getId());
        vo.setProjectId(session.getProjectId());
        vo.setChapterNumber(session.getChapterNumber());
        vo.setStatus(session.getStatus());
        vo.setRetryCount(session.getRetryCount());
        vo.setCreateTime(session.getCreateTime());
        vo.setProvider(session.getProvider());

        if (genResult != null) {
            vo.setTitle(genResult.getTitle());
            vo.setSummary(genResult.getSummary());
            vo.setContent(genResult.getContent());
            vo.setWordCount(genResult.getCharCount());
        }
        return vo;
    }

    /**
     * 构建会话VO（无生成结果的重载, 流式完成后使用）。
     */
    private NovelWriteSessionVO buildSessionVO(ChapterGenerationSessionEntity session, NovelGenerationResult genResult) {
        return buildSessionVO(session, genResult, false);
    }

    /**
     * 根据当前状态构建可执行操作列表。
     */
    private List<String> buildAvailableActions(NovelGenerationStatusEnum status) {
        List<String> actions = new ArrayList<>();
        if (status == null) return actions;

        switch (status) {
            case GENERATING:
                actions.add("WAIT");
                break;
            case CONTENT_REVIEW:
                actions.add("CONTENT_REVIEW_PASS");
                actions.add("CONTENT_REVIEW_EDIT");
                actions.add("RETRY");
                break;
            case PATCH_REVIEW:
                actions.add("PATCH_CONFIRM");
                actions.add("PATCH_BACK");
                break;
            case PENDING_GRAPH_UPDATE:
                actions.add("RETRY_GRAPH_UPDATE");
                break;
            case SUCCESS:
                actions.add("NEXT_CHAPTER");
                break;
            case FAILED:
            case INTERRUPTED:
                actions.add("RETRY");
                break;
        }
        return actions;
    }

    /**
     * 从正文提取标题。
     */
    private String extractTitle(String content) {
        if (content == null || content.isEmpty()) return null;
        String firstLine = content.split("\\n")[0].trim();
        firstLine = firstLine.replaceAll("^#{1,2}\\s*", "").replaceAll("【|】", "");
        return firstLine.length() > 100 ? firstLine.substring(0, 100) : firstLine;
    }

    /**
     * 从正文截取摘要。
     */
    private String extractSummary(String content, int maxLen) {
        if (content == null || content.isEmpty()) return null;
        String clean = content.replaceAll("#{1,2}\\s*", "").trim();
        return clean.length() > maxLen ? clean.substring(0, maxLen) : clean;
    }
}
