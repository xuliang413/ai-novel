package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelFamilyTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelLoveStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelRelationTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

/**
 * 小说角色关系创建表单。
 * <p>
 * 只开放 KNOWS、LOVES、HATES、IS_FAMILY_OF 四种角色间关系；其他关系中间表仍然只允许 GraphPatch 写作流程维护。
 *
 * @Author AI-Novel
 */
@Data
public class NovelCharacterRelationAddForm {

    /**
     * 所属项目ID，服务层会校验项目必须属于当前登录用户。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 源角色ID，也就是关系的起点角色。
     */
    @Schema(description = "源角色ID")
    @NotNull(message = "源角色ID不能为空")
    private Long characterId;

    /**
     * 目标角色ID，也就是关系的终点角色。
     */
    @Schema(description = "目标角色ID")
    @NotNull(message = "目标角色ID不能为空")
    private Long targetCharacterId;

    /**
     * 关系大类，只允许 KNOWS、LOVES、HATES、IS_FAMILY_OF。
     */
    @Schema(description = "关系大类")
    @NotBlank(message = "关系大类不能为空")
    private String relationType;

    /**
     * KNOWS 子类型，仅 relationType=KNOWS 时使用，必须在白名单枚举内。
     */
    @SchemaEnum(desc = "KNOWS子类型", value = NovelRelationTypeEnum.class)
    @CheckEnum(value = NovelRelationTypeEnum.class, required = false, message = "KNOWS子类型错误")
    private String knowsRelationType;

    /**
     * 爱慕状态，仅 relationType=LOVES 时使用。
     */
    @SchemaEnum(desc = "爱慕状态", value = NovelLoveStatusEnum.class)
    @CheckEnum(value = NovelLoveStatusEnum.class, required = false, message = "爱慕状态错误")
    private String loveStatus;

    /**
     * 仇恨强度，仅 relationType=HATES 时使用，范围 1~5。
     */
    @Schema(description = "仇恨强度")
    @Min(value = 1, message = "仇恨强度不能小于1")
    @Max(value = 5, message = "仇恨强度不能大于5")
    private Integer hateIntensity;

    /**
     * 亲缘或师门类型，仅 relationType=IS_FAMILY_OF 时使用。
     */
    @SchemaEnum(desc = "亲缘或师门类型", value = NovelFamilyTypeEnum.class)
    @CheckEnum(value = NovelFamilyTypeEnum.class, required = false, message = "亲缘类型错误")
    private String familyType;
}
