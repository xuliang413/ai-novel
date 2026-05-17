package net.lab1024.sa.admin.module.business.novel.service;

import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphNodeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphRelationEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NovelGraphService 单元测试。
 * <p>
 * 测试重点是白名单 Cypher 模板和属性过滤, Neo4j Driver 使用 mock, 不依赖本地 Neo4j 实例。
 *
 * @Author AI-Novel
 */
class NovelGraphServiceTest {

    /**
     * 被测服务, 直接注入 mock Driver。
     */
    private NovelGraphService novelGraphService;

    /**
     * Neo4j Driver mock, 用来捕获最终执行的 Cypher 和参数。
     */
    private Driver driver;

    /**
     * Neo4j Session mock, 避免测试连接真实图数据库。
     */
    private Session session;

    /**
     * 每个用例前准备独立的服务和 Neo4j mock。
     */
    @BeforeEach
    void setup() {
        novelGraphService = new NovelGraphService();
        driver = mock(Driver.class);
        session = mock(Session.class);
        Result result = mock(Result.class);
        when(driver.session()).thenReturn(session);
        when(session.run(anyString(), anyMap())).thenReturn(result);
        ReflectionTestUtils.setField(novelGraphService, "driver", driver);
    }

    /**
     * MERGE_NODE 模板应包含固定节点标签、项目隔离字段、业务主键字段和参数化 props。
     */
    @Test
    void mergeCharacterShouldUseMergeNodeTemplateAndSafeProps() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("name", "李四");
        props.put("roleType", "PROTAGONIST");

        novelGraphService.mergeCharacter(10001L, 20001L, props);

        org.mockito.ArgumentCaptor<String> cypherCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<Map<String, Object>> paramsCaptor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(session).run(cypherCaptor.capture(), paramsCaptor.capture());

        String cypher = cypherCaptor.getValue();
        Map<String, Object> params = paramsCaptor.getValue();
        assertTrue(cypher.contains("MERGE (n:Character {projectId: $projectId, characterId: $nodeId})"));
        assertTrue(cypher.contains("ON CREATE SET n.archived = false"));
        assertTrue(cypher.contains("SET n += $props"));
        assertEquals(10001L, params.get("projectId"));
        assertEquals(20001L, params.get("nodeId"));
        assertEquals("李四", ((Map<?, ?>) params.get("props")).get("name"));
    }

    /**
     * 节点 props 不允许覆盖 projectId/archived 等系统字段。
     */
    @Test
    void mergeNodeShouldRejectSystemProps() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> novelGraphService.mergeCharacter(10001L, 20001L, Map.of("projectId", 1L)));

        assertTrue(exception.getMessage().contains("系统维护"));
    }

    /**
     * 节点 props 不允许写入未登记在节点白名单里的字段。
     */
    @Test
    void mergeNodeShouldRejectUnknownProps() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> novelGraphService.mergeCharacter(10001L, 20001L, Map.of("unknownField", "x")));

        assertTrue(exception.getMessage().contains("不允许写入属性"));
    }

    /**
     * 11 种节点类型都应有 merge 入口, 保证后续资产 CRUD 可以统一同步 Neo4j。
     */
    @Test
    void shouldSupportAllNodeMergeMethods() {
        novelGraphService.mergeProject(1L, Map.of("name", "剑道独尊"));
        novelGraphService.mergeVolume(1L, 2L, Map.of("title", "少年游"));
        novelGraphService.mergeChapter(1L, 3, Map.of("title", "暗室密函"));
        novelGraphService.mergeCharacter(1L, 4L, Map.of("name", "李四"));
        novelGraphService.mergeLocation(1L, 5L, Map.of("name", "京城"));
        novelGraphService.mergeClue(1L, 6L, Map.of("name", "灭门真相"));
        novelGraphService.mergeItem(1L, 7L, Map.of("name", "密函"));
        novelGraphService.mergeEvent(1L, 8L, Map.of("name", "暗室相逢"));
        novelGraphService.mergeCheat(1L, 9L, Map.of("name", "万倍悟性"));
        novelGraphService.mergeAlias(1L, 10L, Map.of("name", "黑衣客"));
        novelGraphService.mergeNarrativeRule(1L, 11L, Map.of("name", "禁忌词"));

        verify(session, times(11)).run(anyString(), anyMap());
    }

    /**
     * ARCHIVE_NODE 模板应只做 archived=true, 不做物理删除。
     */
    @Test
    void archiveNodeShouldUseArchiveTemplate() {
        novelGraphService.archiveNode(NovelGraphNodeEnum.Character, 1L, 2L);

        org.mockito.ArgumentCaptor<String> cypherCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(session).run(cypherCaptor.capture(), anyMap());

        assertTrue(cypherCaptor.getValue().contains("MATCH (n:Character {projectId: $projectId, characterId: $nodeId})"));
        assertTrue(cypherCaptor.getValue().contains("SET n.archived = true"));
    }

    /**
     * UPDATE_NODE_PROPS 模板应先过节点属性白名单, 再用参数化 props 局部更新。
     */
    @Test
    void updateNodePropsShouldUseUpdateTemplate() {
        novelGraphService.updateNodeProps(NovelGraphNodeEnum.Clue, 1L, 2L, Map.of("revealLevel", 0.5));

        org.mockito.ArgumentCaptor<String> cypherCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<Map<String, Object>> paramsCaptor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(session).run(cypherCaptor.capture(), paramsCaptor.capture());

        assertTrue(cypherCaptor.getValue().contains("MATCH (n:Clue {projectId: $projectId, clueId: $nodeId})"));
        assertTrue(cypherCaptor.getValue().contains("SET n += $props"));
        assertEquals(0.5, ((Map<?, ?>) paramsCaptor.getValue().get("props")).get("revealLevel"));
    }

    /**
     * MERGE_REL 模板应校验关系方向, 并把 projectId 写入关系属性用于图谱隔离。
     */
    @Test
    void mergeRelationShouldUseRelationTemplate() {
        novelGraphService.mergeRelation(
                NovelGraphRelationEnum.CURRENTLY_AT,
                1L,
                NovelGraphNodeEnum.Character,
                2L,
                NovelGraphNodeEnum.Location,
                3L,
                Map.of("sinceChapter", 12)
        );

        org.mockito.ArgumentCaptor<String> cypherCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<Map<String, Object>> paramsCaptor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(session).run(cypherCaptor.capture(), paramsCaptor.capture());

        String cypher = cypherCaptor.getValue();
        assertTrue(cypher.contains("MATCH (from:Character {projectId: $projectId, characterId: $fromId})"));
        assertTrue(cypher.contains("MATCH (to:Location {projectId: $projectId, locationId: $toId})"));
        assertTrue(cypher.contains("MERGE (from)-[r:CURRENTLY_AT {projectId: $projectId}]->(to)"));
        assertEquals(12, ((Map<?, ?>) paramsCaptor.getValue().get("props")).get("sinceChapter"));
    }

    /**
     * DELETE_REL 模板只删除指定关系, 不删除任何节点。
     */
    @Test
    void deleteRelationShouldUseDeleteRelationTemplate() {
        novelGraphService.deleteRelation(
                NovelGraphRelationEnum.CURRENTLY_AT,
                1L,
                NovelGraphNodeEnum.Character,
                2L,
                NovelGraphNodeEnum.Location,
                3L
        );

        org.mockito.ArgumentCaptor<String> cypherCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(session).run(cypherCaptor.capture(), anyMap());

        String cypher = cypherCaptor.getValue();
        assertTrue(cypher.contains("-[r:CURRENTLY_AT {projectId: $projectId}]->"));
        assertTrue(cypher.contains("DELETE r"));
    }

    /**
     * 关系方向必须和枚举定义一致, 防止业务层把关系写反。
     */
    @Test
    void relationShouldRejectWrongDirection() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> novelGraphService.mergeRelation(
                        NovelGraphRelationEnum.CURRENTLY_AT,
                        1L,
                        NovelGraphNodeEnum.Location,
                        3L,
                        NovelGraphNodeEnum.Character,
                        2L,
                        Map.of()
                ));

        assertTrue(exception.getMessage().contains("起点应为"));
    }
}
