package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 金手指类型枚举。
 * <p>
 * 用于金手指创建、编辑和查询时的类型校验，也用于写作检索阶段判断该能力更像能力、物品、空间还是系统。
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelCheatTypeEnum implements BaseEnum {

    /**
     * 能力型金手指，例如万倍悟性、读心术，重点是角色自身能力被增强。
     */
    ABILITY("ABILITY", "能力型"),

    /**
     * 物品绑定型金手指，例如老爷爷戒指、系统面板寄宿物，重点是能力依附在物品上。
     */
    ITEM_BOUND("ITEM_BOUND", "物品绑定型"),

    /**
     * 空间型金手指，例如随身空间、洞天福地，重点是提供独立空间或资源产出。
     */
    SPACE("SPACE", "空间型"),

    /**
     * 系统型金手指，例如任务系统、签到系统，重点是以规则面板驱动成长。
     */
    SYSTEM("SYSTEM", "系统型");

    /**
     * 枚举值，写入数据库和接口请求时使用。
     */
    private final String value;

    /**
     * 中文说明，展示给管理页和接口文档阅读者。
     */
    private final String desc;
}
