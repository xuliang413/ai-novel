package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphPatchOperationTypeEnum;

/**
 * GraphPatch 操作模型 —— 单一图谱变更项
 * <p>
 * 每个操作对应38种业务操作之一，通过operationType映射到5种Cypher模板执行。
 * before值由系统从Neo4j回填(after值由AI抽取)，审阅时用户可见前后对比。
 * inverse操作(撤销)在生成patch时同步计算。
 *
 * @Author AI-Novel
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelGraphPatchOperationModel {

    /**
     * 操作ID, 在同一patch内唯一, 审阅页面用此ID做勾选/取消
     */
    private String opId;

    /**
     * 38种业务操作类型, 每种映射到一个Cypher模板
     */
    private NovelGraphPatchOperationTypeEnum type;

    /**
     * 涉及的角色名, 用于审阅页面展示和Neo4j MATCH定位
     * 非角色类操作可为空(如CREATE_LOCATION用locationName)
     */
    private String characterName;

    /**
     * 目标实体名, 如地点名/物品名/目标角色名
     */
    private String targetName;

    /**
     * 变更前的值, 由系统从Neo4j当前状态回填，AI不填此字段
     * 用于审阅页面的前后对比展示
     */
    private String before;

    /**
     * 变更后的值, 由AI从正文中抽取
     * 字符串形式，具体含义由operationType决定: emotion名/goal文本/powerLevel等
     */
    private String after;

    /**
     * AI抽取的置信度 0.0~1.0
     * 审阅页面按置信度排序展示, 低置信度用橙色标记
     */
    private Float confidence;

    /**
     * 实体MySQL ID(新增实体为null, 由写入时生成)
     */
    private Long entityId;

    /**
     * 目标实体MySQL ID
     */
    private Long targetEntityId;

    /**
     * 额外的属性Map, 用于多字段操作(如角色关系携带relationType/loveStatus等)
     */
    private java.util.Map<String, Object> extraProps;
}
