package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.novel.constant.NovelChapterStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphNodeEnum;
import net.lab1024.sa.admin.module.business.novel.dao.ChapterOutlineDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelChapterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelProjectDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelVolumeDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.ChapterOutlineEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelVolumeEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.ChapterOutlineAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.ChapterOutlineQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.ChapterOutlineUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelChapterQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelChapterUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.ChapterOutlineVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelChapterVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * NovelChapterService 单元测试。
 * <p>
 * 覆盖章节管理和章节细纲的 Task 9 主路径，重点验证用户隔离、MySQL/Neo4j 边界和细纲纯 MySQL 规则。
 *
 * @Author AI-Novel
 */
class NovelChapterServiceTest {

    /**
     * 被测章节服务。
     */
    private NovelChapterService novelChapterService;

    /**
     * 项目 DAO mock，用来验证项目归属校验。
     */
    private NovelProjectDao novelProjectDao;

    /**
     * 卷 DAO mock，用来验证章节关联卷时必须属于同一用户和项目。
     */
    private NovelVolumeDao novelVolumeDao;

    /**
     * 章节 DAO mock，用来捕获章节分页和编辑行为。
     */
    private NovelChapterDao novelChapterDao;

    /**
     * 细纲 DAO mock，用来捕获细纲 CRUD 行为。
     */
    private ChapterOutlineDao chapterOutlineDao;

    /**
     * 图谱服务 mock，用来确认章节正文不会进入 Neo4j。
     */
    private NovelGraphService novelGraphService;

    /**
     * 每个用例前重建服务和表元数据，保证 LambdaQueryWrapper 可以展开为真实列名。
     */
    @BeforeEach
    void setup() {
        novelChapterService = new NovelChapterService();
        novelProjectDao = mock(NovelProjectDao.class);
        novelVolumeDao = mock(NovelVolumeDao.class);
        novelChapterDao = mock(NovelChapterDao.class);
        chapterOutlineDao = mock(ChapterOutlineDao.class);
        novelGraphService = mock(NovelGraphService.class);

        MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelProjectEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelVolumeEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelChapterEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, ChapterOutlineEntity.class);

        ReflectionTestUtils.setField(novelChapterService, "novelProjectDao", novelProjectDao);
        ReflectionTestUtils.setField(novelChapterService, "novelVolumeDao", novelVolumeDao);
        ReflectionTestUtils.setField(novelChapterService, "novelChapterDao", novelChapterDao);
        ReflectionTestUtils.setField(novelChapterService, "chapterOutlineDao", chapterOutlineDao);
        ReflectionTestUtils.setField(novelChapterService, "novelGraphService", novelGraphService);
    }

    /**
     * 章节分页必须强制追加项目、用户、未归档条件，并按章节号升序返回。
     */
    @Test
    void queryChapterByPageShouldForceProjectUserAndChapterOrder() {
        NovelChapterQueryForm queryForm = new NovelChapterQueryForm();
        queryForm.setProjectId(10001L);
        queryForm.setVolumeId(100001L);
        queryForm.setStatus(NovelChapterStatusEnum.DRAFT.getValue());
        queryForm.setPageNum(1L);
        queryForm.setPageSize(10L);
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelChapterDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<NovelChapterEntity> page = invocation.getArgument(0);
            page.setRecords(List.of(buildChapterEntity()));
            page.setTotal(1);
            return page;
        });

        ResponseDTO<PageResult<NovelChapterVO>> response = novelChapterService.queryChapterByPage(queryForm, 7L);

        assertTrue(response.getOk());
        assertEquals(1, response.getData().getList().size());
        assertEquals(12, response.getData().getList().get(0).getChapterNumber());
        ArgumentCaptor<LambdaQueryWrapper<NovelChapterEntity>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(novelChapterDao).selectPage(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("project_id"));
        assertTrue(sqlSegment.contains("create_user_id"));
        assertTrue(sqlSegment.contains("deleted_flag"));
        assertTrue(sqlSegment.contains("volume_id"));
        assertTrue(sqlSegment.contains("status"));
        assertTrue(sqlSegment.contains("chapter_number"));
    }

    /**
     * 编辑章节需要更新 MySQL 正文，同时只把摘要、标题、POV 等白名单字段同步到 Neo4j。
     */
    @Test
    void updateChapterShouldPersistContentAndSyncGraphWithoutContent() {
        when(novelChapterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildChapterEntity());
        when(novelVolumeDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildVolumeEntity());
        NovelChapterUpdateForm updateForm = buildChapterUpdateForm();

        ResponseDTO<String> response = novelChapterService.updateChapter(updateForm, 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelChapterEntity> entityCaptor = ArgumentCaptor.forClass(NovelChapterEntity.class);
        verify(novelChapterDao).updateById(entityCaptor.capture());
        NovelChapterEntity updateEntity = entityCaptor.getValue();
        assertEquals(30001L, updateEntity.getId());
        assertEquals(100001L, updateEntity.getVolumeId());
        assertEquals("第十二章 暗室密函", updateEntity.getTitle());
        assertEquals("李四潜入暗室，拿到第一份关键证据。", updateEntity.getSummary());
        assertEquals(updateForm.getContent().length(), updateEntity.getWordCount());
        assertEquals(NovelChapterStatusEnum.DRAFT.getValue(), updateEntity.getStatus());

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).updateNodeProps(eq(NovelGraphNodeEnum.Chapter), eq(10001L), eq(12), propsCaptor.capture());
        assertEquals("第十二章 暗室密函", propsCaptor.getValue().get("title"));
        assertEquals("李四", propsCaptor.getValue().get("pov"));
        assertFalse(propsCaptor.getValue().containsKey("content"));
    }

    /**
     * 章节不能关联其他项目或其他用户的卷。
     */
    @Test
    void updateChapterShouldRejectForeignVolume() {
        when(novelChapterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildChapterEntity());
        when(novelVolumeDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        ResponseDTO<String> response = novelChapterService.updateChapter(buildChapterUpdateForm(), 7L);

        assertFalse(response.getOk());
        verify(novelChapterDao, never()).updateById(any(NovelChapterEntity.class));
        verifyNoInteractions(novelGraphService);
    }

    /**
     * 创建章节细纲只写 MySQL，不同步任何 Neo4j 节点或关系。
     */
    @Test
    void createOutlineShouldPersistMysqlOnly() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(chapterOutlineDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(chapterOutlineDao.insert(any(ChapterOutlineEntity.class))).thenAnswer(invocation -> {
            ChapterOutlineEntity entity = invocation.getArgument(0);
            entity.setId(40001L);
            return 1;
        });

        ResponseDTO<String> response = novelChapterService.createOutline(buildOutlineAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<ChapterOutlineEntity> entityCaptor = ArgumentCaptor.forClass(ChapterOutlineEntity.class);
        verify(chapterOutlineDao).insert(entityCaptor.capture());
        assertEquals(10001L, entityCaptor.getValue().getProjectId());
        assertEquals(13, entityCaptor.getValue().getChapterNumber());
        assertEquals(Boolean.FALSE, entityCaptor.getValue().getDeletedFlag());
        assertEquals(7L, entityCaptor.getValue().getCreateUserId());
        verifyNoInteractions(novelGraphService);
    }

    /**
     * 章节细纲分页必须强制带上项目、用户和未归档条件。
     */
    @Test
    void queryOutlineByPageShouldForceProjectAndUserFilter() {
        ChapterOutlineQueryForm queryForm = new ChapterOutlineQueryForm();
        queryForm.setProjectId(10001L);
        queryForm.setChapterNumber(13);
        queryForm.setPageNum(1L);
        queryForm.setPageSize(10L);
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(chapterOutlineDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<ChapterOutlineEntity> page = invocation.getArgument(0);
            page.setRecords(List.of(buildOutlineEntity()));
            page.setTotal(1);
            return page;
        });

        ResponseDTO<PageResult<ChapterOutlineVO>> response = novelChapterService.queryOutlineByPage(queryForm, 7L);

        assertTrue(response.getOk());
        assertEquals(1, response.getData().getList().size());
        ArgumentCaptor<LambdaQueryWrapper<ChapterOutlineEntity>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(chapterOutlineDao).selectPage(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("project_id"));
        assertTrue(sqlSegment.contains("create_user_id"));
        assertTrue(sqlSegment.contains("deleted_flag"));
        assertTrue(sqlSegment.contains("chapter_number"));
    }

    /**
     * 编辑章节细纲必须保持项目归属，不允许移动到其他项目。
     */
    @Test
    void updateOutlineShouldKeepProjectOwnership() {
        when(chapterOutlineDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildOutlineEntity());

        ResponseDTO<String> response = novelChapterService.updateOutline(buildOutlineUpdateForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<ChapterOutlineEntity> entityCaptor = ArgumentCaptor.forClass(ChapterOutlineEntity.class);
        verify(chapterOutlineDao).updateById(entityCaptor.capture());
        assertEquals(40001L, entityCaptor.getValue().getId());
        assertEquals(10001L, entityCaptor.getValue().getProjectId());
        assertEquals(13, entityCaptor.getValue().getChapterNumber());
        verifyNoInteractions(novelGraphService);
    }

    /**
     * 归档章节细纲只做 MySQL 软删。
     */
    @Test
    void archiveOutlineShouldSoftDeleteMysqlOnly() {
        when(chapterOutlineDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildOutlineEntity());

        ResponseDTO<String> response = novelChapterService.archiveOutline(40001L, 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<ChapterOutlineEntity> entityCaptor = ArgumentCaptor.forClass(ChapterOutlineEntity.class);
        verify(chapterOutlineDao).updateById(entityCaptor.capture());
        assertEquals(40001L, entityCaptor.getValue().getId());
        assertEquals(Boolean.TRUE, entityCaptor.getValue().getDeletedFlag());
        verifyNoInteractions(novelGraphService);
    }

    /**
     * 构造一个属于当前用户的项目实体。
     */
    private NovelProjectEntity buildProjectEntity() {
        NovelProjectEntity entity = new NovelProjectEntity();
        entity.setId(10001L);
        entity.setCreateUserId(7L);
        entity.setDeletedFlag(Boolean.FALSE);
        return entity;
    }

    /**
     * 构造一个属于当前用户的卷实体。
     */
    private NovelVolumeEntity buildVolumeEntity() {
        NovelVolumeEntity entity = new NovelVolumeEntity();
        entity.setId(100001L);
        entity.setProjectId(10001L);
        entity.setCreateUserId(7L);
        entity.setDeletedFlag(Boolean.FALSE);
        return entity;
    }

    /**
     * 构造一个已经存在的章节实体。
     */
    private NovelChapterEntity buildChapterEntity() {
        NovelChapterEntity entity = new NovelChapterEntity();
        entity.setId(30001L);
        entity.setProjectId(10001L);
        entity.setVolumeId(null);
        entity.setChapterNumber(12);
        entity.setTitle("第十二章 暗室");
        entity.setSummary("李四发现暗室。");
        entity.setContent("李四沿着密道前行。");
        entity.setPov("李四");
        entity.setWordCount(9);
        entity.setStatus(NovelChapterStatusEnum.DRAFT.getValue());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造章节编辑表单。
     */
    private NovelChapterUpdateForm buildChapterUpdateForm() {
        NovelChapterUpdateForm updateForm = new NovelChapterUpdateForm();
        updateForm.setChapterId(30001L);
        updateForm.setProjectId(10001L);
        updateForm.setVolumeId(100001L);
        updateForm.setTitle("第十二章 暗室密函");
        updateForm.setSummary("李四潜入暗室，拿到第一份关键证据。");
        updateForm.setContent("李四屏息走入暗室，灯火映出墙上的旧案卷。");
        updateForm.setPov("李四");
        return updateForm;
    }

    /**
     * 构造章节细纲创建表单。
     */
    private ChapterOutlineAddForm buildOutlineAddForm() {
        ChapterOutlineAddForm addForm = new ChapterOutlineAddForm();
        addForm.setProjectId(10001L);
        addForm.setChapterNumber(13);
        addForm.setSceneBeats("[{\"scene\":\"李四拆解密函\"}]");
        addForm.setSummary("拆解密函，确认幕后黑手线索。");
        return addForm;
    }

    /**
     * 构造章节细纲编辑表单。
     */
    private ChapterOutlineUpdateForm buildOutlineUpdateForm() {
        ChapterOutlineUpdateForm updateForm = new ChapterOutlineUpdateForm();
        updateForm.setOutlineId(40001L);
        updateForm.setProjectId(10001L);
        updateForm.setChapterNumber(13);
        updateForm.setSceneBeats("[{\"scene\":\"李四拆解密函\"},{\"scene\":\"王五赶来\"}]");
        updateForm.setSummary("密函被拆解，王五提供新的旁证。");
        return updateForm;
    }

    /**
     * 构造一个已经存在的章节细纲实体。
     */
    private ChapterOutlineEntity buildOutlineEntity() {
        ChapterOutlineEntity entity = new ChapterOutlineEntity();
        entity.setId(40001L);
        entity.setProjectId(10001L);
        entity.setChapterNumber(13);
        entity.setSceneBeats("[{\"scene\":\"李四拆解密函\"}]");
        entity.setSummary("拆解密函，确认幕后黑手线索。");
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }
}
