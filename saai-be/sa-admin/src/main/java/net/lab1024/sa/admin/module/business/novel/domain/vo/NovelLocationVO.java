package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelLocationTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 小说地点展示 VO。
 * <p>
 * 返回地点的基础设定属性，后续层级关系会通过图谱查询接口单独展示。
 *
 * @Author AI-Novel
 */
@Data
public class NovelLocationVO {

    /**
     * 地点ID。
     */
    @Schema(description = "地点ID")
    private Long id;

    /**
     * 所属项目ID。
     */
    @Schema(description = "所属项目ID")
    private Long projectId;

    /**
     * 地点名称。
     */
    @Schema(description = "地点名称")
    private String name;

    /**
     * 地点类型。
     */
    @SchemaEnum(desc = "地点类型", value = NovelLocationTypeEnum.class)
    private String type;

    /**
     * 地点自然语言描述。
     */
    @Schema(description = "地点描述")
    private String summary;

    /**
     * 归档标记，true 表示地点已归档。
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
