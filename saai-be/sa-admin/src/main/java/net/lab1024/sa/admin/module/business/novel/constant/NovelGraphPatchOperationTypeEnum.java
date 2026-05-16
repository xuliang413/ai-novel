package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * GraphPatch 操作类型枚举
 *
 * 为什么之前用字符串现在要换成枚举：
 * 之前 operationType 是 String（比如 "MOVE_CHARACTER"），前端要用 if-else 判断风险、
 * 默认勾不勾选、展示什么颜色。每次加了新操作类型，前端和后端都要改代码。
 * 现在把所有操作类型集中在枚举里，并给每个操作标好风险等级，前端字典接口
 * 一调就知道每个操作的风险和推荐策略。
 *
 * 风险等级定义：
 * - LOW：纯增量数据，不影响已有数据正确性。比如新增一个角色、记录出场。
 *        这类操作默认勾选，用户可以取消勾选跳过，但一般不会造成数据打架。
 * - MEDIUM：会修改已有数据，但修改范围小且可逆。比如推进线索进度、修改角色状态。
 *          这类操作也默认勾选，但前端应该比 LOW 多一步确认。
 * - HIGH：可能改变已有结构或引发前后矛盾。比如修改已发布章节的人物关系、
 *         暴露马甲。这类操作默认不勾选，用户必须手动确认才执行。
 */
@AllArgsConstructor
@Getter
public enum NovelGraphPatchOperationTypeEnum implements BaseEnum {

    // ==================== 章节操作 ====================
    /**
     * 更新章节摘要 —— 将 AI 抽取的摘要写入 Neo4j 的 Chapter 节点。
     * 风险 LOW：修改已有字段，但仅影响展示，不影响其他关系。
     */
    UPDATE_CHAPTER_SUMMARY("UPDATE_CHAPTER_SUMMARY", "更新章节摘要", RiskLevel.LOW),

    /**
     * 恢复章节摘要 —— undo 操作，恢复到变更前的摘要。
     * 风险 LOW：这是撤销路径，不会主动产生新数据。
     */
    RESTORE_CHAPTER_SUMMARY("RESTORE_CHAPTER_SUMMARY", "恢复章节摘要", RiskLevel.LOW),

    // ==================== 出场类操作（只新增关系，不改已有数据） ====================
    MARK_APPEARANCE("MARK_APPEARANCE", "记录角色出场", RiskLevel.LOW),
    MARK_LOCATION_APPEARANCE("MARK_LOCATION_APPEARANCE", "记录地点出场", RiskLevel.LOW),
    MARK_ITEM_APPEARANCE("MARK_ITEM_APPEARANCE", "记录物品出场", RiskLevel.LOW),
    MARK_EVENT_OCCURRED("MARK_EVENT_OCCURRED", "记录事件发生", RiskLevel.LOW),
    MARK_ALIAS_APPEARANCE("MARK_ALIAS_APPEARANCE", "记录马甲出场", RiskLevel.LOW),
    UNMARK_APPEARANCE("UNMARK_APPEARANCE", "取消出场记录", RiskLevel.MEDIUM),

    // ==================== 创建类操作（纯增量） ====================
    CREATE_CHARACTER("CREATE_CHARACTER", "创建角色节点", RiskLevel.LOW),
    CREATE_LOCATION("CREATE_LOCATION", "创建地点节点", RiskLevel.LOW),
    CREATE_ITEM("CREATE_ITEM", "创建物品节点", RiskLevel.LOW),
    CREATE_EVENT("CREATE_EVENT", "创建事件节点", RiskLevel.LOW),
    CREATE_CLUE("CREATE_CLUE", "创建线索节点", RiskLevel.LOW),
    CREATE_CHEAT("CREATE_CHEAT", "创建金手指节点", RiskLevel.LOW),
    CREATE_ALIAS("CREATE_ALIAS", "创建马甲节点", RiskLevel.LOW),
    CREATE_VOLUME("CREATE_VOLUME", "创建卷节点", RiskLevel.LOW),

    // ==================== 归档操作 ====================
    ARCHIVE_NODE("ARCHIVE_NODE", "归档节点", RiskLevel.HIGH),

    // ==================== 更新类操作（修改已有数据） ====================
    UPDATE_CHARACTER_STATE("UPDATE_CHARACTER_STATE", "修改角色状态", RiskLevel.MEDIUM),
    MARK_CHARACTER_STATUS("MARK_CHARACTER_STATUS", "标记角色存活/离场", RiskLevel.MEDIUM),
    UPDATE_LOCATION("UPDATE_LOCATION", "修改地点信息", RiskLevel.MEDIUM),
    UPDATE_ITEM_STATUS("UPDATE_ITEM_STATUS", "修改物品状态", RiskLevel.MEDIUM),
    UPDATE_EVENT("UPDATE_EVENT", "修改事件信息", RiskLevel.MEDIUM),
    UPDATE_CHEAT("UPDATE_CHEAT", "修改金手指信息", RiskLevel.MEDIUM),
    UPDATE_ALIAS("UPDATE_ALIAS", "修改马甲信息", RiskLevel.MEDIUM),
    UPDATE_VOLUME("UPDATE_VOLUME", "修改卷信息", RiskLevel.LOW),
    UPDATE_CLUE("UPDATE_CLUE", "修改线索信息", RiskLevel.MEDIUM),
    ATTACH_RULE("ATTACH_RULE", "附加叙事规则", RiskLevel.LOW),

    // ==================== 线索相关操作 ====================
    ADVANCE_CLUE("ADVANCE_CLUE", "推进线索进度", RiskLevel.MEDIUM),
    RESTORE_CLUE("RESTORE_CLUE", "回退线索进度", RiskLevel.MEDIUM),
    RESOLVE_CLUE("RESOLVE_CLUE", "标记线索已揭示", RiskLevel.MEDIUM),
    LINK_CLUE_CHARACTER("LINK_CLUE_CHARACTER", "关联线索与角色", RiskLevel.LOW),
    MARK_CHARACTER_KNOWS_CLUE("MARK_CHARACTER_KNOWS_CLUE", "标记角色知晓线索", RiskLevel.LOW),
    INTERSECT_CLUES("INTERSECT_CLUES", "标记两条线索交叉", RiskLevel.MEDIUM),
    TRIGGER_CLUE("TRIGGER_CLUE", "标记事件触发线索", RiskLevel.MEDIUM),
    ASSIGN_CLUE_TO_VOLUME("ASSIGN_CLUE_TO_VOLUME", "将线索分配到卷", RiskLevel.LOW),

    // ==================== 关系类操作 ====================
    /**
     * 修改角色之间的关系 —— 比如"从陌生变朋友""从朋友变恋人"。
     * 风险 HIGH：修改关系可能和之前章节矛盾（比如前面写过两人是师徒，现在又改成父子）。
     */
    UPDATE_CHARACTER_RELATION("UPDATE_CHARACTER_RELATION", "修改角色关系", RiskLevel.HIGH),

    LINK_EVENT_PARTICIPANT("LINK_EVENT_PARTICIPANT", "关联事件参与者", RiskLevel.LOW),

    // ==================== 状态关系操作 ====================
    /**
     * 移动角色位置 —— 从地点 A 到地点 B。
     * 风险 MEDIUM：如果移动逻辑写得不对，角色可能"瞬移"。
     * undo 时可以恢复到旧位置。
     */
    MOVE_CHARACTER("MOVE_CHARACTER", "移动角色位置", RiskLevel.MEDIUM),

    /**
     * 转移物品 —— 从持有人 A 到持有人 B，或置为无主。
     * 风险 MEDIUM：移交后需要确认新持有人是否合理。
     */
    TRANSFER_ITEM("TRANSFER_ITEM", "转移物品持有人", RiskLevel.MEDIUM),

    // ==================== 金手指/马甲操作 ====================
    ASSIGN_CHEAT_TO_CHARACTER("ASSIGN_CHEAT_TO_CHARACTER", "分配金手指给角色", RiskLevel.MEDIUM),
    BIND_CHEAT_TO_ITEM("BIND_CHEAT_TO_ITEM", "绑定金手指到物品", RiskLevel.LOW),
    ASSIGN_ALIAS_TO_CHARACTER("ASSIGN_ALIAS_TO_CHARACTER", "分配马甲给角色", RiskLevel.MEDIUM),
    /**
     * 暴露马甲 —— 某个角色识破了另一个角色的马甲。
     * 风险 HIGH：暴露马甲是不可逆的剧情节点，一旦确认，后续章节都要基于"已暴露"来写。
     */
    REVEAL_ALIAS_TO_CHARACTER("REVEAL_ALIAS_TO_CHARACTER", "暴露马甲", RiskLevel.HIGH);

    private final String value;
    private final String desc;
    private final RiskLevel riskLevel;

    /**
     * 风险等级内部枚举
     */
    @Getter
    @AllArgsConstructor
    public enum RiskLevel {
        LOW("LOW", "低风险"),
        MEDIUM("MEDIUM", "中风险"),
        HIGH("HIGH", "高风险");

        private final String value;
        private final String desc;
    }
}
