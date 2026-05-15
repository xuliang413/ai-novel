package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 小说知识图谱节点类型枚举。
 *
 * M0 先覆盖项目、角色、地点、线索、章节五类节点；后续新增 Volume、Event、Item 等节点时统一在这里扩展，
 * 避免业务代码里散落 Neo4j 标签字符串。
 */
@AllArgsConstructor
@Getter
public enum NovelGraphNodeEnum implements BaseEnum {

    /**
     * 项目节点，对应一本小说或一个创作项目。
     */
    PROJECT("Project", "项目节点"),

    /**
     * 角色节点。
     */
    CHARACTER("Character", "角色节点"),

    /**
     * 地点节点。
     */
    LOCATION("Location", "地点节点"),

    /**
     * 线索节点，也可承载伏笔。
     */
    CLUE("Clue", "线索节点"),

    /**
     * 章节节点，只保存摘要和状态，不保存正文。
     */
    CHAPTER("Chapter", "章节节点");

    private final String value;

    private final String desc;

    /**
     * 返回 Neo4j 标签名称。
     */
    public String label() {
        return value;
    }
}
