package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 图谱面板统一 VO —— 前端图谱组件的数据契约
 *
 * 为什么需要统一的图谱数据结构：
 * 前端图谱组件（不管是 ECharts、D3.js 还是自定义 Canvas）都需要知道
 * "有哪些节点、哪些边、节点的分组和颜色是什么"。
 *
 * 如果把角色关系图、线索推进图、地点分布图每种都返回不同的 JSON 结构，
 * 前端就要写三套渲染逻辑。现在把所有图谱查询统一成 nodes + edges + groups + legends
 * 的结构，前端只写一套图谱渲染引擎，后端通过 type 字段区分图类型。
 *
 * 每个节点包含：
 * - id：唯一标识（mysqlId 映射）
 * - type：节点类型（Character/Location/Clue/Chapter/Event/Item等）
 * - name：显示名称
 * - group：分组编号（前端用分组决定颜色和形状）
 * - properties：附加属性（摘要、状态等，前端 tooltip 用）
 *
 * 每条边包含：
 * - source/target：两端节点 id
 * - label：关系类型
 * - properties：附加属性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelGraphPanelVO {

    @Schema(description = "图谱类型", example = "character_relation")
    private String graphType;

    @Schema(description = "项目ID")
    private Long projectId;

    @Schema(description = "节点列表")
    private List<GraphNode> nodes;

    @Schema(description = "边列表")
    private List<GraphEdge> edges;

    @Schema(description = "分组信息 —— 前端用分组决定颜色和图标")
    private List<GraphGroup> groups;

    @Schema(description = "图例")
    private List<GraphLegend> legends;

    @Schema(description = "过滤器配置（前端动态筛选）")
    private List<GraphFilter> filters;

    @Schema(description = "警告提示（如数据不完整）")
    private List<String> warnings;

    // ==================== 内嵌 VO ====================

    /**
     * 图谱节点 —— 图上的一个圆/方块
     *
     * 每个节点有一个唯一 id（取自 Neo4j 的 mysqlId 加类型前缀），
     * 前端通过 id 在节点和边之间做匹配渲染。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphNode {

        @Schema(description = "节点唯一 ID", example = "character_5")
        private String id;

        @Schema(description = "节点类型", example = "Character")
        private String type;

        @Schema(description = "节点名称（图上显示的文字）", example = "叶尘")
        private String name;

        @Schema(description = "分组编号（决定颜色和形状）", example = "0")
        private int group;

        @Schema(description = "附加属性（tooltip 展示用）")
        private Map<String, Object> properties;

        /**
         * 快捷工厂方法 —— 用 mysqlId 拼接类型前缀保证全局唯一
         *
         * Neo4j 里的节点由 projectId + name 联合唯一，但前端 id 必须是全图唯一的字符串。
         * 这里用 {type}_{mysqlId} 保证不同表节点的 id 不会碰撞。
         */
        public static GraphNode of(String type, Long mysqlId, String name, int group) {
            return GraphNode.builder()
                    .id(type + "_" + mysqlId)
                    .type(type).name(name).group(group)
                    .properties(new LinkedHashMap<>())
                    .build();
        }
    }

    /**
     * 图谱边 —— 节点之间的连线
     *
     * source 和 target 都是节点的 id（GraphNode.id）。
     * label 显示在连线上方，properties 存额外的关系属性（如 acquiredInChapter）。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphEdge {

        @Schema(description = "源节点 ID")
        private String source;

        @Schema(description = "目标节点 ID")
        private String target;

        @Schema(description = "关系标签", example = "APPEARS_IN")
        private String label;

        @Schema(description = "附加属性")
        private Map<String, Object> properties;
    }

    /**
     * 分组定义 —— 前端根据这个决定每类节点的颜色和形状
     *
     * 每组有一个编号（group number），GraphNode.group 指向这里的编号。
     * 比如角色=0（红色圆形）、地点=1（蓝色方形）。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphGroup {

        @Schema(description = "分组编号", example = "0")
        private int group;

        @Schema(description = "分组名称", example = "角色")
        private String name;

        @Schema(description = "颜色", example = "#e74c3c")
        private String color;

        @Schema(description = "节点形状", example = "circle")
        private String shape;
    }

    /**
     * 图例 —— 前端图谱右下角的说明面板
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphLegend {

        @Schema(description = "图例编号")
        private int index;

        @Schema(description = "图例名称", example = "出场关系")
        private String name;

        @Schema(description = "边颜色", example = "#3498db")
        private String color;

        @Schema(description = "线条样式", example = "solid")
        private String lineStyle;
    }

    /**
     * 筛选器配置 —— 前端图谱工具栏的筛选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphFilter {

        @Schema(description = "筛选字段名")
        private String field;

        @Schema(description = "筛选标签")
        private String label;

        @Schema(description = "可选项")
        private List<String> options;
    }

    /**
     * 分组预设 —— 给各图谱类型提供默认的分组和颜色配置
     */
    public static List<GraphGroup> defaultGroups() {
        List<GraphGroup> groups = new ArrayList<>();
        groups.add(GraphGroup.builder().group(0).name("角色").color("#e74c3c").shape("circle").build());
        groups.add(GraphGroup.builder().group(1).name("地点").color("#3498db").shape("square").build());
        groups.add(GraphGroup.builder().group(2).name("线索").color("#2ecc71").shape("triangle").build());
        groups.add(GraphGroup.builder().group(3).name("章节").color("#9b59b6").shape("diamond").build());
        groups.add(GraphGroup.builder().group(4).name("物品").color("#f39c12").shape("dot").build());
        groups.add(GraphGroup.builder().group(5).name("事件").color("#1abc9c").shape("star").build());
        groups.add(GraphGroup.builder().group(6).name("金手指").color("#e67e22").shape("hexagon").build());
        groups.add(GraphGroup.builder().group(7).name("马甲").color("#34495e").shape("pentagon").build());
        groups.add(GraphGroup.builder().group(8).name("卷").color("#7f8c8d").shape("box").build());
        return groups;
    }
}
