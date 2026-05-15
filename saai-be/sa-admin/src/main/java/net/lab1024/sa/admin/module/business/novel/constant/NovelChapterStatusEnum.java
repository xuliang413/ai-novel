package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 章节状态枚举。
 */
@AllArgsConstructor
@Getter
public enum NovelChapterStatusEnum implements BaseEnum {

    DRAFT("DRAFT", "草稿"),
    PENDING_GRAPH_CONFIRM("PENDING_GRAPH_CONFIRM", "待确认图谱变更"),
    PENDING_GRAPH_UPDATE("PENDING_GRAPH_UPDATE", "待同步图谱"),
    PUBLISHED("PUBLISHED", "已发布"),
    INTERRUPTED_DRAFT("INTERRUPTED_DRAFT", "中断草稿");

    private final String value;

    private final String desc;
}
