package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 线索状态枚举。
 *
 * 线索状态会直接影响写作检索：ACTIVE 的线索更容易被放进 Prompt。
 */
@AllArgsConstructor
@Getter
public enum NovelClueStatusEnum implements BaseEnum {

    /**
     * 伏笔已存在，但故事里还没正式推进。
     */
    DORMANT("DORMANT", "未激活"),

    /**
     * 正在推进，读者已经开始获得信息。
     */
    ACTIVE("ACTIVE", "推进中"),

    /**
     * 已揭示或已解决。
     */
    RESOLVED("RESOLVED", "已解决");

    /**
     * 持久化和接口传输使用的值。
     */
    private final String value;

    /**
     * 给人看的中文说明。
     */
    private final String desc;
}
