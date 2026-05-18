package net.lab1024.sa.admin.module.business.novel.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphNodeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphPatchOperationTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphRelationEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelPatchRiskLevel;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchOperationModel;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelWriteSessionVO.NovelGraphPatchVO;
import org.neo4j.driver.Record;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * GraphPatch 服务 —— 实现38种业务操作→5种Cypher模板的映射、分级和逆操作生成。
 * <p>
 * 核心职责:
 * 1. 38种操作→5种Cypher模板映射(GraphOps → Cypher)
 * 2. inversePatch生成(正向操作的逻辑逆)
 * 3. 操作分级(READY/HIGH/CONFLICT/BLOCKED)
 * 4. LLM抽取Prompt构建和JSON解析
 * 5. before值回填(从Neo4j查询当前值)
 *
 * @Author AI-Novel
 */
@Slf4j
@Service
public class NovelGraphPatchService {

    /** JSON解析器, 解析LLM返回的GraphPatch JSON数组 */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private NovelGraphService novelGraphService;

    @Resource
    private NovelLLMService novelLLMService;

    // ============================================================
    // 38→5 映射表
    // ============================================================

    /**
     * 38种操作 → 5种Cypher模板 映射。
     * <p>
     * 五种模板:
     * - MERGE_NODE: 创建或合并节点(CREATE_CHARACTER, CREATE_LOCATION, CREATE_ITEM, CREATE_EVENT)
     * - UPDATE_NODE_PROPS: 更新节点属性(CHANGE_EMOTION, ADVANCE_CLUE, CHANGE_GOAL等)
     * - MERGE_REL: 创建或合并关系(CHARACTER_APPEARS, ADD_KNOWS, MOVE_CHARACTER等)
     * - DELETE_REL: 删除关系(REMOVE_KNOWS, REMOVE_POSSESSES等)
     * - ARCHIVE_NODE: 归档节点(撤销时用，正常写作流程不触发)
     */
    public enum CypherTemplate {
        MERGE_NODE,
        UPDATE_NODE_PROPS,
        MERGE_REL,
        DELETE_REL,
        ARCHIVE_NODE
    }

    /**
     * 查询38种操作对应的Cypher模板。
     */
    public CypherTemplate getCypherTemplate(NovelGraphPatchOperationTypeEnum opType) {
        String code = opType.getCode();
        if (code.startsWith("CREATE_")) return CypherTemplate.MERGE_NODE;
        if (code.startsWith("CHANGE_") || code.equals("ADVANCE_CLUE") || code.equals("CONSUME_ITEM")) return CypherTemplate.UPDATE_NODE_PROPS;
        if (code.startsWith("REMOVE_")) return CypherTemplate.DELETE_REL;
        if (code.startsWith("ADD_") || code.startsWith("UPDATE_") || code.equals("REVEAL_ALIAS")) return CypherTemplate.MERGE_REL;
        if (code.endsWith("_APPEARS") || code.equals("MOVE_CHARACTER")) return CypherTemplate.MERGE_REL;
        return CypherTemplate.MERGE_REL;
    }

    /**
     * 查询操作对应的Neo4j节点类型。
     */
    public NovelGraphNodeEnum getNodeType(NovelGraphPatchOperationTypeEnum opType) {
        String code = opType.getCode();
        if (code.equals("CHANGE_CHARACTER_STATUS")) return NovelGraphNodeEnum.Character;
        if (code.startsWith("CHANGE_") && !code.contains("CHARACTER"))
            return NovelGraphNodeEnum.Character; // CHANGE_EMOTION/CHANGE_GOAL/CHANGE_GOAL_PROGRESS/CHANGE_POWER_LEVEL
        if (code.contains("CHARACTER") && !code.equals("MOVE_CHARACTER"))
            return NovelGraphNodeEnum.Character;
        if (code.contains("LOCATION")) return NovelGraphNodeEnum.Location;
        if (code.contains("ITEM")) return NovelGraphNodeEnum.Item;
        if (code.contains("EVENT")) return NovelGraphNodeEnum.Event;
        if (code.equals("ADVANCE_CLUE")) return NovelGraphNodeEnum.Clue;
        if (code.contains("CHEAT")) return NovelGraphNodeEnum.Cheat;
        if (code.contains("ALIAS") && !code.equals("REVEAL_ALIAS")) return NovelGraphNodeEnum.Alias;
        if (code.equals("REVEAL_ALIAS")) return NovelGraphNodeEnum.Alias;
        // 关系类操作不直接对应节点
        return null;
    }

    /**
     * 查询操作对应的Neo4j关系类型。
     */
    public NovelGraphRelationEnum getRelationType(NovelGraphPatchOperationTypeEnum opType) {
        String code = opType.getCode();
        if (code.contains("KNOWS") && code.startsWith("ADD_") || code.startsWith("UPDATE_") && code.contains("KNOWS") || code.contains("KNOWS") && code.startsWith("REMOVE_"))
            return NovelGraphRelationEnum.KNOWS;
        if (code.contains("LOVES")) return NovelGraphRelationEnum.LOVES;
        if (code.contains("HATES")) return NovelGraphRelationEnum.HATES;
        if (code.contains("IS_FAMILY_OF")) return NovelGraphRelationEnum.IS_FAMILY_OF;
        if (code.endsWith("_APPEARS")) return NovelGraphRelationEnum.APPEARS_IN;
        if (code.equals("MOVE_CHARACTER")) return NovelGraphRelationEnum.CURRENTLY_AT;
        if (code.equals("ADVANCE_CLUE") && code.contains("ADVANCE")) return NovelGraphRelationEnum.ADVANCES;
        if (code.contains("POSSESSES")) return NovelGraphRelationEnum.POSSESSES;
        if (code.contains("PARTICIPATES_IN")) return NovelGraphRelationEnum.PARTICIPATES_IN;
        if (code.contains("DRIVES")) return NovelGraphRelationEnum.DRIVES;
        if (code.contains("KNOWS_ABOUT")) return NovelGraphRelationEnum.KNOWS_ABOUT;
        if (code.contains("HAS_CHEAT")) return NovelGraphRelationEnum.HAS_CHEAT;
        if (code.contains("HAS_ALIAS")) return NovelGraphRelationEnum.HAS_ALIAS;
        if (code.equals("REVEAL_ALIAS")) return NovelGraphRelationEnum.KNOWS_ALIAS;
        return null;
    }

    // ============================================================
    // InversePatch 生成
    // ============================================================

    /**
     * 生成逆操作Patch —— 用于撤销。
     * <p>
     * 规则:
     * - CREATE_CHARACTER → inverse = ARCHIVE_NODE(Character)
     * - CHANGE_EMOTION → inverse = 恢复before值
     * - MOVE_CHARACTER → inverse = 恢复原CURRENTLY_AT(forward: DELETE_REL旧+ MERGE_REL新; inverse: 反之)
     * - ADD_KNOWS → inverse = REMOVE_KNOWS
     * - REMOVE_KNOWS → inverse = ADD_KNOWS(恢复原relationType)
     * - ADVANCE_CLUE → inverse = 恢复原revealLevel/currentStage/summary
     */
    public NovelGraphPatchModel generateInversePatch(NovelGraphPatchModel forwardPatch) {
        NovelGraphPatchModel inverse = NovelGraphPatchModel.builder()
                .patchId("INVERSE_" + forwardPatch.getPatchId())
                .projectId(forwardPatch.getProjectId())
                .chapterNumber(forwardPatch.getChapterNumber())
                .operations(new ArrayList<>())
                .build();

        // 逆序处理(后执行的先撤销)
        List<NovelGraphPatchOperationModel> reversed = new ArrayList<>(forwardPatch.getOperations());
        Collections.reverse(reversed);

        for (NovelGraphPatchOperationModel fwdOp : reversed) {
            NovelGraphPatchOperationModel invOp = generateInverseOperation(fwdOp);
            if (invOp != null) {
                inverse.getOperations().add(invOp);
            }
        }
        return inverse;
    }

    /**
     * 生成单个操作的逆操作。
     */
    private NovelGraphPatchOperationModel generateInverseOperation(NovelGraphPatchOperationModel fwdOp) {
        String code = fwdOp.getType().getCode();

        if (code.startsWith("CREATE_")) {
            // 新增实体 → 归档
            return NovelGraphPatchOperationModel.builder()
                    .opId("INV_" + fwdOp.getOpId())
                    .type(fwdOp.getType())
                    .characterName(fwdOp.getCharacterName())
                    .targetName(fwdOp.getTargetName())
                    .entityId(fwdOp.getEntityId())
                    .before(fwdOp.getAfter())
                    .after(null)
                    .confidence(1.0f)
                    .build();
        }

        if (code.startsWith("CHANGE_") || code.equals("ADVANCE_CLUE") || code.equals("CONSUME_ITEM")) {
            // 属性变更 → 恢复before值
            return NovelGraphPatchOperationModel.builder()
                    .opId("INV_" + fwdOp.getOpId())
                    .type(fwdOp.getType())
                    .characterName(fwdOp.getCharacterName())
                    .targetName(fwdOp.getTargetName())
                    .entityId(fwdOp.getEntityId())
                    .before(fwdOp.getAfter())
                    .after(fwdOp.getBefore())
                    .confidence(1.0f)
                    .build();
        }

        if (code.startsWith("ADD_KNOWS") || code.startsWith("ADD_LOVES") || code.startsWith("ADD_HATES")
                || code.startsWith("ADD_IS_FAMILY_OF") || code.startsWith("ADD_POSSESSES")
                || code.startsWith("ADD_PARTICIPATES_IN") || code.startsWith("ADD_DRIVES")
                || code.startsWith("ADD_KNOWS_ABOUT") || code.startsWith("ADD_HAS_CHEAT") || code.startsWith("ADD_HAS_ALIAS")) {
            // 新增关系 → 删除
            String removeCode = code.replace("ADD_", "REMOVE_");
            NovelGraphPatchOperationTypeEnum removeType = NovelGraphPatchOperationTypeEnum.fromCode(removeCode);
            return NovelGraphPatchOperationModel.builder()
                    .opId("INV_" + fwdOp.getOpId())
                    .type(removeType != null ? removeType : fwdOp.getType())
                    .characterName(fwdOp.getCharacterName())
                    .targetName(fwdOp.getTargetName())
                    .entityId(fwdOp.getEntityId())
                    .targetEntityId(fwdOp.getTargetEntityId())
                    .confidence(1.0f)
                    .build();
        }

        if (code.startsWith("REMOVE_")) {
            // 删除关系 → 新增(恢复)
            String addCode = code.replace("REMOVE_", "ADD_");
            NovelGraphPatchOperationTypeEnum addType = NovelGraphPatchOperationTypeEnum.fromCode(addCode);
            return NovelGraphPatchOperationModel.builder()
                    .opId("INV_" + fwdOp.getOpId())
                    .type(addType != null ? addType : fwdOp.getType())
                    .characterName(fwdOp.getCharacterName())
                    .targetName(fwdOp.getTargetName())
                    .entityId(fwdOp.getEntityId())
                    .targetEntityId(fwdOp.getTargetEntityId())
                    .before(fwdOp.getAfter())
                    .after(fwdOp.getBefore())
                    .confidence(1.0f)
                    .build();
        }

        if (code.equals("MOVE_CHARACTER")) {
            // 移动 → 逆向移动
            return NovelGraphPatchOperationModel.builder()
                    .opId("INV_" + fwdOp.getOpId())
                    .type(fwdOp.getType())
                    .characterName(fwdOp.getCharacterName())
                    .targetName(fwdOp.getBefore())
                    .entityId(fwdOp.getEntityId())
                    .before(fwdOp.getAfter())
                    .after(fwdOp.getBefore())
                    .confidence(1.0f)
                    .build();
        }

        if (code.equals("REVEAL_ALIAS")) {
            // 识破 → 恢复未识破
            return NovelGraphPatchOperationModel.builder()
                    .opId("INV_" + fwdOp.getOpId())
                    .type(fwdOp.getType())
                    .characterName(fwdOp.getCharacterName())
                    .targetName(fwdOp.getTargetName())
                    .entityId(fwdOp.getEntityId())
                    .before("true")
                    .after("false")
                    .confidence(1.0f)
                    .build();
        }

        log.warn("无法生成逆操作, 未知操作类型: {}", code);
        return null;
    }

    // ============================================================
    // LLM抽取 Prompt构建
    // ============================================================

    /**
     * GraphPatch抽取的System Prompt。
     * <p>
     * 告诉AI只输出38种操作中的JSON数组, 格式严格限定, 不允许自由发挥。
     */
    private static final String EXTRACTION_SYSTEM_PROMPT = "你是一个小说知识图谱分析器。"
            + "请仔细阅读以下章节正文, 分析其中的角色状态变化、关系发展和线索推进, "
            + "以JSON数组格式输出GraphPatch操作列表。\n"
            + "只允许以下38种操作类型, 每个操作严格按格式输出:\n"
            + "{\"type\":\"操作编码\",\"characterName\":\"角色名\",\"targetName\":\"目标名\","
            + "\"after\":\"变更后的值\",\"confidence\":0.8}\n\n"
            + "# 允许的操作类型:\n"
            + "CREATE_CHARACTER(新增角色), CREATE_LOCATION(新增地点), CREATE_ITEM(新增物品), CREATE_EVENT(新增事件)\n"
            + "CHARACTER_APPEARS(角色出场), LOCATION_APPEARS(地点出场), ITEM_APPEARS(物品出场), EVENT_APPEARS(事件出场), CHEAT_APPEARS(金手指出场), ALIAS_APPEARS(马甲出场)\n"
            + "CHANGE_EMOTION(情绪变化,after=ANGER/FEAR/DETERMINED/DESPAIR/JOY/SADNESS/CALM/SUSPICIOUS/SHAME/PRIDE/HOPE/GRIEF/ANXIETY), "
            + "CHANGE_GOAL(目标变化), CHANGE_GOAL_PROGRESS(目标进度,after=0~1), CHANGE_POWER_LEVEL(战力变化), CHANGE_CHARACTER_STATUS(存活状态变化)\n"
            + "MOVE_CHARACTER(角色移动,after=地点名)\n"
            + "ADVANCE_CLUE(线索推进,after=推进描述)\n"
            + "ADD_KNOWS/UPDATE_KNOWS/REMOVE_KNOWS(认识关系), ADD_LOVES/UPDATE_LOVES/REMOVE_LOVES(爱慕关系), ADD_HATES/UPDATE_HATES/REMOVE_HATES(仇恨关系), ADD_IS_FAMILY_OF/UPDATE_IS_FAMILY_OF/REMOVE_IS_FAMILY_OF(亲缘关系)\n"
            + "ADD_POSSESSES/REMOVE_POSSESSES(持有物品), ADD_PARTICIPATES_IN(参与事件), ADD_DRIVES(推动线索), ADD_KNOWS_ABOUT(知情线索), ADD_HAS_CHEAT(拥有金手指), ADD_HAS_ALIAS(拥有马甲), REVEAL_ALIAS(马甲被识破), CONSUME_ITEM(物品消耗)\n\n"
            + "# 注意事项:\n"
            + "1. 只输出发生了变化的操作, 没有变化的不要输出\n"
            + "2. confidence表示你对这个变更的信心(0.0~1.0), 明确写了才高信心\n"
            + "3. 新增角色只用CREATE_CHARACTER, 不要用CHARACTER_APPEARS+CREATE_CHARACTER两次\n"
            + "4. 出场记录对每个明确出场的主要角色/地点都要输出\n"
            + "5. 情绪变化用枚举值(ANGER/FEAR等), 不要用自然语言\n"
            + "6. 不要输出任何解释文字, 只要纯JSON数组";

    /**
     * 构建GraphPatch抽取Prompt。
     * <p>
     * System Prompt定义输出格式和38种操作, User Prompt是章节正文。
     * 返回完整Prompt供NovelLLMService调用。
     *
     * @return 抽取Prompt
     */
    public String buildExtractionPrompt() {
        return EXTRACTION_SYSTEM_PROMPT;
    }

    /**
     * 解析LLM返回的GraphPatch JSON, 生成NovelGraphPatchModel。
     * <p>
     * 解析失败时返回空Patch(operations=[]), 让用户跳过图谱更新直接发布。
     *
     * @param llmResponseJson LLM返回的原始JSON文本
     * @param projectId 项目ID
     * @param chapterNumber 章节号
     * @return 解析后的GraphPatch模型
     */
    public NovelGraphPatchModel parseExtractionResult(String llmResponseJson, Long projectId, Integer chapterNumber) {
        NovelGraphPatchModel patch = NovelGraphPatchModel.builder()
                .patchId(UUID.randomUUID().toString())
                .projectId(projectId)
                .chapterNumber(chapterNumber)
                .operations(new ArrayList<>())
                .build();

        if (llmResponseJson == null || llmResponseJson.trim().isEmpty()) {
            log.warn("GraphPatch抽取结果为空, projectId={}, chapterNumber={}", projectId, chapterNumber);
            return patch;
        }

        try {
            // 清洗LLM输出: 提取JSON数组
            String json = extractJsonArray(llmResponseJson);
            if (json == null) {
                log.warn("GraphPatch抽取结果中未找到JSON数组, raw={}", llmResponseJson.substring(0, Math.min(200, llmResponseJson.length())));
                return patch;
            }

            List<Map<String, Object>> rawOps = objectMapper.readValue(json,
                    new TypeReference<List<Map<String, Object>>>() {});

            for (int i = 0; i < rawOps.size(); i++) {
                Map<String, Object> rawOp = rawOps.get(i);
                String typeCode = (String) rawOp.get("type");
                if (typeCode == null) continue;

                NovelGraphPatchOperationTypeEnum opType = NovelGraphPatchOperationTypeEnum.fromCode(typeCode);
                if (opType == null) {
                    log.warn("未知操作类型: {}, 跳过", typeCode);
                    continue;
                }

                NovelGraphPatchOperationModel op = NovelGraphPatchOperationModel.builder()
                        .opId(patch.getPatchId() + "_" + i)
                        .type(opType)
                        .characterName((String) rawOp.get("characterName"))
                        .targetName((String) rawOp.get("targetName"))
                        .after((String) rawOp.get("after"))
                        .confidence(rawOp.get("confidence") != null
                                ? ((Number) rawOp.get("confidence")).floatValue() : 0.5f)
                        .extraProps(extractExtraProps(rawOp))
                        .build();

                patch.getOperations().add(op);
            }

            log.info("GraphPatch解析完成, projectId={}, chapterNumber={}, operationCount={}",
                    projectId, chapterNumber, patch.getOperations().size());

        } catch (Exception e) {
            log.error("GraphPatch JSON解析失败, projectId={}, chapterNumber={}", projectId, chapterNumber, e);
        }
        return patch;
    }

    // ============================================================
    // LLM抽取 → 解析 → before回填 全流程
    // ============================================================

    /**
     * 抽取并解析GraphPatch —— 把正文发给LLM, 解析JSON, 回填before值。
     * <p>
     * 完整流程: 构建抽取Prompt → LLM生成 → 解析JSON → 回填before值 → 返回Patch模型。
     * LLM返回空或解析失败返回空Patch(operations=[]), 允许跳过图谱更新直接发布。
     *
     * @param chapterContent 章节正文
     * @param projectId 项目ID
     * @param chapterNumber 章节号
     * @param userId 当前用户ID
     * @return GraphPatch模型, 可能含零操作
     */
    public NovelGraphPatchModel extractAndParsePatches(String chapterContent, Long projectId,
                                                        Integer chapterNumber, Long userId) {
        try {
            String extractionPrompt = buildExtractionPrompt();
            String llmResponse = novelLLMService.extractGraphPatch(chapterContent, extractionPrompt, userId);
            if (llmResponse == null) {
                log.info("用户{}未配置API Key, 跳过GraphPatch抽取", userId);
                return emptyPatch(projectId, chapterNumber);
            }

            NovelGraphPatchModel patch = parseExtractionResult(llmResponse, projectId, chapterNumber);
            if (patch.getOperations().isEmpty()) {
                log.info("GraphPatch抽取结果为空或解析失败, projectId={}, chapterNumber={}", projectId, chapterNumber);
                return patch;
            }

            backfillBeforeValues(patch);
            return patch;
        } catch (Exception e) {
            log.error("GraphPatch抽取流程异常, projectId={}, chapterNumber={}", projectId, chapterNumber, e);
            return emptyPatch(projectId, chapterNumber);
        }
    }

    /**
     * 回填before值 —— 从Neo4j查询每条操作的当前值, 填入before字段。
     * <p>
     * 只对 UPDATE_NODE_PROPS 类操作查询——CREATE和关系类操作before为null。
     * 查询Neo4j节点的当前属性Map, 将操作涉及的属性值写入before。
     */
    private void backfillBeforeValues(NovelGraphPatchModel patch) {
        for (NovelGraphPatchOperationModel op : patch.getOperations()) {
            CypherTemplate template = getCypherTemplate(op.getType());
            if (template != CypherTemplate.UPDATE_NODE_PROPS) {
                continue;
            }

            NovelGraphNodeEnum nodeType = getNodeType(op.getType());
            if (nodeType == null) {
                continue;
            }

            // 确定业务ID: 当前只有角色节点通过characterName查回characterId, 其他节点同样逻辑
            Object nodeId = resolveNodeId(op, nodeType, patch.getProjectId());
            if (nodeId == null) {
                log.info("无法解析节点ID, 跳过before回填 opType={}, characterName={}",
                        op.getType().getCode(), op.getCharacterName());
                continue;
            }

            Map<String, Object> currentProps = novelGraphService.queryNodeProps(
                    nodeType, patch.getProjectId(), nodeId);

            String beforeValue = extractPropertyForOperation(op, currentProps);
            op.setBefore(beforeValue);
        }
    }

    /**
     * 从操角色名查MySQL获节点业务ID。
     */
    private Object resolveNodeId(NovelGraphPatchOperationModel op, NovelGraphNodeEnum nodeType, Long projectId) {
        String name = op.getCharacterName();
        if (name == null && op.getTargetName() != null) {
            name = op.getTargetName();
        }
        if (name == null) {
            return null;
        }

        // 对每种节点类型, 通过 name + projectId 查MySQL获ID
        switch (nodeType) {
            case Character:
                return resolveCharacterId(name, projectId);
            case Location:
                return resolveLocationId(name, projectId);
            case Clue:
                return resolveClueId(name, projectId);
            case Item:
                return resolveItemId(name, projectId);
            default:
                return null;
        }
    }

    /**
     * 从节点当前属性中提取操作对应的值。
     */
    private String extractPropertyForOperation(NovelGraphPatchOperationModel op, Map<String, Object> currentProps) {
        String code = op.getType().getCode();
        if (code.equals("CHANGE_EMOTION")) {
            Object v = currentProps.get("currentEmotion");
            return v != null ? v.toString() : null;
        }
        if (code.equals("CHANGE_GOAL")) {
            Object v = currentProps.get("currentGoal");
            return v != null ? v.toString() : null;
        }
        if (code.equals("CHANGE_GOAL_PROGRESS")) {
            Object v = currentProps.get("goalProgress");
            return v != null ? v.toString() : null;
        }
        if (code.equals("CHANGE_POWER_LEVEL")) {
            Object v = currentProps.get("powerLevel");
            return v != null ? v.toString() : null;
        }
        if (code.equals("CHANGE_CHARACTER_STATUS")) {
            Object v = currentProps.get("currentStatus");
            return v != null ? v.toString() : null;
        }
        if (code.equals("ADVANCE_CLUE") || code.equals("CONSUME_ITEM")) {
            Object v = currentProps.get("summary");
            return v != null ? v.toString() : null;
        }
        return null;
    }

    // ============================================================
    // Model → VO 转换
    // ============================================================

    /**
     * 将GraphPatch Model列表转换为前端VO列表。
     * <p>
     * 审阅页面只展示操作描述/角色名/前后对比/置信度/风险等级, 不暴露内部entityId。
     *
     * @param patch GraphPatch模型
     * @return GraphPatch VO列表
     */
    public List<NovelGraphPatchVO> convertToVoList(NovelGraphPatchModel patch) {
        List<NovelGraphPatchVO> voList = new ArrayList<>();
        for (NovelGraphPatchOperationModel op : patch.getOperations()) {
            NovelGraphPatchVO vo = new NovelGraphPatchVO();
            vo.setOperationType(op.getType().getCode());
            vo.setOperationDesc(op.getType().getDesc());
            vo.setCharacterName(op.getCharacterName());
            vo.setBeforeValue(op.getBefore());
            vo.setAfterValue(op.getAfter());
            vo.setConfidence(op.getConfidence());
            vo.setRiskLevel(op.getType().getRiskLevel().name());
            vo.setConfirmed(op.getType().getRiskLevel() != NovelPatchRiskLevel.HIGH);
            voList.add(vo);
        }
        return voList;
    }

    // ============================================================
    // MySQL ID 解析
    // ============================================================

    @Resource
    private net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterDao novelCharacterDao;
    @Resource
    private net.lab1024.sa.admin.module.business.novel.dao.NovelLocationDao novelLocationDao;
    @Resource
    private net.lab1024.sa.admin.module.business.novel.dao.NovelClueDao novelClueDao;
    @Resource
    private net.lab1024.sa.admin.module.business.novel.dao.NovelItemDao novelItemDao;

    private Long resolveCharacterId(String name, Long projectId) {
        var entity = novelCharacterDao.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity>()
                .eq(net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity::getProjectId, projectId)
                .eq(net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity::getName, name)
                .eq(net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity::getDeletedFlag, false)
                .last("LIMIT 1"));
        return entity != null ? entity.getId() : null;
    }

    private Long resolveLocationId(String name, Long projectId) {
        var entity = novelLocationDao.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity>()
                .eq(net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity::getProjectId, projectId)
                .eq(net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity::getName, name)
                .eq(net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity::getDeletedFlag, false)
                .last("LIMIT 1"));
        return entity != null ? entity.getId() : null;
    }

    private Long resolveClueId(String name, Long projectId) {
        var entity = novelClueDao.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity>()
                .eq(net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity::getProjectId, projectId)
                .eq(net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity::getName, name)
                .eq(net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity::getDeletedFlag, false)
                .last("LIMIT 1"));
        return entity != null ? entity.getId() : null;
    }

    private Long resolveItemId(String name, Long projectId) {
        var entity = novelItemDao.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<net.lab1024.sa.admin.module.business.novel.domain.entity.NovelItemEntity>()
                .eq(net.lab1024.sa.admin.module.business.novel.domain.entity.NovelItemEntity::getProjectId, projectId)
                .eq(net.lab1024.sa.admin.module.business.novel.domain.entity.NovelItemEntity::getName, name)
                .eq(net.lab1024.sa.admin.module.business.novel.domain.entity.NovelItemEntity::getDeletedFlag, false)
                .last("LIMIT 1"));
        return entity != null ? entity.getId() : null;
    }

    private NovelGraphPatchModel emptyPatch(Long projectId, Integer chapterNumber) {
        return NovelGraphPatchModel.builder()
                .patchId(UUID.randomUUID().toString())
                .projectId(projectId)
                .chapterNumber(chapterNumber)
                .operations(new ArrayList<>())
                .build();
    }

    // ============================================================
    // GraphPatch 白名单执行 (Slice 2)
    // ============================================================

    /**
     * 执行GraphPatch列表 —— 将用户确认后的VO列表映射到5种Cypher模板写入Neo4j。
     * <p>
     * 只处理 UPDATE_NODE_PROPS / MERGE_REL / DELETE_REL 三类操作。
     * CREATE_* 操作需要先创建MySQL实体再写Neo4j, 当前阶段跳过(前端标为"需手动创建")。
     *
     * @param confirmedVos 用户确认后的Patch VO列表
     * @param projectId 项目ID
     * @param chapterNumber 当前章节号
     */
    public void executePatches(List<NovelGraphPatchVO> confirmedVos, Long projectId, Integer chapterNumber) {
        int applied = 0;
        for (NovelGraphPatchVO vo : confirmedVos) {
            if (Boolean.FALSE.equals(vo.getConfirmed())) {
                continue;
            }

            NovelGraphPatchOperationTypeEnum opType = NovelGraphPatchOperationTypeEnum.fromCode(vo.getOperationType());
            if (opType == null) {
                log.warn("未知操作类型: {}, 跳过执行", vo.getOperationType());
                continue;
            }

            try {
                dispatchExecution(opType, vo, projectId, chapterNumber);
                applied++;
            } catch (Exception e) {
                log.error("执行GraphPatch失败 opType={}, characterName={}", vo.getOperationType(), vo.getCharacterName(), e);
                throw new RuntimeException("GraphPatch执行失败: " + vo.getOperationDesc() + " - " + e.getMessage(), e);
            }
        }
        log.info("GraphPatch执行完成 projectId={}, chapterNumber={}, total={}, applied={}",
                projectId, chapterNumber, confirmedVos.size(), applied);
    }

    /**
     * 按操作类型分发到具体Cypher模板执行。
     */
    private void dispatchExecution(NovelGraphPatchOperationTypeEnum opType, NovelGraphPatchVO vo,
                                    Long projectId, Integer chapterNumber) {
        CypherTemplate template = getCypherTemplate(opType);

        switch (template) {
            case UPDATE_NODE_PROPS:
                executeUpdateProps(opType, vo, projectId);
                break;
            case MERGE_REL:
                executeMergeRel(opType, vo, projectId, chapterNumber);
                break;
            case DELETE_REL:
                executeDeleteRel(opType, vo, projectId, chapterNumber);
                break;
            case MERGE_NODE:
                log.info("CREATE_* 操作跳过(需先创建MySQL实体): {}", opType.getCode());
                break;
            default:
                log.warn("未支持的Cypher模板: {}", template);
        }
    }

    /**
     * 执行 UPDATE_NODE_PROPS —— 更新节点属性。
     */
    private void executeUpdateProps(NovelGraphPatchOperationTypeEnum opType, NovelGraphPatchVO vo, Long projectId) {
        NovelGraphNodeEnum nodeType = getNodeType(opType);
        if (nodeType == null) return;

        Long nodeId = resolveNodeIdByName(vo.getCharacterName(), nodeType, projectId);
        if (nodeId == null) {
            log.info("UPDATE_PROPS跳过: 实体不存在 name={}, nodeType={}", vo.getCharacterName(), nodeType);
            return;
        }

        Map<String, Object> props = buildUpdateProps(opType, vo);
        novelGraphService.updateNodeProps(nodeType, projectId, nodeId, props);
    }

    /**
     * 构建 UPDATE_NODE_PROPS 的属性Map。
     */
    private Map<String, Object> buildUpdateProps(NovelGraphPatchOperationTypeEnum opType, NovelGraphPatchVO vo) {
        Map<String, Object> props = new LinkedHashMap<>();
        String code = opType.getCode();
        if (code.equals("CHANGE_EMOTION")) {
            props.put("currentEmotion", vo.getAfterValue());
        } else if (code.equals("CHANGE_GOAL")) {
            props.put("currentGoal", vo.getAfterValue());
        } else if (code.equals("CHANGE_GOAL_PROGRESS")) {
            props.put("goalProgress", parseBigDecimal(vo.getAfterValue()));
        } else if (code.equals("CHANGE_POWER_LEVEL")) {
            props.put("powerLevel", vo.getAfterValue());
        } else if (code.equals("CHANGE_CHARACTER_STATUS")) {
            props.put("currentStatus", vo.getAfterValue());
        } else if (code.equals("ADVANCE_CLUE")) {
            props.put("summary", vo.getAfterValue());
        } else if (code.equals("CONSUME_ITEM")) {
            props.put("quantity", vo.getAfterValue() != null ? Integer.parseInt(vo.getAfterValue()) : 0);
        }
        return props;
    }

    /**
     * 执行 MERGE_REL —— 创建或合并关系。
     */
    private void executeMergeRel(NovelGraphPatchOperationTypeEnum opType, NovelGraphPatchVO vo,
                                  Long projectId, Integer chapterNumber) {
        NovelGraphRelationEnum relType = getRelationType(opType);
        if (relType == null) return;

        // 解析起止节点
        NovelGraphNodeEnum fromNodeType = NovelGraphNodeEnum.valueOf(relType.getFromNode().equals("*") ? 
                inferFromNodeForAsterisk(opType) : relType.getFromNode());
        NovelGraphNodeEnum toNodeType = NovelGraphNodeEnum.valueOf(relType.getToNode());

        Object fromId = resolveNodeIdByName(vo.getCharacterName(), fromNodeType, projectId);
        if (fromId == null && !opType.getCode().startsWith("CREATE_")) {
            log.info("MERGE_REL跳过: from节点不存在 name={}, nodeType={}", vo.getCharacterName(), fromNodeType);
            return;
        }

        Object toId;
        if (toNodeType == NovelGraphNodeEnum.Chapter) {
            // APPEARS_IN/ADVANCES 的目标是章节
            toId = chapterNumber;
        } else {
            String targetName = vo.getAfterValue() != null ? vo.getAfterValue() : vo.getBeforeValue();
            toId = resolveNodeIdByName(targetName, toNodeType, projectId);
        }
        if (toId == null) {
            log.info("MERGE_REL跳过: to节点不存在 target={}, nodeType={}", vo.getAfterValue(), toNodeType);
            return;
        }

        Map<String, Object> props = new LinkedHashMap<>();
        novelGraphService.mergeRelation(relType, projectId, fromNodeType, fromId, toNodeType, toId, props);
    }

    /**
     * 执行 DELETE_REL —— 删除关系。
     */
    private void executeDeleteRel(NovelGraphPatchOperationTypeEnum opType, NovelGraphPatchVO vo,
                                   Long projectId, Integer chapterNumber) {
        NovelGraphRelationEnum relType = getRelationType(opType);
        if (relType == null) return;

        NovelGraphNodeEnum fromNodeType = NovelGraphNodeEnum.valueOf(
                relType.getFromNode().equals("*") ? inferFromNodeForAsterisk(opType) : relType.getFromNode());
        NovelGraphNodeEnum toNodeType = NovelGraphNodeEnum.valueOf(relType.getToNode());

        Object fromId = resolveNodeIdByName(vo.getCharacterName(), fromNodeType, projectId);
        if (fromId == null) {
            log.info("DELETE_REL跳过: from节点不存在 name={}", vo.getCharacterName());
            return;
        }

        Object toId;
        if (toNodeType == NovelGraphNodeEnum.Chapter) {
            toId = chapterNumber;
        } else {
            toId = resolveNodeIdByName(vo.getBeforeValue() != null ? vo.getBeforeValue() : vo.getAfterValue(),
                    toNodeType, projectId);
        }
        if (toId == null) {
            log.info("DELETE_REL跳过: to节点不存在");
            return;
        }

        novelGraphService.deleteRelation(relType, projectId, fromNodeType, fromId, toNodeType, toId);
    }

    /**
     * 对于 fromNode="*" 的关系, 根据操作类型推断实际起点节点类型。
     */
    private String inferFromNodeForAsterisk(NovelGraphPatchOperationTypeEnum opType) {
        String code = opType.getCode();
        if (code.contains("CHARACTER")) return "Character";
        if (code.contains("LOCATION")) return "Location";
        if (code.contains("ITEM")) return "Item";
        if (code.contains("EVENT")) return "Event";
        if (code.contains("CHEAT")) return "Cheat";
        if (code.contains("ALIAS")) return "Alias";
        return "Character";
    }

    /**
     * 通过名称+项目ID解析节点业务ID。
     */
    private Long resolveNodeIdByName(String name, NovelGraphNodeEnum nodeType, Long projectId) {
        if (name == null) return null;
        switch (nodeType) {
            case Character: return resolveCharacterId(name, projectId);
            case Location: return resolveLocationId(name, projectId);
            case Clue: return resolveClueId(name, projectId);
            case Item: return resolveItemId(name, projectId);
            default: return null;
        }
    }

    private java.math.BigDecimal parseBigDecimal(String value) {
        if (value == null) return null;
        try { return new java.math.BigDecimal(value); } catch (NumberFormatException e) { return null; }
    }

    /**
     * 按风险等级过滤操作 —— 高风险操作默认不勾选。
     * <p>
     * return READY级(默认勾选)和HIGH级(默认不勾选)分开。
     */
    public PatchSplit splitByRisk(NovelGraphPatchModel patch) {
        PatchSplit split = new PatchSplit();
        for (NovelGraphPatchOperationModel op : patch.getOperations()) {
            if (op.getType().getRiskLevel() == NovelPatchRiskLevel.HIGH) {
                split.highRiskOps.add(op);
            } else {
                split.readyOps.add(op);
            }
        }
        return split;
    }

    /**
     * Patch按风险分拆结果。
     */
    public static class PatchSplit {
        /** 低风险操作, 默认勾选 */
        public List<NovelGraphPatchOperationModel> readyOps = new ArrayList<>();
        /** 高风险操作, 默认不勾选 */
        public List<NovelGraphPatchOperationModel> highRiskOps = new ArrayList<>();
    }

    // ============================================================
    // 辅助方法
    // ============================================================

    /**
     * 从LLM返回文本中提取JSON数组。
     */
    private String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        // 尝试找 ```json ```
        int codeStart = text.indexOf("```json");
        if (codeStart >= 0) {
            int codeEnd = text.indexOf("```", codeStart + 6);
            if (codeEnd > codeStart) {
                String block = text.substring(codeStart + 6, codeEnd);
                int bs = block.indexOf('[');
                int be = block.lastIndexOf(']');
                if (bs >= 0 && be > bs) return block.substring(bs, be + 1);
            }
        }
        return null;
    }

    /**
     * 提取额外属性(如relationType、loveStatus、familyType等)。
     */
    private Map<String, Object> extractExtraProps(Map<String, Object> rawOp) {
        Map<String, Object> extra = new LinkedHashMap<>();
        String[] extraKeys = {"relationType", "loveStatus", "hateIntensity", "familyType",
                "emotionIntensity", "secondaryEmotion", "quantity", "revealedTo"};
        for (String key : extraKeys) {
            if (rawOp.containsKey(key)) {
                extra.put(key, rawOp.get(key));
            }
        }
        return extra.isEmpty() ? null : extra;
    }

    // ============================================================
    // 写后校验 (Task 18)
    // ============================================================

    /**
     * 写后校验 —— DORMANT伏笔回溯 + 线索停滞检查。
     * <p>
     * 纯Cypher查询, 返回提醒列表。
     *
     * @param projectId 项目ID
     * @param currentChapterNumber 当前完成的章节号
     * @return 校验提醒列表
     */
    public List<String> runPostWriteChecks(Long projectId, Integer currentChapterNumber) {
        List<String> reminders = new ArrayList<>();

        // DORMANT伏笔回溯: 子类型FORESHADOWING且DORMANT超过10章的线索
        String dormantCypher = "MATCH (cl:Clue {projectId: $projectId, archived: false}) "
                + "WHERE cl.clueStatus = 'DORMANT' AND cl.subType = 'FORESHADOWING' "
                + "AND cl.lastAlertedChapter IS NULL OR cl.lastAlertedChapter < $minChapter "
                + "OPTIONAL MATCH (ch:Chapter {projectId: $projectId})-[r:ADVANCES]->(cl) "
                + "WHERE ch.chapterNumber >= $minChapter "
                + "WITH cl, count(r) AS recentAdvances "
                + "WHERE recentAdvances = 0 "
                + "RETURN cl.name AS name, cl.description AS description "
                + "LIMIT 5";

        List<Record> dormantRecords = novelGraphService.query(dormantCypher,
                Map.of("projectId", projectId, "minChapter", currentChapterNumber - 10));
        for (Record r : dormantRecords) {
            reminders.add("DORMANT伏笔可启动: " + r.get("name").asString()
                    + "——" + (r.get("description").isNull() ? "" : r.get("description").asString()));
        }

        // 线索停滞: ACTIVE线索最近10章无推进
        String stagnantCypher = "MATCH (cl:Clue {projectId: $projectId, archived: false}) "
                + "WHERE cl.clueStatus = 'ACTIVE' "
                + "AND (cl.lastAlertedChapter IS NULL OR cl.lastAlertedChapter < $minChapter) "
                + "OPTIONAL MATCH (ch:Chapter {projectId: $projectId})-[r:ADVANCES]->(cl) "
                + "WHERE ch.chapterNumber >= $minChapter "
                + "WITH cl, count(r) AS recentAdvances "
                + "WHERE recentAdvances = 0 "
                + "RETURN cl.name AS name "
                + "LIMIT 5";

        List<Record> stagnantRecords = novelGraphService.query(stagnantCypher,
                Map.of("projectId", projectId, "minChapter", currentChapterNumber - 10));
        for (Record r : stagnantRecords) {
            reminders.add("线索停滞超过10章: " + r.get("name").asString());
        }

        // 角色失踪: ACTIVE角色最近15章未出场
        String missingCypher = "MATCH (c:Character {projectId: $projectId, archived: false}) "
                + "WHERE c.currentStatus = 'ACTIVE' "
                + "OPTIONAL MATCH (ch:Chapter {projectId: $projectId})-[r:APPEARS_IN]->(c) "
                + "WHERE ch.chapterNumber >= $minChapter "
                + "WITH c, count(r) AS recentAppearances "
                + "WHERE recentAppearances = 0 "
                + "RETURN c.name AS name "
                + "LIMIT 5";

        List<Record> missingRecords = novelGraphService.query(missingCypher,
                Map.of("projectId", projectId, "minChapter", currentChapterNumber - 15));
        for (Record r : missingRecords) {
            reminders.add("角色失踪超过15章: " + r.get("name").asString());
        }

        log.info("写后校验 projectId={}, chapterNumber={}, reminders={}",
                projectId, currentChapterNumber, reminders.size());
        return reminders;
    }
}
