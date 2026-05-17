package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterRoleEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueSubTypeEnum;
import net.lab1024.sa.admin.module.business.novel.dao.ChapterOutlineDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterLocationDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterRelationDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelChapterAppearanceDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelChapterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelClueDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelLocationDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelProjectDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelVolumeDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.ChapterOutlineEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterRelationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterAppearanceEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelVolumeEntity;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelRetrieveContextVO;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 小说上下文检索服务测试。
 * <p>
 * 覆盖 Task 10 的候选池收窄、章节兜底、线索过滤和 Token 预算裁剪规则，确保后续写作引擎拿到的是可审阅的上下文预览。
 *
 * @Author AI-Novel
 */
class NovelRetrieveServiceTest {

    /**
     * 被测上下文检索服务。
     */
    private NovelRetrieveService novelRetrieveService;

    /**
     * 项目 DAO mock，用来校验项目归属和读取上下文预算。
     */
    private NovelProjectDao novelProjectDao;

    /**
     * 卷 DAO mock，用来读取当前章所属卷的概要。
     */
    private NovelVolumeDao novelVolumeDao;

    /**
     * 章节 DAO mock，用来读取当前章元信息和上一章摘要。
     */
    private NovelChapterDao novelChapterDao;

    /**
     * 章节细纲 DAO mock，用来读取 ChapterIntent 的优先来源。
     */
    private ChapterOutlineDao chapterOutlineDao;

    /**
     * 角色 DAO mock，用来构建角色候选池和 POV 兜底。
     */
    private NovelCharacterDao novelCharacterDao;

    /**
     * 角色当前位置 DAO mock，用来判断 POV 当前所在地。
     */
    private NovelCharacterLocationDao novelCharacterLocationDao;

    /**
     * 地点 DAO mock，用来读取当前位置或首章全量地点兜底。
     */
    private NovelLocationDao novelLocationDao;

    /**
     * 线索 DAO mock，用来读取活跃线索并排除休眠线索。
     */
    private NovelClueDao novelClueDao;

    /**
     * 角色关系 DAO mock，用来补充当前候选角色之间的关键关系。
     */
    private NovelCharacterRelationDao novelCharacterRelationDao;

    /**
     * 章节出场 DAO mock，用来给上一章出场实体加排序权重。
     */
    private NovelChapterAppearanceDao novelChapterAppearanceDao;

    /**
     * 每个用例前重建服务、DAO mock 和 MyBatis 表元信息，保证 LambdaQueryWrapper 可以正常解析列名。
     */
    @BeforeEach
    void setup() {
        novelRetrieveService = new NovelRetrieveService();
        novelProjectDao = mock(NovelProjectDao.class);
        novelVolumeDao = mock(NovelVolumeDao.class);
        novelChapterDao = mock(NovelChapterDao.class);
        chapterOutlineDao = mock(ChapterOutlineDao.class);
        novelCharacterDao = mock(NovelCharacterDao.class);
        novelCharacterLocationDao = mock(NovelCharacterLocationDao.class);
        novelLocationDao = mock(NovelLocationDao.class);
        novelClueDao = mock(NovelClueDao.class);
        novelCharacterRelationDao = mock(NovelCharacterRelationDao.class);
        novelChapterAppearanceDao = mock(NovelChapterAppearanceDao.class);

        MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelProjectEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelVolumeEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelChapterEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, ChapterOutlineEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelCharacterEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelCharacterLocationEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelLocationEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelClueEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelCharacterRelationEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelChapterAppearanceEntity.class);

        ReflectionTestUtils.setField(novelRetrieveService, "novelProjectDao", novelProjectDao);
        ReflectionTestUtils.setField(novelRetrieveService, "novelVolumeDao", novelVolumeDao);
        ReflectionTestUtils.setField(novelRetrieveService, "novelChapterDao", novelChapterDao);
        ReflectionTestUtils.setField(novelRetrieveService, "chapterOutlineDao", chapterOutlineDao);
        ReflectionTestUtils.setField(novelRetrieveService, "novelCharacterDao", novelCharacterDao);
        ReflectionTestUtils.setField(novelRetrieveService, "novelCharacterLocationDao", novelCharacterLocationDao);
        ReflectionTestUtils.setField(novelRetrieveService, "novelLocationDao", novelLocationDao);
        ReflectionTestUtils.setField(novelRetrieveService, "novelClueDao", novelClueDao);
        ReflectionTestUtils.setField(novelRetrieveService, "novelCharacterRelationDao", novelCharacterRelationDao);
        ReflectionTestUtils.setField(novelRetrieveService, "novelChapterAppearanceDao", novelChapterAppearanceDao);
    }

    /**
     * 第一章没有图谱候选时，需要退回项目全量有效角色和地点，避免新书开篇上下文为空。
     */
    @Test
    void buildContextPreviewShouldFallbackToFullPoolOnFirstChapter() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity("李四", 6000, 8000));
        when(novelChapterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(chapterOutlineDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(novelCharacterDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                buildCharacterEntity(20001L, "李四", NovelCharacterRoleEnum.PROTAGONIST.getValue(), NovelCharacterStatusEnum.ACTIVE.getValue()),
                buildCharacterEntity(20002L, "王五", NovelCharacterRoleEnum.SUPPORTING.getValue(), NovelCharacterStatusEnum.ACTIVE.getValue()),
                buildCharacterEntity(20003L, "旧掌门", NovelCharacterRoleEnum.SUPPORTING.getValue(), NovelCharacterStatusEnum.DEAD.getValue())
        ));
        when(novelCharacterLocationDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(novelLocationDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                buildLocationEntity(30001L, "青云宗"),
                buildLocationEntity(30002L, "山门")
        ));
        when(novelClueDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(novelCharacterRelationDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(novelChapterAppearanceDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        ResponseDTO<NovelRetrieveContextVO> response = novelRetrieveService.buildContextPreview(10001L, 1, null, 7L);

        assertTrue(response.getOk());
        NovelRetrieveContextVO context = response.getData();
        assertEquals("李四", context.getPov());
        assertEquals(2, context.getCharacters().size());
        assertEquals(2, context.getLocations().size());
        assertEquals("李四", context.getCharacters().get(0).getName());
        assertFalse(context.getCharacters().stream().anyMatch(character -> "旧掌门".equals(character.getName())));
        assertTrue(context.getFallbackNotes().contains("FIRST_CHAPTER_FULL_POOL"));
        assertTrue(context.getFallbackNotes().contains("NO_CURRENT_LOCATION"));
    }

    /**
     * 当前章没有卷时应跳过卷概要；上一章摘要和细纲摘要仍要进入上下文，休眠线索不能进入写前 Prompt。
     */
    @Test
    void buildContextPreviewShouldUseOutlinePreviousChapterAndSkipDormantClues() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity(null, 6000, 8000));
        when(novelChapterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(
                buildChapterEntity(30002L, 2, null, null, "第二章", "待写"),
                buildChapterEntity(30001L, 1, null, "李四", "第一章", "李四拜入青云宗")
        );
        when(chapterOutlineDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildOutlineEntity(2, "李四第一次接触暗室线索"));
        when(novelCharacterDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                buildCharacterEntity(20001L, "李四", NovelCharacterRoleEnum.PROTAGONIST.getValue(), NovelCharacterStatusEnum.ACTIVE.getValue())
        ));
        when(novelCharacterLocationDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(novelClueDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                buildClueEntity(40001L, "灭门真相", NovelClueStatusEnum.ACTIVE.getValue()),
                buildClueEntity(40002L, "旧剑痕", NovelClueStatusEnum.DORMANT.getValue())
        ));
        when(novelCharacterRelationDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(novelChapterAppearanceDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        ResponseDTO<NovelRetrieveContextVO> response = novelRetrieveService.buildContextPreview(10001L, 2, "强化悬疑感", 7L);

        assertTrue(response.getOk());
        NovelRetrieveContextVO context = response.getData();
        assertNull(context.getVolumeSummary());
        assertEquals("第一章", context.getPreviousChapter().getTitle());
        assertEquals("李四拜入青云宗", context.getPreviousChapter().getSummary());
        assertEquals("李四第一次接触暗室线索", context.getChapterIntent());
        assertEquals(1, context.getClues().size());
        assertEquals("灭门真相", context.getClues().get(0).getName());
    }

    /**
     * 当前章和项目都没有显式 POV 时，应从 PROTAGONIST 角色兜底，保证后续写作仍能有视角锚点。
     */
    @Test
    void buildContextPreviewShouldFallbackPovToProtagonistRole() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity(null, 6000, 8000));
        when(novelChapterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildChapterEntity(30003L, 3, null, null, "第三章", "待写"));
        when(chapterOutlineDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(novelCharacterDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                buildCharacterEntity(20002L, "王五", NovelCharacterRoleEnum.SUPPORTING.getValue(), NovelCharacterStatusEnum.ACTIVE.getValue()),
                buildCharacterEntity(20001L, "李四", NovelCharacterRoleEnum.PROTAGONIST.getValue(), NovelCharacterStatusEnum.ACTIVE.getValue())
        ));
        when(novelCharacterLocationDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(novelClueDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(novelCharacterRelationDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(novelChapterAppearanceDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        ResponseDTO<NovelRetrieveContextVO> response = novelRetrieveService.buildContextPreview(10001L, 3, null, 7L);

        assertTrue(response.getOk());
        assertEquals("李四", response.getData().getPov());
        assertEquals("李四", response.getData().getCharacters().get(0).getName());
    }

    /**
     * POV 存在 CURRENTLY_AT 记录时，需要把当前地点放入地点候选池，帮助章节写作保持空间连续性。
     */
    @Test
    void buildContextPreviewShouldIncludeCurrentLocationWhenPovHasLocation() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity(null, 6000, 8000));
        when(novelChapterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildChapterEntity(30004L, 4, 100001L, "李四", "第四章", "待写"));
        when(novelVolumeDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildVolumeEntity());
        when(chapterOutlineDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(novelCharacterDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                buildCharacterEntity(20001L, "李四", NovelCharacterRoleEnum.PROTAGONIST.getValue(), NovelCharacterStatusEnum.ACTIVE.getValue())
        ));
        when(novelCharacterLocationDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildCharacterLocationEntity(20001L, 30001L));
        when(novelLocationDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildLocationEntity(30001L, "青云宗暗室"));
        when(novelClueDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(novelCharacterRelationDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(novelChapterAppearanceDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        ResponseDTO<NovelRetrieveContextVO> response = novelRetrieveService.buildContextPreview(10001L, 4, null, 7L);

        assertTrue(response.getOk());
        assertEquals("第一卷：少年游", response.getData().getVolumeSummary().getTitle());
        assertEquals(1, response.getData().getLocations().size());
        assertEquals("青云宗暗室", response.getData().getLocations().get(0).getName());
    }

    /**
     * 上下文超过预算时，服务应先压缩卡片再丢弃低优先级内容，并保证最终不超过硬上限。
     */
    @Test
    void buildContextPreviewShouldShortenAndDiscardByTokenBudget() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity("李四", 60, 90));
        when(novelChapterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildChapterEntity(30005L, 5, null, "李四", "第五章", repeat("上一章发生了许多复杂铺垫", 8)));
        when(chapterOutlineDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildOutlineEntity(5, repeat("本章需要推进主线并维持悬念", 8)));
        when(novelCharacterDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                buildVerboseCharacterEntity(20001L, "李四", NovelCharacterRoleEnum.PROTAGONIST.getValue()),
                buildVerboseCharacterEntity(20002L, "王五", NovelCharacterRoleEnum.SUPPORTING.getValue()),
                buildVerboseCharacterEntity(20003L, "赵六", NovelCharacterRoleEnum.MINOR.getValue())
        ));
        when(novelCharacterLocationDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(novelClueDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                buildVerboseClueEntity(40001L, "灭门真相"),
                buildVerboseClueEntity(40002L, "暗室密函")
        ));
        when(novelCharacterRelationDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(novelChapterAppearanceDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        ResponseDTO<NovelRetrieveContextVO> response = novelRetrieveService.buildContextPreview(10001L, 5, null, 7L);

        assertTrue(response.getOk());
        NovelRetrieveContextVO context = response.getData();
        assertTrue(context.getTokenStats().getTruncated());
        assertTrue(context.getTokenStats().getEstimatedTokens() <= context.getTokenStats().getTokenHardLimit());
        assertTrue(context.getTokenStats().getShortenedCount() > 0);
        assertTrue(context.getTokenStats().getDiscardedCount() > 0);
        assertEquals("李四", context.getCharacters().get(0).getName());
    }

    /**
     * 构造一个属于当前用户的项目实体。
     *
     * @param protagonistName 项目主角名，可为空以覆盖 POV 兜底分支
     * @param tokenBudget 上下文软预算
     * @param tokenHardLimit 上下文硬上限
     * @return 项目实体
     */
    private NovelProjectEntity buildProjectEntity(String protagonistName, Integer tokenBudget, Integer tokenHardLimit) {
        NovelProjectEntity entity = new NovelProjectEntity();
        entity.setId(10001L);
        entity.setName("剑道独尊");
        entity.setProtagonistName(protagonistName);
        entity.setTokenBudget(tokenBudget);
        entity.setTokenHardLimit(tokenHardLimit);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造一个章节实体。
     *
     * @param id 章节主键
     * @param chapterNumber 章节号
     * @param volumeId 所属卷ID，可为空
     * @param pov POV 角色名，可为空
     * @param title 章节标题
     * @param summary 章节摘要
     * @return 章节实体
     */
    private NovelChapterEntity buildChapterEntity(Long id, Integer chapterNumber, Long volumeId, String pov, String title, String summary) {
        NovelChapterEntity entity = new NovelChapterEntity();
        entity.setId(id);
        entity.setProjectId(10001L);
        entity.setVolumeId(volumeId);
        entity.setChapterNumber(chapterNumber);
        entity.setPov(pov);
        entity.setTitle(title);
        entity.setSummary(summary);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造一个卷实体。
     *
     * @return 卷实体
     */
    private NovelVolumeEntity buildVolumeEntity() {
        NovelVolumeEntity entity = new NovelVolumeEntity();
        entity.setId(100001L);
        entity.setProjectId(10001L);
        entity.setNumber(1);
        entity.setTitle("第一卷：少年游");
        entity.setSummary("李四进入青云宗，从杂役弟子开始踏上修行路。");
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造一个章节细纲实体。
     *
     * @param chapterNumber 章节号
     * @param summary 细纲摘要
     * @return 章节细纲实体
     */
    private ChapterOutlineEntity buildOutlineEntity(Integer chapterNumber, String summary) {
        ChapterOutlineEntity entity = new ChapterOutlineEntity();
        entity.setId(50001L);
        entity.setProjectId(10001L);
        entity.setChapterNumber(chapterNumber);
        entity.setSceneBeats(summary);
        entity.setSummary(summary);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造一个角色实体。
     *
     * @param id 角色ID
     * @param name 角色名
     * @param roleType 角色定位
     * @param currentStatus 当前存活状态
     * @return 角色实体
     */
    private NovelCharacterEntity buildCharacterEntity(Long id, String name, String roleType, String currentStatus) {
        NovelCharacterEntity entity = new NovelCharacterEntity();
        entity.setId(id);
        entity.setProjectId(10001L);
        entity.setName(name);
        entity.setRoleType(roleType);
        entity.setDescription(name + "的基础设定。");
        entity.setCurrentStatus(currentStatus);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造一个长文本角色实体，用来触发 Token 裁剪。
     *
     * @param id 角色ID
     * @param name 角色名
     * @param roleType 角色定位
     * @return 角色实体
     */
    private NovelCharacterEntity buildVerboseCharacterEntity(Long id, String name, String roleType) {
        NovelCharacterEntity entity = buildCharacterEntity(id, name, roleType, NovelCharacterStatusEnum.ACTIVE.getValue());
        entity.setDescription(repeat(name + "背负复杂过往与长期目标。", 10));
        entity.setCurrentGoal(repeat("追查暗室背后的真正主谋。", 6));
        entity.setCurrentEmotion("克制而紧张");
        return entity;
    }

    /**
     * 构造一个地点实体。
     *
     * @param id 地点ID
     * @param name 地点名
     * @return 地点实体
     */
    private NovelLocationEntity buildLocationEntity(Long id, String name) {
        NovelLocationEntity entity = new NovelLocationEntity();
        entity.setId(id);
        entity.setProjectId(10001L);
        entity.setName(name);
        entity.setSummary(name + "是当前剧情的重要空间。");
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造一个角色当前位置实体。
     *
     * @param characterId 角色ID
     * @param locationId 地点ID
     * @return 角色当前位置实体
     */
    private NovelCharacterLocationEntity buildCharacterLocationEntity(Long characterId, Long locationId) {
        NovelCharacterLocationEntity entity = new NovelCharacterLocationEntity();
        entity.setId(60001L);
        entity.setProjectId(10001L);
        entity.setCharacterId(characterId);
        entity.setLocationId(locationId);
        entity.setLocationName("青云宗暗室");
        entity.setCurrentFlag(Boolean.TRUE);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造一个线索实体。
     *
     * @param id 线索ID
     * @param name 线索名
     * @param clueStatus 线索生命周期状态
     * @return 线索实体
     */
    private NovelClueEntity buildClueEntity(Long id, String name, String clueStatus) {
        NovelClueEntity entity = new NovelClueEntity();
        entity.setId(id);
        entity.setProjectId(10001L);
        entity.setName(name);
        entity.setSubType(NovelClueSubTypeEnum.PLOT_THREAD.getValue());
        entity.setDescription(name + "的设定说明。");
        entity.setSummary(name + "正在推进。");
        entity.setPriority(4);
        entity.setClueStatus(clueStatus);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造一个长文本线索实体，用来触发 Token 裁剪。
     *
     * @param id 线索ID
     * @param name 线索名
     * @return 线索实体
     */
    private NovelClueEntity buildVerboseClueEntity(Long id, String name) {
        NovelClueEntity entity = buildClueEntity(id, name, NovelClueStatusEnum.ACTIVE.getValue());
        entity.setDescription(repeat(name + "牵动主线冲突并关联多个角色选择。", 10));
        entity.setSummary(repeat(name + "仍处在证据未完整阶段。", 8));
        return entity;
    }

    /**
     * 重复拼接文本，方便构造超预算上下文。
     *
     * @param value 要重复的文本
     * @param times 重复次数
     * @return 拼接后的文本
     */
    private String repeat(String value, int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            builder.append(value);
        }
        return builder.toString();
    }
}
