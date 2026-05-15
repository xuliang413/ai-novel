package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

/**
 * 上下文预览条目。
 *
 * 前端看到的一张小卡片就是它。角色、地点、线索都共用这套结构。
 */
@Data
public class ContextPreviewItemModel {

    /**
     * 业务实体 ID。
     */
    private Long id;

    /**
     * 展示名称。
     */
    private String name;

    /**
     * 业务类型或定位。
     */
    private String type;

    /**
     * 摘要。
     *
     * 会被截短到适合预览的长度，完整信息仍在资产表里。
     */
    private String summary;

    /**
     * 来源。
     *
     * 当前通常是 ChapterIntent，表示这张卡片来自本章写作意图。
     */
    private String source;

    /**
     * 是否本章强相关。
     */
    private Boolean required;

    /**
     * 展示优先级。
     */
    private Integer priority;
}
