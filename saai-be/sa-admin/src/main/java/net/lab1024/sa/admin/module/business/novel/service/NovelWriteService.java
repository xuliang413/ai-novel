package net.lab1024.sa.admin.module.business.novel.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.constant.NovelChapterStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGenerationProviderEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGenerationStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphChangeStatusEnum;
import net.lab1024.sa.admin.module.business.novel.dao.ChapterGenerationSessionDao;
import net.lab1024.sa.admin.module.business.novel.dao.GraphChangeLogDao;
import net.lab1024.sa.admin.module.business.novel.dao.WritingLogDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.ChapterGenerationSessionEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.GraphChangeLogEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelAliasEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCheatEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelEventEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelItemEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.WritingLogEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelContentReviewPassForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelPatchBackForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelPatchConfirmForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelUndoForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelWriteRecoverForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelWriteStartForm;
import net.lab1024.sa.admin.module.business.novel.domain.model.ChapterIntentCandidateModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ChapterIntentModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ContentQualityCheckModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ContextPreviewItemModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ContextPreviewModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchOperationModel;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelChapterVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelGraphPatchVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelUndoVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelWriteDraftVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelWriteRecoverVO;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 写作编排——把各个步骤串起来，控制整个写作流程。
 *
 * 一个完整的写作流程：
 * 1. 用户点"开始写作" → 自动检测有没有可用的 AI Key
 * 2. 有 Key → 调 AI 写一章，没有 → 用 mock 数据
 * 3. 写完先保存草稿，质检通过后进入审阅
 * 4. 用户通过审阅 → AI 抽取图谱变更 → 展示变更单让用户确认
 * 5. 用户确认变更 → 写入 Neo4j，章节正式发布
 * 6. 写错了可以撤销（undo），只撤图谱不删正文
 *
 * 降级保护：API Key 没配 → AI 调用失败 → 都自动 fallback 到 mock，不阻塞流程。
 */
@Slf4j
@Service
public class NovelWriteService {

    private static final String PATCH_READY = "READY";

    private static final String PATCH_LOW_CONFIDENCE = "LOW_CONFIDENCE";

    private static final String CONFIDENCE_HIGH = "HIGH";

    private static final String CONFIDENCE_MEDIUM = "MEDIUM";

    private static final String CONFIDENCE_LOW = "LOW";

    @Resource
    private NovelProjectService novelProjectService;

    @Resource
    private NovelAssetService novelAssetService;

    @Resource
    private NovelChapterService novelChapterService;

    @Resource
    private NovelGraphService novelGraphService;

    @Resource
    private ChapterGenerationSessionDao generationSessionDao;

    @Resource
    private GraphChangeLogDao graphChangeLogDao;

    @Resource
    private WritingLogDao writingLogDao;

    @Resource
    private NovelLLMService novelLLMService;

    @Resource
    private net.lab1024.sa.admin.module.business.novel.dao.UserApiKeyDao userApiKeyDao;

    @Resource
    private net.lab1024.sa.base.module.support.apiencrypt.service.ApiEncryptService apiEncryptService;

    /**
     * 开始写作——自动判断用 DeepSeek 还是通义千问，都没有就降级。
     *
     * 流程：
     * 1. 检查项目在不在、算出下一个章节号
     * 2. 组装写作意图（ChapterIntent）——这章视角谁、想写什么
     * 3. 查 API Key 表，有可用的 Key 就用 AI，都没有就降级
     * 4. 写完了看看质量行不行（字数够不够、POV 角色出场没）
     * 5. 质量没问题就存 session + 存章节（状态 CONTENT_REVIEW）
     * 6. 同步一份到 Neo4j 图谱上
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<NovelWriteDraftVO> start(NovelWriteStartForm form) {
        NovelProjectEntity project = novelProjectService.getAvailableProject(form.getProjectId());
        if (project == null) {
            return ResponseDTO.userErrorParam("小说项目不存在");
        }

        Integer chapterNo = form.getChapterNo() == null ? novelChapterService.getNextChapterNo(form.getProjectId()) : form.getChapterNo();
        List<NovelCharacterEntity> characters = novelAssetService.listCharacters(form.getProjectId());
        List<NovelLocationEntity> locations = novelAssetService.listLocations(form.getProjectId());
        List<NovelClueEntity> clues = novelAssetService.listClues(form.getProjectId());

        ChapterIntentModel intent = buildChapterIntent(form, project, chapterNo, characters, locations, clues);
        ContextPreviewModel contextPreview = buildContextPreview(project, chapterNo, intent, characters, locations, clues);

        // provider 是本次写作的“发动机”：优先真实模型，拿不到可用 Key 就走 MOCK，保证功能链路不中断。
        String provider = resolveAvailableProvider();
        String title;
        String summary;
        String content;
        String promptSnapshot;

        if ("DEEPSEEK".equals(provider) || "TONGYI".equals(provider)) {
            // promptSnapshot 不保存完整大段提示词，只保留能排查“谁、哪章、哪个模型”的轻量线索。
            promptSnapshot = buildPromptSnapshot(project, intent, contextPreview, provider);
            NovelLLMService.LLMChapterResult result = novelLLMService.generateChapter(project, intent, contextPreview, provider);
            if (result != null && StringUtils.isNotBlank(result.content()) && StringUtils.isNotBlank(result.title())) {
                title = result.title();
                summary = result.summary();
                content = result.content();
            } else {
                // AI 返回空内容时不让用户卡死在生成页，直接降级成可审阅草稿。
                provider = NovelGenerationProviderEnum.MOCK.getValue();
                title = "第" + chapterNo + "章：" + project.getProjectName();
                summary = "第" + chapterNo + "章待审阅草稿（LLM 调用失败，回退降级）。";
                content = buildMockContent(project, chapterNo, characters, locations, clues);
                promptSnapshot = "/write " + chapterNo + " (fallback to mock)";
            }
        } else {
            // 没有任何可用 Key 时仍然走完整状态机，方便前端和后续 GraphPatch 流程一起联调。
            provider = NovelGenerationProviderEnum.MOCK.getValue();
            title = "第" + chapterNo + "章：" + project.getProjectName();
            summary = "第" + chapterNo + "章待审阅草稿（未配置 AI Key，使用降级模式）。";
            content = buildMockContent(project, chapterNo, characters, locations, clues);
            promptSnapshot = "/write " + chapterNo;
        }

        // 质检不是最终裁判，只是提前把明显风险告诉前端，比如 POV 没出现、字数太短。
        ContentQualityCheckModel qualityCheck = buildQualityCheck(content, intent);

        // session 是写作状态机的核心记录：后面恢复、返回上一步、确认图谱、撤销都靠它串起来。
        ChapterGenerationSessionEntity session = new ChapterGenerationSessionEntity();
        session.setProjectId(form.getProjectId());
        session.setChapterNo(chapterNo);
        session.setProvider(provider);
        session.setStatus(NovelGenerationStatusEnum.CONTENT_REVIEW.getValue());
        session.setPromptSnapshot(promptSnapshot);
        session.setIntentJson(JSON.toJSONString(intent));
        session.setContextSnapshot(JSON.toJSONString(contextPreview));
        session.setContentReviewJson(JSON.toJSONString(qualityCheck));
        session.setResultExcerpt(StringUtils.abbreviate(content, 500));
        session.setCreateUserId(AdminRequestUtil.getRequestUserId());
        generationSessionDao.insert(session);

        // 章节先以 DRAFT 落库，只有 GraphPatch 确认成功后才会变 PUBLISHED。
        NovelChapterEntity chapter = saveDraftChapter(form.getProjectId(), chapterNo, title, summary, content, session.getSessionId());
        session.setChapterId(chapter.getChapterId());
        generationSessionDao.updateById(session);

        // MySQL 是正文主存储，Neo4j 是关系检索层；这里同步基础节点，后面写作检索才能查到。
        novelGraphService.mergeProject(project);
        novelGraphService.mergeChapter(chapter);

        NovelWriteDraftVO vo = new NovelWriteDraftVO();
        vo.setSessionId(session.getSessionId());
        vo.setSessionStatus(session.getStatus());
        vo.setChapter(toChapterVO(chapter));
        vo.setChapterIntent(intent);
        vo.setContextPreview(contextPreview);
        vo.setQualityCheck(qualityCheck);
        return ResponseDTO.ok(vo);
    }

    /**
     * 流式写作——AI 写到哪就实时推送出去，最后才保存。
     *
     * 跟 startMock 的区别：
     * - 不等人写完整章，收到一个字就推一个字
     * - onToken、onComplete、onError 三个回调给 WebSocket Handler 用
     * - 只有 onComplete 被调用时才存 session + 存章节
     */
    public void startStream(NovelWriteStartForm form,
                            java.util.function.Consumer<String> onToken,
                            java.util.function.Consumer<NovelWriteDraftVO> onComplete,
                            java.util.function.Consumer<String> onError) {
        NovelProjectEntity project = novelProjectService.getAvailableProject(form.getProjectId());
        if (project == null) {
            onError.accept("小说项目不存在");
            return;
        }

        Integer chapterNo = form.getChapterNo() == null ? novelChapterService.getNextChapterNo(form.getProjectId()) : form.getChapterNo();
        List<NovelCharacterEntity> characters = novelAssetService.listCharacters(form.getProjectId());
        List<NovelLocationEntity> locations = novelAssetService.listLocations(form.getProjectId());
        List<NovelClueEntity> clues = novelAssetService.listClues(form.getProjectId());

        ChapterIntentModel intent = buildChapterIntent(form, project, chapterNo, characters, locations, clues);
        ContextPreviewModel contextPreview = buildContextPreview(project, chapterNo, intent, characters, locations, clues);

        String provider = resolveAvailableProvider();
        if (NovelGenerationProviderEnum.MOCK.getValue().equals(provider)) {
            // 流式接口也要能无 Key 运行：把整段 mock 内容一次性推给前端，再走完成回调。
            String title = "第" + chapterNo + "章：" + project.getProjectName();
            String content = buildMockContent(project, chapterNo, characters, locations, clues);
            String summary = "第" + chapterNo + "章待审阅草稿（未配置 AI Key，使用降级模式）。";
            NovelWriteDraftVO vo = saveStreamSession(form.getProjectId(), chapterNo, provider,
                    title, summary, content, intent, contextPreview, project);
            onToken.accept(content);
            onComplete.accept(vo);
            return;
        }

        novelLLMService.generateChapterStream(project, intent, contextPreview, provider,
                onToken,
                result -> {
                    try {
                        if (result == null || StringUtils.isBlank(result.content()) || StringUtils.isBlank(result.title())) {
                            // 流式模型完成了但没给出完整 JSON 时，仍然保存一份 mock 草稿，避免会话悬空。
                            String fallbackTitle = "第" + chapterNo + "章：" + project.getProjectName();
                            String fallbackContent = buildMockContent(project, chapterNo, characters, locations, clues);
                            String fallbackSummary = "第" + chapterNo + "章待审阅草稿（LLM 调用失败，回退降级）。";
                            NovelWriteDraftVO vo = saveStreamSession(form.getProjectId(), chapterNo,
                                    NovelGenerationProviderEnum.MOCK.getValue(),
                                    fallbackTitle, fallbackSummary, fallbackContent,
                                    intent, contextPreview, project);
                            onComplete.accept(vo);
                        } else {
                            NovelWriteDraftVO vo = saveStreamSession(form.getProjectId(), chapterNo, provider,
                                    result.title(), result.summary(), result.content(),
                                    intent, contextPreview, project);
                            onComplete.accept(vo);
                        }
                    } catch (Exception e) {
                        log.error("Failed to save stream session", e);
                        onError.accept("保存章节失败：" + e.getMessage());
                    }
                },
                errorMsg -> onError.accept(errorMsg));
    }

    private NovelWriteDraftVO saveStreamSession(Long projectId, Integer chapterNo, String provider,
                                                 String title, String summary, String content,
                                                 ChapterIntentModel intent, ContextPreviewModel contextPreview,
                                                 NovelProjectEntity project) {
        // 流式生成结束后统一走这里保存，和普通 start 保持同一套 session / chapter 结构。
        ContentQualityCheckModel qualityCheck = buildQualityCheck(content, intent);

        ChapterGenerationSessionEntity session = new ChapterGenerationSessionEntity();
        session.setProjectId(projectId);
        session.setChapterNo(chapterNo);
        session.setProvider(provider);
        session.setStatus(NovelGenerationStatusEnum.CONTENT_REVIEW.getValue());
        session.setPromptSnapshot(provider + ":/write " + chapterNo + " pov=" + intent.getPov());
        session.setIntentJson(JSON.toJSONString(intent));
        session.setContextSnapshot(JSON.toJSONString(contextPreview));
        session.setContentReviewJson(JSON.toJSONString(qualityCheck));
        session.setResultExcerpt(StringUtils.abbreviate(content, 500));
        session.setCreateUserId(AdminRequestUtil.getRequestUserId());
        generationSessionDao.insert(session);

        NovelChapterEntity chapter = saveDraftChapter(projectId, chapterNo, title, summary, content, session.getSessionId());
        session.setChapterId(chapter.getChapterId());
        generationSessionDao.updateById(session);

        novelGraphService.mergeProject(project);
        novelGraphService.mergeChapter(chapter);

        NovelWriteDraftVO vo = new NovelWriteDraftVO();
        vo.setSessionId(session.getSessionId());
        vo.setSessionStatus(session.getStatus());
        vo.setChapter(toChapterVO(chapter));
        vo.setChapterIntent(intent);
        vo.setContextPreview(contextPreview);
        vo.setQualityCheck(qualityCheck);
        return vo;
    }

    /**
     * 正文审阅通过 → 生成图谱变更单 → 停在变更确认页。
     *
     * 怎么生成变更单：
     * - 如果这章是 DeepSeek/通义千问写的 → 优先让 AI 看一遍正文自动抽取
     * - AI 抽取失败或者这章是 mock 的 → 用老方法（buildGraphPatch）生成默认变更
     *
     * 这里只是生成变更单，不会真的写 Neo4j。要等 confirmPatch 才是真写。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<NovelGraphPatchVO> passContentReview(NovelContentReviewPassForm form) {
        ChapterGenerationSessionEntity session = generationSessionDao.selectById(form.getSessionId());
        if (session == null) {
            return ResponseDTO.userErrorParam("生成会话不存在");
        }

        NovelChapterEntity chapter = novelChapterService.getById(form.getChapterId());
        if (chapter == null || !Objects.equals(chapter.getProjectId(), session.getProjectId())) {
            return ResponseDTO.userErrorParam("章节不存在");
        }

        if (StringUtils.isNotBlank(form.getContent())) {
            // 用户可能在审阅页改过正文；GraphPatch 必须基于最终审阅内容生成。
            chapter.setContent(form.getContent());
        }
        if (StringUtils.isNotBlank(form.getSummary())) {
            // 摘要也允许人工改，后面 UPDATE_CHAPTER_SUMMARY 会把它同步到图谱。
            chapter.setSummary(form.getSummary());
        }

        ChapterIntentModel intent = parse(session.getIntentJson(), ChapterIntentModel.class);
        ContentQualityCheckModel qualityCheck = buildQualityCheck(chapter.getContent(), intent);

        List<NovelCharacterEntity> characters = novelAssetService.listCharacters(chapter.getProjectId());
        List<NovelLocationEntity> locations = novelAssetService.listLocations(chapter.getProjectId());
        List<NovelClueEntity> clues = novelAssetService.listClues(chapter.getProjectId());

        NovelGraphPatchModel graphPatch = null;
        NovelProjectEntity project = novelProjectService.getAvailableProject(chapter.getProjectId());

        if (NovelGenerationProviderEnum.DEEPSEEK.getValue().equals(session.getProvider())
                || NovelGenerationProviderEnum.TONGYI.getValue().equals(session.getProvider())) {
            // 真实模型写出来的正文优先让模型自己读一遍，抽出“谁出场、线索是否推进”。
            graphPatch = novelLLMService.extractGraphPatch(project, chapter, characters, locations, clues, session.getProvider());
            if (graphPatch == null || CollectionUtils.isEmpty(graphPatch.getOperations())) {
                log.warn("LLM GraphPatch extraction failed or returned empty, fallback to mock. chapterNo={}", chapter.getChapterNo());
                graphPatch = null;
            }
        }

        if (graphPatch == null) {
            // 抽取失败时用规则兜底：至少生成章节发布、候选出场、线索推进这些基本操作。
            graphPatch = buildGraphPatch(chapter);
        }

        // 校验 patch：缺必填字段的标 BLOCKED，before值跟图谱不一致的标 CONFLICT
        novelGraphService.checkBlocked(graphPatch);
        novelGraphService.validatePatch(graphPatch);

        NovelGraphPatchModel inversePatch = buildInversePatch(graphPatch);

        chapter.setStatus(NovelChapterStatusEnum.PENDING_GRAPH_CONFIRM.getValue());
        novelChapterService.save(chapter);

        session.setStatus(NovelGenerationStatusEnum.PATCH_REVIEW.getValue());
        session.setChapterId(chapter.getChapterId());
        session.setContentReviewJson(JSON.toJSONString(qualityCheck));
        session.setGraphPatchJson(JSON.toJSONString(graphPatch));
        session.setInversePatchJson(JSON.toJSONString(inversePatch));
        session.setOperationBatchId(graphPatch.getOperationBatchId());
        generationSessionDao.updateById(session);

        NovelGraphPatchVO vo = buildGraphPatchVO(session, chapter, graphPatch, inversePatch);
        return ResponseDTO.ok(vo);
    }

    /**
     * 用户点了"确认变更"——真正把图谱变更写到 Neo4j 里，同时发布章节。
     *
     * 执行顺序：
     * 1. 从 session 里取出之前生成的变更单（graphPatch + inversePatch）
     * 2. 只执行用户勾选了的那几条（或者前端传了具体哪些要执行）
     * 3. NovelGraphService.applyGraphPatch —— 受控 Cypher 写入 Neo4j
     * 4. 写入变更日志（patch + inversePatch 都存，undo 要用）
     * 5. 章节状态改为 PUBLISHED
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<NovelChapterVO> confirmPatch(NovelPatchConfirmForm form) {
        ChapterGenerationSessionEntity session = generationSessionDao.selectById(form.getSessionId());
        if (session == null) {
            return ResponseDTO.userErrorParam("生成会话不存在");
        }
        if (!NovelGenerationStatusEnum.PATCH_REVIEW.getValue().equals(session.getStatus())) {
            return ResponseDTO.userErrorParam("当前会话不在 GraphPatch 确认状态");
        }

        NovelChapterEntity chapter = novelChapterService.getById(session.getChapterId());
        if (chapter == null) {
            return ResponseDTO.userErrorParam("章节不存在");
        }

        NovelGraphPatchModel graphPatch = parse(session.getGraphPatchJson(), NovelGraphPatchModel.class);
        NovelGraphPatchModel inversePatch = parse(session.getInversePatchJson(), NovelGraphPatchModel.class);
        if (graphPatch == null || inversePatch == null) {
            return ResponseDTO.userErrorParam("待确认 GraphPatch 不存在");
        }

        // 前端传 operationIds 时按用户选择执行；没传时只执行默认勾选的安全项。
        NovelGraphPatchModel executablePatch = filterPatch(graphPatch, form.getOperationIds(), true);
        if (CollectionUtils.isEmpty(executablePatch.getOperations())) {
            return ResponseDTO.userErrorParam("没有可执行的 GraphPatch 操作");
        }

        // 执行前再校验一遍：防止用户停了太久，图谱已经被别人改过了
        novelGraphService.validatePatch(executablePatch);

        // 只保存本次实际执行项对应的 inversePatch。这样 /undo 不会撤销用户没勾选的内容。
        NovelGraphPatchModel executableInversePatch = filterPatch(inversePatch, executablePatch.getOperations().stream()
                .map(NovelGraphPatchOperationModel::getOperationId)
                .collect(Collectors.toList()), false);

        session.setStatus(NovelGenerationStatusEnum.APPLYING_PATCH.getValue());
        generationSessionDao.updateById(session);

        try {
            // 真正写 Neo4j 的入口只有这里；service 外面不直接拼 Cypher。
            novelGraphService.applyGraphPatch(executablePatch);
        } catch (Exception e) {
            // 图谱失败时正文不丢，章节进入“待同步图谱”，方便修完问题后重新确认。
            chapter.setStatus(NovelChapterStatusEnum.PENDING_GRAPH_UPDATE.getValue());
            novelChapterService.save(chapter);
            session.setStatus(NovelGenerationStatusEnum.PATCH_REVIEW.getValue());
            session.setErrorMessage(StringUtils.abbreviate(e.getMessage(), 1000));
            generationSessionDao.updateById(session);
            return ResponseDTO.userErrorParam("图谱更新失败：" + StringUtils.abbreviate(e.getMessage(), 200));
        }

        GraphChangeLogEntity changeLog = new GraphChangeLogEntity();
        changeLog.setProjectId(chapter.getProjectId());
        changeLog.setChapterId(chapter.getChapterId());
        changeLog.setChapterNo(chapter.getChapterNo());
        changeLog.setSessionId(session.getSessionId());
        changeLog.setPatchId(executablePatch.getPatchId());
        changeLog.setOperationBatchId(executablePatch.getOperationBatchId());
        changeLog.setPatchJson(JSON.toJSONString(executablePatch));
        changeLog.setInversePatchJson(JSON.toJSONString(executableInversePatch));
        changeLog.setStatus(NovelGraphChangeStatusEnum.APPLIED.getValue());
        changeLog.setCreateUserId(AdminRequestUtil.getRequestUserId());
        graphChangeLogDao.insert(changeLog);

        // 写作日志是给后续仪表盘和成本统计看的，不参与写作状态机判断。
        WritingLogEntity writingLog = new WritingLogEntity();
        writingLog.setProjectId(chapter.getProjectId());
        writingLog.setChapterId(chapter.getChapterId());
        writingLog.setChapterNo(chapter.getChapterNo());
        writingLog.setWordCount(StringUtils.defaultString(chapter.getContent()).replaceAll("\\s+", "").length());
        writingLog.setTokenUsed(StringUtils.defaultString(session.getPromptSnapshot()).length() / 2);
        writingLog.setSuccess(true);
        writingLog.setProvider(session.getProvider());
        writingLog.setCreateUserId(AdminRequestUtil.getRequestUserId());
        writingLogDao.insert(writingLog);

        chapter.setStatus(NovelChapterStatusEnum.PUBLISHED.getValue());
        novelChapterService.save(chapter);
        // 发布后再 merge 一次章节节点，把 PUBLISHED 状态同步给检索层。
        novelGraphService.mergeChapter(chapter);

        session.setStatus(NovelGenerationStatusEnum.SUCCESS.getValue());
        session.setGraphPatchJson(JSON.toJSONString(executablePatch));
        session.setInversePatchJson(JSON.toJSONString(executableInversePatch));
        session.setErrorMessage(null);
        generationSessionDao.updateById(session);
        return ResponseDTO.ok(toChapterVO(chapter));
    }

    /**
     * 从 PATCH_REVIEW 返回 CONTENT_REVIEW，正文保留草稿，候选 Patch 丢弃。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<NovelWriteRecoverVO> backToContentReview(NovelPatchBackForm form) {
        ChapterGenerationSessionEntity session = generationSessionDao.selectById(form.getSessionId());
        if (session == null) {
            return ResponseDTO.userErrorParam("生成会话不存在");
        }

        NovelChapterEntity chapter = session.getChapterId() == null ? null : novelChapterService.getById(session.getChapterId());
        if (chapter != null) {
            // 这里只回退状态和清空候选 Patch，不回滚正文，因为用户可能只是想改文字再重新过审。
            chapter.setStatus(NovelChapterStatusEnum.DRAFT.getValue());
            novelChapterService.save(chapter);
        }

        session.setStatus(NovelGenerationStatusEnum.CONTENT_REVIEW.getValue());
        session.setGraphPatchJson(null);
        session.setInversePatchJson(null);
        session.setOperationBatchId(null);
        generationSessionDao.updateById(session);
        return ResponseDTO.ok(buildRecoverVO(session));
    }

    /**
     * 恢复项目最近一次或指定章节的写作会话。
     */
    public ResponseDTO<NovelWriteRecoverVO> recover(NovelWriteRecoverForm form) {
        ChapterGenerationSessionEntity session = latestSession(form.getProjectId(), form.getChapterNo());
        if (session == null) {
            return ResponseDTO.userErrorParam("没有可恢复的写作会话");
        }
        return ResponseDTO.ok(buildRecoverVO(session));
    }

    /**
     * 回滚——把最近一次写进图谱的东西撤销掉。
     *
     * 只撤图谱不删正文：
     * - 先从变更日志里找到最近一条"已执行"的记录
     * - 把 inversePatch（反向变更单）执行一遍，Neo4j 恢复到变更前的状态
     * - 章节状态改成 PENDING_GRAPH_UPDATE（正文还在，可以重新发布）
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<NovelUndoVO> undo(NovelUndoForm form) {
        GraphChangeLogEntity changeLog = graphChangeLogDao.selectOne(new LambdaQueryWrapper<GraphChangeLogEntity>()
                .eq(GraphChangeLogEntity::getProjectId, form.getProjectId())
                .eq(GraphChangeLogEntity::getStatus, NovelGraphChangeStatusEnum.APPLIED.getValue())
                .orderByDesc(GraphChangeLogEntity::getChangeLogId)
                .last("limit 1"));
        if (changeLog == null) {
            return ResponseDTO.userErrorParam("没有可撤销的图谱变更");
        }

        NovelGraphPatchModel inversePatch = parse(changeLog.getInversePatchJson(), NovelGraphPatchModel.class);
        if (inversePatch == null || CollectionUtils.isEmpty(inversePatch.getOperations())) {
            return ResponseDTO.userErrorParam("反向 GraphPatch 不存在");
        }

        try {
            // 撤销执行的是 inversePatch：它只改 Neo4j，不删除 MySQL 章节正文。
            novelGraphService.applyGraphPatch(inversePatch);
        } catch (Exception e) {
            changeLog.setStatus(NovelGraphChangeStatusEnum.FAILED.getValue());
            changeLog.setErrorMessage(StringUtils.abbreviate(e.getMessage(), 1000));
            graphChangeLogDao.updateById(changeLog);
            return ResponseDTO.userErrorParam("撤销图谱变更失败：" + StringUtils.abbreviate(e.getMessage(), 200));
        }

        changeLog.setStatus(NovelGraphChangeStatusEnum.UNDONE.getValue());
        graphChangeLogDao.updateById(changeLog);

        NovelChapterEntity chapter = changeLog.getChapterId() == null ? null : novelChapterService.getById(changeLog.getChapterId());
        if (chapter != null) {
            // 图谱已回滚，但正文还在，所以章节变成待同步状态，提醒用户需要重新确认图谱。
            chapter.setStatus(NovelChapterStatusEnum.PENDING_GRAPH_UPDATE.getValue());
            novelChapterService.save(chapter);
            novelGraphService.mergeChapter(chapter);
        }

        NovelUndoVO vo = new NovelUndoVO();
        vo.setOperationBatchId(changeLog.getOperationBatchId());
        vo.setChapterId(changeLog.getChapterId());
        vo.setChapterNo(changeLog.getChapterNo());
        vo.setGraphChangeStatus(changeLog.getStatus());
        vo.setChapterStatus(chapter == null ? null : chapter.getStatus());
        vo.setInversePatch(inversePatch);
        return ResponseDTO.ok(vo);
    }

    private NovelChapterEntity saveDraftChapter(Long projectId, Integer chapterNo, String title, String summary, String content, Long sessionId) {
        NovelChapterEntity chapter = novelChapterService.getByProjectAndNo(projectId, chapterNo);
        if (chapter == null) {
            // 同一项目同一章只保留一条正文记录，重复生成会覆盖草稿而不是插入新章节。
            chapter = new NovelChapterEntity();
            chapter.setProjectId(projectId);
            chapter.setChapterNo(chapterNo);
        }
        chapter.setTitle(title);
        chapter.setSummary(summary);
        chapter.setContent(content);
        chapter.setStatus(NovelChapterStatusEnum.DRAFT.getValue());
        chapter.setGenerationSessionId(sessionId);
        novelChapterService.save(chapter);
        return chapter;
    }

    private ChapterIntentModel buildChapterIntent(NovelWriteStartForm form,
                                                  NovelProjectEntity project,
                                                  Integer chapterNo,
                                                  List<NovelCharacterEntity> characters,
                                                  List<NovelLocationEntity> locations,
                                                  List<NovelClueEntity> clues) {
        ChapterIntentModel intent = new ChapterIntentModel();
        intent.setProjectId(project.getProjectId());
        intent.setChapterNo(chapterNo);
        // POV 没传就用项目主角，再没有就用第一个角色；不要让模型在“谁视角”上空转。
        intent.setPov(StringUtils.defaultIfBlank(form.getPov(), StringUtils.defaultIfBlank(project.getProtagonist(), firstCharacter(characters))));
        intent.setChapterGoal(StringUtils.defaultIfBlank(form.getChapterGoal(), "推进《" + project.getProjectName() + "》第" + chapterNo + "章的主线冲突"));
        // 候选角色/地点/线索既服务前端预览，也服务后面 Neo4j 检索和 Prompt 组装。
        intent.setCandidateCharacters(toIntentCandidates(filterByNames(characters, form.getCandidateCharacters(), NovelCharacterEntity::getCharacterName), NovelCharacterEntity::getCharacterId, NovelCharacterEntity::getCharacterName, NovelCharacterEntity::getRoleType, "USER_OR_PROJECT"));
        intent.setTargetClues(toIntentCandidates(filterByNames(clues, form.getTargetClues(), NovelClueEntity::getClueName), NovelClueEntity::getClueId, NovelClueEntity::getClueName, NovelClueEntity::getClueType, "USER_OR_ACTIVE_CLUE"));
        intent.setCandidateLocations(toIntentCandidates(filterByNames(locations, form.getCandidateLocations(), NovelLocationEntity::getLocationName), NovelLocationEntity::getLocationId, NovelLocationEntity::getLocationName, NovelLocationEntity::getLocationType, "USER_OR_PROJECT"));
        if (CollectionUtils.isNotEmpty(form.getExtraInstructions())) {
            intent.setExtraInstructions(form.getExtraInstructions());
        }
        return intent;
    }

    private <T> List<T> filterByNames(List<T> source, List<String> names, Function<T, String> nameGetter) {
        if (CollectionUtils.isEmpty(source)) {
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(names)) {
            // 用户没指定时不要把全项目都塞给模型，最多取 5 个，控制提示词长度。
            return source.stream().limit(5).collect(Collectors.toList());
        }
        Set<String> nameSet = names.stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        List<T> matched = source.stream()
                .filter(item -> nameSet.contains(nameGetter.apply(item)))
                .collect(Collectors.toList());
        // 前端传了名字但没匹配到时，退回默认候选，避免生成出来完全没有上下文。
        return CollectionUtils.isEmpty(matched) ? source.stream().limit(5).collect(Collectors.toList()) : matched;
    }

    private <T> List<ChapterIntentCandidateModel> toIntentCandidates(List<T> source,
                                                                     Function<T, Long> idGetter,
                                                                     Function<T, String> nameGetter,
                                                                     Function<T, String> typeGetter,
                                                                     String sourceType) {
        List<ChapterIntentCandidateModel> result = new ArrayList<>();
        int priority = 1;
        for (T item : source) {
            ChapterIntentCandidateModel candidate = new ChapterIntentCandidateModel();
            candidate.setId(idGetter.apply(item));
            candidate.setName(nameGetter.apply(item));
            candidate.setType(typeGetter.apply(item));
            candidate.setSource(sourceType);
            // 前 3 个默认 required，表示“尽量写进本章”；后面的只是可参考候选。
            candidate.setRequired(priority <= 3);
            candidate.setPriority(priority++);
            result.add(candidate);
        }
        return result;
    }

    private ContextPreviewModel buildContextPreview(NovelProjectEntity project,
                                                    Integer chapterNo,
                                                    ChapterIntentModel intent,
                                                    List<NovelCharacterEntity> characters,
                                                    List<NovelLocationEntity> locations,
                                                    List<NovelClueEntity> clues) {
        ContextPreviewModel preview = new ContextPreviewModel();
        preview.setProjectId(project.getProjectId());
        preview.setChapterNo(chapterNo);
        preview.setProjectSummary(project.getSummary());
        preview.setCharacterCards(toContextItems(characters, NovelCharacterEntity::getCharacterId, NovelCharacterEntity::getCharacterName, NovelCharacterEntity::getRoleType, NovelCharacterEntity::getSummary, intent.getCandidateCharacters()));
        preview.setClueCards(toContextItems(clues, NovelClueEntity::getClueId, NovelClueEntity::getClueName, NovelClueEntity::getClueType, NovelClueEntity::getSummary, intent.getTargetClues()));
        preview.setLocationCards(toContextItems(locations, NovelLocationEntity::getLocationId, NovelLocationEntity::getLocationName, NovelLocationEntity::getLocationType, NovelLocationEntity::getSummary, intent.getCandidateLocations()));
        // token 估算只是为了给前端一个“上下文大概多重”的感觉，不是精确计费。
        int textLength = StringUtils.length(project.getSummary())
                + preview.getCharacterCards().stream().mapToInt(item -> StringUtils.length(item.getSummary())).sum()
                + preview.getClueCards().stream().mapToInt(item -> StringUtils.length(item.getSummary())).sum()
                + preview.getLocationCards().stream().mapToInt(item -> StringUtils.length(item.getSummary())).sum();
        preview.setEstimatedTokens(Math.max(1, textLength / 2));
        preview.setTruncatedItems(0);
        return preview;
    }

    private <T> List<ContextPreviewItemModel> toContextItems(List<T> source,
                                                            Function<T, Long> idGetter,
                                                            Function<T, String> nameGetter,
                                                            Function<T, String> typeGetter,
                                                            Function<T, String> summaryGetter,
                                                            List<ChapterIntentCandidateModel> candidates) {
        Set<Long> candidateIds = candidates.stream().map(ChapterIntentCandidateModel::getId).collect(Collectors.toSet());
        List<ContextPreviewItemModel> result = new ArrayList<>();
        int priority = 1;
        for (T item : source) {
            Long id = idGetter.apply(item);
            if (!candidateIds.contains(id)) {
                // 预览只展示本章候选，不把全项目设定都摊出来，避免审阅页太乱。
                continue;
            }
            ContextPreviewItemModel contextItem = new ContextPreviewItemModel();
            contextItem.setId(id);
            contextItem.setName(nameGetter.apply(item));
            contextItem.setType(typeGetter.apply(item));
            contextItem.setSummary(StringUtils.abbreviate(StringUtils.defaultString(summaryGetter.apply(item)), 300));
            contextItem.setSource("ChapterIntent");
            contextItem.setRequired(priority <= 3);
            contextItem.setPriority(priority++);
            result.add(contextItem);
        }
        return result;
    }

    private ContentQualityCheckModel buildQualityCheck(String content, ChapterIntentModel intent) {
        ContentQualityCheckModel qualityCheck = new ContentQualityCheckModel();
        String safeContent = StringUtils.defaultString(content);
        // 对中文网文先用“去空白字符长度”粗略当字数，够排查和前端提示用。
        qualityCheck.setWordCount(safeContent.replaceAll("\\s+", "").length());
        String pov = intent == null ? null : intent.getPov();
        qualityCheck.setPov(pov);
        // 这是轻量规则，不做 NLP：只检查 POV 名字有没有在正文出现。
        qualityCheck.setPovMentioned(StringUtils.isBlank(pov) || safeContent.contains(pov));
        // 章末检测同样是兜底提示，真正好坏仍由人工审阅。
        qualityCheck.setHasChapterEnding(safeContent.contains("下一章") || safeContent.contains("结尾") || safeContent.contains("入口"));
        if (qualityCheck.getWordCount() < 500) {
            qualityCheck.getWarnings().add("当前 mock 草稿字数偏短，真实模型接入后需放宽目标字数。");
        }
        if (!Boolean.TRUE.equals(qualityCheck.getPovMentioned())) {
            qualityCheck.getWarnings().add("正文未显式出现 POV 角色。");
        }
        if (!Boolean.TRUE.equals(qualityCheck.getHasChapterEnding())) {
            qualityCheck.getWarnings().add("未检测到清晰章末推进提示。");
        }
        return qualityCheck;
    }

    private NovelGraphPatchModel buildGraphPatch(NovelChapterEntity chapter) {
        // 兜底 GraphPatch 只基于已有资产和正文关键词判断，不创造复杂事实。
        List<NovelCharacterEntity> characters = novelAssetService.listCharacters(chapter.getProjectId());
        List<NovelLocationEntity> locations = novelAssetService.listLocations(chapter.getProjectId());
        List<NovelClueEntity> clues = novelAssetService.listClues(chapter.getProjectId());
        List<NovelItemEntity> items = novelAssetService.listItems(chapter.getProjectId());
        List<NovelEventEntity> events = novelAssetService.listEvents(chapter.getProjectId());
        List<NovelCheatEntity> cheats = novelAssetService.listCheats(chapter.getProjectId());
        List<NovelAliasEntity> aliases = novelAssetService.listAliases(chapter.getProjectId());

        NovelGraphPatchModel patch = new NovelGraphPatchModel();
        patch.setPatchId(UUID.randomUUID().toString());
        patch.setOperationBatchId(UUID.randomUUID().toString());
        patch.setProjectId(chapter.getProjectId());
        patch.setChapterId(chapter.getChapterId());
        patch.setChapterNo(chapter.getChapterNo());
        patch.setStatus(PATCH_READY);

        addChapterSummaryOperation(patch, chapter);
        // 候选数量故意限制：兜底逻辑宁可少提几条，也不把整本书资产都塞进审阅单。
        characters.stream().limit(5).forEach(character -> addAppearanceOperation(patch, "CHARACTER", character.getCharacterId(), character.getCharacterName(), chapter.getContent()));
        locations.stream().limit(2).forEach(location -> addAppearanceOperation(patch, "LOCATION", location.getLocationId(), location.getLocationName(), chapter.getContent()));
        items.stream().limit(3).forEach(item -> addAppearanceOperation(patch, "ITEM", item.getItemId(), item.getItemName(), chapter.getContent()));
        events.stream().limit(2).forEach(event -> addAppearanceOperation(patch, "EVENT", event.getEventId(), event.getEventName(), chapter.getContent()));
        cheats.stream().limit(2).forEach(cheat -> addAppearanceOperation(patch, "CHEAT", cheat.getCheatId(), cheat.getCheatName(), chapter.getContent()));
        aliases.stream().limit(2).forEach(alias -> addAppearanceOperation(patch, "ALIAS", alias.getAliasId(), alias.getAliasName(), chapter.getContent()));
        clues.stream().limit(1).forEach(clue -> addClueOperation(patch, clue, chapter));
        if (patch.getOperations().stream().anyMatch(operation -> PATCH_LOW_CONFIDENCE.equals(operation.getValidationStatus()))) {
            patch.getWarnings().add("存在低置信操作，默认不勾选；确认前请人工检查。");
        }
        return patch;
    }

    private void addChapterSummaryOperation(NovelGraphPatchModel patch, NovelChapterEntity chapter) {
        NovelGraphPatchOperationModel operation = baseOperation(patch, "UPDATE_CHAPTER_SUMMARY", "CHAPTER", chapter.getChapterId(), "第" + chapter.getChapterNo() + "章");
        operation.setBeforeStatus(chapter.getStatus());
        operation.setAfterStatus(NovelChapterStatusEnum.PUBLISHED.getValue());
        operation.setBeforeSummary(chapter.getSummary());
        operation.setAfterSummary(chapter.getSummary());
        operation.setConfidence(CONFIDENCE_HIGH);
        operation.setValidationStatus(PATCH_READY);
        operation.setSelected(true);
        operation.setEvidence("章节正文审阅通过。");
        operation.setReason("发布章节时同步更新 Chapter 节点摘要和状态。");
        patch.getOperations().add(operation);
    }

    private void addAppearanceOperation(NovelGraphPatchModel patch, String targetType, Long targetId, String targetName, String content) {
        // 名字真的出现在正文里才默认勾选；没出现的只作为低置信候选，让人来判断。
        boolean mentioned = StringUtils.contains(content, targetName);
        NovelGraphPatchOperationModel operation = baseOperation(patch, "MARK_APPEARANCE", targetType, targetId, targetName);
        operation.setConfidence(mentioned ? CONFIDENCE_HIGH : CONFIDENCE_LOW);
        operation.setValidationStatus(mentioned ? PATCH_READY : PATCH_LOW_CONFIDENCE);
        operation.setSelected(mentioned);
        operation.setEvidence(mentioned ? "正文中出现“" + targetName + "”。" : "来自上下文候选，但正文未显式出现。");
        operation.setReason("记录本章出场，用于后续前章延续和角色/地点检索。");
        patch.getOperations().add(operation);
    }

    private void addClueOperation(NovelGraphPatchModel patch, NovelClueEntity clue, NovelChapterEntity chapter) {
        // 线索推进比出场更敏感，所以即使没显式提到，也标成 MEDIUM 而不是直接丢掉。
        boolean mentioned = StringUtils.contains(chapter.getContent(), clue.getClueName());
        NovelGraphPatchOperationModel operation = baseOperation(patch, "ADVANCE_CLUE", "CLUE", clue.getClueId(), clue.getClueName());
        operation.setBeforeStatus(clue.getClueStatus());
        operation.setAfterStatus(NovelClueStatusEnum.ACTIVE.getValue());
        operation.setBeforeSummary(clue.getSummary());
        operation.setAfterSummary(StringUtils.abbreviate(StringUtils.defaultString(clue.getSummary()) + " 第" + chapter.getChapterNo() + "章出现推进迹象。", 500));
        operation.setConfidence(mentioned ? CONFIDENCE_HIGH : CONFIDENCE_MEDIUM);
        operation.setValidationStatus(PATCH_READY);
        operation.setSelected(true);
        operation.setEvidence(mentioned ? "正文中出现线索“" + clue.getClueName() + "”。" : "使用本章目标线索作为 mock 推进项。");
        operation.setReason("推进线索状态，并建立 Chapter -> Clue 的推进关系。");
        patch.getOperations().add(operation);
    }

    private NovelGraphPatchOperationModel baseOperation(NovelGraphPatchModel patch, String operationType, String targetType, Long targetId, String targetName) {
        NovelGraphPatchOperationModel operation = new NovelGraphPatchOperationModel();
        // 每条 operation 都有自己的 ID，前端勾选和后端生成 inversePatch 都靠它对齐。
        operation.setOperationId(UUID.randomUUID().toString());
        operation.setOperationType(operationType);
        operation.setTargetType(targetType);
        operation.setTargetId(targetId);
        operation.setTargetName(targetName);
        return operation;
    }

    /**
     * 根据正向 GraphPatch 生成反向变更单。
     *
     * 它不是完美的时间机器，但要做到“用户点撤销时，图谱尽量回到确认前”：
     * - 状态、摘要、通用 value 会前后对调；
     * - 出场记录会变成删除 APPEARS_IN；
     * - 线索推进会变成恢复旧线索状态；
     * - 新增节点不物理删除，而是归档，方便排查历史。
     */
    private NovelGraphPatchModel buildInversePatch(NovelGraphPatchModel graphPatch) {
        NovelGraphPatchModel inversePatch = new NovelGraphPatchModel();
        inversePatch.setPatchId(UUID.randomUUID().toString());
        inversePatch.setOperationBatchId(graphPatch.getOperationBatchId());
        inversePatch.setProjectId(graphPatch.getProjectId());
        inversePatch.setChapterId(graphPatch.getChapterId());
        inversePatch.setChapterNo(graphPatch.getChapterNo());
        inversePatch.setStatus(PATCH_READY);
        for (NovelGraphPatchOperationModel operation : graphPatch.getOperations()) {
            NovelGraphPatchOperationModel inverseOperation = new NovelGraphPatchOperationModel();
            // operationId 保持一致，方便前端和 graph_change_log 对照“正向哪条对应反向哪条”。
            inverseOperation.setOperationId(operation.getOperationId());
            inverseOperation.setTargetType(operation.getTargetType());
            inverseOperation.setTargetId(operation.getTargetId());
            inverseOperation.setTargetName(operation.getTargetName());
            inverseOperation.setSourceType(operation.getSourceType());
            inverseOperation.setSourceName(operation.getSourceName());
            inverseOperation.setFromName(operation.getToName());
            inverseOperation.setToName(operation.getFromName());
            inverseOperation.setRelationType(operation.getRelationType());
            inverseOperation.setBeforeStatus(operation.getAfterStatus());
            inverseOperation.setAfterStatus(operation.getBeforeStatus());
            inverseOperation.setBeforeSummary(operation.getAfterSummary());
            inverseOperation.setAfterSummary(operation.getBeforeSummary());
            inverseOperation.setBeforeValue(operation.getAfterValue());
            inverseOperation.setAfterValue(operation.getBeforeValue());
            inverseOperation.setProperties(operation.getProperties());
            inverseOperation.setConfidence(operation.getConfidence());
            inverseOperation.setValidationStatus(operation.getValidationStatus());
            inverseOperation.setSelected(operation.getSelected());
            inverseOperation.setEvidence("反向操作：" + StringUtils.defaultString(operation.getEvidence()));
            inverseOperation.setReason("撤销 GraphPatch 操作。");
            // 不同业务动作的撤销语义不一样，这里把“正向动作”翻译成“反向动作”。
            if ("UPDATE_CHAPTER_SUMMARY".equals(operation.getOperationType())) {
                inverseOperation.setOperationType("RESTORE_CHAPTER_SUMMARY");
            } else if ("MARK_APPEARANCE".equals(operation.getOperationType())) {
                inverseOperation.setOperationType("UNMARK_APPEARANCE");
            } else if ("ADVANCE_CLUE".equals(operation.getOperationType())) {
                inverseOperation.setOperationType("RESTORE_CLUE");
            } else if (StringUtils.startsWith(operation.getOperationType(), "CREATE_")) {
                inverseOperation.setOperationType("ARCHIVE_NODE");
            } else {
                inverseOperation.setOperationType(operation.getOperationType());
            }
            inversePatch.getOperations().add(inverseOperation);
        }
        return inversePatch;
    }

    /**
     * 从 GraphPatch 中筛出真正要执行的操作。
     *
     * 两种模式：
     * - operationIds 为空：按 selected=true 自动选中项执行，低置信项默认不执行；
     * - operationIds 不为空：尊重用户明确勾选，即使它是 LOW_CONFIDENCE 也允许进入执行队列。
     *
     * 注意：这里不处理 BLOCKED / CONFLICT，那是 NovelGraphService.applyGraphPatch 的最后防线。
     */
    private NovelGraphPatchModel filterPatch(NovelGraphPatchModel patch, List<String> operationIds, boolean defaultSelectedOnly) {
        Set<String> operationIdSet = CollectionUtils.isEmpty(operationIds) ? new HashSet<>() : new HashSet<>(operationIds);
        NovelGraphPatchModel filtered = new NovelGraphPatchModel();
        filtered.setPatchId(patch.getPatchId());
        filtered.setOperationBatchId(patch.getOperationBatchId());
        filtered.setProjectId(patch.getProjectId());
        filtered.setChapterId(patch.getChapterId());
        filtered.setChapterNo(patch.getChapterNo());
        filtered.setStatus(patch.getStatus());
        filtered.setWarnings(patch.getWarnings());
        filtered.setOperations(patch.getOperations().stream()
                .filter(operation -> CollectionUtils.isEmpty(operationIds)
                        ? !defaultSelectedOnly || Boolean.TRUE.equals(operation.getSelected())
                        : operationIdSet.contains(operation.getOperationId()))
                .filter(operation -> !PATCH_LOW_CONFIDENCE.equals(operation.getValidationStatus()) || operationIdSet.contains(operation.getOperationId()))
                .collect(Collectors.toList()));
        return filtered;
    }

    private NovelGraphPatchVO buildGraphPatchVO(ChapterGenerationSessionEntity session,
                                                NovelChapterEntity chapter,
                                                NovelGraphPatchModel graphPatch,
                                                NovelGraphPatchModel inversePatch) {
        NovelGraphPatchVO vo = new NovelGraphPatchVO();
        vo.setSessionId(session.getSessionId());
        vo.setSessionStatus(session.getStatus());
        vo.setChapter(toChapterVO(chapter));
        vo.setGraphPatch(graphPatch);
        vo.setInversePatch(inversePatch);
        return vo;
    }

    private NovelWriteRecoverVO buildRecoverVO(ChapterGenerationSessionEntity session) {
        NovelWriteRecoverVO vo = new NovelWriteRecoverVO();
        vo.setSessionId(session.getSessionId());
        vo.setSessionStatus(session.getStatus());
        vo.setOperationBatchId(session.getOperationBatchId());
        NovelChapterEntity chapter = session.getChapterId() == null ? null : novelChapterService.getById(session.getChapterId());
        vo.setChapter(chapter == null ? null : toChapterVO(chapter));
        vo.setChapterIntent(parse(session.getIntentJson(), ChapterIntentModel.class));
        vo.setContextPreview(parse(session.getContextSnapshot(), ContextPreviewModel.class));
        vo.setQualityCheck(parse(session.getContentReviewJson(), ContentQualityCheckModel.class));
        vo.setGraphPatch(parse(session.getGraphPatchJson(), NovelGraphPatchModel.class));
        vo.setInversePatch(parse(session.getInversePatchJson(), NovelGraphPatchModel.class));
        return vo;
    }

    private ChapterGenerationSessionEntity latestSession(Long projectId, Integer chapterNo) {
        LambdaQueryWrapper<ChapterGenerationSessionEntity> wrapper = new LambdaQueryWrapper<ChapterGenerationSessionEntity>()
                .eq(ChapterGenerationSessionEntity::getProjectId, projectId)
                .eq(chapterNo != null, ChapterGenerationSessionEntity::getChapterNo, chapterNo)
                .orderByDesc(ChapterGenerationSessionEntity::getSessionId)
                .last("limit 1");
        return generationSessionDao.selectOne(wrapper);
    }

    private <T> T parse(String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        return JSON.parseObject(json, clazz);
    }

    /**
     * 自动检测现在能用哪个 AI：DeepSeek 不行就通义千问，都没有就 mock。
     *
     * 查 t_user_api_key 表，先看 DeepSeek 的 Key 有没有配，再看通义千问。
     */
    private String resolveAvailableProvider() {
        net.lab1024.sa.admin.module.business.novel.domain.entity.UserApiKeyEntity keyEntity =
                userApiKeyDao.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<net.lab1024.sa.admin.module.business.novel.domain.entity.UserApiKeyEntity>()
                        .eq(net.lab1024.sa.admin.module.business.novel.domain.entity.UserApiKeyEntity::getUserId, AdminRequestUtil.getRequestUserId())
                        .last("limit 1"));
        if (keyEntity != null) {
            if (isValidKey(keyEntity.getDeepseekKey())) {
                return NovelGenerationProviderEnum.DEEPSEEK.getValue();
            }
            if (isValidKey(keyEntity.getQwenKey())) {
                return NovelGenerationProviderEnum.TONGYI.getValue();
            }
        }
        return NovelGenerationProviderEnum.MOCK.getValue();
    }

    /**
     * 判断一个 API Key 能不能用。
     *
     * 先当它是加密的试解密，解出来了就是有效的。
     * 解不出来但看着像明文（sk-开头 或 含 dashscope）也当有效。
     */
    private boolean isValidKey(String encrypted) {
        if (StringUtils.isBlank(encrypted)) {
            return false;
        }
        String decrypted = apiEncryptService.decrypt(encrypted);
        if (StringUtils.isNotBlank(decrypted)) {
            return true;
        }
        if (encrypted.startsWith("sk-") || encrypted.contains("dashscope")) {
            return true;
        }
        return false;
    }

    private String buildPromptSnapshot(NovelProjectEntity project,
                                        ChapterIntentModel intent,
                                        ContextPreviewModel contextPreview,
                                        String provider) {
        return provider + ":/write " + intent.getChapterNo() + " pov=" + intent.getPov();
    }

    private NovelChapterVO toChapterVO(NovelChapterEntity chapter) {
        return SmartBeanUtil.copy(chapter, NovelChapterVO.class);
    }

    /**
     * 把角色、地点、线索拼成一段可读文本，降级时用。
     */
    private String buildContextSnapshot(NovelProjectEntity project,
                                        List<NovelCharacterEntity> characters,
                                        List<NovelLocationEntity> locations,
                                        List<NovelClueEntity> clues) {
        return "项目=" + project.getProjectName()
                + "；角色=" + characters.stream().map(NovelCharacterEntity::getCharacterName).collect(Collectors.joining("，"))
                + "；地点=" + locations.stream().map(NovelLocationEntity::getLocationName).collect(Collectors.joining("，"))
                + "；线索=" + clues.stream().map(NovelClueEntity::getClueName).collect(Collectors.joining("，"));
    }

    /**
     * 构建中文 mock 草稿。
     */
    private String buildMockContent(NovelProjectEntity project,
                                    Integer chapterNo,
                                    List<NovelCharacterEntity> characters,
                                    List<NovelLocationEntity> locations,
                                    List<NovelClueEntity> clues) {
        String protagonist = StringUtils.defaultIfBlank(project.getProtagonist(), firstCharacter(characters));
        String location = firstLocation(locations);
        String clue = firstClue(clues);
        return """
                # %s

                这是《%s》的第 %d 章降级生成内容。
                %s 来到%s，带着一个明确目标：推动剧情向前，同时不丢掉故事最初承诺给读者的期待。当前作品类型是"%s"，本章会围绕写作闭环展开。
                本章显性线索是"%s"。它暂时不会被解决，而是先埋入场景中，方便后续追踪线索状态。
                章节结尾，%s 做出一个看似很小、却无法撤回的选择，为下一章留下继续推进的入口。
                """.formatted(
                "第" + chapterNo + "章",
                project.getProjectName(),
                chapterNo,
                protagonist,
                location,
                StringUtils.defaultIfBlank(project.getGenre(), "未分类"),
                clue,
                protagonist
        );
    }

    private String firstCharacter(List<NovelCharacterEntity> characters) {
        return characters.isEmpty() ? "主角" : characters.get(0).getCharacterName();
    }

    private String firstLocation(List<NovelLocationEntity> locations) {
        return locations.isEmpty() ? "开场地点" : locations.get(0).getLocationName();
    }

    private String firstClue(List<NovelClueEntity> clues) {
        return clues.isEmpty() ? "第一条未解线索" : clues.get(0).getClueName();
    }
}
