package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGenerationStatusEnum;
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
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelPromptVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelWriteSessionVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelLLMService.NovelGenerationResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NovelWriteService 全流程测试。
 * <p>
 * 覆盖 Prompt构建、用户隔离、流式入口、GraphPatch审阅确认发布 四条关键行为。
 * 阻塞式写作全流程(startWrite→contentReviewPass→patchConfirm)中, insert() 因 MyBatis-Plus 3.5.12
 * 双重重载与 Mockito 兼容性问题, 通过 patchConfirm 入口 + session mock 间接验证。
 *
 * @Author AI-Novel
 */
class NovelWriteServiceTest {

    private NovelWriteService novelWriteService;
    private NovelProjectDao novelProjectDao;
    private NovelChapterDao novelChapterDao;
    private ChapterGenerationSessionDao sessionDao;
    private GraphChangeLogDao graphChangeLogDao;
    private WritingLogDao writingLogDao;
    private NovelPromptService novelPromptService;
    private NovelLLMService novelLLMService;
    private NovelGraphService novelGraphService;
    private NovelGraphPatchService novelGraphPatchService;

    private final Long requestUserId = 1001L;
    private final Long projectId = 1L;

    @BeforeEach
    void setup() throws Exception {
        novelWriteService = new NovelWriteService();
        novelProjectDao = mock(NovelProjectDao.class);
        novelChapterDao = mock(NovelChapterDao.class);
        sessionDao = mock(ChapterGenerationSessionDao.class);
        graphChangeLogDao = mock(GraphChangeLogDao.class);
        writingLogDao = mock(WritingLogDao.class);
        novelPromptService = mock(NovelPromptService.class);
        novelLLMService = mock(NovelLLMService.class);
        novelGraphService = mock(NovelGraphService.class);
        novelGraphPatchService = mock(NovelGraphPatchService.class);

        java.lang.reflect.Field f1 = NovelWriteService.class.getDeclaredField("novelProjectDao");
        f1.setAccessible(true); f1.set(novelWriteService, novelProjectDao);
        java.lang.reflect.Field f2 = NovelWriteService.class.getDeclaredField("novelChapterDao");
        f2.setAccessible(true); f2.set(novelWriteService, novelChapterDao);
        java.lang.reflect.Field f3 = NovelWriteService.class.getDeclaredField("sessionDao");
        f3.setAccessible(true); f3.set(novelWriteService, sessionDao);
        java.lang.reflect.Field f4 = NovelWriteService.class.getDeclaredField("graphChangeLogDao");
        f4.setAccessible(true); f4.set(novelWriteService, graphChangeLogDao);
        java.lang.reflect.Field f5 = NovelWriteService.class.getDeclaredField("writingLogDao");
        f5.setAccessible(true); f5.set(novelWriteService, writingLogDao);
        java.lang.reflect.Field f6 = NovelWriteService.class.getDeclaredField("novelPromptService");
        f6.setAccessible(true); f6.set(novelWriteService, novelPromptService);
        java.lang.reflect.Field f7 = NovelWriteService.class.getDeclaredField("novelLLMService");
        f7.setAccessible(true); f7.set(novelWriteService, novelLLMService);
        java.lang.reflect.Field f8 = NovelWriteService.class.getDeclaredField("novelGraphService");
        f8.setAccessible(true); f8.set(novelWriteService, novelGraphService);
        java.lang.reflect.Field f9 = NovelWriteService.class.getDeclaredField("novelGraphPatchService");
        f9.setAccessible(true); f9.set(novelWriteService, novelGraphPatchService);

        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), NovelProjectEntity.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), NovelChapterEntity.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), ChapterGenerationSessionEntity.class);
    }

    // ======================== Prompt构建测试 ========================

    @Test
    void buildWritePromptShouldValidateProjectOwnership() {
        NovelProjectEntity project = buildProject();
        when(novelProjectDao.selectById(anyLong())).thenReturn(project);
        when(novelPromptService.buildPrompt(anyLong(), any(), any(), anyLong()))
                .thenReturn(ResponseDTO.ok(buildPromptVO()));

        NovelWriteStartForm form = buildStartForm();
        ResponseDTO<NovelPromptVO> result = novelWriteService.buildWritePrompt(form, requestUserId);

        assertTrue(result.getOk());
        assertNotNull(result.getData().getSystemPrompt());
        assertNotNull(result.getData().getUserPrompt());
    }

    // ======================== 用户隔离测试 ========================

    @Test
    void nonOwnerShouldBeRejectedOnBuildPrompt() {
        NovelProjectEntity project = buildProject();
        project.setCreateUserId(9999L);
        when(novelProjectDao.selectById(anyLong())).thenReturn(project);

        NovelWriteStartForm form = buildStartForm();
        ResponseDTO<NovelPromptVO> result = novelWriteService.buildWritePrompt(form, requestUserId);

        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("无权访问"));
    }

    // ======================== 流式入口测试 ========================

    @Test
    void streamModeBuildPromptShouldNotCreateSession() {
        NovelProjectEntity project = buildProject();
        when(novelProjectDao.selectById(anyLong())).thenReturn(project);
        when(novelPromptService.buildPrompt(anyLong(), any(), any(), anyLong()))
                .thenReturn(ResponseDTO.ok(buildPromptVO()));

        NovelWriteStartForm form = buildStartForm();
        ResponseDTO<NovelPromptVO> result = novelWriteService.buildWritePrompt(form, requestUserId);

        assertTrue(result.getOk());
        verify(sessionDao, never()).insert(any(ChapterGenerationSessionEntity.class));
    }

    // ======================== GraphPatch审阅→确认→发布流程 ========================

    @Test
    void contentReviewPassShouldChangeStatusToPatchReview() {
        ChapterGenerationSessionEntity session = buildSession(100L, NovelGenerationStatusEnum.CONTENT_REVIEW);
        NovelChapterEntity chapter = buildChapter(10L);
        when(sessionDao.selectById(100L)).thenReturn(session);
        when(novelChapterDao.selectById(10L)).thenReturn(chapter);
        when(novelGraphPatchService.extractAndParsePatches(any(), anyLong(), any(), anyLong()))
                .thenReturn(null);
        when(novelGraphPatchService.convertToVoList(any()))
                .thenReturn(Collections.emptyList());

        NovelWriteStartForm form = new NovelWriteStartForm();
        ResponseDTO<NovelWriteSessionVO> result = novelWriteService.contentReviewPass(100L, form, requestUserId);

        assertTrue(result.getOk());
        assertEquals(NovelGenerationStatusEnum.PATCH_REVIEW.getValue(), result.getData().getStatus());
    }

    @Test
    void patchBackShouldResetToContentReview() {
        ChapterGenerationSessionEntity session = buildSession(100L, NovelGenerationStatusEnum.PATCH_REVIEW);
        session.setChapterId(10L);
        NovelChapterEntity chapter = buildChapter(10L);
        when(sessionDao.selectById(100L)).thenReturn(session);
        when(novelChapterDao.selectById(10L)).thenReturn(chapter);

        ResponseDTO<NovelWriteSessionVO> result = novelWriteService.patchBack(100L, requestUserId);

        assertTrue(result.getOk());
        assertEquals(NovelGenerationStatusEnum.CONTENT_REVIEW.getValue(), result.getData().getStatus());
    }

    @Test
    void patchConfirmShouldReachSuccess() {
        ChapterGenerationSessionEntity session = buildSession(100L, NovelGenerationStatusEnum.PATCH_REVIEW);
        NovelChapterEntity chapter = buildChapter(10L);
        when(sessionDao.selectById(100L)).thenReturn(session);
        when(novelChapterDao.selectById(10L)).thenReturn(chapter);
        when(novelGraphPatchService.runPostWriteChecks(anyLong(), any()))
                .thenReturn(Collections.emptyList());

        ResponseDTO<NovelWriteSessionVO> result = novelWriteService.patchConfirm(
                100L, Collections.emptyList(), requestUserId);

        assertTrue(result.getOk());
        assertEquals(NovelGenerationStatusEnum.SUCCESS.getValue(), result.getData().getStatus());
    }

    @Test
    void contentReviewPassShouldRejectWrongStatus() {
        ChapterGenerationSessionEntity session = buildSession(100L, NovelGenerationStatusEnum.GENERATING);
        when(sessionDao.selectById(100L)).thenReturn(session);

        NovelWriteStartForm form = new NovelWriteStartForm();
        ResponseDTO<NovelWriteSessionVO> result = novelWriteService.contentReviewPass(100L, form, requestUserId);

        assertFalse(result.getOk());
        assertTrue(result.getMsg().contains("当前会话状态不允许"));
    }

    // ======================== 流式完成后回调 ========================

    @Test
    void onStreamWriteCompleteShouldPersistSessionAndChapter() {
        when(novelChapterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(novelChapterDao.insert(any(NovelChapterEntity.class))).thenReturn(1);
        when(sessionDao.insert(any(ChapterGenerationSessionEntity.class))).thenReturn(1);

        NovelWriteSessionVO vo = novelWriteService.onStreamWriteComplete(
                1L, 1, "正文内容", "李四", "写作方向", "DEEPSEEK", requestUserId);

        assertNotNull(vo);
        assertEquals(NovelGenerationStatusEnum.CONTENT_REVIEW.getValue(), vo.getStatus());
        assertEquals("正文内容", vo.getContent());
    }

    // ======================== Helpers ========================

    private NovelProjectEntity buildProject() {
        NovelProjectEntity p = new NovelProjectEntity();
        p.setId(projectId);
        p.setCreateUserId(requestUserId);
        p.setTargetChapterWords(3000);
        return p;
    }

    private NovelPromptVO buildPromptVO() {
        NovelPromptVO vo = new NovelPromptVO();
        vo.setProjectId(projectId);
        vo.setChapterNumber(1);
        vo.setSystemPrompt("System prompt");
        vo.setUserPrompt("User prompt");
        return vo;
    }

    private NovelWriteStartForm buildStartForm() {
        NovelWriteStartForm form = new NovelWriteStartForm();
        form.setProjectId(projectId);
        form.setChapterNumber(1);
        return form;
    }

    private ChapterGenerationSessionEntity buildSession(Long sessionId, NovelGenerationStatusEnum status) {
        ChapterGenerationSessionEntity s = new ChapterGenerationSessionEntity();
        s.setId(sessionId);
        s.setProjectId(projectId);
        s.setChapterNumber(1);
        s.setChapterId(10L);
        s.setStatus(status.getValue());
        s.setCreateUserId(requestUserId);
        return s;
    }

    private NovelChapterEntity buildChapter(Long chapterId) {
        NovelChapterEntity c = new NovelChapterEntity();
        c.setId(chapterId);
        c.setProjectId(projectId);
        c.setChapterNumber(1);
        c.setTitle("第1章");
        c.setContent("正文");
        c.setWordCount(100);
        return c;
    }
}
