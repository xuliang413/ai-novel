package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 小说角色关系分页查询表单。
 * <p>
 * 查询条件只表达筛选意图，项目归属、当前用户隔离和未归档条件由服务层强制追加。
 *
 * @Author AI-Novel
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelCharacterRelationQueryForm extends PageParam {

    /**
     * 所属项目ID，查询前会校验项目归属。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 源角色ID筛选，不传表示不过滤起点角色。
     */
    @Schema(description = "源角色ID")
    private Long characterId;

    /**
     * 目标角色ID筛选，不传表示不过滤终点角色。
     */
    @Schema(description = "目标角色ID")
    private Long targetCharacterId;

    /**
     * 关系大类筛选，只对 KNOWS、LOVES、HATES、IS_FAMILY_OF 有意义。
     */
    @Schema(description = "关系大类")
    private String relationType;
}
