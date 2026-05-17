package net.lab1024.sa.admin.module.business.novel.service;

import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphNodeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphRelationEnum;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 小说知识图谱基础服务。
 * <p>
 * 本服务是所有 Neo4j 写入的唯一低层入口, 只提供 5 种白名单 Cypher 模板:
 * MERGE_NODE、ARCHIVE_NODE、MERGE_REL、DELETE_REL、UPDATE_NODE_PROPS。
 * LLM、前端和用户输入都不能直接传入 Cypher, 只能传业务枚举和经过白名单校验的属性。
 *
 * @Author AI-Novel
 */
@Service
public class NovelGraphService {

    /**
     * 由 Spring 容器管理的 Neo4j Driver, 每次操作创建短生命周期 Session。
     */
    @Resource
    private Driver driver;

    /**
     * 节点规格表, 定义每种节点的标签、业务主键字段以及允许写入的业务属性。
     */
    private static final Map<NovelGraphNodeEnum, NodeSpec> NODE_SPEC_MAP = createNodeSpecMap();

    /**
     * 关系属性中禁止由外部 props 覆盖的系统字段。
     */
    private static final Set<String> BLOCKED_RELATION_PROPS = Set.of("projectId", "archived", "createdAt", "updatedAt");

    /**
     * 合并 Project 节点。
     *
     * @param projectId 项目ID, 也是 Project 节点的唯一业务键
     * @param props 允许写入的项目业务属性
     */
    public void mergeProject(Long projectId, Map<String, Object> props) {
        mergeNode(NovelGraphNodeEnum.Project, projectId, projectId, props);
    }

    /**
     * 合并 Volume 节点。
     *
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param volumeId MySQL 卷ID
     * @param props 允许写入的卷业务属性
     */
    public void mergeVolume(Long projectId, Long volumeId, Map<String, Object> props) {
        mergeNode(NovelGraphNodeEnum.Volume, projectId, volumeId, props);
    }

    /**
     * 合并 Chapter 节点。
     *
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param chapterNumber 全局章节号, 与 Neo4j 约束中的 chapterNumber 对齐
     * @param props 允许写入的章节业务属性
     */
    public void mergeChapter(Long projectId, Integer chapterNumber, Map<String, Object> props) {
        mergeNode(NovelGraphNodeEnum.Chapter, projectId, chapterNumber, props);
    }

    /**
     * 合并 Character 节点。
     *
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param characterId MySQL 角色ID
     * @param props 允许写入的角色业务属性
     */
    public void mergeCharacter(Long projectId, Long characterId, Map<String, Object> props) {
        mergeNode(NovelGraphNodeEnum.Character, projectId, characterId, props);
    }

    /**
     * 合并 Location 节点。
     *
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param locationId MySQL 地点ID
     * @param props 允许写入的地点业务属性
     */
    public void mergeLocation(Long projectId, Long locationId, Map<String, Object> props) {
        mergeNode(NovelGraphNodeEnum.Location, projectId, locationId, props);
    }

    /**
     * 合并 Clue 节点。
     *
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param clueId MySQL 线索ID
     * @param props 允许写入的线索业务属性
     */
    public void mergeClue(Long projectId, Long clueId, Map<String, Object> props) {
        mergeNode(NovelGraphNodeEnum.Clue, projectId, clueId, props);
    }

    /**
     * 合并 Item 节点。
     *
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param itemId MySQL 物品ID
     * @param props 允许写入的物品业务属性
     */
    public void mergeItem(Long projectId, Long itemId, Map<String, Object> props) {
        mergeNode(NovelGraphNodeEnum.Item, projectId, itemId, props);
    }

    /**
     * 合并 Event 节点。
     *
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param eventId MySQL 事件ID
     * @param props 允许写入的事件业务属性
     */
    public void mergeEvent(Long projectId, Long eventId, Map<String, Object> props) {
        mergeNode(NovelGraphNodeEnum.Event, projectId, eventId, props);
    }

    /**
     * 合并 Cheat 节点。
     *
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param cheatId MySQL 金手指ID
     * @param props 允许写入的金手指业务属性
     */
    public void mergeCheat(Long projectId, Long cheatId, Map<String, Object> props) {
        mergeNode(NovelGraphNodeEnum.Cheat, projectId, cheatId, props);
    }

    /**
     * 合并 Alias 节点。
     *
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param aliasId MySQL 马甲ID
     * @param props 允许写入的马甲业务属性
     */
    public void mergeAlias(Long projectId, Long aliasId, Map<String, Object> props) {
        mergeNode(NovelGraphNodeEnum.Alias, projectId, aliasId, props);
    }

    /**
     * 合并 NarrativeRule 节点。
     *
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param ruleId MySQL 叙事规则ID
     * @param props 允许写入的叙事规则业务属性
     */
    public void mergeNarrativeRule(Long projectId, Long ruleId, Map<String, Object> props) {
        mergeNode(NovelGraphNodeEnum.NarrativeRule, projectId, ruleId, props);
    }

    /**
     * 归档节点。
     * <p>
     * 图谱不做物理删除, 归档只把 archived 置为 true, 检索阶段默认过滤该字段。
     *
     * @param nodeType 节点类型枚举
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param nodeId 节点业务ID; Project 节点传 projectId
     */
    public void archiveNode(NovelGraphNodeEnum nodeType, Long projectId, Object nodeId) {
        NodeSpec spec = requireNodeSpec(nodeType);
        String cypher = buildArchiveNodeCypher(spec);
        execute(cypher, createNodeKeyParams(spec, projectId, nodeId));
    }

    /**
     * 更新节点部分属性。
     * <p>
     * 与 mergeNode 不同, 该方法要求节点已存在; 常用于 GraphPatch 更新角色状态或线索进度。
     *
     * @param nodeType 节点类型枚举
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param nodeId 节点业务ID; Project 节点传 projectId
     * @param props 需要更新的业务属性
     */
    public void updateNodeProps(NovelGraphNodeEnum nodeType, Long projectId, Object nodeId, Map<String, Object> props) {
        NodeSpec spec = requireNodeSpec(nodeType);
        Map<String, Object> safeProps = validateNodeProps(spec, props);
        Map<String, Object> params = createNodeKeyParams(spec, projectId, nodeId);
        params.put("props", safeProps);
        execute(buildUpdateNodePropsCypher(spec), params);
    }

    /**
     * 合并关系。
     * <p>
     * 起点、终点和关系类型都来自枚举与节点规格, 只允许关系属性通过 props 进入参数表。
     *
     * @param relationType 关系类型枚举
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param fromNode 起点节点类型
     * @param fromId 起点业务ID
     * @param toNode 终点节点类型
     * @param toId 终点业务ID
     * @param props 关系属性
     */
    public void mergeRelation(NovelGraphRelationEnum relationType, Long projectId, NovelGraphNodeEnum fromNode, Object fromId,
                              NovelGraphNodeEnum toNode, Object toId, Map<String, Object> props) {
        RelationSpec spec = createRelationSpec(relationType, fromNode, toNode);
        Map<String, Object> params = createRelationParams(spec, projectId, fromId, toId, props);
        execute(buildMergeRelCypher(spec), params);
    }

    /**
     * 删除关系。
     * <p>
     * 只删除指定项目、指定起止节点之间的指定关系, 不允许 DETACH DELETE 节点。
     *
     * @param relationType 关系类型枚举
     * @param projectId 项目ID, 用于图谱项目隔离
     * @param fromNode 起点节点类型
     * @param fromId 起点业务ID
     * @param toNode 终点节点类型
     * @param toId 终点业务ID
     */
    public void deleteRelation(NovelGraphRelationEnum relationType, Long projectId, NovelGraphNodeEnum fromNode, Object fromId,
                               NovelGraphNodeEnum toNode, Object toId) {
        RelationSpec spec = createRelationSpec(relationType, fromNode, toNode);
        Map<String, Object> params = createRelationParams(spec, projectId, fromId, toId, Collections.emptyMap());
        execute(buildDeleteRelCypher(spec), params);
    }

    /**
     * 合并节点的通用入口。
     */
    private void mergeNode(NovelGraphNodeEnum nodeType, Long projectId, Object nodeId, Map<String, Object> props) {
        NodeSpec spec = requireNodeSpec(nodeType);
        Map<String, Object> safeProps = validateNodeProps(spec, props);
        Map<String, Object> params = createNodeKeyParams(spec, projectId, nodeId);
        params.put("props", safeProps);
        execute(buildMergeNodeCypher(spec), params);
    }

    /**
     * 执行 Cypher。
     */
    private void execute(String cypher, Map<String, Object> params) {
        // Session 按次创建按次关闭, Driver 由 Spring 维护单例, 避免长连接泄漏。
        try (Session session = driver.session()) {
            session.run(cypher, params);
        }
    }

    /**
     * 构建 MERGE_NODE 模板。
     */
    String buildMergeNodeCypher(NodeSpec spec) {
        return "MERGE (n:" + spec.label + " " + spec.matchPattern() + ") "
                + "ON CREATE SET n.archived = false, n.createdAt = datetime() "
                + "SET n += $props, n.updatedAt = datetime() "
                + "RETURN n";
    }

    /**
     * 构建 ARCHIVE_NODE 模板。
     */
    String buildArchiveNodeCypher(NodeSpec spec) {
        return "MATCH (n:" + spec.label + " " + spec.matchPattern() + ") "
                + "SET n.archived = true, n.updatedAt = datetime() "
                + "RETURN n";
    }

    /**
     * 构建 UPDATE_NODE_PROPS 模板。
     */
    String buildUpdateNodePropsCypher(NodeSpec spec) {
        return "MATCH (n:" + spec.label + " " + spec.matchPattern() + ") "
                + "SET n += $props, n.updatedAt = datetime() "
                + "RETURN n";
    }

    /**
     * 构建 MERGE_REL 模板。
     */
    String buildMergeRelCypher(RelationSpec spec) {
        return "MATCH (from:" + spec.fromSpec.label + " " + spec.fromSpec.relationMatchPattern("fromId") + ") "
                + "MATCH (to:" + spec.toSpec.label + " " + spec.toSpec.relationMatchPattern("toId") + ") "
                + "MERGE (from)-[r:" + spec.relationType.getType() + " {projectId: $projectId}]->(to) "
                + "SET r += $props, r.updatedAt = datetime() "
                + "RETURN r";
    }

    /**
     * 构建 DELETE_REL 模板。
     */
    String buildDeleteRelCypher(RelationSpec spec) {
        return "MATCH (from:" + spec.fromSpec.label + " " + spec.fromSpec.relationMatchPattern("fromId") + ")"
                + "-[r:" + spec.relationType.getType() + " {projectId: $projectId}]->"
                + "(to:" + spec.toSpec.label + " " + spec.toSpec.relationMatchPattern("toId") + ") "
                + "DELETE r";
    }

    /**
     * 校验节点属性白名单。
     */
    private Map<String, Object> validateNodeProps(NodeSpec spec, Map<String, Object> props) {
        Map<String, Object> safeProps = props == null ? Collections.emptyMap() : props;
        for (String propName : safeProps.keySet()) {
            // projectId、archived、时间戳和业务主键都由模板控制, 不接受外部覆盖。
            if (spec.blockedProps.contains(propName)) {
                throw new IllegalArgumentException("Neo4j节点属性[" + propName + "]由系统维护, 禁止通过props写入");
            }
            if (!spec.allowedProps.contains(propName)) {
                throw new IllegalArgumentException("Neo4j节点[" + spec.label + "]不允许写入属性[" + propName + "]");
            }
        }
        return new LinkedHashMap<>(safeProps);
    }

    /**
     * 校验关系属性。
     */
    private Map<String, Object> validateRelationProps(Map<String, Object> props) {
        Map<String, Object> safeProps = props == null ? Collections.emptyMap() : props;
        for (String propName : safeProps.keySet()) {
            // 关系的 projectId 必须来自方法参数, 防止跨项目关系被伪造。
            if (BLOCKED_RELATION_PROPS.contains(propName)) {
                throw new IllegalArgumentException("Neo4j关系属性[" + propName + "]由系统维护, 禁止通过props写入");
            }
        }
        return new LinkedHashMap<>(safeProps);
    }

    /**
     * 创建节点键参数。
     */
    private Map<String, Object> createNodeKeyParams(NodeSpec spec, Long projectId, Object nodeId) {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId不能为空");
        }
        if (nodeId == null) {
            throw new IllegalArgumentException(spec.label + "节点业务ID不能为空");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        if (!spec.projectKey()) {
            params.put("nodeId", nodeId);
        }
        return params;
    }

    /**
     * 创建关系参数。
     */
    private Map<String, Object> createRelationParams(RelationSpec spec, Long projectId, Object fromId, Object toId, Map<String, Object> props) {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId不能为空");
        }
        if (fromId == null || toId == null) {
            throw new IllegalArgumentException("关系起点和终点业务ID不能为空");
        }
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        if (!spec.fromSpec.projectKey()) {
            params.put("fromId", fromId);
        }
        if (!spec.toSpec.projectKey()) {
            params.put("toId", toId);
        }
        params.put("props", validateRelationProps(props));
        return params;
    }

    /**
     * 根据节点枚举获取节点规格。
     */
    private NodeSpec requireNodeSpec(NovelGraphNodeEnum nodeType) {
        NodeSpec spec = NODE_SPEC_MAP.get(nodeType);
        if (spec == null) {
            throw new IllegalArgumentException("未支持的Neo4j节点类型: " + nodeType);
        }
        return spec;
    }

    /**
     * 创建关系规格并校验起止节点方向。
     */
    private RelationSpec createRelationSpec(NovelGraphRelationEnum relationType, NovelGraphNodeEnum fromNode, NovelGraphNodeEnum toNode) {
        NodeSpec fromSpec = requireNodeSpec(fromNode);
        NodeSpec toSpec = requireNodeSpec(toNode);
        if (!"*".equals(relationType.getFromNode()) && !relationType.getFromNode().equals(fromSpec.label)) {
            throw new IllegalArgumentException("关系[" + relationType.getType() + "]起点应为[" + relationType.getFromNode() + "]");
        }
        if (!relationType.getToNode().equals(toSpec.label)) {
            throw new IllegalArgumentException("关系[" + relationType.getType() + "]终点应为[" + relationType.getToNode() + "]");
        }
        return new RelationSpec(relationType, fromSpec, toSpec);
    }

    /**
     * 创建节点规格表。
     */
    private static Map<NovelGraphNodeEnum, NodeSpec> createNodeSpecMap() {
        Map<NovelGraphNodeEnum, NodeSpec> map = new HashMap<>();
        map.put(NovelGraphNodeEnum.Project, new NodeSpec("Project", "projectId", Set.of(
                "name", "genre", "worldBuilding", "protagonistName", "styleDescription", "platform",
                "targetTotalWords", "targetChapterWords", "tokenBudget", "tokenHardLimit", "status", "remark")));
        map.put(NovelGraphNodeEnum.Volume, new NodeSpec("Volume", "volumeId", Set.of("number", "title", "summary")));
        map.put(NovelGraphNodeEnum.Chapter, new NodeSpec("Chapter", "chapterNumber", Set.of(
                "volumeId", "title", "summary", "pov", "wordCount", "status", "embedding")));
        map.put(NovelGraphNodeEnum.Character, new NodeSpec("Character", "characterId", Set.of(
                "name", "roleType", "description", "currentGoal", "goalProgress", "goalStatus",
                "currentEmotion", "emotionIntensity", "secondaryEmotion", "powerLevel", "currentStatus")));
        map.put(NovelGraphNodeEnum.Location, new NodeSpec("Location", "locationId", Set.of("name", "type", "summary")));
        map.put(NovelGraphNodeEnum.Clue, new NodeSpec("Clue", "clueId", Set.of(
                "name", "type", "subType", "description", "priority", "targetChapter", "tone",
                "summary", "revealLevel", "currentStage", "clueStatus", "lastAlertedChapter")));
        map.put(NovelGraphNodeEnum.Item, new NodeSpec("Item", "itemId", Set.of("name", "type", "summary", "quantity", "itemStatus")));
        map.put(NovelGraphNodeEnum.Event, new NodeSpec("Event", "eventId", Set.of("name", "summary", "chapterOccurred")));
        map.put(NovelGraphNodeEnum.Cheat, new NodeSpec("Cheat", "cheatId", Set.of(
                "name", "type", "summary", "origin", "limitation", "evolution", "currentStage")));
        map.put(NovelGraphNodeEnum.Alias, new NodeSpec("Alias", "aliasId", Set.of(
                "name", "type", "aliasContext", "summary", "revealed", "revealedTo")));
        map.put(NovelGraphNodeEnum.NarrativeRule, new NodeSpec("NarrativeRule", "ruleId", Set.of("name", "content", "priority")));
        return Collections.unmodifiableMap(map);
    }

    /**
     * 节点规格。
     */
    static final class NodeSpec {

        /**
         * Neo4j 标签名, 只来自 NovelGraphNodeEnum, 不接受外部字符串。
         */
        private final String label;

        /**
         * 节点业务主键属性名, 如 characterId、locationId、chapterNumber。
         */
        private final String keyProperty;

        /**
         * 节点允许写入的业务属性白名单。
         */
        private final Set<String> allowedProps;

        /**
         * 节点由系统模板托管的字段, 禁止通过 props 覆盖。
         */
        private final Set<String> blockedProps;

        /**
         * 创建节点规格。
         *
         * @param label Neo4j 标签名
         * @param keyProperty 节点业务主键属性名
         * @param allowedProps 允许写入的业务属性白名单
         */
        NodeSpec(String label, String keyProperty, Set<String> allowedProps) {
            this.label = label;
            this.keyProperty = keyProperty;
            this.allowedProps = mergePatchMetadata(allowedProps);
            this.blockedProps = mergeBlockedProps(keyProperty);
        }

        /**
         * 判断该节点是否直接用 projectId 作为唯一业务键。
         */
        boolean projectKey() {
            return "projectId".equals(keyProperty);
        }

        /**
         * 节点模板里的匹配模式。
         */
        String matchPattern() {
            if (projectKey()) {
                return "{projectId: $projectId}";
            }
            return "{projectId: $projectId, " + keyProperty + ": $nodeId}";
        }

        /**
         * 关系模板里的节点匹配模式。
         */
        String relationMatchPattern(String idParamName) {
            if (projectKey()) {
                return "{projectId: $projectId}";
            }
            return "{projectId: $projectId, " + keyProperty + ": $" + idParamName + "}";
        }

        /**
         * 加入 GraphPatch 元数据字段。
         */
        private static Set<String> mergePatchMetadata(Set<String> allowedProps) {
            java.util.HashSet<String> merged = new java.util.HashSet<>(allowedProps);
            merged.add("createdByPatchId");
            merged.add("updatedByPatchId");
            return Collections.unmodifiableSet(merged);
        }

        /**
         * 合并系统托管字段; Project 节点的 keyProperty 与 projectId 重合, 这里用 Set 去重。
         */
        private static Set<String> mergeBlockedProps(String keyProperty) {
            java.util.HashSet<String> blocked = new java.util.HashSet<>();
            blocked.add("projectId");
            blocked.add("archived");
            blocked.add("createdAt");
            blocked.add("updatedAt");
            blocked.add(keyProperty);
            return Collections.unmodifiableSet(blocked);
        }
    }

    /**
     * 关系规格。
     */
    static final class RelationSpec {

        /**
         * 关系类型枚举, 决定 Cypher 里的关系标签。
         */
        private final NovelGraphRelationEnum relationType;

        /**
         * 起点节点规格。
         */
        private final NodeSpec fromSpec;

        /**
         * 终点节点规格。
         */
        private final NodeSpec toSpec;

        /**
         * 创建关系规格。
         *
         * @param relationType 关系类型枚举
         * @param fromSpec 起点节点规格
         * @param toSpec 终点节点规格
         */
        RelationSpec(NovelGraphRelationEnum relationType, NodeSpec fromSpec, NodeSpec toSpec) {
            this.relationType = relationType;
            this.fromSpec = fromSpec;
            this.toSpec = toSpec;
        }
    }
}
