package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectGenreEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectPlatformEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说项目创建表单。
 * <p>
 * 只接收用户可编辑的项目基础信息，创建用户、状态、归档标记由服务层统一写入。
 *
 * @Author AI-Novel
 */
@Data
public class NovelProjectAddForm {

    /**
     * 项目名称，也是列表和图谱 Project 节点里最重要的可读名称。
     */
    @Schema(description = "项目名称")
    @NotBlank(message = "项目名称不能为空")
    @Length(max = 255, message = "项目名称最多255个字符")
    private String name;

    /**
     * 小说类型，影响默认叙事规则和世界观模板注入。
     */
    @SchemaEnum(desc = "小说类型", value = NovelProjectGenreEnum.class)
    @CheckEnum(value = NovelProjectGenreEnum.class, required = true, message = "小说类型错误")
    private String genre;

    /**
     * 世界观概述，后续会作为每章写作 Prompt 的长期背景。
     */
    @Schema(description = "世界观概述")
    private String worldBuilding;

    /**
     * 主角名快捷字段，完整人设仍以角色管理里的主角记录为准。
     */
    @Schema(description = "主角名")
    @Length(max = 100, message = "主角名最多100个字符")
    private String protagonistName;

    /**
     * 文风描述，用于约束 AI 生成时的语气、节奏和句式偏好。
     */
    @Schema(description = "文风描述")
    private String styleDescription;

    /**
     * 目标平台，用于后续选择平台叙事规则和内容红线。
     */
    @SchemaEnum(desc = "目标平台", value = NovelProjectPlatformEnum.class)
    @CheckEnum(value = NovelProjectPlatformEnum.class, required = false, message = "目标平台错误")
    private String platform;

    /**
     * 目标总字数，只用于进度统计和规划参考，不作为生成硬限制。
     */
    @Schema(description = "目标总字数")
    @Min(value = 1, message = "目标总字数必须大于0")
    private Integer targetTotalWords;

    /**
     * 每章目标字数，质检阶段会拿生成结果和这个值做偏差检查。
     */
    @Schema(description = "每章目标字数")
    @Min(value = 1, message = "每章目标字数必须大于0")
    private Integer targetChapterWords;

    /**
     * 上下文 Token 软预算，检索阶段会尽量把上下文控制在这个范围内。
     */
    @Schema(description = "上下文Token目标预算")
    @Min(value = 1, message = "上下文Token目标预算必须大于0")
    private Integer tokenBudget;

    /**
     * 上下文 Token 硬上限，任何 Prompt 组装结果都不能超过这个值。
     */
    @Schema(description = "上下文Token硬上限")
    @Min(value = 1, message = "上下文Token硬上限必须大于0")
    private Integer tokenHardLimit;

    /**
     * 项目备注，只服务于人工维护，不参与 AI Prompt。
     */
    @Schema(description = "备注")
    @Length(max = 500, message = "备注最多500个字符")
    private String remark;
}
