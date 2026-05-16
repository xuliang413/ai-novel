package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CHAPTER关系类型枚举(KNOWS关系的子类型)
 * 枚举严格校验, 不在白名单内的类型拒绝写入
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

    private final String value;

    private final String desc;

    /**
     * 所有合法KNOWS子类型值集合, 用于枚举严格校验
     */
    public static final Set<String> ALLOWED_VALUES = Arrays.stream(values())
            .map(e -> e.value)
            .collect(Collectors.toSet());
}
