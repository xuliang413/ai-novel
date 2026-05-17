package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphNodeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectGenreEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectStatusEnum;
import net.lab1024.sa.admin.module.business.novel.dao.NovelProjectDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelProjectAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelProjectQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelProjectVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.builder.MapperBuilderAssistant;
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
 * NovelProjectService 单元测试。
 * <p>
 * 重点覆盖项目创建、分页查询、详情隔离和归档这些第一阶段必须稳定的项目管理行为。
 *
 * @Author AI-Novel
 */
class NovelProjectServiceTest {

    /**
     * 被测项目服务，使用 mock DAO 和 mock 图谱服务隔离数据库与 Neo4j。
     */
    private NovelProjectService novelProjectService;

    /**
     * 项目 DAO mock，用来捕获 MySQL 写入内容和分页查询条件。
     */
    private NovelProjectDao novelProjectDao;

    /**
     * 图谱服务 mock，用来验证 Project 节点是否按业务动作同步。
     */
    private NovelGraphService novelGraphService;

    /**
     * 每个用例前重建服务和 mock，避免用例间状态互相污染。
     */
    @BeforeEach
    void setup() {
        novelProjectService = new NovelProjectService();
        novelProjectDao = mock(NovelProjectDao.class);
        novelGraphService = mock(NovelGraphService.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), NovelProjectEntity.class);
        ReflectionTestUtils.setField(novelProjectService, "novelProjectDao", novelProjectDao);
        ReflectionTestUtils.setField(novelProjectService, "novelGraphService", novelGraphService);
    }

    /**
     * 创建项目时应写入 SmartAdmin 当前用户、默认状态和 Token 默认值，并同步 Neo4j Project 节点。
     */
    @Test
    void createProjectShouldPersistDefaultsAndSyncGraph() {
        NovelProjectAddForm addForm = buildAddForm();
        when(novelProjectDao.insert(any(NovelProjectEntity.class))).thenAnswer(invocation -> {
            NovelProjectEntity entity = invocation.getArgument(0);
            entity.setId(10001L);
            return 1;
        });

        ResponseDTO<String> response = novelProjectService.createProject(addForm, 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelProjectEntity> entityCaptor = ArgumentCaptor.forClass(NovelProjectEntity.class);
        verify(novelProjectDao).insert(entityCaptor.capture());
        NovelProjectEntity insertEntity = entityCaptor.getValue();
        assertEquals(7L, insertEntity.getCreateUserId());
        assertEquals(Boolean.FALSE, insertEntity.getDeletedFlag());
        assertEquals(NovelProjectStatusEnum.ACTIVE.getValue(), insertEntity.getStatus());
        assertEquals(3000, insertEntity.getTargetChapterWords());
        assertEquals(6000, insertEntity.getTokenBudget());
        assertEquals(8000, insertEntity.getTokenHardLimit());

        ArgumentCaptor<Map<String, Object>> propsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(novelGraphService).mergeProject(eq(10001L), propsCaptor.capture());
        assertEquals("剑道独尊", propsCaptor.getValue().get("name"));
        assertEquals(NovelProjectGenreEnum.XIANXIA.getValue(), propsCaptor.getValue().get("genre"));
        assertEquals(6000, propsCaptor.getValue().get("tokenBudget"));
    }

    /**
     * Token 硬上限必须大于等于预算，避免后续上下文检索阶段出现永远无法满足的预算配置。
     */
    @Test
    void createProjectShouldRejectHardLimitLowerThanBudget() {
        NovelProjectAddForm addForm = buildAddForm();
        addForm.setTokenBudget(9000);
        addForm.setTokenHardLimit(8000);

        ResponseDTO<String> response = novelProjectService.createProject(addForm, 7L);

        assertFalse(response.getOk());
        verify(novelProjectDao, never()).insert(any(NovelProjectEntity.class));
        verifyNoInteractions(novelGraphService);
    }

    /**
     * 项目详情必须按 createUserId 查询，查不到时按不存在处理，防止跨用户探测项目。
     */
    @Test
    void getDetailShouldRejectMissingOrOtherUserProject() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        ResponseDTO<NovelProjectVO> response = novelProjectService.getDetail(10001L, 7L);

        assertFalse(response.getOk());
        assertTrue(response.getMsg().contains("项目不存在"));
    }

    /**
     * 分页查询必须把当前用户和未归档条件压进查询条件，并返回 VO 分页结果。
     */
    @Test
    void queryByPageShouldForceUserFilterAndReturnProjectVO() {
        NovelProjectEntity entity = buildProjectEntity();
        NovelProjectQueryForm queryForm = new NovelProjectQueryForm();
        queryForm.setPageNum(1L);
        queryForm.setPageSize(10L);
        when(novelProjectDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenAnswer(invocation -> {
            Page<NovelProjectEntity> page = invocation.getArgument(0);
            page.setRecords(List.of(entity));
            page.setTotal(1);
            return page;
        });

        ResponseDTO<PageResult<NovelProjectVO>> response = novelProjectService.queryByPage(queryForm, 7L);

        assertTrue(response.getOk());
        assertEquals(1, response.getData().getList().size());
        assertEquals("剑道独尊", response.getData().getList().get(0).getName());
        ArgumentCaptor<LambdaQueryWrapper<NovelProjectEntity>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(novelProjectDao).selectPage(any(Page.class), wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertTrue(sqlSegment.contains("create_user_id"));
        assertTrue(sqlSegment.contains("deleted_flag"));
    }

    /**
     * 归档只做软删除和状态变更，并同步 Neo4j Project 节点为 archived。
     */
    @Test
    void archiveProjectShouldMarkDeletedAndArchiveGraph() {
        NovelProjectEntity entity = buildProjectEntity();
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(entity);

        ResponseDTO<String> response = novelProjectService.archiveProject(10001L, 7L);

        assertTrue(response.getOk());
        ArgumentCaptor<NovelProjectEntity> entityCaptor = ArgumentCaptor.forClass(NovelProjectEntity.class);
        verify(novelProjectDao).updateById(entityCaptor.capture());
        assertEquals(10001L, entityCaptor.getValue().getId());
        assertEquals(Boolean.TRUE, entityCaptor.getValue().getDeletedFlag());
        assertEquals(NovelProjectStatusEnum.ARCHIVED.getValue(), entityCaptor.getValue().getStatus());
        verify(novelGraphService).archiveNode(NovelGraphNodeEnum.Project, 10001L, 10001L);
    }

    /**
     * 构造一个最小可创建的项目表单，单个用例只覆盖自己关心的字段差异。
     */
    private NovelProjectAddForm buildAddForm() {
        NovelProjectAddForm addForm = new NovelProjectAddForm();
        addForm.setName("剑道独尊");
        addForm.setGenre(NovelProjectGenreEnum.XIANXIA.getValue());
        addForm.setWorldBuilding("灵气复苏后的宗门世界");
        addForm.setProtagonistName("李四");
        addForm.setStyleDescription("白描克制，节奏紧凑");
        addForm.setPlatform("QIDIAN");
        addForm.setTargetTotalWords(1_000_000);
        return addForm;
    }

    /**
     * 构造一个已归属当前用户的项目实体，供详情、分页和归档用例复用。
     */
    private NovelProjectEntity buildProjectEntity() {
        NovelProjectEntity entity = new NovelProjectEntity();
        entity.setId(10001L);
        entity.setName("剑道独尊");
        entity.setGenre(NovelProjectGenreEnum.XIANXIA.getValue());
        entity.setWorldBuilding("灵气复苏后的宗门世界");
        entity.setProtagonistName("李四");
        entity.setStyleDescription("白描克制，节奏紧凑");
        entity.setPlatform("QIDIAN");
        entity.setTargetTotalWords(1_000_000);
        entity.setTargetChapterWords(3000);
        entity.setTokenBudget(6000);
        entity.setTokenHardLimit(8000);
        entity.setStatus(NovelProjectStatusEnum.ACTIVE.getValue());
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }
}
