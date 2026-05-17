package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelAliasTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 小说马甲展示 VO。
 * <p>
 * 返回马甲设定字段和写作流程维护出的暴露状态，便于作者审阅身份线是否被提前泄露。
 *
 * @Author AI-Novel
 */
@Data
public class NovelAliasVO {

    /**
     * 马甲ID。
     */
    @Schema(description = "马甲ID")
    private Long id;

    /**
     * 所属项目ID。
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 马甲名称。
     */
    @Schema(description = "马甲名称")
    private String name;

    /**
     * 马甲类型。
     */
    @SchemaEnum(desc = "马甲类型", value = NovelAliasTypeEnum.class)
    private String type;

    /**
     * 使用场景描述。
     */
    @Schema(description = "使用场景描述")
    private String aliasContext;

    /**
     * 马甲描述。
     */
    @Schema(description = "马甲描述")
    private String summary;

    /**
     * 是否已被识破，由写作流程确认后回写。
     */
    @Schema(description = "是否已被识破")
    private Boolean revealed;

    /**
     * 被谁识破，通常记录角色名称或角色ID摘要。
     */
    @Schema(description = "被谁识破")
    private String revealedTo;

    /**
     * 归档标记，true 表示马甲已归档。
     */
    @Schema(description = "归档标记")
    private Boolean deletedFlag;

    /**
     * 创建用户ID。
     */
    @Schema(description = "创建用户ID")
    private Long createUserId;

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
