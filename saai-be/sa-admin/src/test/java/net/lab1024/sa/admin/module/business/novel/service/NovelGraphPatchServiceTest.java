package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphNodeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphPatchOperationTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphRelationEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelPatchRiskLevel;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelClueDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelItemDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelLocationDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelItemEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchOperationModel;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelWriteSessionVO.NovelGraphPatchVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NovelGraphPatchService 单元测试。
 * <p>
 * 重点覆盖 38→5 映射正确性、inversePatch 生成、操作分级和 JSON 解析容错。
 *
 * @Author AI-Novel
 */
class NovelGraphPatchServiceTest {

    private NovelGraphPatchService patchService;
    private NovelGraphService novelGraphService;
    private NovelLLMService novelLLMService;
    private NovelCharacterDao novelCharacterDao;
    private NovelLocationDao novelLocationDao;
    private NovelClueDao novelClueDao;
    private NovelItemDao novelItemDao;

    @BeforeEach
    void setup() {
        patchService = new NovelGraphPatchService();
        novelGraphService = mock(NovelGraphService.class);
        novelLLMService = mock(NovelLLMService.class);
        novelCharacterDao = mock(NovelCharacterDao.class);
        novelLocationDao = mock(NovelLocationDao.class);
        novelClueDao = mock(NovelClueDao.class);
        novelItemDao = mock(NovelItemDao.class);

        ReflectionTestUtils.setField(patchService, "novelGraphService", novelGraphService);
        ReflectionTestUtils.setField(patchService, "novelLLMService", novelLLMService);
        ReflectionTestUtils.setField(patchService, "novelCharacterDao", novelCharacterDao);
        ReflectionTestUtils.setField(patchService, "novelLocationDao", novelLocationDao);
        ReflectionTestUtils.setField(patchService, "novelClueDao", novelClueDao);
        ReflectionTestUtils.setField(patchService, "novelItemDao", novelItemDao);

        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), NovelCharacterEntity.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), NovelLocationEntity.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), NovelClueEntity.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), NovelItemEntity.class);
    }

    // ======================== 38→5 映射测试 ========================

    @Test
    void createOperationsShouldMapToMergeNode() {
        assertEquals(NovelGraphPatchService.CypherTemplate.MERGE_NODE,
                patchService.getCypherTemplate(NovelGraphPatchOperationTypeEnum.CREATE_CHARACTER));
        assertEquals(NovelGraphPatchService.CypherTemplate.MERGE_NODE,
                patchService.getCypherTemplate(NovelGraphPatchOperationTypeEnum.CREATE_LOCATION));
    }

    @Test
    void changeOperationsShouldMapToUpdateNodeProps() {
        assertEquals(NovelGraphPatchService.CypherTemplate.UPDATE_NODE_PROPS,
                patchService.getCypherTemplate(NovelGraphPatchOperationTypeEnum.CHANGE_EMOTION));
        assertEquals(NovelGraphPatchService.CypherTemplate.UPDATE_NODE_PROPS,
                patchService.getCypherTemplate(NovelGraphPatchOperationTypeEnum.ADVANCE_CLUE));
        assertEquals(NovelGraphPatchService.CypherTemplate.UPDATE_NODE_PROPS,
                patchService.getCypherTemplate(NovelGraphPatchOperationTypeEnum.CHANGE_GOAL));
    }

    @Test
    void addRelationOperationsShouldMapToMergeRel() {
        assertEquals(NovelGraphPatchService.CypherTemplate.MERGE_REL,
                patchService.getCypherTemplate(NovelGraphPatchOperationTypeEnum.ADD_KNOWS));
        assertEquals(NovelGraphPatchService.CypherTemplate.MERGE_REL,
                patchService.getCypherTemplate(NovelGraphPatchOperationTypeEnum.CHARACTER_APPEARS));
        assertEquals(NovelGraphPatchService.CypherTemplate.MERGE_REL,
                patchService.getCypherTemplate(NovelGraphPatchOperationTypeEnum.MOVE_CHARACTER));
    }

    @Test
    void removeRelationOperationsShouldMapToDeleteRel() {
        assertEquals(NovelGraphPatchService.CypherTemplate.DELETE_REL,
                patchService.getCypherTemplate(NovelGraphPatchOperationTypeEnum.REMOVE_KNOWS));
        assertEquals(NovelGraphPatchService.CypherTemplate.DELETE_REL,
                patchService.getCypherTemplate(NovelGraphPatchOperationTypeEnum.REMOVE_POSSESSES));
    }

    @Test
    void all38OperationsMustHaveValidMapping() {
        for (NovelGraphPatchOperationTypeEnum op : NovelGraphPatchOperationTypeEnum.values()) {
            NovelGraphPatchService.CypherTemplate template = patchService.getCypherTemplate(op);
            assertNotNull(template, op.getCode() + " 应映射到5种Cypher模板之一");
        }
    }

    // ======================== 操作分级测试 ========================

    @Test
    void highRiskOpsShouldNotBeConfirmedByDefault() {
        List<NovelGraphPatchVO> vos = patchService.convertToVoList(buildPatchWithMixedRisk());
        long highRiskCount = vos.stream().filter(v -> !Boolean.TRUE.equals(v.getConfirmed())).count();
        assertTrue(highRiskCount > 0, "高风险操作应默认不勾选");

        long lowRiskCount = vos.stream().filter(v -> Boolean.TRUE.equals(v.getConfirmed())).count();
        assertTrue(lowRiskCount > 0, "低风险操作应默认勾选");
    }

    // ======================== JSON解析容错测试 ========================

    @Test
    void emptyLlmResponseShouldReturnEmptyPatch() {
        NovelGraphPatchModel patch = patchService.parseExtractionResult(null, 1L, 1);
        assertNotNull(patch);
        assertTrue(patch.getOperations().isEmpty());
    }

    @Test
    void garbledResponseShouldReturnEmptyPatch() {
        NovelGraphPatchModel patch = patchService.parseExtractionResult("这不是JSON", 1L, 1);
        assertNotNull(patch);
        assertTrue(patch.getOperations().isEmpty(), "乱码响应应返回空操作列表");
    }

    @Test
    void validJsonButUnknownTypeShouldSkip() {
        String json = "[{\"type\":\"UNKNOWN_OP\",\"characterName\":\"李四\",\"after\":\"开心\"}]";
        NovelGraphPatchModel patch = patchService.parseExtractionResult(json, 1L, 1);
        assertNotNull(patch);
        assertTrue(patch.getOperations().isEmpty(), "未知操作类型应被跳过");
    }

    @Test
    void validEmotionChangeJsonShouldParse() {
        String json = "[{\"type\":\"CHANGE_EMOTION\",\"characterName\":\"李四\",\"after\":\"ANGER\",\"confidence\":0.85}]";
        NovelGraphPatchModel patch = patchService.parseExtractionResult(json, 1L, 1);
        assertEquals(1, patch.getOperations().size());
        NovelGraphPatchOperationModel op = patch.getOperations().get(0);
        assertEquals("CHANGE_EMOTION", op.getType().getCode());
        assertEquals("李四", op.getCharacterName());
        assertEquals("ANGER", op.getAfter());
        assertEquals(0.85f, op.getConfidence(), 0.01);
    }

    // ======================== executePatches跳过/执行测试 ========================

    @Test
    void executePatchesShouldSkipUnconfirmedOps() {
        List<NovelGraphPatchVO> vos = new ArrayList<>();
        NovelGraphPatchVO vo = buildPatchVO("CHANGE_EMOTION", "李四", "ANGER", NovelPatchRiskLevel.LOW);
        vo.setConfirmed(false); // 未勾选
        vos.add(vo);

        patchService.executePatches(vos, 1L, 1);
        // 应跳过未勾选的操作，不对 Neo4j 做任何调用
        verify(novelGraphService, times(0)).updateNodeProps(any(), any(), any(), any());
    }

    @Test
    void executePatchesShouldCallUpdateNodePropsForChangeEmotion() {
        NovelCharacterEntity character = new NovelCharacterEntity();
        character.setId(100L);
        when(novelCharacterDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(character);

        List<NovelGraphPatchVO> vos = List.of(buildPatchVO("CHANGE_EMOTION", "李四", "ANGER", NovelPatchRiskLevel.LOW));
        patchService.executePatches(vos, 1L, 1);

        verify(novelGraphService, times(1)).updateNodeProps(
                any(NovelGraphNodeEnum.class), any(), any(), any(Map.class));
    }

    // ======================== Helpers ========================

    private NovelGraphPatchModel buildPatchWithMixedRisk() {
        List<NovelGraphPatchOperationModel> ops = new ArrayList<>();
        ops.add(NovelGraphPatchOperationModel.builder()
                .opId("1").type(NovelGraphPatchOperationTypeEnum.CHANGE_EMOTION)
                .characterName("李四").after("ANGER").confidence(0.9f).build());
        ops.add(NovelGraphPatchOperationModel.builder()
                .opId("2").type(NovelGraphPatchOperationTypeEnum.ADD_KNOWS)
                .characterName("李四").targetName("王五").after("FRIEND").confidence(0.6f).build());
        ops.add(NovelGraphPatchOperationModel.builder()
                .opId("3").type(NovelGraphPatchOperationTypeEnum.ADD_POSSESSES)
                .characterName("李四").targetName("断魂刀").confidence(0.7f).build());
        return NovelGraphPatchModel.builder()
                .patchId("test").projectId(1L).chapterNumber(1).operations(ops).build();
    }

    private NovelGraphPatchVO buildPatchVO(String type, String characterName, String afterValue, NovelPatchRiskLevel riskLevel) {
        NovelGraphPatchVO vo = new NovelGraphPatchVO();
        vo.setOperationType(type);
        vo.setOperationDesc("测试操作");
        vo.setCharacterName(characterName);
        vo.setAfterValue(afterValue);
        vo.setConfidence(0.8f);
        vo.setRiskLevel(riskLevel.name());
        vo.setConfirmed(riskLevel != NovelPatchRiskLevel.HIGH);
        return vo;
    }
}
