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
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelAssetQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCheatAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelClueAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelEventAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelItemAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelLocationAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelNarrativeRuleAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelVolumeAddForm;
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
}
