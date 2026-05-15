package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.domain.PageParam;
import org.hibernate.validator.constraints.Length;

/**
 * 小说资产通用分页查询表单。
 *
 * 角色、地点、线索、物品、事件、金手指、马甲、叙事规则都共用它。
 * 不同资产会选择性使用 keyword/type/status。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NovelAssetQueryForm extends PageParam {

    /**
     * 只查当前小说项目下的资产，避免不同项目设定串在一起。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 名称关键字。
     */
    @Length(max = 100, message = "关键字最多 100 个字符")
    private String keyword;

    /**
     * 类型筛选。
     */
    @Length(max = 50, message = "类型最多 50 个字符")
    private String type;

    /**
     * 状态筛选。
     */
    @Length(max = 50, message = "状态最多 50 个字符")
    private String status;
}
