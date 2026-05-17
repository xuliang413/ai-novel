package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelFamilyTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelLoveStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelRelationTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 小说角色关系展示 VO。
 * <p>
 * 返回角色关系的大类和对应属性，用于关系管理页和后续关系网查询。
 *
 * @Author AI-Novel
 */
@Data
public class NovelCharacterRelationVO {

    /**
     * 角色关系ID。
     */
    @Schema(description = "角色关系ID")
    private Long id;

    /**
     * 所属项目ID。
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 源角色ID。
     */
    @Schema(description = "源角色ID")
    private Long characterId;

    /**
     * 目标角色ID。
     */
    @Schema(description = "目标角色ID")
    private Long targetCharacterId;

    /**
     * 关系大类，取值为 KNOWS、LOVES、HATES、IS_FAMILY_OF。
     */
    @Schema(description = "关系大类")
    private String relationType;

    /**
     * KNOWS 子类型。
     */
    @SchemaEnum(desc = "KNOWS子类型", value = NovelRelationTypeEnum.class)
    private String knowsRelationType;

    /**
     * 爱慕状态。
     */
    @SchemaEnum(desc = "爱慕状态", value = NovelLoveStatusEnum.class)
    private String loveStatus;

    /**
     * 仇恨强度，范围 1~5。
     */
    @Schema(description = "仇恨强度")
    private Integer hateIntensity;

    /**
     * 亲缘或师门类型。
     */
    @SchemaEnum(desc = "亲缘或师门类型", value = NovelFamilyTypeEnum.class)
    private String familyType;

    /**
     * 归档标记，true 表示关系已归档。
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
