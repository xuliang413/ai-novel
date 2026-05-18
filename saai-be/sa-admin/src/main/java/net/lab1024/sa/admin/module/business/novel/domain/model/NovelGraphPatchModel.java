package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * GraphPatch 模型 —— 一次写作流程产生的全部图谱变更
 * <p>
 * 由AI从章节正文中抽取，用户审阅确认后由白名单执行器写入Neo4j。
 * patchId绑定写作会话，operations按执行顺序排列(先CREATE后UPDATE后RELATION)。
 *
 * @Author AI-Novel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelGraphPatchModel {

    /**
     * Patch批次ID, 绑定一次写作会话, 用于幂等和撤销
     */
    private String patchId;

    /**
     * 项目ID, 所有图谱操作都带projectId隔离
     */
    private Long projectId;

    /**
     * 触发生成的章节号
     */
    private Integer chapterNumber;

    /**
     * 操作列表, 按执行顺序: 先CREATE_NODE → UPDATE_PROPS → MERGE_REL → DELETE_REL
     */
    @Builder.Default
    private List<NovelGraphPatchOperationModel> operations = new ArrayList<>();

    /**
     * 按操作类型分组，返回 CREATE / UPDATE / RELATION / DELETE 四组
     */
    public OperationGroups groupByType() {
        OperationGroups groups = new OperationGroups();
        for (NovelGraphPatchOperationModel op : operations) {
            String code = op.getType().getCode();
            if (code.startsWith("CREATE_")) {
                groups.creates.add(op);
            } else if (code.startsWith("CHANGE_") || code.equals("ADVANCE_CLUE")) {
                groups.updates.add(op);
            } else if (code.startsWith("ADD_") || code.startsWith("UPDATE_") || code.equals("REVEAL_ALIAS") || code.equals("CONSUME_ITEM")) {
                groups.relations.add(op);
            } else if (code.startsWith("REMOVE_")) {
                groups.deletes.add(op);
            } else {
                // 出场类、移动类归入relations
                if (code.endsWith("_APPEARS") || code.equals("MOVE_CHARACTER")) {
                    groups.relations.add(op);
                } else {
                    groups.relations.add(op);
                }
            }
        }
        return groups;
    }

    /**
     * 操作分组, 按执行顺序排列
     */
    @Data
    public static class OperationGroups {
        /** 新增实体: CREATE_CHARACTER, CREATE_LOCATION, CREATE_ITEM, CREATE_EVENT */
        private List<NovelGraphPatchOperationModel> creates = new ArrayList<>();
        /** 更新属性: CHANGE_EMOTION, CHANGE_GOAL, ADVANCE_CLUE 等 */
        private List<NovelGraphPatchOperationModel> updates = new ArrayList<>();
        /** 新增关系: ADD_KNOWS, CHARACTER_APPEARS, MOVE_CHARACTER 等 */
        private List<NovelGraphPatchOperationModel> relations = new ArrayList<>();
        /** 删除关系: REMOVE_KNOWS, REMOVE_POSSESSES 等 */
        private List<NovelGraphPatchOperationModel> deletes = new ArrayList<>();
    }
}
