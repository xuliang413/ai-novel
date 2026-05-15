package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 小说知识图谱关系类型枚举。
 *
 * 覆盖技术方案 §4.3 定义的结构、出场、角色、状态、线索、金手指和马甲关系。
 */
@AllArgsConstructor
@Getter
public enum NovelGraphRelationEnum implements BaseEnum {

    CONTAINS("CONTAINS", "项目包含实体"),

    PREVIOUS("PREVIOUS", "上一章到下一章"),

    HAS_RULE("HAS_RULE", "项目配置叙事规则"),

    APPEARS_IN("APPEARS_IN", "实体在章节中出场"),

    KNOWS("KNOWS", "一般相识关系"),

    LOVES("LOVES", "爱慕关系"),

    HATES("HATES", "仇恨关系"),

    IS_FAMILY_OF("IS_FAMILY_OF", "亲属或师门关系"),

    CURRENTLY_AT("CURRENTLY_AT", "角色当前位置"),

    POSSESSES("POSSESSES", "角色持有物品"),

    PARTICIPATES_IN("PARTICIPATES_IN", "角色参与事件"),

    DRIVES("DRIVES", "角色推动线索"),

    ADVANCES("ADVANCES", "章节推进线索"),

    INVOLVES("INVOLVES", "线索牵连角色"),

    KNOWS_ABOUT("KNOWS_ABOUT", "角色知情线索"),

    INTERSECTS("INTERSECTS", "线索交汇"),

    BELONGS_TO("BELONGS_TO", "线索属于卷"),

    TRIGGERS("TRIGGERS", "事件触发线索"),

    HAS_CHEAT("HAS_CHEAT", "角色拥有金手指"),

    BOUND_TO("BOUND_TO", "金手指绑定物品"),

    HAS_ALIAS("HAS_ALIAS", "角色拥有马甲"),

    KNOWS_ALIAS("KNOWS_ALIAS", "角色知道马甲"),

    OWNS_ASSET("OWNS_ASSET", "项目直接拥有资产");

    /**
     * Neo4j 关系类型名。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;

    /**
     * 返回 Neo4j 关系类型名称。
     */
    public String type() {
        return value;
    }
}
