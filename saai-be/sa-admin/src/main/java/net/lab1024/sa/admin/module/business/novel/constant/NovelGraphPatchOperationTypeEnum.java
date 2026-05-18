package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * GraphPatch业务操作类型枚举 —— 38种操作映射到5种Cypher模板
 *
 * 每个操作标注默认风险等级:
 * - LOW(READY): 纯事实——正文写了就是写了, 默认勾选
 * - HIGH: 最易被AI误判, 默认不勾选, 必须作者手动确认
 * - BLOCKED: 不可勾选(操作冲突/实体不存在等)
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelGraphPatchOperationTypeEnum {

    // ==============================
    // 新增实体(4) - LOW: AI识别到的新实体, 正文写了就是写了
    // ==============================
    /** 新增角色: MERGE_NODE(Character), 出现在正文中但Neo4j里不存在的新角色 */
    CREATE_CHARACTER("CREATE_CHARACTER", "新增角色", NovelPatchRiskLevel.LOW),
    /** 新增地点: MERGE_NODE(Location) */
    CREATE_LOCATION("CREATE_LOCATION", "新增地点", NovelPatchRiskLevel.LOW),
    /** 新增物品: MERGE_NODE(Item) */
    CREATE_ITEM("CREATE_ITEM", "新增物品", NovelPatchRiskLevel.LOW),
    /** 新增事件: MERGE_NODE(Event) */
    CREATE_EVENT("CREATE_EVENT", "新增事件", NovelPatchRiskLevel.LOW),

    // ==============================
    // 出场记录(6) - LOW: 纯事实操作
    // ==============================
    /** 角色出场: MERGE_REL(APPEARS_IN) */
    CHARACTER_APPEARS("CHARACTER_APPEARS", "角色出场", NovelPatchRiskLevel.LOW),
    /** 地点出场: MERGE_REL(APPEARS_IN) */
    LOCATION_APPEARS("LOCATION_APPEARS", "地点出场", NovelPatchRiskLevel.LOW),
    /** 物品出场: MERGE_REL(APPEARS_IN) */
    ITEM_APPEARS("ITEM_APPEARS", "物品出场", NovelPatchRiskLevel.LOW),
    /** 事件出场: MERGE_REL(APPEARS_IN) */
    EVENT_APPEARS("EVENT_APPEARS", "事件出场", NovelPatchRiskLevel.LOW),
    /** 金手指出场: MERGE_REL(APPEARS_IN) */
    CHEAT_APPEARS("CHEAT_APPEARS", "金手指出场", NovelPatchRiskLevel.LOW),
    /** 马甲出场: MERGE_REL(APPEARS_IN) */
    ALIAS_APPEARS("ALIAS_APPEARS", "马甲出场", NovelPatchRiskLevel.LOW),

    // ==============================
    // 角色状态变更(5) - LOW: AI抽取大概率对, 错了作者审阅时一眼能看穿
    // ==============================
    /** 情绪变化: UPDATE_NODE_PROPS(Character.emotion+intensity) */
    CHANGE_EMOTION("CHANGE_EMOTION", "情绪变化", NovelPatchRiskLevel.LOW),
    /** 目标变化: UPDATE_NODE_PROPS(Character.goal) */
    CHANGE_GOAL("CHANGE_GOAL", "目标变化", NovelPatchRiskLevel.LOW),
    /** 目标进度: UPDATE_NODE_PROPS(Character.goalProgress) */
    CHANGE_GOAL_PROGRESS("CHANGE_GOAL_PROGRESS", "目标进度变化", NovelPatchRiskLevel.LOW),
    /** 战力变化: UPDATE_NODE_PROPS(Character.powerLevel) */
    CHANGE_POWER_LEVEL("CHANGE_POWER_LEVEL", "战力变化", NovelPatchRiskLevel.LOW),
    /** 存活状态变化: UPDATE_NODE_PROPS(Character.currentStatus) */
    CHANGE_CHARACTER_STATUS("CHANGE_CHARACTER_STATUS", "存活状态变化", NovelPatchRiskLevel.LOW),

    // ==============================
    // 位置移动(1) - LOW: DELETE_REL旧CURRENTLY_AT + MERGE_REL新CURRENTLY_AT
    // ==============================
    /** 角色移动 */
    MOVE_CHARACTER("MOVE_CHARACTER", "角色移动", NovelPatchRiskLevel.LOW),

    // ==============================
    // 线索推进(1) - LOW: UPDATE_NODE_PROPS(Clue) + MERGE_REL(ADVANCES)
    // ==============================
    /** 线索推进: 揭露程度/阶段/摘要更新 */
    ADVANCE_CLUE("ADVANCE_CLUE", "线索推进", NovelPatchRiskLevel.LOW),

    // ==============================
    // 角色关系KNOWS(3) - HIGH: 最易被AI误判, 对峙≠恨, 对话≠朋友
    // ==============================
    /** 新增认识关系: MERGE_REL(KNOWS) */
    ADD_KNOWS("ADD_KNOWS", "新增认识关系", NovelPatchRiskLevel.HIGH),
    /** 更新认识关系: MERGE_REL(KNOWS)更新relationType */
    UPDATE_KNOWS("UPDATE_KNOWS", "更新认识关系", NovelPatchRiskLevel.HIGH),
    /** 移除认识关系: DELETE_REL(KNOWS) */
    REMOVE_KNOWS("REMOVE_KNOWS", "移除认识关系", NovelPatchRiskLevel.HIGH),

    // ==============================
    // 角色关系LOVES(3) - HIGH
    // ==============================
    /** 新增爱慕: MERGE_REL(LOVES) */
    ADD_LOVES("ADD_LOVES", "新增爱慕关系", NovelPatchRiskLevel.HIGH),
    /** 更新爱慕: MERGE_REL(LOVES)更新loveStatus */
    UPDATE_LOVES("UPDATE_LOVES", "更新爱慕关系", NovelPatchRiskLevel.HIGH),
    /** 移除爱慕: DELETE_REL(LOVES) */
    REMOVE_LOVES("REMOVE_LOVES", "移除爱慕关系", NovelPatchRiskLevel.HIGH),

    // ==============================
    // 角色关系HATES(3) - HIGH
    // ==============================
    /** 新增仇恨: MERGE_REL(HATES) */
    ADD_HATES("ADD_HATES", "新增仇恨关系", NovelPatchRiskLevel.HIGH),
    /** 更新仇恨: MERGE_REL(HATES)更新hateIntensity */
    UPDATE_HATES("UPDATE_HATES", "更新仇恨关系", NovelPatchRiskLevel.HIGH),
    /** 移除仇恨: DELETE_REL(HATES) */
    REMOVE_HATES("REMOVE_HATES", "移除仇恨关系", NovelPatchRiskLevel.HIGH),

    // ==============================
    // 角色关系IS_FAMILY_OF(3) - HIGH
    // ==============================
    /** 新增亲缘: MERGE_REL(IS_FAMILY_OF) */
    ADD_IS_FAMILY_OF("ADD_IS_FAMILY_OF", "新增亲缘关系", NovelPatchRiskLevel.HIGH),
    /** 更新亲缘: MERGE_REL(IS_FAMILY_OF)更新familyType */
    UPDATE_IS_FAMILY_OF("UPDATE_IS_FAMILY_OF", "更新亲缘关系", NovelPatchRiskLevel.HIGH),
    /** 移除亲缘: DELETE_REL(IS_FAMILY_OF) */
    REMOVE_IS_FAMILY_OF("REMOVE_IS_FAMILY_OF", "移除亲缘关系", NovelPatchRiskLevel.HIGH),

    // ==============================
    // 其他关系变更(9) - 混合风险
    // ==============================
    /** 新增持有物品: MERGE_REL(POSSESSES) - HIGH: 物品归属容易误判 */
    ADD_POSSESSES("ADD_POSSESSES", "新增持有物品", NovelPatchRiskLevel.HIGH),
    /** 移除持有物品: DELETE_REL(POSSESSES) - HIGH: 物品消耗/丢失 */
    REMOVE_POSSESSES("REMOVE_POSSESSES", "移除持有物品", NovelPatchRiskLevel.HIGH),
    /** 新增参与事件: MERGE_REL(PARTICIPATES_IN) - LOW */
    ADD_PARTICIPATES_IN("ADD_PARTICIPATES_IN", "新增参与事件", NovelPatchRiskLevel.LOW),
    /** 新增推动线索: MERGE_REL(DRIVES) - LOW */
    ADD_DRIVES("ADD_DRIVES", "新增推动线索", NovelPatchRiskLevel.LOW),
    /** 新增知情线索: MERGE_REL(KNOWS_ABOUT) - HIGH: 信息不对称容易误判 */
    ADD_KNOWS_ABOUT("ADD_KNOWS_ABOUT", "新增知情线索", NovelPatchRiskLevel.HIGH),
    /** 新增拥有金手指: MERGE_REL(HAS_CHEAT) - HIGH */
    ADD_HAS_CHEAT("ADD_HAS_CHEAT", "新增拥有金手指", NovelPatchRiskLevel.HIGH),
    /** 新增拥有马甲: MERGE_REL(HAS_ALIAS) - HIGH */
    ADD_HAS_ALIAS("ADD_HAS_ALIAS", "新增拥有马甲", NovelPatchRiskLevel.HIGH),
    /** 马甲被识破: UPDATE_NODE_PROPS(Alias.revealed) + MERGE_REL(KNOWS_ALIAS) - HIGH: 暴露身份是剧情关键节点 */
    REVEAL_ALIAS("REVEAL_ALIAS", "马甲被识破", NovelPatchRiskLevel.HIGH),
    /** 物品消耗: UPDATE_NODE_PROPS(Item.quantity/status) - HIGH: 数量扣减不可逆 */
    CONSUME_ITEM("CONSUME_ITEM", "物品消耗/损坏", NovelPatchRiskLevel.HIGH);

    /** 操作类型编码 */
    private final String code;

    /** 中文描述, 审阅界面展示 */
    private final String desc;

    /** 默认风险等级: LOW默认勾选, HIGH默认不勾选 */
    private final NovelPatchRiskLevel riskLevel;

    /**
     * 根据编码查枚举, 用于GraphPatch JSON解析和逆操作生成。
     *
     * @param code 操作类型编码
     * @return 匹配的枚举, 未匹配返回null
     */
    public static NovelGraphPatchOperationTypeEnum fromCode(String code) {
        if (code == null) return null;
        for (NovelGraphPatchOperationTypeEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
