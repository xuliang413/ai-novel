package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 线索类型枚举。
 *
 * 用来区分线索在故事结构里的重量：主线、支线或暗线。
 */
@AllArgsConstructor
@Getter
public enum NovelClueTypeEnum implements BaseEnum {

    /**
     * 主线线索，通常和核心矛盾相关。
     */
    MAIN("MAIN", "主线"),

    /**
     * 支线线索，服务阶段性剧情。
     */
    SUB("SUB", "支线"),

    /**
     * 暗线，读者可能暂时意识不到它的重要性。
     */
    HIDDEN("HIDDEN", "暗线");

    /**
     * 持久化和接口传输使用的值。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;
}
