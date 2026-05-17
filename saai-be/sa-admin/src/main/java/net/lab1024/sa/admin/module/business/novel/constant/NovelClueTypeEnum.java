package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 线索类型枚举
 * MAIN主线全篇核心, SUB支线辅助推进, HIDDEN暗线读者暂时不知道但作者和系统知道
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelClueTypeEnum implements BaseEnum {

    /**
     * 主线, 全篇核心故事线
     */
    MAIN("MAIN", "主线"),

    /**
     * 支线, 辅助推进的故事线
     */
    SUB("SUB", "支线"),

    /**
     * 暗线, 读者暂时不知道但作者和系统知道的线
     */
    HIDDEN("HIDDEN", "暗线");

    /**
     * 存入数据库和接口传输使用的稳定线索类型编码。
     */
    private final String value;

    /**
     * 给人看的线索类型说明，用于 Swagger 枚举说明和前端展示。
     */
    private final String desc;
}
