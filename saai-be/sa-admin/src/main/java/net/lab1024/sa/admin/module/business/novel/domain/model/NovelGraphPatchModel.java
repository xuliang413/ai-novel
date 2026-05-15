package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 图谱变更单。
 *
 * 一次章节发布、一次用户自然语言操作，都会生成一张 GraphPatch。
 * 它像一张待签字的变更单：里面列出多条 operation，用户确认后才写入 Neo4j。
 */
@Data
public class NovelGraphPatchModel {

    /**
     * 变更单 ID。
     *
     * 写入 Neo4j 后会作为 createdByPatchId / updatedByPatchId，方便追查某个节点是谁改出来的。
     */
    private String patchId;

    /**
     * 操作批次 ID。
     *
     * 一次确认可能包含多条 operation，这个 ID 把它们绑成一组。
     * graph_change_log 也用它做幂等和撤销。
     */
    private String operationBatchId;

    /**
     * 项目 ID。所有图谱写入必须带它，避免不同小说串数据。
     */
    private Long projectId;

    /**
     * 关联章节 ID。非章节类操作可以为空。
     */
    private Long chapterId;

    /**
     * 关联章节序号。出场、线索推进、章节摘要这些操作会用到。
     */
    private Integer chapterNo;

    /**
     * 变更单状态。
     *
     * 常见：PENDING_CONFIRM / READY / APPLIED / UNDONE。
     */
    private String status;

    /**
     * 本次要审阅或执行的操作列表。
     */
    private List<NovelGraphPatchOperationModel> operations = new ArrayList<>();

    /**
     * 给前端和审阅人的整体提醒。
     *
     * 例如“存在低置信操作”“上下文严重裁剪过”。
     */
    private List<String> warnings = new ArrayList<>();
}
