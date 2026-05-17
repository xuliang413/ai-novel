package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterRoleEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelAliasTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCheatTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueSubTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueToneEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelFamilyTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphNodeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphRelationEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelItemStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelItemTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelLocationTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelLoveStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelRelationTypeEnum;
import net.lab1024.sa.admin.module.business.novel.dao.NovelAliasDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterRelationDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCheatDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelClueDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelEventDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelItemDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelLocationDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelNarrativeRuleDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelProjectDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelVolumeDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelAliasEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterRelationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCheatEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelEventEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelItemEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelNarrativeRuleEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelVolumeEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelAliasAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelAliasQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterRelationAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterRelationQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterRelationUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCheatAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCheatQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelClueAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelClueQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelEventAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelEventQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelItemAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelItemQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelLocationAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelLocationQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelNarrativeRuleAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelNarrativeRuleQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelVolumeAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelVolumeQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelAliasVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelCharacterVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelCharacterRelationVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelCheatVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelClueVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelEventVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelItemVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelLocationVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelNarrativeRuleVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelVolumeVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
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
 * NovelAssetService 单元测试。
 * <p>
 * 先覆盖 Task 7a 的角色和地点 CRUD 主路径，重点验证项目归属、用户隔离和 Neo4j 同步。
 *
 * @Author AI-Novel
 */
class NovelAssetServiceTest {

    /**
     * 被测资产服务，当前测试只覆盖角色和地点部分。
     */
    private NovelAssetService novelAssetService;

    /**
     * 项目 DAO mock，用来验证资产创建前必须校验项目归属。
     */
    private NovelProjectDao novelProjectDao;

    /**
     * 角色 DAO mock，用来捕获角色写入和角色分页查询条件。
     */
    private NovelCharacterDao novelCharacterDao;

    /**
     * 地点 DAO mock，用来捕获地点写入和地点分页查询条件。
     */
    private NovelLocationDao novelLocationDao;

    /**
     * 线索 DAO mock，用来捕获线索写入和线索分页查询条件。
     */
    private NovelClueDao novelClueDao;

    /**
     * 物品 DAO mock，用来捕获物品写入和物品分页查询条件。
     */
    private NovelItemDao novelItemDao;

    /**
     * 事件 DAO mock，用来捕获事件写入和事件分页查询条件。
     */
    private NovelEventDao novelEventDao;

    /**
     * 金手指 DAO mock，用来捕获金手指写入和分页查询条件。
     */
    private NovelCheatDao novelCheatDao;

    /**
     * 马甲 DAO mock，用来捕获马甲写入和分页查询条件。
     */
    private NovelAliasDao novelAliasDao;

    /**
     * 叙事规则 DAO mock，用来验证规则只落 MySQL，不进入 Neo4j。
     */
    private NovelNarrativeRuleDao novelNarrativeRuleDao;

    /**
     * 卷 DAO mock，用来捕获卷写入和分页查询条件。
     */
    private NovelVolumeDao novelVolumeDao;

    /**
     * 角色关系 DAO mock，用来捕获关系写入、分页查询和归档条件。
     */
    private NovelCharacterRelationDao novelCharacterRelationDao;

    /**
     * 图谱服务 mock，用来验证角色和地点是否同步到 Neo4j。
     */
    private NovelGraphService novelGraphService;

    /**
     * 每个用例前重建服务和表元数据，保证 LambdaQueryWrapper 可以展开为真实列名。
     */
    @BeforeEach
    void setup() {
        novelAssetService = new NovelAssetService();
        novelProjectDao = mock(NovelProjectDao.class);
        novelCharacterDao = mock(NovelCharacterDao.class);
        novelLocationDao = mock(NovelLocationDao.class);
        novelClueDao = mock(NovelClueDao.class);
        novelItemDao = mock(NovelItemDao.class);
        novelEventDao = mock(NovelEventDao.class);
        novelCheatDao = mock(NovelCheatDao.class);
        novelAliasDao = mock(NovelAliasDao.class);
        novelNarrativeRuleDao = mock(NovelNarrativeRuleDao.class);
        novelVolumeDao = mock(NovelVolumeDao.class);
        novelCharacterRelationDao = mock(NovelCharacterRelationDao.class);
        novelGraphService = mock(NovelGraphService.class);
        MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelProjectEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelCharacterEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelLocationEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelClueEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelItemEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelEventEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelCheatEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelAliasEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelNarrativeRuleEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelVolumeEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelCharacterRelationEntity.class);
        ReflectionTestUtils.setField(novelAssetService, "novelProjectDao", novelProjectDao);
        ReflectionTestUtils.setField(novelAssetService, "novelCharacterDao", novelCharacterDao);
        ReflectionTestUtils.setField(novelAssetService, "novelLocationDao", novelLocationDao);
        ReflectionTestUtils.setField(novelAssetService, "novelClueDao", novelClueDao);
        ReflectionTestUtils.setField(novelAssetService, "novelItemDao", novelItemDao);
        ReflectionTestUtils.setField(novelAssetService, "novelEventDao", novelEventDao);
        ReflectionTestUtils.setField(novelAssetService, "novelCheatDao", novelCheatDao);
        ReflectionTestUtils.setField(novelAssetService, "novelAliasDao", novelAliasDao);
        ReflectionTestUtils.setField(novelAssetService, "novelNarrativeRuleDao", novelNarrativeRuleDao);
        ReflectionTestUtils.setField(novelAssetService, "novelVolumeDao", novelVolumeDao);
        ReflectionTestUtils.setField(novelAssetService, "novelCharacterRelationDao", novelCharacterRelationDao);
        ReflectionTestUtils.setField(novelAssetService, "novelGraphService", novelGraphService);
    }

    /**
     * 创建角色必须先校验项目属于当前用户，再写入默认状态并同步 Character 节点。
     */
    @Test
    void createCharacterShouldPersistOwnedProjectAndSyncGraph() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelCharacterDao.insert(any(NovelCharacterEntity.class))).thenAnswer(invocation -> {
            NovelCharacterEntity entity = invocation.getArgument(0);
            entity.setId(20001L);
            return 1;
        });

        ResponseDTO<String> response = novelAssetService.createCharacter(buildCharacterAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelCharacterEntity> entityCaptor = ArgumentCaptor.forClass(NovelCharacterEntity.class);
        verify(novelCharacterDao).insert(entityCaptor.capture());
        NovelCharacterEntity insertEntity = entityCaptor.getValue();
        assertEquals(10001L, insertEntity.getProjectId());
        assertEquals(7L, insertEntity.getCreateUserId());
        assertEquals(Boolean.FALSE, insertEntity.getDeletedFlag());
        assertEquals(NovelCharacterStatusEnum.ACTIVE.getValue(), insertEntity.getCurrentStatus());

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeCharacter(eq(10001L), eq(20001L), propsCaptor.capture());
        assertEquals("李四", propsCaptor.getValue().get("name"));
        assertEquals(NovelCharacterRoleEnum.PROTAGONIST.getValue(), propsCaptor.getValue().get("roleType"));
    }

    /**
     * 当前用户不拥有项目时，角色创建必须拒绝，避免把资产挂到别人的项目下。
     */
    @Test
    void createCharacterShouldRejectOtherUserProject() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        ResponseDTO<String> response = novelAssetService.createCharacter(buildCharacterAddForm(), 7L);

        assertFalse(response.getOk());
        verify(novelCharacterDao, never()).insert(any(NovelCharacterEntity.class));
        verifyNoInteractions(novelGraphService);
    }

    /**
     * 角色分页查询必须强制带上 projectId、createUserId 和 deletedFlag 条件。
     */
    @Test
    void queryCharacterByPageShouldForceProjectAndUserFilter() {
        NovelCharacterQueryForm queryForm = new NovelCharacterQueryForm();
        queryForm.setProjectId(10001L);
        queryForm.setPageNum(1L);
        queryForm.setPageSize(10L);
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelCharacterDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<NovelCharacterEntity> page = invocation.getArgument(0);
            page.setRecords(List.of(buildCharacterEntity()));
            page.setTotal(1);
            return page;
        });

        ResponseDTO<PageResult<NovelCharacterVO>> response = novelAssetService.queryCharacterByPage(queryForm, 7L);

        assertTrue(response.getOk());
        assertEquals(1, response.getData().getList().size());
        assertEquals("李四", response.getData().getList().get(0).getName());
        ArgumentCaptor<LambdaQueryWrapper<NovelCharacterEntity>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(novelCharacterDao).selectPage(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("project_id"));
        assertTrue(sqlSegment.contains("create_user_id"));
        assertTrue(sqlSegment.contains("deleted_flag"));
    }

    /**
     * 创建地点必须先校验项目属于当前用户，再写入地点设定并同步 Location 节点。
     */
    @Test
    void createLocationShouldPersistOwnedProjectAndSyncGraph() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelLocationDao.insert(any(NovelLocationEntity.class))).thenAnswer(invocation -> {
            NovelLocationEntity entity = invocation.getArgument(0);
            entity.setId(30001L);
            return 1;
        });

        ResponseDTO<String> response = novelAssetService.createLocation(buildLocationAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelLocationEntity> entityCaptor = ArgumentCaptor.forClass(NovelLocationEntity.class);
        verify(novelLocationDao).insert(entityCaptor.capture());
        NovelLocationEntity insertEntity = entityCaptor.getValue();
        assertEquals(10001L, insertEntity.getProjectId());
        assertEquals(7L, insertEntity.getCreateUserId());
        assertEquals(Boolean.FALSE, insertEntity.getDeletedFlag());

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeLocation(eq(10001L), eq(30001L), propsCaptor.capture());
        assertEquals("青云宗", propsCaptor.getValue().get("name"));
        assertEquals(NovelLocationTypeEnum.SECT.getValue(), propsCaptor.getValue().get("type"));
    }

    /**
     * 地点分页查询必须强制带上 projectId、createUserId 和 deletedFlag 条件。
     */
    @Test
    void queryLocationByPageShouldForceProjectAndUserFilter() {
        NovelLocationQueryForm queryForm = new NovelLocationQueryForm();
        queryForm.setProjectId(10001L);
        queryForm.setPageNum(1L);
        queryForm.setPageSize(10L);
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelLocationDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<NovelLocationEntity> page = invocation.getArgument(0);
            page.setRecords(List.of(buildLocationEntity()));
            page.setTotal(1);
            return page;
        });

        ResponseDTO<PageResult<NovelLocationVO>> response = novelAssetService.queryLocationByPage(queryForm, 7L);

        assertTrue(response.getOk());
        assertEquals(1, response.getData().getList().size());
        assertEquals("青云宗", response.getData().getList().get(0).getName());
        ArgumentCaptor<LambdaQueryWrapper<NovelLocationEntity>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(novelLocationDao).selectPage(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("project_id"));
        assertTrue(sqlSegment.contains("create_user_id"));
        assertTrue(sqlSegment.contains("deleted_flag"));
    }

    /**
     * 创建线索必须写入完整设定属性、默认生命周期状态，并同步 Clue 节点。
     */
    @Test
    void createClueShouldPersistSettingFieldsAndSyncGraph() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelClueDao.insert(any(NovelClueEntity.class))).thenAnswer(invocation -> {
            NovelClueEntity entity = invocation.getArgument(0);
            entity.setId(40001L);
            return 1;
        });

        ResponseDTO<String> response = novelAssetService.createClue(buildClueAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelClueEntity> entityCaptor = ArgumentCaptor.forClass(NovelClueEntity.class);
        verify(novelClueDao).insert(entityCaptor.capture());
        NovelClueEntity insertEntity = entityCaptor.getValue();
        assertEquals(10001L, insertEntity.getProjectId());
        assertEquals(7L, insertEntity.getCreateUserId());
        assertEquals(Boolean.FALSE, insertEntity.getDeletedFlag());
        assertEquals(NovelClueStatusEnum.DORMANT.getValue(), insertEntity.getClueStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(insertEntity.getRevealLevel()));

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeClue(eq(10001L), eq(40001L), propsCaptor.capture());
        assertEquals("灭门真相", propsCaptor.getValue().get("name"));
        assertEquals(NovelClueSubTypeEnum.PLOT_THREAD.getValue(), propsCaptor.getValue().get("subType"));
        assertEquals(4, propsCaptor.getValue().get("priority"));
    }

    /**
     * 线索分页查询必须强制带上 projectId、createUserId 和 deletedFlag 条件。
     */
    @Test
    void queryClueByPageShouldForceProjectAndUserFilter() {
        NovelClueQueryForm queryForm = new NovelClueQueryForm();
        queryForm.setProjectId(10001L);
        queryForm.setPageNum(1L);
        queryForm.setPageSize(10L);
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelClueDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<NovelClueEntity> page = invocation.getArgument(0);
            page.setRecords(List.of(buildClueEntity()));
            page.setTotal(1);
            return page;
        });

        ResponseDTO<PageResult<NovelClueVO>> response = novelAssetService.queryClueByPage(queryForm, 7L);

        assertTrue(response.getOk());
        assertEquals(1, response.getData().getList().size());
        assertEquals("灭门真相", response.getData().getList().get(0).getName());
        ArgumentCaptor<LambdaQueryWrapper<NovelClueEntity>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(novelClueDao).selectPage(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("project_id"));
        assertTrue(sqlSegment.contains("create_user_id"));
        assertTrue(sqlSegment.contains("deleted_flag"));
    }

    /**
     * 创建物品必须写入设定字段、默认物品状态，并同步 Item 节点。
     */
    @Test
    void createItemShouldPersistSettingFieldsAndSyncGraph() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelItemDao.insert(any(NovelItemEntity.class))).thenAnswer(invocation -> {
            NovelItemEntity entity = invocation.getArgument(0);
            entity.setId(50001L);
            return 1;
        });

        ResponseDTO<String> response = novelAssetService.createItem(buildItemAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelItemEntity> entityCaptor = ArgumentCaptor.forClass(NovelItemEntity.class);
        verify(novelItemDao).insert(entityCaptor.capture());
        NovelItemEntity insertEntity = entityCaptor.getValue();
        assertEquals(10001L, insertEntity.getProjectId());
        assertEquals(7L, insertEntity.getCreateUserId());
        assertEquals(Boolean.FALSE, insertEntity.getDeletedFlag());
        assertEquals(NovelItemStatusEnum.INTACT.getValue(), insertEntity.getItemStatus());

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeItem(eq(10001L), eq(50001L), propsCaptor.capture());
        assertEquals("密函", propsCaptor.getValue().get("name"));
        assertEquals(NovelItemTypeEnum.DOCUMENT.getValue(), propsCaptor.getValue().get("type"));
        assertEquals(NovelItemStatusEnum.INTACT.getValue(), propsCaptor.getValue().get("itemStatus"));
    }

    /**
     * 物品分页查询必须强制带上 projectId、createUserId 和 deletedFlag 条件。
     */
    @Test
    void queryItemByPageShouldForceProjectAndUserFilter() {
        NovelItemQueryForm queryForm = new NovelItemQueryForm();
        queryForm.setProjectId(10001L);
        queryForm.setPageNum(1L);
        queryForm.setPageSize(10L);
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelItemDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<NovelItemEntity> page = invocation.getArgument(0);
            page.setRecords(List.of(buildItemEntity()));
            page.setTotal(1);
            return page;
        });

        ResponseDTO<PageResult<NovelItemVO>> response = novelAssetService.queryItemByPage(queryForm, 7L);

        assertTrue(response.getOk());
        assertEquals(1, response.getData().getList().size());
        assertEquals("密函", response.getData().getList().get(0).getName());
        ArgumentCaptor<LambdaQueryWrapper<NovelItemEntity>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(novelItemDao).selectPage(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("project_id"));
        assertTrue(sqlSegment.contains("create_user_id"));
        assertTrue(sqlSegment.contains("deleted_flag"));
    }

    /**
     * 创建事件必须写入设定字段和发生章节，并同步 Event 节点。
     */
    @Test
    void createEventShouldPersistSettingFieldsAndSyncGraph() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelEventDao.insert(any(NovelEventEntity.class))).thenAnswer(invocation -> {
            NovelEventEntity entity = invocation.getArgument(0);
            entity.setId(60001L);
            return 1;
        });

        ResponseDTO<String> response = novelAssetService.createEvent(buildEventAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelEventEntity> entityCaptor = ArgumentCaptor.forClass(NovelEventEntity.class);
        verify(novelEventDao).insert(entityCaptor.capture());
        NovelEventEntity insertEntity = entityCaptor.getValue();
        assertEquals(10001L, insertEntity.getProjectId());
        assertEquals(7L, insertEntity.getCreateUserId());
        assertEquals(Boolean.FALSE, insertEntity.getDeletedFlag());
        assertEquals(12, insertEntity.getChapterOccurred());

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeEvent(eq(10001L), eq(60001L), propsCaptor.capture());
        assertEquals("暗室相遇", propsCaptor.getValue().get("name"));
        assertEquals(12, propsCaptor.getValue().get("chapterOccurred"));
    }

    /**
     * 事件分页查询必须强制带上 projectId、createUserId 和 deletedFlag 条件。
     */
    @Test
    void queryEventByPageShouldForceProjectAndUserFilter() {
        NovelEventQueryForm queryForm = new NovelEventQueryForm();
        queryForm.setProjectId(10001L);
        queryForm.setPageNum(1L);
        queryForm.setPageSize(10L);
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelEventDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<NovelEventEntity> page = invocation.getArgument(0);
            page.setRecords(List.of(buildEventEntity()));
            page.setTotal(1);
            return page;
        });

        ResponseDTO<PageResult<NovelEventVO>> response = novelAssetService.queryEventByPage(queryForm, 7L);

        assertTrue(response.getOk());
        assertEquals(1, response.getData().getList().size());
        assertEquals("暗室相遇", response.getData().getList().get(0).getName());
        ArgumentCaptor<LambdaQueryWrapper<NovelEventEntity>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(novelEventDao).selectPage(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("project_id"));
        assertTrue(sqlSegment.contains("create_user_id"));
        assertTrue(sqlSegment.contains("deleted_flag"));
    }

    /**
     * 创建金手指必须写入设定字段，给副作用阶段设置安全默认值，并同步 Cheat 节点。
     */
    @Test
    void createCheatShouldPersistSettingFieldsDefaultStageAndSyncGraph() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelCheatDao.insert(any(NovelCheatEntity.class))).thenAnswer(invocation -> {
            NovelCheatEntity entity = invocation.getArgument(0);
            entity.setId(70001L);
            return 1;
        });

        ResponseDTO<String> response = novelAssetService.createCheat(buildCheatAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelCheatEntity> entityCaptor = ArgumentCaptor.forClass(NovelCheatEntity.class);
        verify(novelCheatDao).insert(entityCaptor.capture());
        NovelCheatEntity insertEntity = entityCaptor.getValue();
        assertEquals(10001L, insertEntity.getProjectId());
        assertEquals(7L, insertEntity.getCreateUserId());
        assertEquals(Boolean.FALSE, insertEntity.getDeletedFlag());
        assertEquals("无副作用", insertEntity.getCurrentStage());

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeCheat(eq(10001L), eq(70001L), propsCaptor.capture());
        assertEquals("万倍悟性", propsCaptor.getValue().get("name"));
        assertEquals(NovelCheatTypeEnum.ABILITY.getValue(), propsCaptor.getValue().get("type"));
        assertEquals("无副作用", propsCaptor.getValue().get("currentStage"));
    }

    /**
     * 金手指分页查询必须强制带上 projectId、createUserId 和 deletedFlag 条件。
     */
    @Test
    void queryCheatByPageShouldForceProjectAndUserFilter() {
        NovelCheatQueryForm queryForm = new NovelCheatQueryForm();
        queryForm.setProjectId(10001L);
        queryForm.setPageNum(1L);
        queryForm.setPageSize(10L);
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelCheatDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<NovelCheatEntity> page = invocation.getArgument(0);
            page.setRecords(List.of(buildCheatEntity()));
            page.setTotal(1);
            return page;
        });

        ResponseDTO<PageResult<NovelCheatVO>> response = novelAssetService.queryCheatByPage(queryForm, 7L);

        assertTrue(response.getOk());
        assertEquals(1, response.getData().getList().size());
        assertEquals("万倍悟性", response.getData().getList().get(0).getName());
        ArgumentCaptor<LambdaQueryWrapper<NovelCheatEntity>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(novelCheatDao).selectPage(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("project_id"));
        assertTrue(sqlSegment.contains("create_user_id"));
        assertTrue(sqlSegment.contains("deleted_flag"));
    }

    /**
     * 创建马甲必须写入设定字段，默认未识破，并同步 Alias 节点。
     */
    @Test
    void createAliasShouldPersistSettingFieldsDefaultRevealStateAndSyncGraph() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelAliasDao.insert(any(NovelAliasEntity.class))).thenAnswer(invocation -> {
            NovelAliasEntity entity = invocation.getArgument(0);
            entity.setId(80001L);
            return 1;
        });

        ResponseDTO<String> response = novelAssetService.createAlias(buildAliasAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelAliasEntity> entityCaptor = ArgumentCaptor.forClass(NovelAliasEntity.class);
        verify(novelAliasDao).insert(entityCaptor.capture());
        NovelAliasEntity insertEntity = entityCaptor.getValue();
        assertEquals(10001L, insertEntity.getProjectId());
        assertEquals(7L, insertEntity.getCreateUserId());
        assertEquals(Boolean.FALSE, insertEntity.getDeletedFlag());
        assertEquals(Boolean.FALSE, insertEntity.getRevealed());

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeAlias(eq(10001L), eq(80001L), propsCaptor.capture());
        assertEquals("暗影", propsCaptor.getValue().get("name"));
        assertEquals(NovelAliasTypeEnum.ONLINE_IDENTITY.getValue(), propsCaptor.getValue().get("type"));
        assertEquals(Boolean.FALSE, propsCaptor.getValue().get("revealed"));
    }

    /**
     * 马甲分页查询必须强制带上 projectId、createUserId 和 deletedFlag 条件。
     */
    @Test
    void queryAliasByPageShouldForceProjectAndUserFilter() {
        NovelAliasQueryForm queryForm = new NovelAliasQueryForm();
        queryForm.setProjectId(10001L);
        queryForm.setPageNum(1L);
        queryForm.setPageSize(10L);
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelAliasDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<NovelAliasEntity> page = invocation.getArgument(0);
            page.setRecords(List.of(buildAliasEntity()));
            page.setTotal(1);
            return page;
        });

        ResponseDTO<PageResult<NovelAliasVO>> response = novelAssetService.queryAliasByPage(queryForm, 7L);

        assertTrue(response.getOk());
        assertEquals(1, response.getData().getList().size());
        assertEquals("暗影", response.getData().getList().get(0).getName());
        ArgumentCaptor<LambdaQueryWrapper<NovelAliasEntity>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(novelAliasDao).selectPage(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("project_id"));
        assertTrue(sqlSegment.contains("create_user_id"));
        assertTrue(sqlSegment.contains("deleted_flag"));
    }

    /**
     * 创建叙事规则只写 MySQL；按阶段一方案，规则拼 System Prompt 使用，不进入 Neo4j。
     */
    @Test
    void createNarrativeRuleShouldPersistMysqlOnlyWithoutGraphSync() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelNarrativeRuleDao.insert(any(NovelNarrativeRuleEntity.class))).thenAnswer(invocation -> {
            NovelNarrativeRuleEntity entity = invocation.getArgument(0);
            entity.setId(90001L);
            return 1;
        });

        ResponseDTO<String> response = novelAssetService.createNarrativeRule(buildNarrativeRuleAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelNarrativeRuleEntity> entityCaptor = ArgumentCaptor.forClass(NovelNarrativeRuleEntity.class);
        verify(novelNarrativeRuleDao).insert(entityCaptor.capture());
        NovelNarrativeRuleEntity insertEntity = entityCaptor.getValue();
        assertEquals(10001L, insertEntity.getProjectId());
        assertEquals(7L, insertEntity.getCreateUserId());
        assertEquals(Boolean.FALSE, insertEntity.getDeletedFlag());
        assertEquals(5, insertEntity.getPriority());
        verifyNoInteractions(novelGraphService);
    }

    /**
     * 叙事规则分页查询必须强制带上 projectId、createUserId 和 deletedFlag 条件。
     */
    @Test
    void queryNarrativeRuleByPageShouldForceProjectAndUserFilter() {
        NovelNarrativeRuleQueryForm queryForm = new NovelNarrativeRuleQueryForm();
        queryForm.setProjectId(10001L);
        queryForm.setPageNum(1L);
        queryForm.setPageSize(10L);
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelNarrativeRuleDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<NovelNarrativeRuleEntity> page = invocation.getArgument(0);
            page.setRecords(List.of(buildNarrativeRuleEntity()));
            page.setTotal(1);
            return page;
        });

        ResponseDTO<PageResult<NovelNarrativeRuleVO>> response = novelAssetService.queryNarrativeRuleByPage(queryForm, 7L);

        assertTrue(response.getOk());
        assertEquals(1, response.getData().getList().size());
        assertEquals("平台红线", response.getData().getList().get(0).getName());
        ArgumentCaptor<LambdaQueryWrapper<NovelNarrativeRuleEntity>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(novelNarrativeRuleDao).selectPage(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("project_id"));
        assertTrue(sqlSegment.contains("create_user_id"));
        assertTrue(sqlSegment.contains("deleted_flag"));
    }

    /**
     * 创建卷必须写入卷序号和概要，并同步 Volume 节点与 Project -> Volume 包含关系。
     */
    @Test
    void createVolumeShouldPersistAndSyncGraphNodeAndContainRelation() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelVolumeDao.insert(any(NovelVolumeEntity.class))).thenAnswer(invocation -> {
            NovelVolumeEntity entity = invocation.getArgument(0);
            entity.setId(100001L);
            return 1;
        });

        ResponseDTO<String> response = novelAssetService.createVolume(buildVolumeAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelVolumeEntity> entityCaptor = ArgumentCaptor.forClass(NovelVolumeEntity.class);
        verify(novelVolumeDao).insert(entityCaptor.capture());
        NovelVolumeEntity insertEntity = entityCaptor.getValue();
        assertEquals(10001L, insertEntity.getProjectId());
        assertEquals(7L, insertEntity.getCreateUserId());
        assertEquals(Boolean.FALSE, insertEntity.getDeletedFlag());
        assertEquals(1, insertEntity.getNumber());

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeVolume(eq(10001L), eq(100001L), propsCaptor.capture());
        assertEquals("第一卷：少年游", propsCaptor.getValue().get("title"));
        verify(novelGraphService).mergeRelation(eq(NovelGraphRelationEnum.CONTAINS), eq(10001L),
                eq(NovelGraphNodeEnum.Project), eq(10001L), eq(NovelGraphNodeEnum.Volume), eq(100001L), any(Map.class));
    }

    /**
     * 卷分页查询必须强制带上 projectId、createUserId 和 deletedFlag 条件。
     */
    @Test
    void queryVolumeByPageShouldForceProjectAndUserFilter() {
        NovelVolumeQueryForm queryForm = new NovelVolumeQueryForm();
        queryForm.setProjectId(10001L);
        queryForm.setPageNum(1L);
        queryForm.setPageSize(10L);
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelVolumeDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<NovelVolumeEntity> page = invocation.getArgument(0);
            page.setRecords(List.of(buildVolumeEntity()));
            page.setTotal(1);
            return page;
        });

        ResponseDTO<PageResult<NovelVolumeVO>> response = novelAssetService.queryVolumeByPage(queryForm, 7L);

        assertTrue(response.getOk());
        assertEquals(1, response.getData().getList().size());
        assertEquals("第一卷：少年游", response.getData().getList().get(0).getTitle());
        ArgumentCaptor<LambdaQueryWrapper<NovelVolumeEntity>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(novelVolumeDao).selectPage(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("project_id"));
        assertTrue(sqlSegment.contains("create_user_id"));
        assertTrue(sqlSegment.contains("deleted_flag"));
    }

    /**
     * 创建 KNOWS 关系必须严格校验子类型，并把子类型同步为 Neo4j 关系属性 relationType。
     */
    @Test
    void createKnowsRelationShouldValidateSubTypeAndSyncGraph() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelCharacterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildCharacterEntity());
        when(novelCharacterRelationDao.insert(any(NovelCharacterRelationEntity.class))).thenAnswer(invocation -> {
            NovelCharacterRelationEntity entity = invocation.getArgument(0);
            entity.setId(110001L);
            return 1;
        });

        ResponseDTO<String> response = novelAssetService.createCharacterRelation(buildKnowsRelationAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelCharacterRelationEntity> entityCaptor = ArgumentCaptor.forClass(NovelCharacterRelationEntity.class);
        verify(novelCharacterRelationDao).insert(entityCaptor.capture());
        NovelCharacterRelationEntity insertEntity = entityCaptor.getValue();
        assertEquals("KNOWS", insertEntity.getRelationType());
        assertEquals(NovelRelationTypeEnum.ALLY.getValue(), insertEntity.getKnowsRelationType());
        assertEquals(Boolean.FALSE, insertEntity.getDeletedFlag());
        assertEquals(7L, insertEntity.getCreateUserId());

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeRelation(eq(NovelGraphRelationEnum.KNOWS), eq(10001L),
                eq(NovelGraphNodeEnum.Character), eq(20001L), eq(NovelGraphNodeEnum.Character), eq(20002L), propsCaptor.capture());
        assertEquals(NovelRelationTypeEnum.ALLY.getValue(), propsCaptor.getValue().get("relationType"));
    }

    /**
     * KNOWS 子类型不在白名单时必须拒绝写入，防止自由文本污染图谱关系。
     */
    @Test
    void createKnowsRelationShouldRejectUnknownSubType() {
        NovelCharacterRelationAddForm addForm = buildKnowsRelationAddForm();
        addForm.setKnowsRelationType("BROKEN");
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelCharacterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildCharacterEntity());

        ResponseDTO<String> response = novelAssetService.createCharacterRelation(addForm, 7L);

        assertFalse(response.getOk());
        verify(novelCharacterRelationDao, never()).insert(any(NovelCharacterRelationEntity.class));
        verifyNoInteractions(novelGraphService);
    }

    /**
     * 创建 LOVES 关系必须保存爱慕状态，并同步为 Neo4j 关系属性 status。
     */
    @Test
    void createLoveRelationShouldPersistStatusAndSyncGraph() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelCharacterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildCharacterEntity());
        when(novelCharacterRelationDao.insert(any(NovelCharacterRelationEntity.class))).thenAnswer(invocation -> {
            NovelCharacterRelationEntity entity = invocation.getArgument(0);
            entity.setId(110002L);
            return 1;
        });

        ResponseDTO<String> response = novelAssetService.createCharacterRelation(buildLoveRelationAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelCharacterRelationEntity> entityCaptor = ArgumentCaptor.forClass(NovelCharacterRelationEntity.class);
        verify(novelCharacterRelationDao).insert(entityCaptor.capture());
        NovelCharacterRelationEntity insertEntity = entityCaptor.getValue();
        assertEquals("LOVES", insertEntity.getRelationType());
        assertEquals(NovelLoveStatusEnum.MUTUAL.getValue(), insertEntity.getLoveStatus());
        assertEquals(null, insertEntity.getKnowsRelationType());

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeRelation(eq(NovelGraphRelationEnum.LOVES), eq(10001L),
                eq(NovelGraphNodeEnum.Character), eq(20001L), eq(NovelGraphNodeEnum.Character), eq(20002L), propsCaptor.capture());
        assertEquals(NovelLoveStatusEnum.MUTUAL.getValue(), propsCaptor.getValue().get("status"));
    }

    /**
     * 创建 HATES 关系必须保存 1~5 的仇恨强度，并同步为 Neo4j 关系属性 intensity。
     */
    @Test
    void createHateRelationShouldPersistIntensityAndSyncGraph() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelCharacterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildCharacterEntity());
        when(novelCharacterRelationDao.insert(any(NovelCharacterRelationEntity.class))).thenAnswer(invocation -> {
            NovelCharacterRelationEntity entity = invocation.getArgument(0);
            entity.setId(110003L);
            return 1;
        });

        ResponseDTO<String> response = novelAssetService.createCharacterRelation(buildHateRelationAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelCharacterRelationEntity> entityCaptor = ArgumentCaptor.forClass(NovelCharacterRelationEntity.class);
        verify(novelCharacterRelationDao).insert(entityCaptor.capture());
        NovelCharacterRelationEntity insertEntity = entityCaptor.getValue();
        assertEquals("HATES", insertEntity.getRelationType());
        assertEquals(5, insertEntity.getHateIntensity());

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeRelation(eq(NovelGraphRelationEnum.HATES), eq(10001L),
                eq(NovelGraphNodeEnum.Character), eq(20001L), eq(NovelGraphNodeEnum.Character), eq(20002L), propsCaptor.capture());
        assertEquals(5, propsCaptor.getValue().get("intensity"));
    }

    /**
     * 创建 IS_FAMILY_OF 关系必须保存亲缘或师门类型，并同步为 Neo4j 关系属性 familyType。
     */
    @Test
    void createFamilyRelationShouldPersistFamilyTypeAndSyncGraph() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelCharacterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildCharacterEntity());
        when(novelCharacterRelationDao.insert(any(NovelCharacterRelationEntity.class))).thenAnswer(invocation -> {
            NovelCharacterRelationEntity entity = invocation.getArgument(0);
            entity.setId(110004L);
            return 1;
        });

        ResponseDTO<String> response = novelAssetService.createCharacterRelation(buildFamilyRelationAddForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelCharacterRelationEntity> entityCaptor = ArgumentCaptor.forClass(NovelCharacterRelationEntity.class);
        verify(novelCharacterRelationDao).insert(entityCaptor.capture());
        NovelCharacterRelationEntity insertEntity = entityCaptor.getValue();
        assertEquals("IS_FAMILY_OF", insertEntity.getRelationType());
        assertEquals(NovelFamilyTypeEnum.DISCIPLE.getValue(), insertEntity.getFamilyType());

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeRelation(eq(NovelGraphRelationEnum.IS_FAMILY_OF), eq(10001L),
                eq(NovelGraphNodeEnum.Character), eq(20001L), eq(NovelGraphNodeEnum.Character), eq(20002L), propsCaptor.capture());
        assertEquals(NovelFamilyTypeEnum.DISCIPLE.getValue(), propsCaptor.getValue().get("familyType"));
    }

    /**
     * 编辑角色关系如果改变关系大类，必须删除旧 Neo4j 边并合并新边，避免图谱里残留双关系。
     */
    @Test
    void updateCharacterRelationShouldDeleteOldEdgeAndMergeNewEdgeWhenTypeChanged() {
        when(novelCharacterRelationDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildKnowsRelationEntity());
        when(novelCharacterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildCharacterEntity());

        ResponseDTO<String> response = novelAssetService.updateCharacterRelation(buildLoveRelationUpdateForm(), 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelCharacterRelationEntity> entityCaptor = ArgumentCaptor.forClass(NovelCharacterRelationEntity.class);
        verify(novelCharacterRelationDao).updateById(entityCaptor.capture());
        NovelCharacterRelationEntity updateEntity = entityCaptor.getValue();
        assertEquals(110001L, updateEntity.getId());
        assertEquals("LOVES", updateEntity.getRelationType());
        assertEquals(NovelLoveStatusEnum.MUTUAL.getValue(), updateEntity.getLoveStatus());
        assertEquals(null, updateEntity.getKnowsRelationType());

        verify(novelGraphService).deleteRelation(NovelGraphRelationEnum.KNOWS, 10001L,
                NovelGraphNodeEnum.Character, 20001L, NovelGraphNodeEnum.Character, 20002L);
        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeRelation(eq(NovelGraphRelationEnum.LOVES), eq(10001L),
                eq(NovelGraphNodeEnum.Character), eq(20001L), eq(NovelGraphNodeEnum.Character), eq(20002L), propsCaptor.capture());
        assertEquals(NovelLoveStatusEnum.MUTUAL.getValue(), propsCaptor.getValue().get("status"));
    }

    /**
     * 角色关系分页查询必须强制带上 projectId、createUserId 和 deletedFlag 条件。
     */
    @Test
    void queryCharacterRelationByPageShouldForceProjectAndUserFilter() {
        NovelCharacterRelationQueryForm queryForm = new NovelCharacterRelationQueryForm();
        queryForm.setProjectId(10001L);
        queryForm.setRelationType("KNOWS");
        queryForm.setPageNum(1L);
        queryForm.setPageSize(10L);
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelCharacterRelationDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<NovelCharacterRelationEntity> page = invocation.getArgument(0);
            page.setRecords(List.of(buildKnowsRelationEntity()));
            page.setTotal(1);
            return page;
        });

        ResponseDTO<PageResult<NovelCharacterRelationVO>> response = novelAssetService.queryCharacterRelationByPage(queryForm, 7L);

        assertTrue(response.getOk());
        assertEquals(1, response.getData().getList().size());
        assertEquals("KNOWS", response.getData().getList().get(0).getRelationType());
        ArgumentCaptor<LambdaQueryWrapper<NovelCharacterRelationEntity>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(novelCharacterRelationDao).selectPage(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("project_id"));
        assertTrue(sqlSegment.contains("create_user_id"));
        assertTrue(sqlSegment.contains("deleted_flag"));
        assertTrue(sqlSegment.contains("relation_type"));
    }

    /**
     * 归档角色关系必须软删 MySQL 记录，并删除 Neo4j 中对应方向的关系。
     */
    @Test
    void archiveCharacterRelationShouldSoftDeleteMysqlAndDeleteGraphRelation() {
        when(novelCharacterRelationDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildKnowsRelationEntity());

        ResponseDTO<String> response = novelAssetService.archiveCharacterRelation(110001L, 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelCharacterRelationEntity> entityCaptor = ArgumentCaptor.forClass(NovelCharacterRelationEntity.class);
        verify(novelCharacterRelationDao).updateById(entityCaptor.capture());
        assertEquals(110001L, entityCaptor.getValue().getId());
        assertEquals(Boolean.TRUE, entityCaptor.getValue().getDeletedFlag());
        verify(novelGraphService).deleteRelation(NovelGraphRelationEnum.KNOWS, 10001L,
                NovelGraphNodeEnum.Character, 20001L, NovelGraphNodeEnum.Character, 20002L);
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
     * 构造角色创建表单，默认只使用管理页允许编辑的设定字段。
     */
    private NovelCharacterAddForm buildCharacterAddForm() {
        NovelCharacterAddForm addForm = new NovelCharacterAddForm();
        addForm.setProjectId(10001L);
        addForm.setName("李四");
        addForm.setRoleType(NovelCharacterRoleEnum.PROTAGONIST.getValue());
        addForm.setDescription("少年剑修，克制而执拗。");
        return addForm;
    }

    /**
     * 构造一个已存在的角色实体。
     */
    private NovelCharacterEntity buildCharacterEntity() {
        NovelCharacterEntity entity = new NovelCharacterEntity();
        entity.setId(20001L);
        entity.setProjectId(10001L);
        entity.setName("李四");
        entity.setRoleType(NovelCharacterRoleEnum.PROTAGONIST.getValue());
        entity.setDescription("少年剑修，克制而执拗。");
        entity.setCurrentStatus(NovelCharacterStatusEnum.ACTIVE.getValue());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造地点创建表单。
     */
    private NovelLocationAddForm buildLocationAddForm() {
        NovelLocationAddForm addForm = new NovelLocationAddForm();
        addForm.setProjectId(10001L);
        addForm.setName("青云宗");
        addForm.setType(NovelLocationTypeEnum.SECT.getValue());
        addForm.setSummary("坐落在群山之间的剑修宗门。");
        return addForm;
    }

    /**
     * 构造一个已存在的地点实体。
     */
    private NovelLocationEntity buildLocationEntity() {
        NovelLocationEntity entity = new NovelLocationEntity();
        entity.setId(30001L);
        entity.setProjectId(10001L);
        entity.setName("青云宗");
        entity.setType(NovelLocationTypeEnum.SECT.getValue());
        entity.setSummary("坐落在群山之间的剑修宗门。");
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造线索创建表单，覆盖完整的长期设定字段。
     */
    private NovelClueAddForm buildClueAddForm() {
        NovelClueAddForm addForm = new NovelClueAddForm();
        addForm.setProjectId(10001L);
        addForm.setName("灭门真相");
        addForm.setType(NovelClueTypeEnum.MAIN.getValue());
        addForm.setSubType(NovelClueSubTypeEnum.PLOT_THREAD.getValue());
        addForm.setDescription("李四追查家族灭门背后的真正主谋。");
        addForm.setPriority(4);
        addForm.setTargetChapter(80);
        addForm.setTone(NovelClueToneEnum.TENSE.getValue());
        return addForm;
    }

    /**
     * 构造一个已存在的线索实体。
     */
    private NovelClueEntity buildClueEntity() {
        NovelClueEntity entity = new NovelClueEntity();
        entity.setId(40001L);
        entity.setProjectId(10001L);
        entity.setName("灭门真相");
        entity.setType(NovelClueTypeEnum.MAIN.getValue());
        entity.setSubType(NovelClueSubTypeEnum.PLOT_THREAD.getValue());
        entity.setDescription("李四追查家族灭门背后的真正主谋。");
        entity.setPriority(4);
        entity.setTargetChapter(80);
        entity.setTone(NovelClueToneEnum.TENSE.getValue());
        entity.setRevealLevel(BigDecimal.ZERO);
        entity.setClueStatus(NovelClueStatusEnum.DORMANT.getValue());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造物品创建表单，只使用管理页允许直接编辑的设定字段。
     */
    private NovelItemAddForm buildItemAddForm() {
        NovelItemAddForm addForm = new NovelItemAddForm();
        addForm.setProjectId(10001L);
        addForm.setName("密函");
        addForm.setType(NovelItemTypeEnum.DOCUMENT.getValue());
        addForm.setSummary("藏有宗门旧案证据的一封密函。");
        return addForm;
    }

    /**
     * 构造一个已存在的物品实体。
     */
    private NovelItemEntity buildItemEntity() {
        NovelItemEntity entity = new NovelItemEntity();
        entity.setId(50001L);
        entity.setProjectId(10001L);
        entity.setName("密函");
        entity.setType(NovelItemTypeEnum.DOCUMENT.getValue());
        entity.setSummary("藏有宗门旧案证据的一封密函。");
        entity.setItemStatus(NovelItemStatusEnum.INTACT.getValue());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造事件创建表单。
     */
    private NovelEventAddForm buildEventAddForm() {
        NovelEventAddForm addForm = new NovelEventAddForm();
        addForm.setProjectId(10001L);
        addForm.setName("暗室相遇");
        addForm.setSummary("李四在青云宗暗室撞见持密函的黑衣人。");
        addForm.setChapterOccurred(12);
        return addForm;
    }

    /**
     * 构造一个已存在的事件实体。
     */
    private NovelEventEntity buildEventEntity() {
        NovelEventEntity entity = new NovelEventEntity();
        entity.setId(60001L);
        entity.setProjectId(10001L);
        entity.setName("暗室相遇");
        entity.setSummary("李四在青云宗暗室撞见持密函的黑衣人。");
        entity.setChapterOccurred(12);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }
    /**
     * 构造金手指创建表单，覆盖管理页允许直接编辑的设定字段。
     */
    private NovelCheatAddForm buildCheatAddForm() {
        NovelCheatAddForm addForm = new NovelCheatAddForm();
        addForm.setProjectId(10001L);
        addForm.setName("万倍悟性");
        addForm.setType(NovelCheatTypeEnum.ABILITY.getValue());
        addForm.setSummary("主角顿悟速度远超常人。");
        addForm.setOrigin("祖传玉简觉醒。");
        addForm.setLimitation("连续使用会透支精神。");
        addForm.setEvolution("随境界提升解锁更高倍率。");
        return addForm;
    }

    /**
     * 构造一个已经存在的金手指实体。
     */
    private NovelCheatEntity buildCheatEntity() {
        NovelCheatEntity entity = new NovelCheatEntity();
        entity.setId(70001L);
        entity.setProjectId(10001L);
        entity.setName("万倍悟性");
        entity.setType(NovelCheatTypeEnum.ABILITY.getValue());
        entity.setSummary("主角顿悟速度远超常人。");
        entity.setOrigin("祖传玉简觉醒。");
        entity.setLimitation("连续使用会透支精神。");
        entity.setEvolution("随境界提升解锁更高倍率。");
        entity.setCurrentStage("无副作用");
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造马甲创建表单，默认不包含暴露状态。
     */
    private NovelAliasAddForm buildAliasAddForm() {
        NovelAliasAddForm addForm = new NovelAliasAddForm();
        addForm.setProjectId(10001L);
        addForm.setName("暗影");
        addForm.setType(NovelAliasTypeEnum.ONLINE_IDENTITY.getValue());
        addForm.setAliasContext("在黑市论坛发布线索时使用。");
        addForm.setSummary("无人知道真实身份的情报贩子。");
        return addForm;
    }

    /**
     * 构造一个已经存在的马甲实体。
     */
    private NovelAliasEntity buildAliasEntity() {
        NovelAliasEntity entity = new NovelAliasEntity();
        entity.setId(80001L);
        entity.setProjectId(10001L);
        entity.setName("暗影");
        entity.setType(NovelAliasTypeEnum.ONLINE_IDENTITY.getValue());
        entity.setAliasContext("在黑市论坛发布线索时使用。");
        entity.setSummary("无人知道真实身份的情报贩子。");
        entity.setRevealed(Boolean.FALSE);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造叙事规则创建表单。
     */
    private NovelNarrativeRuleAddForm buildNarrativeRuleAddForm() {
        NovelNarrativeRuleAddForm addForm = new NovelNarrativeRuleAddForm();
        addForm.setProjectId(10001L);
        addForm.setName("平台红线");
        addForm.setContent("避免血腥描写和现实敏感表达。");
        addForm.setPriority(5);
        return addForm;
    }

    /**
     * 构造一个已经存在的叙事规则实体。
     */
    private NovelNarrativeRuleEntity buildNarrativeRuleEntity() {
        NovelNarrativeRuleEntity entity = new NovelNarrativeRuleEntity();
        entity.setId(90001L);
        entity.setProjectId(10001L);
        entity.setName("平台红线");
        entity.setContent("避免血腥描写和现实敏感表达。");
        entity.setPriority(5);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造卷创建表单，卷概要会在写作检索时注入 Prompt。
     */
    private NovelVolumeAddForm buildVolumeAddForm() {
        NovelVolumeAddForm addForm = new NovelVolumeAddForm();
        addForm.setProjectId(10001L);
        addForm.setNumber(1);
        addForm.setTitle("第一卷：少年游");
        addForm.setSummary("李四拜入青云宗，从杂役弟子开始踏上修行路。");
        return addForm;
    }

    /**
     * 构造一个已经存在的卷实体。
     */
    private NovelVolumeEntity buildVolumeEntity() {
        NovelVolumeEntity entity = new NovelVolumeEntity();
        entity.setId(100001L);
        entity.setProjectId(10001L);
        entity.setNumber(1);
        entity.setTitle("第一卷：少年游");
        entity.setSummary("李四拜入青云宗，从杂役弟子开始踏上修行路。");
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }
    /**
     * 构造 KNOWS 关系创建表单。
     */
    private NovelCharacterRelationAddForm buildKnowsRelationAddForm() {
        NovelCharacterRelationAddForm addForm = new NovelCharacterRelationAddForm();
        addForm.setProjectId(10001L);
        addForm.setCharacterId(20001L);
        addForm.setTargetCharacterId(20002L);
        addForm.setRelationType("KNOWS");
        addForm.setKnowsRelationType(NovelRelationTypeEnum.ALLY.getValue());
        return addForm;
    }

    /**
     * 构造 LOVES 关系创建表单。
     */
    private NovelCharacterRelationAddForm buildLoveRelationAddForm() {
        NovelCharacterRelationAddForm addForm = new NovelCharacterRelationAddForm();
        addForm.setProjectId(10001L);
        addForm.setCharacterId(20001L);
        addForm.setTargetCharacterId(20002L);
        addForm.setRelationType("LOVES");
        addForm.setLoveStatus(NovelLoveStatusEnum.MUTUAL.getValue());
        return addForm;
    }

    /**
     * 构造 LOVES 关系编辑表单。
     */
    private NovelCharacterRelationUpdateForm buildLoveRelationUpdateForm() {
        NovelCharacterRelationUpdateForm updateForm = new NovelCharacterRelationUpdateForm();
        updateForm.setRelationId(110001L);
        updateForm.setProjectId(10001L);
        updateForm.setCharacterId(20001L);
        updateForm.setTargetCharacterId(20002L);
        updateForm.setRelationType("LOVES");
        updateForm.setLoveStatus(NovelLoveStatusEnum.MUTUAL.getValue());
        return updateForm;
    }

    /**
     * 构造 HATES 关系创建表单。
     */
    private NovelCharacterRelationAddForm buildHateRelationAddForm() {
        NovelCharacterRelationAddForm addForm = new NovelCharacterRelationAddForm();
        addForm.setProjectId(10001L);
        addForm.setCharacterId(20001L);
        addForm.setTargetCharacterId(20002L);
        addForm.setRelationType("HATES");
        addForm.setHateIntensity(5);
        return addForm;
    }

    /**
     * 构造 IS_FAMILY_OF 关系创建表单。
     */
    private NovelCharacterRelationAddForm buildFamilyRelationAddForm() {
        NovelCharacterRelationAddForm addForm = new NovelCharacterRelationAddForm();
        addForm.setProjectId(10001L);
        addForm.setCharacterId(20001L);
        addForm.setTargetCharacterId(20002L);
        addForm.setRelationType("IS_FAMILY_OF");
        addForm.setFamilyType(NovelFamilyTypeEnum.DISCIPLE.getValue());
        return addForm;
    }

    /**
     * 构造一个已经存在的 KNOWS 关系实体。
     */
    private NovelCharacterRelationEntity buildKnowsRelationEntity() {
        NovelCharacterRelationEntity entity = new NovelCharacterRelationEntity();
        entity.setId(110001L);
        entity.setProjectId(10001L);
        entity.setCharacterId(20001L);
        entity.setTargetCharacterId(20002L);
        entity.setRelationType("KNOWS");
        entity.setKnowsRelationType(NovelRelationTypeEnum.ALLY.getValue());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }
}
