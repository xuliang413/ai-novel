package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelAliasTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说马甲创建表单。
 * <p>
 * 管理页只维护马甲设定；是否识破和被谁识破属于高风险剧情事实，只能由写作流程确认后修改。
 *
 * @Author AI-Novel
 */
@Data
public class NovelAliasAddForm {

    /**
     * 所属项目ID，服务层会校验项目必须属于当前登录用户。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 马甲名称，用于列表展示、图谱节点和写作上下文检索。
     */
    @Schema(description = "马甲名称")
    @NotBlank(message = "马甲名称不能为空")
    @Length(max = 100, message = "马甲名称最多100个字符")
    private String name;

    /**
     * 马甲类型，用来区分网络身份、伪装身份、第二人格或其他身份。
     */
    @SchemaEnum(desc = "马甲类型", value = NovelAliasTypeEnum.class)
    @CheckEnum(value = NovelAliasTypeEnum.class, required = true, message = "马甲类型错误")
    private String type;

    /**
     * 使用场景描述，说明这个身份通常在什么剧情或环境下出现。
     */
    @Schema(description = "使用场景描述")
    private String aliasContext;

    /**
     * 马甲描述，记录外显身份、人设特征和与真实身份的差异。
     */
    @Schema(description = "马甲描述")
    private String summary;
}
