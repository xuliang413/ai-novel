package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 章节生成会话状态枚举。
 */
@AllArgsConstructor
@Getter
public enum NovelGenerationStatusEnum implements BaseEnum {

    GENERATING("GENERATING", "生成中"),
    CONTENT_REVIEW("CONTENT_REVIEW", "正文审阅"),
    EXTRACTING_PATCH("EXTRACTING_PATCH", "抽取图谱变更"),
    PATCH_REVIEW("PATCH_REVIEW", "图谱变更确认"),
    APPLYING_PATCH("APPLYING_PATCH", "执行图谱变更"),
    SUCCESS("SUCCESS", "生成成功"),
    FAILED("FAILED", "生成失败"),
    INTERRUPTED("INTERRUPTED", "生成中断");

    private final String value;

    private final String desc;
}
