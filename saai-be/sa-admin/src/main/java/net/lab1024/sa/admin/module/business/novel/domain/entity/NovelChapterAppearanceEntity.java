package net.lab1024.sa.admin.module.business.novel.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节出场记录 实体类 —— 每章写了什么实体都记在这里
 * 支持6种实体类型出场: CHARACTER/LOCATION/ITEM/EVENT/CHEAT/ALIAS
 * 写作检索时通过此表查角色的最近出场章节
 *
 * @Author AI-Novel
 */
@Data
@TableName("t_novel_chapter_appearance")
public class NovelChapterAppearanceEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    /**
     * 章节ID
     */
    private Long chapterId;

    /**
     * 章节号, 冗余字段便于查询(如"查某角色最近出场的章节号")
     */
    private Integer chapterNumber;

    /**
     * 实体类型: CHARACTER角色/LOCATION地点/ITEM物品/EVENT事件/CHEAT金手指/ALIAS马甲
     */
    private String entityType;

    /**
     * 实体ID
     */
    private Long entityId;

    /**
     * 实体名称, 冗余存储便于展示
     */
    private String entityName;

    private Boolean deletedFlag;

    private Long createUserId;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;
}
