package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 线索情绪基调枚举
 * 约束AI写相关段落时的语气, 让线索的叙事风格保持统一
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelClueToneEnum implements BaseEnum {

    /**
     * 悲剧
     */
    TRAGIC("TRAGIC", "悲剧"),

    /**
     * 紧张
     */
    TENSE("TENSE", "紧张"),

    /**
     * 温情
     */
    ROMANTIC("ROMANTIC", "温情"),

    /**
     * 热血
     */
    HEROIC("HEROIC", "热血"),

    /**
     * 神秘
     */
    MYSTERIOUS("MYSTERIOUS", "神秘"),

    /**
     * 黑暗
     */
    DARK("DARK", "黑暗");

    /**
     * 存入数据库和接口传输使用的稳定情绪基调编码。
     */
    private final String value;

    /**
     * 给人看的情绪基调说明，用于 Swagger 枚举说明和前端展示。
     */
    private final String desc;
}
