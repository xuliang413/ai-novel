package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 写作会话状态枚举(状态机)
 * IDLE空闲(无活跃会话), GENERATING正在生成, CONTENT_REVIEW正文审阅中,
 * PATCH_REVIEW图谱变更审阅中, PENDING_GRAPH_UPDATE图谱写入失败待重试,
 * SUCCESS终态成功, INTERRUPTED中断, FAILED失败(LLM异常, 默认重试3次后降级Mock)
 *
 * @Author AI-Novel
 */
@AllArgsConstructor
@Getter
public enum NovelGenerationStatusEnum implements BaseEnum {

    /**
     * 空闲, 无活跃会话
     */
    IDLE("IDLE", "空闲"),

    /**
     * AI正在生成正文
     */
    GENERATING("GENERATING", "正在生成"),

    /**
     * 正文已保存, 等待作者审阅
     */
    CONTENT_REVIEW("CONTENT_REVIEW", "正文审阅中"),

    /**
     * GraphPatch已抽取, 等待作者确认
     */
    PATCH_REVIEW("PATCH_REVIEW", "图谱变更审阅中"),

    /**
     * 图谱写入失败, 等待重试
     */
    PENDING_GRAPH_UPDATE("PENDING_GRAPH_UPDATE", "图谱更新待重试"),

    /**
     * 终态, 章节发布图谱更新完成
     */
    SUCCESS("SUCCESS", "成功"),

    /**
     * 中断, 用户取消或超时, P1可恢复
     */
    INTERRUPTED("INTERRUPTED", "中断"),

    /**
     * 失败, LLM异常等, 默认重试3次后降级Mock
     */
    FAILED("FAILED", "失败");

    private final String value;

    private final String desc;

    /**
     * 根据字符串值查找对应的枚举, 找不到返回null。
     * 用于从数据库或JSON反序列化状态时快速映射。
     *
     * @param value 枚举字符串值
     * @return 匹配的枚举, 未匹配返回null
     */
    public static NovelGenerationStatusEnum fromValue(String value) {
        if (value == null) return null;
        for (NovelGenerationStatusEnum e : values()) {
            if (e.value.equals(value)) return e;
        }
        return null;
    }
}
