package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 线索生命周期状态枚举
 * DORMANT休眠(不进写前Prompt, 写后校验回溯提醒),
 * ACTIVE活跃(每章检索进展+推进者, 占Token),
 * RESOLVED已收束(不进Prompt不校验, 保留在图谱中用于历史追溯)
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelClueStatusEnum implements BaseEnum {

    /**
     * 休眠, 不进写前Prompt, 写后校验回溯提醒
     */
    DORMANT("DORMANT", "休眠"),

    /**
     * 活跃, 每章检索进展+推进者
     */
    ACTIVE("ACTIVE", "活跃"),

    /**
     * 已收束, 不进Prompt不校验, 保留历史
     */
    RESOLVED("RESOLVED", "已收束");

    /**
     * 存入数据库和接口传输使用的稳定线索状态编码。
     */
    private final String value;

    /**
     * 给人看的线索状态说明，用于 Swagger 枚举说明和前端展示。
     */
    private final String desc;
}
