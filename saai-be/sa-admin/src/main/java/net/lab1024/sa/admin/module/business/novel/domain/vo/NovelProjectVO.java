package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectGenreEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectPlatformEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 小说项目展示 VO。
 * <p>
 * 面向前端返回项目列表和详情，不暴露任何可绕过用户隔离的查询参数。
 *
 * @Author AI-Novel
 */
@Data
public class NovelProjectVO {

    /**
     * 项目ID。
     */
    @Schema(description = "项目ID")
    private Long id;

    /**
     * 项目名称。
     */
    @Schema(description = "项目名称")
    private String name;

    /**
     * 小说类型。
     */
    @SchemaEnum(desc = "小说类型", value = NovelProjectGenreEnum.class)
    private String genre;

    /**
     * 世界观概述。
     */
    @Schema(description = "世界观概述")
    private String worldBuilding;

    /**
     * 主角名快捷字段。
     */
    @Schema(description = "主角名")
    private String protagonistName;

    /**
     * 文风描述。
     */
    @Schema(description = "文风描述")
    private String styleDescription;

    /**
     * 目标平台。
     */
    @SchemaEnum(desc = "目标平台", value = NovelProjectPlatformEnum.class)
    private String platform;

    /**
     * 目标总字数。
     */
    @Schema(description = "目标总字数")
    private Integer targetTotalWords;

    /**
     * 每章目标字数。
     */
    @Schema(description = "每章目标字数")
    private Integer targetChapterWords;

    /**
     * 上下文 Token 软预算。
     */
    @Schema(description = "上下文Token目标预算")
    private Integer tokenBudget;

    /**
     * 上下文 Token 硬上限。
     */
    @Schema(description = "上下文Token硬上限")
    private Integer tokenHardLimit;

    /**
     * 项目状态。
     */
    @SchemaEnum(desc = "项目状态", value = NovelProjectStatusEnum.class)
    private String status;

    /**
     * 归档标记，true 表示项目已归档。
     */
    @Schema(description = "归档标记")
    private Boolean deletedFlag;

    /**
     * 创建用户ID。
     */
    @Schema(description = "创建用户ID")
    private Long createUserId;

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 最后更新时间。
     */
    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
