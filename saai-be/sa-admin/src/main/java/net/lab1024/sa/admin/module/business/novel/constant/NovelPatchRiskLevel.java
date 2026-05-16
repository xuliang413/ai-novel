package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * GraphPatch操作风险等级
 * LOW低风险, MEDIUM中风险, HIGH高风险需要作者手动确认
 *
 * @Author AI-Novel
 */
@Getter
@AllArgsConstructor
public enum NovelPatchRiskLevel {

    /** 低风险: 纯事实操作, 正文写了就是写了, 默认勾选 */
    LOW("LOW", "低风险"),

    /** 中风险: 需要作者注意但大概率正确 */
    MEDIUM("MEDIUM", "中风险"),

    /** 高风险: 最易被AI误判, 默认不勾选, 必须作者手动确认 */
    HIGH("HIGH", "高风险");

    private final String value;

    private final String desc;
}
