package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图谱变更日志实体。
 *
 * 每次 GraphPatch 真正写入 Neo4j 后，都会在这里留一份账。
 * undo 能不能工作，主要就看 patchJson / inversePatchJson 是否完整。
 */
@Data
@TableName("t_graph_change_log")
public class GraphChangeLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long changeLogId;

    /**
     * 所属小说项目 ID。
     */
    private Long projectId;

    /**
     * 关联章节 ID；非章节触发的图谱操作可以为空。
     */
    private Long chapterId;

    /**
     * 关联章节序号，方便后台列表按章节排查。
     */
    private Integer chapterNo;

    /**
     * 触发本次变更的写作会话 ID。
     */
    private Long sessionId;

    /**
     * 正向 GraphPatch ID。
     */
    private String patchId;

    /**
     * 操作批次 ID。
     *
     * 一次确认里的多条操作共用它，撤销时也按这个批次理解。
     */
    private String operationBatchId;

    /**
     * 实际执行的正向 GraphPatch JSON。
     */
    private String patchJson;

    /**
     * 实际执行项对应的反向 GraphPatch JSON。
     *
     * 注意：用户没勾选的操作不会出现在这里，避免 undo 多撤。
     */
    private String inversePatchJson;

    /**
     * 变更状态：APPLIED / UNDONE / FAILED。
     */
    private String status;

    /**
     * 执行失败时的错误信息。
     */
    private String errorMessage;

    /**
     * 执行确认的用户 ID。
     */
    private Long createUserId;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
