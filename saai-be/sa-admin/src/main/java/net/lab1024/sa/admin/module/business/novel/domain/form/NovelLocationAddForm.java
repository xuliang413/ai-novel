package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelLocationTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 小说地点创建表单。
 * <p>
 * 地点属于世界观设定资产，管理页允许直接维护名称、类型和自然语言描述。
 *
 * @Author AI-Novel
 */
@Data
public class NovelLocationAddForm {

    /**
     * 所属项目ID，服务层会校验项目必须属于当前登录用户。
     */
    @Schema(description = "所属项目ID")
    @NotNull(message = "所属项目ID不能为空")
    private Long projectId;

    /**
     * 地点名称，用于地图、图谱节点和写作提示词中的地点识别。
     */
    @Schema(description = "地点名称")
    @NotBlank(message = "地点名称不能为空")
    @Length(max = 200, message = "地点名称最多200个字符")
    private String name;

    /**
     * 地点类型，用于区分城市、宗门、秘境等不同空间设定。
     */
    @SchemaEnum(desc = "地点类型", value = NovelLocationTypeEnum.class)
    @CheckEnum(value = NovelLocationTypeEnum.class, required = true, message = "地点类型错误")
    private String type;

    /**
     * 地点自然语言描述，记录环境、风格、规则和剧情用途。
     */
    @Schema(description = "地点描述")
    private String summary;
}
