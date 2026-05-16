package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

import java.util.Map;

/**
 * GraphPatch 单个白名单业务操作。
 *
 * 可以把它理解成“图谱变更单上的一行”。前端展示给作者确认的是这些字段，
 * 后端真正写 Neo4j 时也只认这些字段，不认 LLM 直接给的 Cypher。
 *
 * 这样做的目的很朴素：AI 可以提建议，但不能直接动数据库；用户确认后，
 * 后端再把这些业务动作翻译成受控 Cypher。
 */
@Data
public class NovelGraphPatchOperationModel {

    /**
     * 单个操作 ID。
     *
     * 用户在前端勾选/取消某条变更时，用它告诉后端“我要执行哪几条”。
     * 注意：撤销 inversePatch 也沿用同一个 operationId，方便前后对照。
     */
    private String operationId;

    /**
     * 业务操作类型。
     *
     * 例子：
     * - MARK_APPEARANCE：记录某个角色/地点/物品在本章出现
     * - MOVE_CHARACTER：移动角色当前位置
     * - ADVANCE_CLUE：推进线索
     * - CREATE_ITEM：创建物品节点
     *
     * 这里不是底层 CRUD，也不是 Cypher。它必须落在 NovelGraphService 的白名单里。
     */
    private String operationType;

    /**
     * 目标实体类型。
     *
     * 例子：CHARACTER、LOCATION、CLUE、ITEM、EVENT、CHEAT、ALIAS、VOLUME、RULE。
     * 后端用它决定 Neo4j 标签；填错会在执行前被 BLOCKED。
     */
    private String targetType;

    /**
     * 目标实体在 MySQL 里的主键。
     *
     * 已存在实体通常会有 targetId；AI 新抽出来但还没落 MySQL 的实体可能为空。
     * Neo4j 写入时主要靠 projectId + targetName 隔离，targetId 更多用于审阅展示和追溯。
     */
    private Long targetId;

    /**
     * 目标实体名称。
     *
     * 这是审阅时最常看的字段，比如“李四”“国师府暗室”“灭门案真相”。
     * Neo4j 大部分 MERGE 也靠它和 projectId 定位节点。
     */
    private String targetName;

    /**
     * 来源实体类型。
     *
     * 目前主要给复杂关系预留，例如“事件触发线索”“金手指绑定物品”这类关系。
     * 简单操作可以为空。
     */
    private String sourceType;

    /**
     * 来源实体名称。
     *
     * 和 sourceType 配套使用。多数关系操作现在用 targetName + toName 表达，
     * 这里保留是为了以后做更自然的 from/to 展示。
     */
    private String sourceName;

    /**
     * 关系变更前的名称。
     *
     * 例如角色移动时，fromName 可以是旧地点“青云城”。
     * 有些操作没有旧值，允许为空。
     */
    private String fromName;

    /**
     * 关系变更后的名称。
     *
     * 例如：
     * - MOVE_CHARACTER：toName 是新地点
     * - TRANSFER_ITEM：toName 是新的持有人
     * - ASSIGN_CHEAT_TO_CHARACTER：toName 是拥有该金手指的角色
     */
    private String toName;

    /**
     * 关系或节点类型补充。
     *
     * 对 UPDATE_CHARACTER_RELATION 来说，它可取 KNOWS / LOVES / HATES / IS_FAMILY_OF；
     * 对 CREATE/UPDATE 节点来说，它也可作为 type 字段写入图谱。
     */
    private String relationType;

    /**
     * 变更前状态。
     *
     * 常用于冲突检查和撤销，比如线索从 DORMANT 变 ACTIVE。
     */
    private String beforeStatus;

    /**
     * 变更后状态。
     *
     * 用户确认后，后端会把这个状态写进对应节点。
     */
    private String afterStatus;

    /**
     * 变更前摘要。
     *
     * 这是 inversePatch 能恢复旧内容的关键字段。没有它，撤销就只能猜。
     */
    private String beforeSummary;

    /**
     * 变更后摘要。
     *
     * 例如线索推进后新的进展说明、章节发布后的摘要。
     */
    private String afterSummary;

    /**
     * 变更前通用值。
     *
     * 用在叙事规则、关系属性这类不是“状态/摘要”的字段上。
     */
    private String beforeValue;

    /**
     * 变更后通用值。
     *
     * 例如 ATTACH_RULE 时可以放规则内容，关系操作也可以拿它存补充说明。
     */
    private String afterValue;

    /**
     * 扩展属性。
     *
     * 现在的白名单执行器只使用少量固定字段；这个 Map 给后续更细的属性留余地。
     * 使用时要小心：不要让它绕过白名单直接写任意 Cypher。
     */
    private Map<String, Object> properties;

    /**
     * 置信度。
     *
     * 常见取值 HIGH / MEDIUM / LOW。LOW 一般默认不勾选，
     * 让作者自己判断"AI 这条是不是想多了"。
     */
    private String confidence;

    /**
     * 风险等级 —— 从 NovelGraphPatchOperationTypeEnum.riskLevel 赋值。
     *
     * LOW=默认勾选，MEDIUM=默认勾选但需确认，HIGH=默认不勾选，需用户手动确认。
     * 前端根据此字段决定每条操作的默认勾选状态和展示颜色。
     */
    private String riskLevel;

    /**
     * 校验状态。
     *
     * - READY：可以执行
     * - LOW_CONFIDENCE：可执行但默认不选
     * - CONFLICT：和当前图谱冲突，需要人处理
     * - BLOCKED：字段缺失或不合法，不能执行
     */
    private String validationStatus;

    /**
     * 前端默认是否勾选。
     *
     * 这只是默认建议，不是安全边界；后端执行前仍会过滤 BLOCKED / CONFLICT。
     */
    private Boolean selected;

    /**
     * 证据。
     *
     * 尽量放正文原句或用户命令，让审阅的人知道“为什么会产生这条变更”。
     */
    private String evidence;

    /**
     * 后端解释。
     *
     * evidence 讲“从哪看出来”，reason 讲“为什么要这样改图谱”。
     */
    private String reason;
}
