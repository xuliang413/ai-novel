package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 角色关系网 VO —— 适配 @antv/g6 的 nodes + edges 结构
 * <p>
 * 前端直接用 nodes 和 edges 渲染图谱可视化，无需二次转换。
 *
 * @Author AI-Novel
 */
@Data
public class NovelCharacterNetworkVO {

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "角色节点列表, 每个节点含id/label/roleType/emotion等")
    private List<GraphNode> nodes;

    @Schema(description = "关系边列表, 每个边含source/target/relationType")
    private List<GraphEdge> edges;

    /**
     * 图谱节点 —— 一个角色在图谱中的可视化单元
     */
    @Data
    public static class GraphNode {

        @Schema(description = "节点唯一标识, MySQL角色ID")
        private String id;

        @Schema(description = "节点显示名, 角色名")
        private String label;

        @Schema(description = "角色定位: PROTAGONIST/ANTAGONIST/SUPPORTING/MINOR")
        private String roleType;

        @Schema(description = "当前情绪")
        private String currentEmotion;

        @Schema(description = "存活状态")
        private String currentStatus;

        @Schema(description = "节点在图中的分组(按roleType归类)")
        private String group;
    }

    /**
     * 图谱边 —— 两个角色之间的连线
     */
    @Data
    public static class GraphEdge {

        @Schema(description = "源角色ID")
        private String source;

        @Schema(description = "目标角色ID")
        private String target;

        @Schema(description = "关系类型: KNOWS/LOVES/HATES/IS_FAMILY_OF")
        private String relationType;

        @Schema(description = "关系详细标签, 如FRIEND/UNREQUITED/MASTER")
        private String relationLabel;
    }
}
