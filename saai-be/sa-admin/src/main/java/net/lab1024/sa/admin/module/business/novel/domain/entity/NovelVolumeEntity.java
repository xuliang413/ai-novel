package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说卷 实体类
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_volume")
public class NovelVolumeEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 卷序号, 从1开始, 决定排列顺序
     */
    private Integer number;

    /**
     * 卷标题, 如"第二卷：京城风云"
     */
    private String title;

    /**
     * 卷概要, 自然语言, 注入该卷所有章节的Prompt(告诉AI这一整段的方向, 不被前几章的支线带偏)
     */
    private String summary;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
