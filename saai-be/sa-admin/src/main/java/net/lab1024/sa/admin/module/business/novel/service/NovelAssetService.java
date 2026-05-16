package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelItemStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueStatusEnum;
import net.lab1024.sa.admin.module.business.novel.dao.NovelAliasDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCheatDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelClueDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelEventDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelItemDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelLocationDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelNarrativeRuleDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelVolumeDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelAliasEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCheatEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelEventEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelItemEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelNarrativeRuleEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
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
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelAliasUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCheatUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelClueUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelEventUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelItemUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelLocationUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelNarrativeRuleUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelVolumeUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelVolumeAddForm;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 小说设定服务——角色、地点、线索的增删查，新增时同步到 Neo4j 图谱。
 */
@Service
public class NovelAssetService {

    @Resource
    private NovelCharacterDao novelCharacterDao;

    @Resource
    private NovelLocationDao novelLocationDao;

    @Resource
    private NovelClueDao novelClueDao;

    @Resource
    private NovelVolumeDao novelVolumeDao;

    @Resource
    private NovelItemDao novelItemDao;

    @Resource
    private NovelEventDao novelEventDao;

    @Resource
    private NovelCheatDao novelCheatDao;

    @Resource
    private NovelAliasDao novelAliasDao;

    @Resource
    private NovelNarrativeRuleDao novelNarrativeRuleDao;

    @Resource
    private NovelProjectService novelProjectService;

    @Resource
    private NovelGraphService novelGraphService;

    /**
     * 新增角色，并建立 Project -> Character 的图关系。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> addCharacter(NovelCharacterAddForm addForm) {
        NovelProjectEntity project = getProjectOrNull(addForm.getProjectId());
        if (project == null) {
            return ResponseDTO.userErrorParam("小说项目不存在");
        }

        NovelCharacterEntity entity = SmartBeanUtil.copy(addForm, NovelCharacterEntity.class);
        entity.setCurrentStatus(StringUtils.defaultIfBlank(addForm.getCurrentStatus(), NovelCharacterStatusEnum.ACTIVE.getValue()));
        entity.setDeletedFlag(false);
        novelCharacterDao.insert(entity);

        novelGraphService.mergeProject(project);
        novelGraphService.mergeCharacter(entity);
        return ResponseDTO.ok(entity.getCharacterId());
    }

    /**
     * 新增地点，并建立 Project -> Location 的图关系。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> addLocation(NovelLocationAddForm addForm) {
        NovelProjectEntity project = getProjectOrNull(addForm.getProjectId());
        if (project == null) {
            return ResponseDTO.userErrorParam("小说项目不存在");
        }

        NovelLocationEntity entity = SmartBeanUtil.copy(addForm, NovelLocationEntity.class);
        entity.setDeletedFlag(false);
        novelLocationDao.insert(entity);

        novelGraphService.mergeProject(project);
        novelGraphService.mergeLocation(entity);
        return ResponseDTO.ok(entity.getLocationId());
    }

    /**
     * 新增线索，并建立 Project -> Clue 的图关系。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> addClue(NovelClueAddForm addForm) {
        NovelProjectEntity project = getProjectOrNull(addForm.getProjectId());
        if (project == null) {
            return ResponseDTO.userErrorParam("小说项目不存在");
        }

        NovelClueEntity entity = SmartBeanUtil.copy(addForm, NovelClueEntity.class);
        entity.setClueStatus(StringUtils.defaultIfBlank(addForm.getClueStatus(), NovelClueStatusEnum.DORMANT.getValue()));
        entity.setDeletedFlag(false);
        novelClueDao.insert(entity);

        novelGraphService.mergeProject(project);
        novelGraphService.mergeClue(entity);
        return ResponseDTO.ok(entity.getClueId());
    }

    /**
     * 新增卷，并建立 Project -> Volume 图关系。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> addVolume(NovelVolumeAddForm addForm) {
        NovelProjectEntity project = getProjectOrNull(addForm.getProjectId());
        if (project == null) {
            return ResponseDTO.userErrorParam("小说项目不存在");
        }

        NovelVolumeEntity entity = SmartBeanUtil.copy(addForm, NovelVolumeEntity.class);
        entity.setDeletedFlag(false);
        novelVolumeDao.insert(entity);

        novelGraphService.mergeProject(project);
        novelGraphService.mergeVolume(entity);
        return ResponseDTO.ok(entity.getVolumeId());
    }

    /**
     * 新增物品，并建立 Project -> Item 图关系。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> addItem(NovelItemAddForm addForm) {
        NovelProjectEntity project = getProjectOrNull(addForm.getProjectId());
        if (project == null) {
            return ResponseDTO.userErrorParam("小说项目不存在");
        }

        NovelItemEntity entity = SmartBeanUtil.copy(addForm, NovelItemEntity.class);
        entity.setItemStatus(StringUtils.defaultIfBlank(addForm.getItemStatus(), NovelItemStatusEnum.INTACT.getValue()));
        entity.setDeletedFlag(false);
        novelItemDao.insert(entity);

        novelGraphService.mergeProject(project);
        novelGraphService.mergeItem(entity);
        return ResponseDTO.ok(entity.getItemId());
    }

    /**
     * 新增事件，并建立 Project -> Event 图关系。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> addEvent(NovelEventAddForm addForm) {
        NovelProjectEntity project = getProjectOrNull(addForm.getProjectId());
        if (project == null) {
            return ResponseDTO.userErrorParam("小说项目不存在");
        }

        NovelEventEntity entity = SmartBeanUtil.copy(addForm, NovelEventEntity.class);
        entity.setDeletedFlag(false);
        novelEventDao.insert(entity);

        novelGraphService.mergeProject(project);
        novelGraphService.mergeEvent(entity);
        return ResponseDTO.ok(entity.getEventId());
    }

    /**
     * 新增金手指，并建立 Project -> Cheat 图关系。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> addCheat(NovelCheatAddForm addForm) {
        NovelProjectEntity project = getProjectOrNull(addForm.getProjectId());
        if (project == null) {
            return ResponseDTO.userErrorParam("小说项目不存在");
        }

        NovelCheatEntity entity = SmartBeanUtil.copy(addForm, NovelCheatEntity.class);
        entity.setDeletedFlag(false);
        novelCheatDao.insert(entity);

        novelGraphService.mergeProject(project);
        novelGraphService.mergeCheat(entity);
        return ResponseDTO.ok(entity.getCheatId());
    }

    /**
     * 新增马甲，并建立 Project -> Alias 图关系。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> addAlias(NovelAliasAddForm addForm) {
        NovelProjectEntity project = getProjectOrNull(addForm.getProjectId());
        if (project == null) {
            return ResponseDTO.userErrorParam("小说项目不存在");
        }

        NovelAliasEntity entity = SmartBeanUtil.copy(addForm, NovelAliasEntity.class);
        entity.setRevealed(Boolean.TRUE.equals(addForm.getRevealed()));
        entity.setDeletedFlag(false);
        novelAliasDao.insert(entity);

        novelGraphService.mergeProject(project);
        novelGraphService.mergeAlias(entity);
        return ResponseDTO.ok(entity.getAliasId());
    }

    /**
     * 新增叙事规则，并建立 Project -> NarrativeRule 图关系。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> addNarrativeRule(NovelNarrativeRuleAddForm addForm) {
        NovelProjectEntity project = getProjectOrNull(addForm.getProjectId());
        if (project == null) {
            return ResponseDTO.userErrorParam("小说项目不存在");
        }

        NovelNarrativeRuleEntity entity = SmartBeanUtil.copy(addForm, NovelNarrativeRuleEntity.class);
        entity.setPriority(addForm.getPriority() == null ? 3 : addForm.getPriority());
        entity.setDeletedFlag(false);
        novelNarrativeRuleDao.insert(entity);

        novelGraphService.mergeProject(project);
        novelGraphService.mergeNarrativeRule(entity);
        return ResponseDTO.ok(entity.getRuleId());
    }

    /**
     * 分页查询角色。
     *
     * 给管理页用：支持按名称、角色定位、当前状态筛选。
     */
    public ResponseDTO<PageResult<NovelCharacterEntity>> queryCharacter(NovelAssetQueryForm queryForm) {
        Page<NovelCharacterEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize(), true);
        LambdaQueryWrapper<NovelCharacterEntity> wrapper = new LambdaQueryWrapper<NovelCharacterEntity>()
                .eq(NovelCharacterEntity::getProjectId, queryForm.getProjectId())
                .eq(NovelCharacterEntity::getDeletedFlag, false)
                .like(StringUtils.isNotBlank(queryForm.getKeyword()), NovelCharacterEntity::getCharacterName, queryForm.getKeyword())
                .eq(StringUtils.isNotBlank(queryForm.getType()), NovelCharacterEntity::getRoleType, queryForm.getType())
                .eq(StringUtils.isNotBlank(queryForm.getStatus()), NovelCharacterEntity::getCurrentStatus, queryForm.getStatus())
                .orderByAsc(NovelCharacterEntity::getCharacterId);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(novelCharacterDao.selectPage(page, wrapper), page.getRecords()));
    }

    /**
     * 分页查询地点。
     *
     * 地点没有业务 status，筛选时只看名称和地点类型。
     */
    public ResponseDTO<PageResult<NovelLocationEntity>> queryLocation(NovelAssetQueryForm queryForm) {
        Page<NovelLocationEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize(), true);
        LambdaQueryWrapper<NovelLocationEntity> wrapper = new LambdaQueryWrapper<NovelLocationEntity>()
                .eq(NovelLocationEntity::getProjectId, queryForm.getProjectId())
                .eq(NovelLocationEntity::getDeletedFlag, false)
                .like(StringUtils.isNotBlank(queryForm.getKeyword()), NovelLocationEntity::getLocationName, queryForm.getKeyword())
                .eq(StringUtils.isNotBlank(queryForm.getType()), NovelLocationEntity::getLocationType, queryForm.getType())
                .orderByAsc(NovelLocationEntity::getLocationId);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(novelLocationDao.selectPage(page, wrapper), page.getRecords()));
    }

    /**
     * 分页查询线索。
     *
     * 线索状态会影响写作检索：ACTIVE 更容易进入上下文，DORMANT 通常留给写后校验。
     */
    public ResponseDTO<PageResult<NovelClueEntity>> queryClue(NovelAssetQueryForm queryForm) {
        Page<NovelClueEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize(), true);
        LambdaQueryWrapper<NovelClueEntity> wrapper = new LambdaQueryWrapper<NovelClueEntity>()
                .eq(NovelClueEntity::getProjectId, queryForm.getProjectId())
                .eq(NovelClueEntity::getDeletedFlag, false)
                .like(StringUtils.isNotBlank(queryForm.getKeyword()), NovelClueEntity::getClueName, queryForm.getKeyword())
                .eq(StringUtils.isNotBlank(queryForm.getType()), NovelClueEntity::getClueType, queryForm.getType())
                .eq(StringUtils.isNotBlank(queryForm.getStatus()), NovelClueEntity::getClueStatus, queryForm.getStatus())
                .orderByAsc(NovelClueEntity::getClueId);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(novelClueDao.selectPage(page, wrapper), page.getRecords()));
    }

    /**
     * 分页查询卷。
     *
     * P0 阶段卷主要用于大纲和章节归属，后续可以接入 Volume -> Chapter 的结构关系。
     */
    public ResponseDTO<PageResult<NovelVolumeEntity>> queryVolume(NovelAssetQueryForm queryForm) {
        Page<NovelVolumeEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize(), true);
        LambdaQueryWrapper<NovelVolumeEntity> wrapper = new LambdaQueryWrapper<NovelVolumeEntity>()
                .eq(NovelVolumeEntity::getProjectId, queryForm.getProjectId())
                .eq(NovelVolumeEntity::getDeletedFlag, false)
                .like(StringUtils.isNotBlank(queryForm.getKeyword()), NovelVolumeEntity::getVolumeTitle, queryForm.getKeyword())
                .orderByAsc(NovelVolumeEntity::getVolumeNo);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(novelVolumeDao.selectPage(page, wrapper), page.getRecords()));
    }

    /**
     * 分页查询物品。
     *
     * 物品状态会影响剧情风险，例如 DESTROYED / LOST 这类高风险变化必须人工确认。
     */
    public ResponseDTO<PageResult<NovelItemEntity>> queryItem(NovelAssetQueryForm queryForm) {
        Page<NovelItemEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize(), true);
        LambdaQueryWrapper<NovelItemEntity> wrapper = new LambdaQueryWrapper<NovelItemEntity>()
                .eq(NovelItemEntity::getProjectId, queryForm.getProjectId())
                .eq(NovelItemEntity::getDeletedFlag, false)
                .like(StringUtils.isNotBlank(queryForm.getKeyword()), NovelItemEntity::getItemName, queryForm.getKeyword())
                .eq(StringUtils.isNotBlank(queryForm.getType()), NovelItemEntity::getItemType, queryForm.getType())
                .eq(StringUtils.isNotBlank(queryForm.getStatus()), NovelItemEntity::getItemStatus, queryForm.getStatus())
                .orderByAsc(NovelItemEntity::getItemId);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(novelItemDao.selectPage(page, wrapper), page.getRecords()));
    }

    /**
     * 分页查询事件。
     *
     * 事件用于承载“已经发生过的剧情事实”，不是章节正文全文。
     */
    public ResponseDTO<PageResult<NovelEventEntity>> queryEvent(NovelAssetQueryForm queryForm) {
        Page<NovelEventEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize(), true);
        LambdaQueryWrapper<NovelEventEntity> wrapper = new LambdaQueryWrapper<NovelEventEntity>()
                .eq(NovelEventEntity::getProjectId, queryForm.getProjectId())
                .eq(NovelEventEntity::getDeletedFlag, false)
                .like(StringUtils.isNotBlank(queryForm.getKeyword()), NovelEventEntity::getEventName, queryForm.getKeyword())
                .orderByAsc(NovelEventEntity::getEventId);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(novelEventDao.selectPage(page, wrapper), page.getRecords()));
    }

    /**
     * 分页查询金手指。
     *
     * 写作检索时会把候选角色拥有的金手指作为关键资产注入 Prompt。
     */
    public ResponseDTO<PageResult<NovelCheatEntity>> queryCheat(NovelAssetQueryForm queryForm) {
        Page<NovelCheatEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize(), true);
        LambdaQueryWrapper<NovelCheatEntity> wrapper = new LambdaQueryWrapper<NovelCheatEntity>()
                .eq(NovelCheatEntity::getProjectId, queryForm.getProjectId())
                .eq(NovelCheatEntity::getDeletedFlag, false)
                .like(StringUtils.isNotBlank(queryForm.getKeyword()), NovelCheatEntity::getCheatName, queryForm.getKeyword())
                .eq(StringUtils.isNotBlank(queryForm.getType()), NovelCheatEntity::getCheatType, queryForm.getType())
                .orderByAsc(NovelCheatEntity::getCheatId);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(novelCheatDao.selectPage(page, wrapper), page.getRecords()));
    }

    /**
     * 分页查询马甲。
     *
     * 马甲是否暴露属于高风险剧情事实，自动抽取时只做建议，确认后再写图谱。
     */
    public ResponseDTO<PageResult<NovelAliasEntity>> queryAlias(NovelAssetQueryForm queryForm) {
        Page<NovelAliasEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize(), true);
        LambdaQueryWrapper<NovelAliasEntity> wrapper = new LambdaQueryWrapper<NovelAliasEntity>()
                .eq(NovelAliasEntity::getProjectId, queryForm.getProjectId())
                .eq(NovelAliasEntity::getDeletedFlag, false)
                .like(StringUtils.isNotBlank(queryForm.getKeyword()), NovelAliasEntity::getAliasName, queryForm.getKeyword())
                .eq(StringUtils.isNotBlank(queryForm.getType()), NovelAliasEntity::getAliasType, queryForm.getType())
                .orderByAsc(NovelAliasEntity::getAliasId);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(novelAliasDao.selectPage(page, wrapper), page.getRecords()));
    }

    /**
     * 分页查询叙事规则。
     *
     * 规则按 priority 倒序返回，因为 Prompt 组装时优先级越高越靠前。
     */
    public ResponseDTO<PageResult<NovelNarrativeRuleEntity>> queryNarrativeRule(NovelAssetQueryForm queryForm) {
        Page<NovelNarrativeRuleEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize(), true);
        LambdaQueryWrapper<NovelNarrativeRuleEntity> wrapper = new LambdaQueryWrapper<NovelNarrativeRuleEntity>()
                .eq(NovelNarrativeRuleEntity::getProjectId, queryForm.getProjectId())
                .eq(NovelNarrativeRuleEntity::getDeletedFlag, false)
                .like(StringUtils.isNotBlank(queryForm.getKeyword()), NovelNarrativeRuleEntity::getRuleName, queryForm.getKeyword())
                .eq(StringUtils.isNotBlank(queryForm.getType()), NovelNarrativeRuleEntity::getRuleType, queryForm.getType())
                .orderByDesc(NovelNarrativeRuleEntity::getPriority)
                .orderByAsc(NovelNarrativeRuleEntity::getRuleId);
        return ResponseDTO.ok(SmartPageUtil.convert2PageResult(novelNarrativeRuleDao.selectPage(page, wrapper), page.getRecords()));
    }

    /**
     * 查询项目下有效角色，供 mock 写作组装上下文。
     */
    public List<NovelCharacterEntity> listCharacters(Long projectId) {
        return novelCharacterDao.selectList(new LambdaQueryWrapper<NovelCharacterEntity>()
                .eq(NovelCharacterEntity::getProjectId, projectId)
                .eq(NovelCharacterEntity::getDeletedFlag, false)
                .orderByAsc(NovelCharacterEntity::getCharacterId));
    }

    /**
     * 查询项目下有效地点，供 mock 写作组装上下文。
     */
    public List<NovelLocationEntity> listLocations(Long projectId) {
        return novelLocationDao.selectList(new LambdaQueryWrapper<NovelLocationEntity>()
                .eq(NovelLocationEntity::getProjectId, projectId)
                .eq(NovelLocationEntity::getDeletedFlag, false)
                .orderByAsc(NovelLocationEntity::getLocationId));
    }

    /**
     * 查询项目下有效线索，供 mock 写作组装上下文。
     */
    public List<NovelClueEntity> listClues(Long projectId) {
        return novelClueDao.selectList(new LambdaQueryWrapper<NovelClueEntity>()
                .eq(NovelClueEntity::getProjectId, projectId)
                .eq(NovelClueEntity::getDeletedFlag, false)
                .orderByAsc(NovelClueEntity::getClueId));
    }

    /**
     * 查询项目下有效物品，供 GraphPatch 抽取和写作上下文候选使用。
     */
    public List<NovelItemEntity> listItems(Long projectId) {
        return novelItemDao.selectList(new LambdaQueryWrapper<NovelItemEntity>()
                .eq(NovelItemEntity::getProjectId, projectId)
                .eq(NovelItemEntity::getDeletedFlag, false)
                .orderByAsc(NovelItemEntity::getItemId));
    }

    /**
     * 查询项目下有效事件，供 GraphPatch 抽取和事件关系写入使用。
     */
    public List<NovelEventEntity> listEvents(Long projectId) {
        return novelEventDao.selectList(new LambdaQueryWrapper<NovelEventEntity>()
                .eq(NovelEventEntity::getProjectId, projectId)
                .eq(NovelEventEntity::getDeletedFlag, false)
                .orderByAsc(NovelEventEntity::getEventId));
    }

    /**
     * 查询项目下有效金手指，供 Prompt 注入和 GraphPatch 审阅使用。
     */
    public List<NovelCheatEntity> listCheats(Long projectId) {
        return novelCheatDao.selectList(new LambdaQueryWrapper<NovelCheatEntity>()
                .eq(NovelCheatEntity::getProjectId, projectId)
                .eq(NovelCheatEntity::getDeletedFlag, false)
                .orderByAsc(NovelCheatEntity::getCheatId));
    }

    /**
     * 查询项目下有效马甲，供 Prompt 注入和 GraphPatch 审阅使用。
     */
    public List<NovelAliasEntity> listAliases(Long projectId) {
        return novelAliasDao.selectList(new LambdaQueryWrapper<NovelAliasEntity>()
                .eq(NovelAliasEntity::getProjectId, projectId)
                .eq(NovelAliasEntity::getDeletedFlag, false)
                .orderByAsc(NovelAliasEntity::getAliasId));
    }

    /**
     * 查询项目下有效叙事规则。
     *
     * 这些规则是写作时“系统提示”的重要来源，尤其是平台红线和字数/文风约束。
     */
    public List<NovelNarrativeRuleEntity> listNarrativeRules(Long projectId) {
        return novelNarrativeRuleDao.selectList(new LambdaQueryWrapper<NovelNarrativeRuleEntity>()
                .eq(NovelNarrativeRuleEntity::getProjectId, projectId)
                .eq(NovelNarrativeRuleEntity::getDeletedFlag, false)
                .orderByDesc(NovelNarrativeRuleEntity::getPriority)
                .orderByAsc(NovelNarrativeRuleEntity::getRuleId));
    }

    /**
     * 新增资产前校验项目存在，返回项目对象用于补写图谱 Project 节点。
     */
    private NovelProjectEntity getProjectOrNull(Long projectId) {
        return novelProjectService.getAvailableProject(projectId);
    }

    /**
     * 只复制源对象中的非 null 属性到目标对象
     *
     * 为什么不用 SmartBeanUtil.copyProperties：
     * Spring BeanUtils.copyProperties 会把源对象的所有字段（包括 null 值）都复制到目标。
     * 在编辑场景中，前端只传了用户修改的那几个字段，其他字段是 null（Java 默认值）。
     * 如果用默认复制，未传的字段会把 entity 中的已有值覆盖为 null —— 数据丢失。
     * 这里的实现收集源对象中值为 null 的属性名，传给 copyProperties 的 ignore 参数跳过它们。
     */
    private void copyNonNull(Object source, Object target) {
        Set<String> nullNames = new HashSet<>();
        try {
            java.beans.BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(source.getClass());
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                // 跳过 class 属性（每个对象都有 getClass()）
                if ("class".equals(pd.getName())) continue;
                java.lang.reflect.Method getter = pd.getReadMethod();
                if (getter != null) {
                    try {
                        Object value = getter.invoke(source);
                        if (value == null) {
                            nullNames.add(pd.getName());
                        }
                    } catch (Exception ignored) {
                        // 无法读取的属性跳过
                    }
                }
            }
        } catch (Exception ignored) {
            // 反射失败时退化为 Spring copyProperties（全量复制）
        }
        String[] ignore = nullNames.toArray(new String[0]);
        org.springframework.beans.BeanUtils.copyProperties(source, target, ignore);
    }

    // ==================== 资产详情（单条查询） ====================

    public NovelCharacterEntity getCharacterDetail(Long characterId) {
        return novelCharacterDao.selectById(characterId);
    }
    public NovelLocationEntity getLocationDetail(Long locationId) {
        return novelLocationDao.selectById(locationId);
    }
    public NovelClueEntity getClueDetail(Long clueId) {
        return novelClueDao.selectById(clueId);
    }
    public NovelVolumeEntity getVolumeDetail(Long volumeId) {
        return novelVolumeDao.selectById(volumeId);
    }
    public NovelItemEntity getItemDetail(Long itemId) {
        return novelItemDao.selectById(itemId);
    }
    public NovelEventEntity getEventDetail(Long eventId) {
        return novelEventDao.selectById(eventId);
    }
    public NovelCheatEntity getCheatDetail(Long cheatId) {
        return novelCheatDao.selectById(cheatId);
    }
    public NovelAliasEntity getAliasDetail(Long aliasId) {
        return novelAliasDao.selectById(aliasId);
    }
    public NovelNarrativeRuleEntity getNarrativeRuleDetail(Long ruleId) {
        return novelNarrativeRuleDao.selectById(ruleId);
    }

    // ==================== 资产编辑 ====================
    // 每个 update 方法统一：1)查 ID  2)判空+判已归档  3)copyNonNull（跳过 null 字段）4)updateById

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> updateCharacter(NovelCharacterUpdateForm form) {
        NovelCharacterEntity entity = novelCharacterDao.selectById(form.getCharacterId());
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("角色不存在");
        copyNonNull(form, entity);
        novelCharacterDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> updateLocation(NovelLocationUpdateForm form) {
        NovelLocationEntity entity = novelLocationDao.selectById(form.getLocationId());
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("地点不存在");
        copyNonNull(form, entity);
        novelLocationDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> updateClue(NovelClueUpdateForm form) {
        NovelClueEntity entity = novelClueDao.selectById(form.getClueId());
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("线索不存在");
        copyNonNull(form, entity);
        novelClueDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> updateVolume(NovelVolumeUpdateForm form) {
        NovelVolumeEntity entity = novelVolumeDao.selectById(form.getVolumeId());
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("卷不存在");
        copyNonNull(form, entity);
        novelVolumeDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> updateItem(NovelItemUpdateForm form) {
        NovelItemEntity entity = novelItemDao.selectById(form.getItemId());
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("物品不存在");
        copyNonNull(form, entity);
        novelItemDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> updateEvent(NovelEventUpdateForm form) {
        NovelEventEntity entity = novelEventDao.selectById(form.getEventId());
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("事件不存在");
        copyNonNull(form, entity);
        novelEventDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> updateCheat(NovelCheatUpdateForm form) {
        NovelCheatEntity entity = novelCheatDao.selectById(form.getCheatId());
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("金手指不存在");
        copyNonNull(form, entity);
        novelCheatDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> updateAlias(NovelAliasUpdateForm form) {
        NovelAliasEntity entity = novelAliasDao.selectById(form.getAliasId());
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("马甲不存在");
        copyNonNull(form, entity);
        novelAliasDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> updateNarrativeRule(NovelNarrativeRuleUpdateForm form) {
        NovelNarrativeRuleEntity entity = novelNarrativeRuleDao.selectById(form.getRuleId());
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("叙事规则不存在");
        copyNonNull(form, entity);
        novelNarrativeRuleDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    // ==================== 资产归档（软删除） ====================
    // 归档已检查 deletedFlag：防止误操作已删除的数据

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> archiveCharacter(Long characterId) {
        NovelCharacterEntity entity = novelCharacterDao.selectById(characterId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("角色不存在");
        entity.setDeletedFlag(true);
        novelCharacterDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> archiveLocation(Long locationId) {
        NovelLocationEntity entity = novelLocationDao.selectById(locationId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("地点不存在");
        entity.setDeletedFlag(true);
        novelLocationDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> archiveClue(Long clueId) {
        NovelClueEntity entity = novelClueDao.selectById(clueId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("线索不存在");
        entity.setDeletedFlag(true);
        novelClueDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> archiveVolume(Long volumeId) {
        NovelVolumeEntity entity = novelVolumeDao.selectById(volumeId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("卷不存在");
        entity.setDeletedFlag(true);
        novelVolumeDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> archiveItem(Long itemId) {
        NovelItemEntity entity = novelItemDao.selectById(itemId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("物品不存在");
        entity.setDeletedFlag(true);
        novelItemDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> archiveEvent(Long eventId) {
        NovelEventEntity entity = novelEventDao.selectById(eventId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("事件不存在");
        entity.setDeletedFlag(true);
        novelEventDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> archiveCheat(Long cheatId) {
        NovelCheatEntity entity = novelCheatDao.selectById(cheatId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("金手指不存在");
        entity.setDeletedFlag(true);
        novelCheatDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> archiveAlias(Long aliasId) {
        NovelAliasEntity entity = novelAliasDao.selectById(aliasId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("马甲不存在");
        entity.setDeletedFlag(true);
        novelAliasDao.updateById(entity);
        return ResponseDTO.ok(true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Boolean> archiveNarrativeRule(Long ruleId) {
        NovelNarrativeRuleEntity entity = novelNarrativeRuleDao.selectById(ruleId);
        if (entity == null || Boolean.TRUE.equals(entity.getDeletedFlag())) return ResponseDTO.userErrorParam("叙事规则不存在");
        entity.setDeletedFlag(true);
        novelNarrativeRuleDao.updateById(entity);
        return ResponseDTO.ok(true);
    }
}
