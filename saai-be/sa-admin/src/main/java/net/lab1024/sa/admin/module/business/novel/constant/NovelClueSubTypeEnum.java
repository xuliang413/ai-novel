package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 线索子类型枚举
 * PLOT_THREAD占Token进Prompt持续推进, FORESHADOWING伏笔在休眠期不占Token不进Prompt, 只靠写后校验提醒引爆
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelClueSubTypeEnum implements BaseEnum {

    /**
     * 线索, 持续跨章推进的故事线, 占Token进Prompt
     */
    PLOT_THREAD("PLOT_THREAD", "线索"),

    /**
     * 伏笔, 埋下信息点长期休眠, 休眠期不进Prompt, 靠写后校验提醒
     */
    FORESHADOWING("FORESHADOWING", "伏笔");

    /**
     * 存入数据库和接口传输使用的稳定线索子类型编码。
     */
    private final String value;

    /**
     * 给人看的线索子类型说明，用于 Swagger 枚举说明和前端展示。
     */
    private final String desc;
}
