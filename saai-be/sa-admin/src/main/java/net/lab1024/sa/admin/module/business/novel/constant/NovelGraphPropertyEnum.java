package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 小说知识图谱通用属性枚举。
 *
 * Neo4j 的属性名不能参数化，因此集中定义在这里，业务代码只能从枚举取值。
 */
@AllArgsConstructor
@Getter
public enum NovelGraphPropertyEnum implements BaseEnum {

    PROJECT_ID("projectId", "项目 ID"),
    MYSQL_ID("mysqlId", "MySQL 来源主键"),
    NAME("name", "名称"),
    GENRE("genre", "小说类型"),
    SUMMARY("summary", "摘要"),
    PROTAGONIST("protagonist", "主角"),
    STATUS("status", "业务状态"),
    ROLE("role", "角色定位"),
    TYPE("type", "业务类型"),
    NUMBER("number", "章节序号"),
    TITLE("title", "标题"),
    ARCHIVED("archived", "系统归档标识"),
    CHAPTER_NO("chapterNo", "章节序号关系属性"),
    CREATED_BY_PATCH_ID("createdByPatchId", "创建该节点或关系的 GraphPatch ID"),
    UPDATED_BY_PATCH_ID("updatedByPatchId", "最近更新该节点或关系的 GraphPatch ID"),
    CREATED_AT("createdAt", "图谱创建时间"),
    UPDATED_AT("updatedAt", "图谱更新时间");

    private final String value;

    private final String desc;

    /**
     * 返回 Neo4j 属性名。
     */
    public String key() {
        return value;
    }
}
