package net.lab1024.sa.admin.module.business.novel.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.constant.AdminSwaggerTagConst;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelAliasAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelAliasQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelAliasUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterRelationAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterRelationQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterRelationUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCheatAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCheatQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCheatUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelClueAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelClueQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelClueUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelEventAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelEventQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelEventUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelItemAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelItemQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelItemUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelLocationAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelLocationQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelLocationUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelNarrativeRuleAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelNarrativeRuleQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelNarrativeRuleUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelVolumeAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelVolumeQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelVolumeUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelAliasVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelCharacterRelationVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelCharacterVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelCheatVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelClueVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelEventVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelItemVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelLocationVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelNarrativeRuleVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelVolumeVO;
import net.lab1024.sa.admin.module.business.novel.service.NovelAssetService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小说资产管理接口。
 * <p>
 * 第一阶段先提供角色和地点接口，所有接口都通过 SmartAdmin 登录上下文做用户隔离。
 *
 * @Author AI-Novel
 */
@RestController
@Tag(name = AdminSwaggerTagConst.Business.NOVEL_ASSET)
public class NovelAssetController {

    /**
     * 小说资产服务。
     */
    @Resource
    private NovelAssetService novelAssetService;

    /**
     * 分页查询角色。
     *
     * @param queryForm 查询条件
     * @return 角色分页结果
     */
    @Operation(summary = "分页查询小说角色 @author AI-Novel")
    @PostMapping("/novel/asset/character/page/query")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<PageResult<NovelCharacterVO>> queryCharacterByPage(@RequestBody @Valid NovelCharacterQueryForm queryForm) {
        return novelAssetService.queryCharacterByPage(queryForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询角色详情。
     *
     * @param characterId 角色ID
     * @return 角色详情
     */
    @Operation(summary = "查询小说角色详情 @author AI-Novel")
    @GetMapping("/novel/asset/character/get/{characterId}")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<NovelCharacterVO> getCharacterDetail(@PathVariable Long characterId) {
        return novelAssetService.getCharacterDetail(characterId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 创建角色。
     *
     * @param addForm 创建表单
     * @return 创建结果
     */
    @Operation(summary = "创建小说角色 @author AI-Novel")
    @PostMapping("/novel/asset/character/create")
    @SaCheckPermission("novel:asset:add")
    public ResponseDTO<String> createCharacter(@RequestBody @Valid NovelCharacterAddForm addForm) {
        return novelAssetService.createCharacter(addForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 编辑角色。
     *
     * @param updateForm 编辑表单
     * @return 编辑结果
     */
    @Operation(summary = "编辑小说角色 @author AI-Novel")
    @PostMapping("/novel/asset/character/update")
    @SaCheckPermission("novel:asset:update")
    public ResponseDTO<String> updateCharacter(@RequestBody @Valid NovelCharacterUpdateForm updateForm) {
        return novelAssetService.updateCharacter(updateForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 归档角色。
     *
     * @param characterId 角色ID
     * @return 归档结果
     */
    @Operation(summary = "归档小说角色 @author AI-Novel")
    @GetMapping("/novel/asset/character/archive/{characterId}")
    @SaCheckPermission("novel:asset:archive")
    public ResponseDTO<String> archiveCharacter(@PathVariable Long characterId) {
        return novelAssetService.archiveCharacter(characterId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 分页查询地点。
     *
     * @param queryForm 查询条件
     * @return 地点分页结果
     */
    @Operation(summary = "分页查询小说地点 @author AI-Novel")
    @PostMapping("/novel/asset/location/page/query")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<PageResult<NovelLocationVO>> queryLocationByPage(@RequestBody @Valid NovelLocationQueryForm queryForm) {
        return novelAssetService.queryLocationByPage(queryForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询地点详情。
     *
     * @param locationId 地点ID
     * @return 地点详情
     */
    @Operation(summary = "查询小说地点详情 @author AI-Novel")
    @GetMapping("/novel/asset/location/get/{locationId}")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<NovelLocationVO> getLocationDetail(@PathVariable Long locationId) {
        return novelAssetService.getLocationDetail(locationId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 创建地点。
     *
     * @param addForm 创建表单
     * @return 创建结果
     */
    @Operation(summary = "创建小说地点 @author AI-Novel")
    @PostMapping("/novel/asset/location/create")
    @SaCheckPermission("novel:asset:add")
    public ResponseDTO<String> createLocation(@RequestBody @Valid NovelLocationAddForm addForm) {
        return novelAssetService.createLocation(addForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 编辑地点。
     *
     * @param updateForm 编辑表单
     * @return 编辑结果
     */
    @Operation(summary = "编辑小说地点 @author AI-Novel")
    @PostMapping("/novel/asset/location/update")
    @SaCheckPermission("novel:asset:update")
    public ResponseDTO<String> updateLocation(@RequestBody @Valid NovelLocationUpdateForm updateForm) {
        return novelAssetService.updateLocation(updateForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 归档地点。
     *
     * @param locationId 地点ID
     * @return 归档结果
     */
    @Operation(summary = "归档小说地点 @author AI-Novel")
    @GetMapping("/novel/asset/location/archive/{locationId}")
    @SaCheckPermission("novel:asset:archive")
    public ResponseDTO<String> archiveLocation(@PathVariable Long locationId) {
        return novelAssetService.archiveLocation(locationId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 分页查询线索。
     *
     * @param queryForm 查询条件
     * @return 线索分页结果
     */
    @Operation(summary = "分页查询小说线索 @author AI-Novel")
    @PostMapping("/novel/asset/clue/page/query")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<PageResult<NovelClueVO>> queryClueByPage(@RequestBody @Valid NovelClueQueryForm queryForm) {
        return novelAssetService.queryClueByPage(queryForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询线索详情。
     *
     * @param clueId 线索ID
     * @return 线索详情
     */
    @Operation(summary = "查询小说线索详情 @author AI-Novel")
    @GetMapping("/novel/asset/clue/get/{clueId}")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<NovelClueVO> getClueDetail(@PathVariable Long clueId) {
        return novelAssetService.getClueDetail(clueId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 创建线索。
     *
     * @param addForm 创建表单
     * @return 创建结果
     */
    @Operation(summary = "创建小说线索 @author AI-Novel")
    @PostMapping("/novel/asset/clue/create")
    @SaCheckPermission("novel:asset:add")
    public ResponseDTO<String> createClue(@RequestBody @Valid NovelClueAddForm addForm) {
        return novelAssetService.createClue(addForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 编辑线索。
     *
     * @param updateForm 编辑表单
     * @return 编辑结果
     */
    @Operation(summary = "编辑小说线索 @author AI-Novel")
    @PostMapping("/novel/asset/clue/update")
    @SaCheckPermission("novel:asset:update")
    public ResponseDTO<String> updateClue(@RequestBody @Valid NovelClueUpdateForm updateForm) {
        return novelAssetService.updateClue(updateForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 归档线索。
     *
     * @param clueId 线索ID
     * @return 归档结果
     */
    @Operation(summary = "归档小说线索 @author AI-Novel")
    @GetMapping("/novel/asset/clue/archive/{clueId}")
    @SaCheckPermission("novel:asset:archive")
    public ResponseDTO<String> archiveClue(@PathVariable Long clueId) {
        return novelAssetService.archiveClue(clueId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 分页查询物品。
     *
     * @param queryForm 查询条件
     * @return 物品分页结果
     */
    @Operation(summary = "分页查询小说物品 @author AI-Novel")
    @PostMapping("/novel/asset/item/page/query")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<PageResult<NovelItemVO>> queryItemByPage(@RequestBody @Valid NovelItemQueryForm queryForm) {
        return novelAssetService.queryItemByPage(queryForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询物品详情。
     *
     * @param itemId 物品ID
     * @return 物品详情
     */
    @Operation(summary = "查询小说物品详情 @author AI-Novel")
    @GetMapping("/novel/asset/item/get/{itemId}")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<NovelItemVO> getItemDetail(@PathVariable Long itemId) {
        return novelAssetService.getItemDetail(itemId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 创建物品。
     *
     * @param addForm 创建表单
     * @return 创建结果
     */
    @Operation(summary = "创建小说物品 @author AI-Novel")
    @PostMapping("/novel/asset/item/create")
    @SaCheckPermission("novel:asset:add")
    public ResponseDTO<String> createItem(@RequestBody @Valid NovelItemAddForm addForm) {
        return novelAssetService.createItem(addForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 编辑物品。
     *
     * @param updateForm 编辑表单
     * @return 编辑结果
     */
    @Operation(summary = "编辑小说物品 @author AI-Novel")
    @PostMapping("/novel/asset/item/update")
    @SaCheckPermission("novel:asset:update")
    public ResponseDTO<String> updateItem(@RequestBody @Valid NovelItemUpdateForm updateForm) {
        return novelAssetService.updateItem(updateForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 归档物品。
     *
     * @param itemId 物品ID
     * @return 归档结果
     */
    @Operation(summary = "归档小说物品 @author AI-Novel")
    @GetMapping("/novel/asset/item/archive/{itemId}")
    @SaCheckPermission("novel:asset:archive")
    public ResponseDTO<String> archiveItem(@PathVariable Long itemId) {
        return novelAssetService.archiveItem(itemId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 分页查询事件。
     *
     * @param queryForm 查询条件
     * @return 事件分页结果
     */
    @Operation(summary = "分页查询小说事件 @author AI-Novel")
    @PostMapping("/novel/asset/event/page/query")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<PageResult<NovelEventVO>> queryEventByPage(@RequestBody @Valid NovelEventQueryForm queryForm) {
        return novelAssetService.queryEventByPage(queryForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询事件详情。
     *
     * @param eventId 事件ID
     * @return 事件详情
     */
    @Operation(summary = "查询小说事件详情 @author AI-Novel")
    @GetMapping("/novel/asset/event/get/{eventId}")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<NovelEventVO> getEventDetail(@PathVariable Long eventId) {
        return novelAssetService.getEventDetail(eventId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 创建事件。
     *
     * @param addForm 创建表单
     * @return 创建结果
     */
    @Operation(summary = "创建小说事件 @author AI-Novel")
    @PostMapping("/novel/asset/event/create")
    @SaCheckPermission("novel:asset:add")
    public ResponseDTO<String> createEvent(@RequestBody @Valid NovelEventAddForm addForm) {
        return novelAssetService.createEvent(addForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 编辑事件。
     *
     * @param updateForm 编辑表单
     * @return 编辑结果
     */
    @Operation(summary = "编辑小说事件 @author AI-Novel")
    @PostMapping("/novel/asset/event/update")
    @SaCheckPermission("novel:asset:update")
    public ResponseDTO<String> updateEvent(@RequestBody @Valid NovelEventUpdateForm updateForm) {
        return novelAssetService.updateEvent(updateForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 归档事件。
     *
     * @param eventId 事件ID
     * @return 归档结果
     */
    @Operation(summary = "归档小说事件 @author AI-Novel")
    @GetMapping("/novel/asset/event/archive/{eventId}")
    @SaCheckPermission("novel:asset:archive")
    public ResponseDTO<String> archiveEvent(@PathVariable Long eventId) {
        return novelAssetService.archiveEvent(eventId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 分页查询金手指。
     *
     * @param queryForm 查询条件
     * @return 金手指分页结果
     */
    @Operation(summary = "分页查询小说金手指 @author AI-Novel")
    @PostMapping("/novel/asset/cheat/page/query")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<PageResult<NovelCheatVO>> queryCheatByPage(@RequestBody @Valid NovelCheatQueryForm queryForm) {
        return novelAssetService.queryCheatByPage(queryForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询金手指详情。
     *
     * @param cheatId 金手指ID
     * @return 金手指详情
     */
    @Operation(summary = "查询小说金手指详情 @author AI-Novel")
    @GetMapping("/novel/asset/cheat/get/{cheatId}")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<NovelCheatVO> getCheatDetail(@PathVariable Long cheatId) {
        return novelAssetService.getCheatDetail(cheatId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 创建金手指。
     *
     * @param addForm 创建表单
     * @return 创建结果
     */
    @Operation(summary = "创建小说金手指 @author AI-Novel")
    @PostMapping("/novel/asset/cheat/create")
    @SaCheckPermission("novel:asset:add")
    public ResponseDTO<String> createCheat(@RequestBody @Valid NovelCheatAddForm addForm) {
        return novelAssetService.createCheat(addForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 编辑金手指。
     *
     * @param updateForm 编辑表单
     * @return 编辑结果
     */
    @Operation(summary = "编辑小说金手指 @author AI-Novel")
    @PostMapping("/novel/asset/cheat/update")
    @SaCheckPermission("novel:asset:update")
    public ResponseDTO<String> updateCheat(@RequestBody @Valid NovelCheatUpdateForm updateForm) {
        return novelAssetService.updateCheat(updateForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 归档金手指。
     *
     * @param cheatId 金手指ID
     * @return 归档结果
     */
    @Operation(summary = "归档小说金手指 @author AI-Novel")
    @GetMapping("/novel/asset/cheat/archive/{cheatId}")
    @SaCheckPermission("novel:asset:archive")
    public ResponseDTO<String> archiveCheat(@PathVariable Long cheatId) {
        return novelAssetService.archiveCheat(cheatId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 分页查询马甲。
     *
     * @param queryForm 查询条件
     * @return 马甲分页结果
     */
    @Operation(summary = "分页查询小说马甲 @author AI-Novel")
    @PostMapping("/novel/asset/alias/page/query")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<PageResult<NovelAliasVO>> queryAliasByPage(@RequestBody @Valid NovelAliasQueryForm queryForm) {
        return novelAssetService.queryAliasByPage(queryForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询马甲详情。
     *
     * @param aliasId 马甲ID
     * @return 马甲详情
     */
    @Operation(summary = "查询小说马甲详情 @author AI-Novel")
    @GetMapping("/novel/asset/alias/get/{aliasId}")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<NovelAliasVO> getAliasDetail(@PathVariable Long aliasId) {
        return novelAssetService.getAliasDetail(aliasId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 创建马甲。
     *
     * @param addForm 创建表单
     * @return 创建结果
     */
    @Operation(summary = "创建小说马甲 @author AI-Novel")
    @PostMapping("/novel/asset/alias/create")
    @SaCheckPermission("novel:asset:add")
    public ResponseDTO<String> createAlias(@RequestBody @Valid NovelAliasAddForm addForm) {
        return novelAssetService.createAlias(addForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 编辑马甲。
     *
     * @param updateForm 编辑表单
     * @return 编辑结果
     */
    @Operation(summary = "编辑小说马甲 @author AI-Novel")
    @PostMapping("/novel/asset/alias/update")
    @SaCheckPermission("novel:asset:update")
    public ResponseDTO<String> updateAlias(@RequestBody @Valid NovelAliasUpdateForm updateForm) {
        return novelAssetService.updateAlias(updateForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 归档马甲。
     *
     * @param aliasId 马甲ID
     * @return 归档结果
     */
    @Operation(summary = "归档小说马甲 @author AI-Novel")
    @GetMapping("/novel/asset/alias/archive/{aliasId}")
    @SaCheckPermission("novel:asset:archive")
    public ResponseDTO<String> archiveAlias(@PathVariable Long aliasId) {
        return novelAssetService.archiveAlias(aliasId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 分页查询叙事规则。
     *
     * @param queryForm 查询条件
     * @return 叙事规则分页结果
     */
    @Operation(summary = "分页查询小说叙事规则 @author AI-Novel")
    @PostMapping("/novel/asset/narrative-rule/page/query")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<PageResult<NovelNarrativeRuleVO>> queryNarrativeRuleByPage(@RequestBody @Valid NovelNarrativeRuleQueryForm queryForm) {
        return novelAssetService.queryNarrativeRuleByPage(queryForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询叙事规则详情。
     *
     * @param ruleId 叙事规则ID
     * @return 叙事规则详情
     */
    @Operation(summary = "查询小说叙事规则详情 @author AI-Novel")
    @GetMapping("/novel/asset/narrative-rule/get/{ruleId}")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<NovelNarrativeRuleVO> getNarrativeRuleDetail(@PathVariable Long ruleId) {
        return novelAssetService.getNarrativeRuleDetail(ruleId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 创建叙事规则。
     *
     * @param addForm 创建表单
     * @return 创建结果
     */
    @Operation(summary = "创建小说叙事规则 @author AI-Novel")
    @PostMapping("/novel/asset/narrative-rule/create")
    @SaCheckPermission("novel:asset:add")
    public ResponseDTO<String> createNarrativeRule(@RequestBody @Valid NovelNarrativeRuleAddForm addForm) {
        return novelAssetService.createNarrativeRule(addForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 编辑叙事规则。
     *
     * @param updateForm 编辑表单
     * @return 编辑结果
     */
    @Operation(summary = "编辑小说叙事规则 @author AI-Novel")
    @PostMapping("/novel/asset/narrative-rule/update")
    @SaCheckPermission("novel:asset:update")
    public ResponseDTO<String> updateNarrativeRule(@RequestBody @Valid NovelNarrativeRuleUpdateForm updateForm) {
        return novelAssetService.updateNarrativeRule(updateForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 归档叙事规则。
     *
     * @param ruleId 叙事规则ID
     * @return 归档结果
     */
    @Operation(summary = "归档小说叙事规则 @author AI-Novel")
    @GetMapping("/novel/asset/narrative-rule/archive/{ruleId}")
    @SaCheckPermission("novel:asset:archive")
    public ResponseDTO<String> archiveNarrativeRule(@PathVariable Long ruleId) {
        return novelAssetService.archiveNarrativeRule(ruleId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 分页查询卷。
     *
     * @param queryForm 查询条件
     * @return 卷分页结果
     */
    @Operation(summary = "分页查询小说卷 @author AI-Novel")
    @PostMapping("/novel/asset/volume/page/query")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<PageResult<NovelVolumeVO>> queryVolumeByPage(@RequestBody @Valid NovelVolumeQueryForm queryForm) {
        return novelAssetService.queryVolumeByPage(queryForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询卷详情。
     *
     * @param volumeId 卷ID
     * @return 卷详情
     */
    @Operation(summary = "查询小说卷详情 @author AI-Novel")
    @GetMapping("/novel/asset/volume/get/{volumeId}")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<NovelVolumeVO> getVolumeDetail(@PathVariable Long volumeId) {
        return novelAssetService.getVolumeDetail(volumeId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 创建卷。
     *
     * @param addForm 创建表单
     * @return 创建结果
     */
    @Operation(summary = "创建小说卷 @author AI-Novel")
    @PostMapping("/novel/asset/volume/create")
    @SaCheckPermission("novel:asset:add")
    public ResponseDTO<String> createVolume(@RequestBody @Valid NovelVolumeAddForm addForm) {
        return novelAssetService.createVolume(addForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 编辑卷。
     *
     * @param updateForm 编辑表单
     * @return 编辑结果
     */
    @Operation(summary = "编辑小说卷 @author AI-Novel")
    @PostMapping("/novel/asset/volume/update")
    @SaCheckPermission("novel:asset:update")
    public ResponseDTO<String> updateVolume(@RequestBody @Valid NovelVolumeUpdateForm updateForm) {
        return novelAssetService.updateVolume(updateForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 归档卷。
     *
     * @param volumeId 卷ID
     * @return 归档结果
     */
    @Operation(summary = "归档小说卷 @author AI-Novel")
    @GetMapping("/novel/asset/volume/archive/{volumeId}")
    @SaCheckPermission("novel:asset:archive")
    public ResponseDTO<String> archiveVolume(@PathVariable Long volumeId) {
        return novelAssetService.archiveVolume(volumeId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 分页查询角色关系。
     *
     * @param queryForm 查询条件
     * @return 角色关系分页结果
     */
    @Operation(summary = "分页查询小说角色关系 @author AI-Novel")
    @PostMapping("/novel/asset/character-relation/page/query")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<PageResult<NovelCharacterRelationVO>> queryCharacterRelationByPage(@RequestBody @Valid NovelCharacterRelationQueryForm queryForm) {
        return novelAssetService.queryCharacterRelationByPage(queryForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 查询角色关系详情。
     *
     * @param relationId 角色关系ID
     * @return 角色关系详情
     */
    @Operation(summary = "查询小说角色关系详情 @author AI-Novel")
    @GetMapping("/novel/asset/character-relation/get/{relationId}")
    @SaCheckPermission("novel:asset:query")
    public ResponseDTO<NovelCharacterRelationVO> getCharacterRelationDetail(@PathVariable Long relationId) {
        return novelAssetService.getCharacterRelationDetail(relationId, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 创建角色关系。
     *
     * @param addForm 创建表单
     * @return 创建结果
     */
    @Operation(summary = "创建小说角色关系 @author AI-Novel")
    @PostMapping("/novel/asset/character-relation/create")
    @SaCheckPermission("novel:asset:add")
    public ResponseDTO<String> createCharacterRelation(@RequestBody @Valid NovelCharacterRelationAddForm addForm) {
        return novelAssetService.createCharacterRelation(addForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 编辑角色关系。
     *
     * @param updateForm 编辑表单
     * @return 编辑结果
     */
    @Operation(summary = "编辑小说角色关系 @author AI-Novel")
    @PostMapping("/novel/asset/character-relation/update")
    @SaCheckPermission("novel:asset:update")
    public ResponseDTO<String> updateCharacterRelation(@RequestBody @Valid NovelCharacterRelationUpdateForm updateForm) {
        return novelAssetService.updateCharacterRelation(updateForm, SmartRequestUtil.getRequestUserId());
    }

    /**
     * 归档角色关系。
     *
     * @param relationId 角色关系ID
     * @return 归档结果
     */
    @Operation(summary = "归档小说角色关系 @author AI-Novel")
    @GetMapping("/novel/asset/character-relation/archive/{relationId}")
    @SaCheckPermission("novel:asset:archive")
    public ResponseDTO<String> archiveCharacterRelation(@PathVariable Long relationId) {
        return novelAssetService.archiveCharacterRelation(relationId, SmartRequestUtil.getRequestUserId());
    }
}
