package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小说马甲 实体类
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_alias")
public class NovelAliasEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 马甲名称, 如"夜天子"
     */
    private String name;

    /**
     * 马甲类型: ONLINE_IDENTITY网络身份/DISGUISE乔装身份/ALTER_EGO第二人格/OTHER其他
     */
    private String type;

    /**
     * 使用场景描述
     */
    private String aliasContext;

    /**
     * 马甲描述
     */
    private String summary;

    /**
     * 是否已被识破: 0未暴露 1已暴露, 仅通过写作流程修改(暴露身份是剧情关键节点)
     */
    private Boolean revealed;

    /**
     * 被谁识破, 逗号分隔角色名列表
     */
    private String revealedTo;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
