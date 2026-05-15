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
    DESCRIPTION("description", "描述"),
    PROTAGONIST("protagonist", "主角"),
    STATUS("status", "业务状态"),
    ROLE("role", "角色定位"),
    TYPE("type", "业务类型"),
    SUB_TYPE("subType", "业务子类型"),
    NUMBER("number", "章节序号"),
    TITLE("title", "标题"),
    POV("pov", "视角人物"),
    CURRENT_GOAL("currentGoal", "当前目标"),
    CURRENT_EMOTION("currentEmotion", "当前情绪"),
    ORIGIN("origin", "来源"),
    LIMITATION("limitation", "限制"),
    EVOLUTION("evolution", "升级路径"),
    CONTEXT("context", "使用场景"),
    REVEALED("revealed", "是否已识破"),
    REVEALED_TO("revealedTo", "识破角色列表"),
    VALUE("value", "规则内容"),
    PRIORITY("priority", "优先级"),
    REVEAL_LEVEL("revealLevel", "线索揭示进度"),
    CHAPTER_OCCURRED("chapterOccurred", "事件发生章节"),
    RELATION_TYPE("relationType", "关系类型"),
    INTENSITY("intensity", "强度"),
    FAMILY_TYPE("familyType", "亲属或师门类型"),
    LEVEL("level", "知情程度"),
    ACQUIRED_IN_CHAPTER("acquiredInChapter", "获得章节"),
    CREATED_IN_CHAPTER("createdInChapter", "首现章节"),
    SINCE_CHAPTER("sinceChapter", "知晓章节"),
    INTERSECT_CHAPTER("intersectChapter", "交汇章节"),
    INTERSECT_DESCRIPTION("intersectDescription", "交汇说明"),
    ARCHIVED("archived", "系统归档标识"),
    CHAPTER_NO("chapterNo", "章节序号关系属性"),
    CREATED_BY_PATCH_ID("createdByPatchId", "创建该节点或关系的 GraphPatch ID"),
    UPDATED_BY_PATCH_ID("updatedByPatchId", "最近更新该节点或关系的 GraphPatch ID"),
    CREATED_AT("createdAt", "图谱创建时间"),
    UPDATED_AT("updatedAt", "图谱更新时间");

    /**
     * Neo4j 属性 key。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;

    /**
     * 返回 Neo4j 属性名。
     */
    public String key() {
        return value;
    }
}
