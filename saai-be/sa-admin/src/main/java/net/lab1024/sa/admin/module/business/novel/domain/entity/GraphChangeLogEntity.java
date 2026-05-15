package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图谱变更日志实体。
 */
@Data
@TableName("t_graph_change_log")
public class GraphChangeLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long changeLogId;

    private Long projectId;

    private Long chapterId;

    private Integer chapterNo;

    private Long sessionId;

    private String patchId;

    private String operationBatchId;

    private String patchJson;

    private String inversePatchJson;

    private String status;

    private String errorMessage;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
