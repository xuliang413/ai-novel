package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelAliasEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCheatEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelEventEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelItemEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelNarrativeRuleEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelVolumeEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelAliasAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelAliasUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelAssetQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCheatAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCheatUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelClueAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelClueUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelEventAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelEventUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelIdForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelItemAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelItemUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelLocationAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelLocationUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelNarrativeRuleAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelNarrativeRuleUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelVolumeAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelVolumeUpdateForm;
import net.lab1024.sa.admin.module.business.novel.service.NovelAssetService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小说设定接口——角色、地点、线索、卷、物品、事件、金手指、马甲和叙事规则的录入与查询。
 */
@RestController
@RequestMapping("/novel")
@Tag(name = "AI 小说 - 基础设定")
public class NovelAssetController {

    @Resource
    private NovelAssetService novelAssetService;

    /**
     * 新增角色，并同步建立 Project -> Character 图关系。
     */
    @Operation(summary = "新增小说角色")
    @PostMapping("/character/add")
    public ResponseDTO<Long> addCharacter(@RequestBody @Valid NovelCharacterAddForm addForm) {
        return novelAssetService.addCharacter(addForm);
    }

    @Operation(summary = "分页查询小说角色")
    @PostMapping("/character/query")
    public ResponseDTO<PageResult<NovelCharacterEntity>> queryCharacter(@RequestBody @Valid NovelAssetQueryForm queryForm) {
        return novelAssetService.queryCharacter(queryForm);
    }

    /**
     * 新增地点，并同步建立 Project -> Location 图关系。
     */
    @Operation(summary = "新增小说地点")
    @PostMapping("/location/add")
    public ResponseDTO<Long> addLocation(@RequestBody @Valid NovelLocationAddForm addForm) {
        return novelAssetService.addLocation(addForm);
    }

    @Operation(summary = "分页查询小说地点")
    @PostMapping("/location/query")
    public ResponseDTO<PageResult<NovelLocationEntity>> queryLocation(@RequestBody @Valid NovelAssetQueryForm queryForm) {
        return novelAssetService.queryLocation(queryForm);
    }

    /**
     * 新增线索，并同步建立 Project -> Clue 图关系。
     */
    @Operation(summary = "新增小说线索")
    @PostMapping("/clue/add")
    public ResponseDTO<Long> addClue(@RequestBody @Valid NovelClueAddForm addForm) {
        return novelAssetService.addClue(addForm);
    }

    @Operation(summary = "分页查询小说线索")
    @PostMapping("/clue/query")
    public ResponseDTO<PageResult<NovelClueEntity>> queryClue(@RequestBody @Valid NovelAssetQueryForm queryForm) {
        return novelAssetService.queryClue(queryForm);
    }

    @Operation(summary = "新增小说卷")
    @PostMapping("/volume/add")
    public ResponseDTO<Long> addVolume(@RequestBody @Valid NovelVolumeAddForm addForm) {
        return novelAssetService.addVolume(addForm);
    }

    @Operation(summary = "分页查询小说卷")
    @PostMapping("/volume/query")
    public ResponseDTO<PageResult<NovelVolumeEntity>> queryVolume(@RequestBody @Valid NovelAssetQueryForm queryForm) {
        return novelAssetService.queryVolume(queryForm);
    }

    @Operation(summary = "新增小说物品")
    @PostMapping("/item/add")
    public ResponseDTO<Long> addItem(@RequestBody @Valid NovelItemAddForm addForm) {
        return novelAssetService.addItem(addForm);
    }

    @Operation(summary = "分页查询小说物品")
    @PostMapping("/item/query")
    public ResponseDTO<PageResult<NovelItemEntity>> queryItem(@RequestBody @Valid NovelAssetQueryForm queryForm) {
        return novelAssetService.queryItem(queryForm);
    }

    @Operation(summary = "新增小说事件")
    @PostMapping("/event/add")
    public ResponseDTO<Long> addEvent(@RequestBody @Valid NovelEventAddForm addForm) {
        return novelAssetService.addEvent(addForm);
    }

    @Operation(summary = "分页查询小说事件")
    @PostMapping("/event/query")
    public ResponseDTO<PageResult<NovelEventEntity>> queryEvent(@RequestBody @Valid NovelAssetQueryForm queryForm) {
        return novelAssetService.queryEvent(queryForm);
    }

    @Operation(summary = "新增小说金手指")
    @PostMapping("/cheat/add")
    public ResponseDTO<Long> addCheat(@RequestBody @Valid NovelCheatAddForm addForm) {
        return novelAssetService.addCheat(addForm);
    }

    @Operation(summary = "分页查询小说金手指")
    @PostMapping("/cheat/query")
    public ResponseDTO<PageResult<NovelCheatEntity>> queryCheat(@RequestBody @Valid NovelAssetQueryForm queryForm) {
        return novelAssetService.queryCheat(queryForm);
    }

    @Operation(summary = "新增小说马甲")
    @PostMapping("/alias/add")
    public ResponseDTO<Long> addAlias(@RequestBody @Valid NovelAliasAddForm addForm) {
        return novelAssetService.addAlias(addForm);
    }

    @Operation(summary = "分页查询小说马甲")
    @PostMapping("/alias/query")
    public ResponseDTO<PageResult<NovelAliasEntity>> queryAlias(@RequestBody @Valid NovelAssetQueryForm queryForm) {
        return novelAssetService.queryAlias(queryForm);
    }

    @Operation(summary = "新增小说叙事规则")
    @PostMapping("/rule/add")
    public ResponseDTO<Long> addNarrativeRule(@RequestBody @Valid NovelNarrativeRuleAddForm addForm) {
        return novelAssetService.addNarrativeRule(addForm);
    }

    @Operation(summary = "分页查询小说叙事规则")
    @PostMapping("/rule/query")
    public ResponseDTO<PageResult<NovelNarrativeRuleEntity>> queryNarrativeRule(@RequestBody @Valid NovelAssetQueryForm queryForm) {
        return novelAssetService.queryNarrativeRule(queryForm);
    }

    // ==================== 资产详情（单条查询） ====================

    @Operation(summary = "查询角色详情")
    @PostMapping("/character/detail")
    public ResponseDTO<NovelCharacterEntity> characterDetail(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelAssetService.getCharacterDetail(form.getId()));
    }

    @Operation(summary = "查询地点详情")
    @PostMapping("/location/detail")
    public ResponseDTO<NovelLocationEntity> locationDetail(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelAssetService.getLocationDetail(form.getId()));
    }

    @Operation(summary = "查询线索详情")
    @PostMapping("/clue/detail")
    public ResponseDTO<NovelClueEntity> clueDetail(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelAssetService.getClueDetail(form.getId()));
    }

    @Operation(summary = "查询卷详情")
    @PostMapping("/volume/detail")
    public ResponseDTO<NovelVolumeEntity> volumeDetail(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelAssetService.getVolumeDetail(form.getId()));
    }

    @Operation(summary = "查询物品详情")
    @PostMapping("/item/detail")
    public ResponseDTO<NovelItemEntity> itemDetail(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelAssetService.getItemDetail(form.getId()));
    }

    @Operation(summary = "查询事件详情")
    @PostMapping("/event/detail")
    public ResponseDTO<NovelEventEntity> eventDetail(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelAssetService.getEventDetail(form.getId()));
    }

    @Operation(summary = "查询金手指详情")
    @PostMapping("/cheat/detail")
    public ResponseDTO<NovelCheatEntity> cheatDetail(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelAssetService.getCheatDetail(form.getId()));
    }

    @Operation(summary = "查询马甲详情")
    @PostMapping("/alias/detail")
    public ResponseDTO<NovelAliasEntity> aliasDetail(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelAssetService.getAliasDetail(form.getId()));
    }

    @Operation(summary = "查询叙事规则详情")
    @PostMapping("/rule/detail")
    public ResponseDTO<NovelNarrativeRuleEntity> narrativeRuleDetail(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(novelAssetService.getNarrativeRuleDetail(form.getId()));
    }

    // ==================== 资产编辑 ====================

    @Operation(summary = "编辑角色")
    @PostMapping("/character/update")
    public ResponseDTO<Boolean> updateCharacter(@RequestBody @Valid NovelCharacterUpdateForm form) {
        return novelAssetService.updateCharacter(form);
    }

    @Operation(summary = "编辑地点")
    @PostMapping("/location/update")
    public ResponseDTO<Boolean> updateLocation(@RequestBody @Valid NovelLocationUpdateForm form) {
        return novelAssetService.updateLocation(form);
    }

    @Operation(summary = "编辑线索")
    @PostMapping("/clue/update")
    public ResponseDTO<Boolean> updateClue(@RequestBody @Valid NovelClueUpdateForm form) {
        return novelAssetService.updateClue(form);
    }

    @Operation(summary = "编辑卷")
    @PostMapping("/volume/update")
    public ResponseDTO<Boolean> updateVolume(@RequestBody @Valid NovelVolumeUpdateForm form) {
        return novelAssetService.updateVolume(form);
    }

    @Operation(summary = "编辑物品")
    @PostMapping("/item/update")
    public ResponseDTO<Boolean> updateItem(@RequestBody @Valid NovelItemUpdateForm form) {
        return novelAssetService.updateItem(form);
    }

    @Operation(summary = "编辑事件")
    @PostMapping("/event/update")
    public ResponseDTO<Boolean> updateEvent(@RequestBody @Valid NovelEventUpdateForm form) {
        return novelAssetService.updateEvent(form);
    }

    @Operation(summary = "编辑金手指")
    @PostMapping("/cheat/update")
    public ResponseDTO<Boolean> updateCheat(@RequestBody @Valid NovelCheatUpdateForm form) {
        return novelAssetService.updateCheat(form);
    }

    @Operation(summary = "编辑马甲")
    @PostMapping("/alias/update")
    public ResponseDTO<Boolean> updateAlias(@RequestBody @Valid NovelAliasUpdateForm form) {
        return novelAssetService.updateAlias(form);
    }

    @Operation(summary = "编辑叙事规则")
    @PostMapping("/rule/update")
    public ResponseDTO<Boolean> updateNarrativeRule(@RequestBody @Valid NovelNarrativeRuleUpdateForm form) {
        return novelAssetService.updateNarrativeRule(form);
    }

    // ==================== 资产归档（软删除） ====================

    @Operation(summary = "归档角色")
    @PostMapping("/character/archive")
    public ResponseDTO<Boolean> archiveCharacter(@RequestBody @Valid NovelIdForm form) {
        return novelAssetService.archiveCharacter(form.getId());
    }

    @Operation(summary = "归档地点")
    @PostMapping("/location/archive")
    public ResponseDTO<Boolean> archiveLocation(@RequestBody @Valid NovelIdForm form) {
        return novelAssetService.archiveLocation(form.getId());
    }

    @Operation(summary = "归档线索")
    @PostMapping("/clue/archive")
    public ResponseDTO<Boolean> archiveClue(@RequestBody @Valid NovelIdForm form) {
        return novelAssetService.archiveClue(form.getId());
    }

    @Operation(summary = "归档卷")
    @PostMapping("/volume/archive")
    public ResponseDTO<Boolean> archiveVolume(@RequestBody @Valid NovelIdForm form) {
        return novelAssetService.archiveVolume(form.getId());
    }

    @Operation(summary = "归档物品")
    @PostMapping("/item/archive")
    public ResponseDTO<Boolean> archiveItem(@RequestBody @Valid NovelIdForm form) {
        return novelAssetService.archiveItem(form.getId());
    }

    @Operation(summary = "归档事件")
    @PostMapping("/event/archive")
    public ResponseDTO<Boolean> archiveEvent(@RequestBody @Valid NovelIdForm form) {
        return novelAssetService.archiveEvent(form.getId());
    }

    @Operation(summary = "归档金手指")
    @PostMapping("/cheat/archive")
    public ResponseDTO<Boolean> archiveCheat(@RequestBody @Valid NovelIdForm form) {
        return novelAssetService.archiveCheat(form.getId());
    }

    @Operation(summary = "归档马甲")
    @PostMapping("/alias/archive")
    public ResponseDTO<Boolean> archiveAlias(@RequestBody @Valid NovelIdForm form) {
        return novelAssetService.archiveAlias(form.getId());
    }

    @Operation(summary = "归档叙事规则")
    @PostMapping("/rule/archive")
    public ResponseDTO<Boolean> archiveNarrativeRule(@RequestBody @Valid NovelIdForm form) {
        return novelAssetService.archiveNarrativeRule(form.getId());
    }
}
