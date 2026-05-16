package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图谱变更日志 实体类 —— 记录每次GraphPatch的完整执行结果
 * 状态: APPLIED已执行/UNDONE已撤销/FAILED失败
 * 撤销功能的基础: /undo读取最近APPLIED记录→执行inversePatch→标记UNDONE
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_graph_change_log")
public class GraphChangeLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 关联会话ID
     */
    private Long sessionId;

    /**
     * 关联章节ID
     */
    private Long chapterId;

    /**
     * 章节号
     */
    private Integer chapterNumber;

    /**
     * 操作批次ID, 幂等去重: UNIQUE索引保证同批次只执行一次
     */
    private String operationBatchId;

    /**
     * 执行的GraphPatch(JSON), 含所有操作及其before/after值
     */
    private String patchJson;

    /**
     * 逆向操作(JSON), 撤销时执行此JSON还原图谱
     */
    private String inversePatchJson;

    /**
     * 执行状态: APPLIED成功/UNDONE已撤销/FAILED失败
     */
    private String status;

    /**
     * 失败原因
     */
    private String errorMessage;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
