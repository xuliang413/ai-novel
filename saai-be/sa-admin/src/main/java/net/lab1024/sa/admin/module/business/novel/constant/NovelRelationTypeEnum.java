package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * KNOWS 关系子类型枚举。
 * <p>
 * 用于限定角色之间“认识/社交”关系的细分语义，避免自由文本把图谱关系写乱。
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelRelationTypeEnum implements BaseEnum {

    /**
     * 朋友
     */
    FRIEND("FRIEND", "朋友"),

    /**
     * 盟友
     */
    ALLY("ALLY", "盟友"),

    /**
     * 竞争对手
     */
    RIVAL("RIVAL", "竞争对手"),

    /**
     * 熟人, 认识但不构成朋友/盟友/敌人
     */
    ACQUAINTANCE("ACQUAINTANCE", "熟人"),

    /**
     * 下属
     */
    SUBORDINATE("SUBORDINATE", "下属"),

    /**
     * 敌人, 与其他关系中仅KNOWS的敌人类同
     */
    ENEMY("ENEMY", "敌人");

    /**
     * 枚举值，写入 MySQL 的 knows_relation_type，并同步到 Neo4j 的 relationType 属性。
     */
    private final String value;

    /**
     * 中文说明，用于管理页展示和接口文档说明。
     */
    private final String desc;

    /**
     * 所有合法 KNOWS 子类型值集合，用于服务层白名单校验。
     */
    public static final Set<String> ALLOWED_VALUES = Arrays.stream(values())
            .map(e -> e.value)
            .collect(Collectors.toSet());
}
