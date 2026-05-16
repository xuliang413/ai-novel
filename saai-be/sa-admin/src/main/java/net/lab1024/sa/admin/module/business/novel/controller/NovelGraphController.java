package net.lab1024.sa.admin.module.business.novel.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.business.novel.dao.GraphChangeLogDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.GraphChangeLogEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelIdForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.GraphChangeLogVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelGraphPanelVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelGraphService;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 图谱查询与运维接口
 *
 * 提供图谱面板数据查询、变更历史、健康检查等。图谱写入由写作流程驱动（GraphPatch），不走这里。
 */
@RestController
@RequestMapping("/novel/graph")
@Tag(name = "AI 小说 - 知识图谱")
public class NovelGraphController {

    @Resource
    private NovelGraphService novelGraphService;

    @Resource
    private GraphChangeLogDao graphChangeLogDao;

    /**
     * 图谱健康检查 —— 返回 Neo4j 各节点类型的数量，用于检测丢数据。
     */
    @Operation(summary = "图谱健康检查")
    @PostMapping("/health")
    public ResponseDTO<java.util.Map<String, Long>> health(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelGraphService.healthCheck(form.getId()));
    }

    /**
     * 角色关系图 —— 项目下所有角色之间的社交关系网
     *
     * 节点：Character（红色圆形，group=0）
     * 边：KNOWS/LOVES/HATES/IS_FAMILY_OF
     * 前端可用此图绘制角色社交网络。
     */
    @Operation(summary = "角色关系图谱")
    @PostMapping("/character-relation")
    public ResponseDTO<NovelGraphPanelVO> characterRelation(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelGraphService.queryCharacterRelationGraph(form.getId()));
    }

    /**
     * 线索推进图 —— 哪些章节推进了哪些线索
     *
     * 节点：Clue（绿色三角，group=2）+ Chapter（紫色菱形，group=3）
     * 边：ADVANCES（Chapter → Clue）
     */
    @Operation(summary = "线索推进图谱")
    @PostMapping("/clue-advancement")
    public ResponseDTO<NovelGraphPanelVO> clueAdvancement(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelGraphService.queryClueAdvancementGraph(form.getId()));
    }

    /**
     * 地点人物分布图 —— 当前各地点有哪些角色
     *
     * 节点：Location（蓝色方形，group=1）+ Character（红色圆形，group=0）
     * 边：CURRENTLY_AT（Character → Location）
     */
    @Operation(summary = "地点人物分布图谱")
    @PostMapping("/location-character")
    public ResponseDTO<NovelGraphPanelVO> locationCharacter(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelGraphService.queryLocationCharacterGraph(form.getId()));
    }

    /**
     * 道具流转图 —— 道具的持有者和出场章节
     *
     * 节点：Item（橙色点，group=4）+ Character（group=0）+ Chapter（group=3）
     * 边：POSSESSES + APPEARS_IN
     */
    @Operation(summary = "道具流转图谱")
    @PostMapping("/item-flow")
    public ResponseDTO<NovelGraphPanelVO> itemFlow(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelGraphService.queryItemFlowGraph(form.getId()));
    }

    /**
     * GraphPatch 变更历史查询
     *
     * 返回某个项目下所有 GraphPatch 的执行/撤销/失败记录，
     * 按创建时间倒序排列。每条记录包含操作批次 ID、状态、影响的章节和操作计数。
     *
     * 数据来源：graph_change_log 表，每次 confirmPatch / undo 都会写入。
     */
    @Operation(summary = "GraphPatch 变更历史")
    @PostMapping("/change-log")
    public ResponseDTO<List<GraphChangeLogVO>> changeLog(@RequestBody @Valid NovelIdForm form) {
        List<GraphChangeLogEntity> entities = graphChangeLogDao.selectList(
                new LambdaQueryWrapper<GraphChangeLogEntity>()
                        .eq(GraphChangeLogEntity::getProjectId, form.getId())
                        .orderByDesc(GraphChangeLogEntity::getCreateTime)
                        .last("limit 50"));
        List<GraphChangeLogVO> list = new ArrayList<>();
        for (GraphChangeLogEntity e : entities) {
            list.add(GraphChangeLogVO.builder()
                    .changeLogId(e.getChangeLogId())
                    .projectId(e.getProjectId())
                    .chapterId(e.getChapterId())
                    .chapterNo(e.getChapterNo())
                    .operationBatchId(e.getOperationBatchId())
                    .status(e.getStatus())
                    .errorMessage(e.getErrorMessage())
                    .patchOperationCount(countOps(e.getPatchJson()))
                    .inverseOperationCount(countOps(e.getInversePatchJson()))
                    .createTime(e.getCreateTime())
                    .build());
        }
        return ResponseDTO.ok(list);
    }

    private int countOps(String json) {
        if (json == null) return 0;
        try {
            JSONObject obj = JSON.parseObject(json);
            return obj.getJSONArray("operations") != null ? obj.getJSONArray("operations").size() : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }
}
