package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelFamilyTypeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphNodeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphRelationEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelItemStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelLoveStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelRelationTypeEnum;
import net.lab1024.sa.admin.module.business.novel.dao.NovelAliasDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterRelationDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCheatDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelClueDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelEventDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelItemDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelLocationDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelNarrativeRuleDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelProjectDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelVolumeDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelAliasEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterRelationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCheatEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelEventEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelItemEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelNarrativeRuleEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelVolumeEntity;
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
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.enumeration.BaseEnum;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 小说资产管理服务。
 * <p>
 * 第一阶段先承接角色和地点 CRUD；后续线索、物品、事件等资产继续在这个服务里扩展同一套项目归属和图谱同步规则。
 *
 * @Author AI-Novel
 */
@Slf4j
@Service
public class NovelAssetService {

    /**
     * 项目 DAO，所有资产写入前都要通过它确认项目归属。
     */
    @Resource
    private NovelProjectDao novelProjectDao;

    /**
     * 角色 DAO，负责角色表增删改查。
     */
    @Resource
    private NovelCharacterDao novelCharacterDao;

    /**
     * 地点 DAO，负责地点表增删改查。
     */
    @Resource
    private NovelLocationDao novelLocationDao;

    /**
     * 线索 DAO，负责线索表增删改查。
     */
    @Resource
    private NovelClueDao novelClueDao;

    /**
     * 物品 DAO，负责物品表增删改查。
     */
    @Resource
    private NovelItemDao novelItemDao;

    /**
     * 事件 DAO，负责事件表增删改查。
     */
    @Resource
    private NovelEventDao novelEventDao;

    /**
     * 金手指 DAO，负责金手指表增删改查。
     */
    @Resource
    private NovelCheatDao novelCheatDao;

    /**
     * 马甲 DAO，负责马甲表增删改查。
     */
    @Resource
    private NovelAliasDao novelAliasDao;

    /**
     * 叙事规则 DAO，负责纯 MySQL 的写作约束配置。
     */
    @Resource
    private NovelNarrativeRuleDao novelNarrativeRuleDao;

    /**
     * 卷 DAO，负责卷表增删改查。
     */
    @Resource
    private NovelVolumeDao novelVolumeDao;

    /**
     * 角色关系 DAO，只承接角色到角色的四类强语义关系。
     */
    @Resource
    private NovelCharacterRelationDao novelCharacterRelationDao;

    /**
     * 图谱服务，负责把资产主数据同步到 Neo4j。
     */
    @Resource
    private NovelGraphService novelGraphService;

    /**
     * 分页查询当前用户某个项目下的角色。
     *
     * @param queryForm 查询条件和分页参数
     * @param requestUserId 当前登录用户ID
     * @return 当前用户可见的角色分页结果
     */
    public ResponseDTO<PageResult<NovelCharacterVO>> queryCharacterByPage(NovelCharacterQueryForm queryForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(queryForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        LambdaQueryWrapper<NovelCharacterEntity> queryWrapper = buildCharacterPageWrapper(queryForm, requestUserId);
        Page<NovelCharacterEntity> page = convertCharacterPage(queryForm);
        novelCharacterDao.selectPage(page, queryWrapper);
        PageResult<NovelCharacterVO> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords(), NovelCharacterVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询当前用户可见的角色详情。
     *
     * @param characterId 角色ID
     * @param requestUserId 当前登录用户ID
     * @return 角色详情
     */
    public ResponseDTO<NovelCharacterVO> getCharacterDetail(Long characterId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        NovelCharacterEntity entity = getOwnedCharacter(characterId, requestUserId);
        if (Objects.isNull(entity)) {
            return ResponseDTO.userErrorParam("角色不存在或无权访问");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, NovelCharacterVO.class));
    }

    /**
     * 创建角色，并同步 Neo4j Character 节点。
     *
     * @param addForm 角色创建表单
     * @param requestUserId 当前登录用户ID
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createCharacter(NovelCharacterAddForm addForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(addForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        NovelCharacterEntity entity = SmartBeanUtil.copy(addForm, NovelCharacterEntity.class);
        applyCharacterCreateDefaults(entity, requestUserId);
        novelCharacterDao.insert(entity);
        // 角色节点只同步管理页设定和默认状态，动态字段后续由 GraphPatch 更新。
        novelGraphService.mergeCharacter(entity.getProjectId(), entity.getId(), buildCharacterGraphProps(entity));
        return ResponseDTO.ok();
    }

    /**
     * 编辑角色设定字段，并同步 Neo4j Character 节点。
     *
     * @param updateForm 角色编辑表单
     * @param requestUserId 当前登录用户ID
     * @return 编辑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateCharacter(NovelCharacterUpdateForm updateForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelCharacterEntity oldEntity = getOwnedCharacter(updateForm.getCharacterId(), requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("角色不存在或无权访问");
        }
        if (!Objects.equals(oldEntity.getProjectId(), updateForm.getProjectId())) {
            return ResponseDTO.userErrorParam("角色不能移动到其他项目");
        }

        NovelCharacterEntity updateEntity = SmartBeanUtil.copy(updateForm, NovelCharacterEntity.class);
        updateEntity.setId(updateForm.getCharacterId());
        updateEntity.setProjectId(oldEntity.getProjectId());
        updateEntity.setCreateUserId(requestUserId);
        updateEntity.setDeletedFlag(Boolean.FALSE);
        updateEntity.setCurrentStatus(oldEntity.getCurrentStatus());
        novelCharacterDao.updateById(updateEntity);
        novelGraphService.updateNodeProps(NovelGraphNodeEnum.Character, oldEntity.getProjectId(), oldEntity.getId(), buildCharacterGraphProps(updateEntity));
        return ResponseDTO.ok();
    }

    /**
     * 归档角色，并同步 Neo4j Character 节点归档标记。
     *
     * @param characterId 角色ID
     * @param requestUserId 当前登录用户ID
     * @return 归档结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> archiveCharacter(Long characterId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelCharacterEntity oldEntity = getOwnedCharacter(characterId, requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("角色不存在或无权访问");
        }

        NovelCharacterEntity archiveEntity = new NovelCharacterEntity();
        archiveEntity.setId(characterId);
        archiveEntity.setDeletedFlag(Boolean.TRUE);
        novelCharacterDao.updateById(archiveEntity);
        novelGraphService.archiveNode(NovelGraphNodeEnum.Character, oldEntity.getProjectId(), characterId);
        return ResponseDTO.ok();
    }

    /**
     * 分页查询当前用户某个项目下的地点。
     *
     * @param queryForm 查询条件和分页参数
     * @param requestUserId 当前登录用户ID
     * @return 当前用户可见的地点分页结果
     */
    public ResponseDTO<PageResult<NovelLocationVO>> queryLocationByPage(NovelLocationQueryForm queryForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(queryForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        LambdaQueryWrapper<NovelLocationEntity> queryWrapper = buildLocationPageWrapper(queryForm, requestUserId);
        Page<NovelLocationEntity> page = convertLocationPage(queryForm);
        novelLocationDao.selectPage(page, queryWrapper);
        PageResult<NovelLocationVO> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords(), NovelLocationVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询当前用户可见的地点详情。
     *
     * @param locationId 地点ID
     * @param requestUserId 当前登录用户ID
     * @return 地点详情
     */
    public ResponseDTO<NovelLocationVO> getLocationDetail(Long locationId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        NovelLocationEntity entity = getOwnedLocation(locationId, requestUserId);
        if (Objects.isNull(entity)) {
            return ResponseDTO.userErrorParam("地点不存在或无权访问");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, NovelLocationVO.class));
    }

    /**
     * 创建地点，并同步 Neo4j Location 节点。
     *
     * @param addForm 地点创建表单
     * @param requestUserId 当前登录用户ID
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createLocation(NovelLocationAddForm addForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(addForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        NovelLocationEntity entity = SmartBeanUtil.copy(addForm, NovelLocationEntity.class);
        applyLocationCreateDefaults(entity, requestUserId);
        novelLocationDao.insert(entity);
        novelGraphService.mergeLocation(entity.getProjectId(), entity.getId(), buildLocationGraphProps(entity));
        return ResponseDTO.ok();
    }

    /**
     * 编辑地点设定字段，并同步 Neo4j Location 节点。
     *
     * @param updateForm 地点编辑表单
     * @param requestUserId 当前登录用户ID
     * @return 编辑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateLocation(NovelLocationUpdateForm updateForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelLocationEntity oldEntity = getOwnedLocation(updateForm.getLocationId(), requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("地点不存在或无权访问");
        }
        if (!Objects.equals(oldEntity.getProjectId(), updateForm.getProjectId())) {
            return ResponseDTO.userErrorParam("地点不能移动到其他项目");
        }

        NovelLocationEntity updateEntity = SmartBeanUtil.copy(updateForm, NovelLocationEntity.class);
        updateEntity.setId(updateForm.getLocationId());
        updateEntity.setProjectId(oldEntity.getProjectId());
        updateEntity.setCreateUserId(requestUserId);
        updateEntity.setDeletedFlag(Boolean.FALSE);
        novelLocationDao.updateById(updateEntity);
        novelGraphService.updateNodeProps(NovelGraphNodeEnum.Location, oldEntity.getProjectId(), oldEntity.getId(), buildLocationGraphProps(updateEntity));
        return ResponseDTO.ok();
    }

    /**
     * 归档地点，并同步 Neo4j Location 节点归档标记。
     *
     * @param locationId 地点ID
     * @param requestUserId 当前登录用户ID
     * @return 归档结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> archiveLocation(Long locationId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelLocationEntity oldEntity = getOwnedLocation(locationId, requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("地点不存在或无权访问");
        }

        NovelLocationEntity archiveEntity = new NovelLocationEntity();
        archiveEntity.setId(locationId);
        archiveEntity.setDeletedFlag(Boolean.TRUE);
        novelLocationDao.updateById(archiveEntity);
        novelGraphService.archiveNode(NovelGraphNodeEnum.Location, oldEntity.getProjectId(), locationId);
        return ResponseDTO.ok();
    }

    /**
     * 分页查询当前用户某个项目下的线索。
     *
     * @param queryForm 查询条件和分页参数
     * @param requestUserId 当前登录用户ID
     * @return 当前用户可见的线索分页结果
     */
    public ResponseDTO<PageResult<NovelClueVO>> queryClueByPage(NovelClueQueryForm queryForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(queryForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        LambdaQueryWrapper<NovelClueEntity> queryWrapper = buildCluePageWrapper(queryForm, requestUserId);
        Page<NovelClueEntity> page = convertCluePage(queryForm);
        novelClueDao.selectPage(page, queryWrapper);
        PageResult<NovelClueVO> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords(), NovelClueVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询当前用户可见的线索详情。
     *
     * @param clueId 线索ID
     * @param requestUserId 当前登录用户ID
     * @return 线索详情
     */
    public ResponseDTO<NovelClueVO> getClueDetail(Long clueId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        NovelClueEntity entity = getOwnedClue(clueId, requestUserId);
        if (Objects.isNull(entity)) {
            return ResponseDTO.userErrorParam("线索不存在或无权访问");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, NovelClueVO.class));
    }

    /**
     * 创建线索，并同步 Neo4j Clue 节点。
     *
     * @param addForm 线索创建表单
     * @param requestUserId 当前登录用户ID
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createClue(NovelClueAddForm addForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(addForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        NovelClueEntity entity = SmartBeanUtil.copy(addForm, NovelClueEntity.class);
        applyClueCreateDefaults(entity, requestUserId);
        novelClueDao.insert(entity);
        // 线索节点创建时同步设定字段和默认生命周期，进展字段后续由 GraphPatch 推进。
        novelGraphService.mergeClue(entity.getProjectId(), entity.getId(), buildClueGraphProps(entity));
        return ResponseDTO.ok();
    }

    /**
     * 编辑线索设定字段，并同步 Neo4j Clue 节点。
     *
     * @param updateForm 线索编辑表单
     * @param requestUserId 当前登录用户ID
     * @return 编辑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateClue(NovelClueUpdateForm updateForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelClueEntity oldEntity = getOwnedClue(updateForm.getClueId(), requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("线索不存在或无权访问");
        }
        if (!Objects.equals(oldEntity.getProjectId(), updateForm.getProjectId())) {
            return ResponseDTO.userErrorParam("线索不能移动到其他项目");
        }

        NovelClueEntity updateEntity = SmartBeanUtil.copy(updateForm, NovelClueEntity.class);
        updateEntity.setId(updateForm.getClueId());
        updateEntity.setProjectId(oldEntity.getProjectId());
        updateEntity.setCreateUserId(requestUserId);
        updateEntity.setDeletedFlag(Boolean.FALSE);
        copyClueDynamicFields(oldEntity, updateEntity);
        novelClueDao.updateById(updateEntity);
        novelGraphService.updateNodeProps(NovelGraphNodeEnum.Clue, oldEntity.getProjectId(), oldEntity.getId(), buildClueGraphProps(updateEntity));
        return ResponseDTO.ok();
    }

    /**
     * 归档线索，并同步 Neo4j Clue 节点归档标记。
     *
     * @param clueId 线索ID
     * @param requestUserId 当前登录用户ID
     * @return 归档结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> archiveClue(Long clueId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelClueEntity oldEntity = getOwnedClue(clueId, requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("线索不存在或无权访问");
        }

        NovelClueEntity archiveEntity = new NovelClueEntity();
        archiveEntity.setId(clueId);
        archiveEntity.setDeletedFlag(Boolean.TRUE);
        novelClueDao.updateById(archiveEntity);
        novelGraphService.archiveNode(NovelGraphNodeEnum.Clue, oldEntity.getProjectId(), clueId);
        return ResponseDTO.ok();
    }

    /**
     * 分页查询当前用户某个项目下的物品。
     *
     * @param queryForm 查询条件和分页参数
     * @param requestUserId 当前登录用户ID
     * @return 当前用户可见的物品分页结果
     */
    public ResponseDTO<PageResult<NovelItemVO>> queryItemByPage(NovelItemQueryForm queryForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(queryForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        LambdaQueryWrapper<NovelItemEntity> queryWrapper = buildItemPageWrapper(queryForm, requestUserId);
        Page<NovelItemEntity> page = convertItemPage(queryForm);
        novelItemDao.selectPage(page, queryWrapper);
        PageResult<NovelItemVO> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords(), NovelItemVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询当前用户可见的物品详情。
     *
     * @param itemId 物品ID
     * @param requestUserId 当前登录用户ID
     * @return 物品详情
     */
    public ResponseDTO<NovelItemVO> getItemDetail(Long itemId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        NovelItemEntity entity = getOwnedItem(itemId, requestUserId);
        if (Objects.isNull(entity)) {
            return ResponseDTO.userErrorParam("物品不存在或无权访问");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, NovelItemVO.class));
    }

    /**
     * 创建物品，并同步 Neo4j Item 节点。
     *
     * @param addForm 物品创建表单
     * @param requestUserId 当前登录用户ID
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createItem(NovelItemAddForm addForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(addForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        NovelItemEntity entity = SmartBeanUtil.copy(addForm, NovelItemEntity.class);
        applyItemCreateDefaults(entity, requestUserId);
        novelItemDao.insert(entity);
        novelGraphService.mergeItem(entity.getProjectId(), entity.getId(), buildItemGraphProps(entity));
        return ResponseDTO.ok();
    }

    /**
     * 编辑物品设定字段，并同步 Neo4j Item 节点。
     *
     * @param updateForm 物品编辑表单
     * @param requestUserId 当前登录用户ID
     * @return 编辑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateItem(NovelItemUpdateForm updateForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelItemEntity oldEntity = getOwnedItem(updateForm.getItemId(), requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("物品不存在或无权访问");
        }
        if (!Objects.equals(oldEntity.getProjectId(), updateForm.getProjectId())) {
            return ResponseDTO.userErrorParam("物品不能移动到其他项目");
        }

        NovelItemEntity updateEntity = SmartBeanUtil.copy(updateForm, NovelItemEntity.class);
        updateEntity.setId(updateForm.getItemId());
        updateEntity.setProjectId(oldEntity.getProjectId());
        updateEntity.setCreateUserId(requestUserId);
        updateEntity.setDeletedFlag(Boolean.FALSE);
        updateEntity.setQuantity(oldEntity.getQuantity());
        updateEntity.setItemStatus(oldEntity.getItemStatus());
        novelItemDao.updateById(updateEntity);
        novelGraphService.updateNodeProps(NovelGraphNodeEnum.Item, oldEntity.getProjectId(), oldEntity.getId(), buildItemGraphProps(updateEntity));
        return ResponseDTO.ok();
    }

    /**
     * 归档物品，并同步 Neo4j Item 节点归档标记。
     *
     * @param itemId 物品ID
     * @param requestUserId 当前登录用户ID
     * @return 归档结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> archiveItem(Long itemId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelItemEntity oldEntity = getOwnedItem(itemId, requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("物品不存在或无权访问");
        }

        NovelItemEntity archiveEntity = new NovelItemEntity();
        archiveEntity.setId(itemId);
        archiveEntity.setDeletedFlag(Boolean.TRUE);
        novelItemDao.updateById(archiveEntity);
        novelGraphService.archiveNode(NovelGraphNodeEnum.Item, oldEntity.getProjectId(), itemId);
        return ResponseDTO.ok();
    }

    /**
     * 分页查询当前用户某个项目下的事件。
     *
     * @param queryForm 查询条件和分页参数
     * @param requestUserId 当前登录用户ID
     * @return 当前用户可见的事件分页结果
     */
    public ResponseDTO<PageResult<NovelEventVO>> queryEventByPage(NovelEventQueryForm queryForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(queryForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        LambdaQueryWrapper<NovelEventEntity> queryWrapper = buildEventPageWrapper(queryForm, requestUserId);
        Page<NovelEventEntity> page = convertEventPage(queryForm);
        novelEventDao.selectPage(page, queryWrapper);
        PageResult<NovelEventVO> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords(), NovelEventVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询当前用户可见的事件详情。
     *
     * @param eventId 事件ID
     * @param requestUserId 当前登录用户ID
     * @return 事件详情
     */
    public ResponseDTO<NovelEventVO> getEventDetail(Long eventId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        NovelEventEntity entity = getOwnedEvent(eventId, requestUserId);
        if (Objects.isNull(entity)) {
            return ResponseDTO.userErrorParam("事件不存在或无权访问");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, NovelEventVO.class));
    }

    /**
     * 创建事件，并同步 Neo4j Event 节点。
     *
     * @param addForm 事件创建表单
     * @param requestUserId 当前登录用户ID
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createEvent(NovelEventAddForm addForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(addForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        NovelEventEntity entity = SmartBeanUtil.copy(addForm, NovelEventEntity.class);
        applyEventCreateDefaults(entity, requestUserId);
        novelEventDao.insert(entity);
        novelGraphService.mergeEvent(entity.getProjectId(), entity.getId(), buildEventGraphProps(entity));
        return ResponseDTO.ok();
    }

    /**
     * 编辑事件字段，并同步 Neo4j Event 节点。
     *
     * @param updateForm 事件编辑表单
     * @param requestUserId 当前登录用户ID
     * @return 编辑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateEvent(NovelEventUpdateForm updateForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelEventEntity oldEntity = getOwnedEvent(updateForm.getEventId(), requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("事件不存在或无权访问");
        }
        if (!Objects.equals(oldEntity.getProjectId(), updateForm.getProjectId())) {
            return ResponseDTO.userErrorParam("事件不能移动到其他项目");
        }

        NovelEventEntity updateEntity = SmartBeanUtil.copy(updateForm, NovelEventEntity.class);
        updateEntity.setId(updateForm.getEventId());
        updateEntity.setProjectId(oldEntity.getProjectId());
        updateEntity.setCreateUserId(requestUserId);
        updateEntity.setDeletedFlag(Boolean.FALSE);
        novelEventDao.updateById(updateEntity);
        novelGraphService.updateNodeProps(NovelGraphNodeEnum.Event, oldEntity.getProjectId(), oldEntity.getId(), buildEventGraphProps(updateEntity));
        return ResponseDTO.ok();
    }

    /**
     * 归档事件，并同步 Neo4j Event 节点归档标记。
     *
     * @param eventId 事件ID
     * @param requestUserId 当前登录用户ID
     * @return 归档结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> archiveEvent(Long eventId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelEventEntity oldEntity = getOwnedEvent(eventId, requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("事件不存在或无权访问");
        }

        NovelEventEntity archiveEntity = new NovelEventEntity();
        archiveEntity.setId(eventId);
        archiveEntity.setDeletedFlag(Boolean.TRUE);
        novelEventDao.updateById(archiveEntity);
        novelGraphService.archiveNode(NovelGraphNodeEnum.Event, oldEntity.getProjectId(), eventId);
        return ResponseDTO.ok();
    }

    /**
     * 校验当前请求用户是否存在。
     *
     * @param requestUserId 当前登录用户ID
     * @return 校验结果
     */
    /**
     * 分页查询当前用户某个项目下的金手指。
     *
     * @param queryForm 查询条件和分页参数
     * @param requestUserId 当前登录用户ID
     * @return 当前用户可见的金手指分页结果
     */
    public ResponseDTO<PageResult<NovelCheatVO>> queryCheatByPage(NovelCheatQueryForm queryForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(queryForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        LambdaQueryWrapper<NovelCheatEntity> queryWrapper = buildCheatPageWrapper(queryForm, requestUserId);
        Page<NovelCheatEntity> page = convertCheatPage(queryForm);
        novelCheatDao.selectPage(page, queryWrapper);
        PageResult<NovelCheatVO> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords(), NovelCheatVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询当前用户可见的金手指详情。
     *
     * @param cheatId 金手指ID
     * @param requestUserId 当前登录用户ID
     * @return 金手指详情
     */
    public ResponseDTO<NovelCheatVO> getCheatDetail(Long cheatId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        NovelCheatEntity entity = getOwnedCheat(cheatId, requestUserId);
        if (Objects.isNull(entity)) {
            return ResponseDTO.userErrorParam("金手指不存在或无权访问");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, NovelCheatVO.class));
    }

    /**
     * 创建金手指，并同步 Neo4j Cheat 节点。
     *
     * @param addForm 金手指创建表单
     * @param requestUserId 当前登录用户ID
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createCheat(NovelCheatAddForm addForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(addForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        NovelCheatEntity entity = SmartBeanUtil.copy(addForm, NovelCheatEntity.class);
        applyCheatCreateDefaults(entity, requestUserId);
        novelCheatDao.insert(entity);
        // 金手指节点带上默认副作用阶段，后续阶段变化只由 GraphPatch 审阅流程推进。
        novelGraphService.mergeCheat(entity.getProjectId(), entity.getId(), buildCheatGraphProps(entity));
        return ResponseDTO.ok();
    }

    /**
     * 编辑金手指设定字段，并同步 Neo4j Cheat 节点。
     *
     * @param updateForm 金手指编辑表单
     * @param requestUserId 当前登录用户ID
     * @return 编辑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateCheat(NovelCheatUpdateForm updateForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelCheatEntity oldEntity = getOwnedCheat(updateForm.getCheatId(), requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("金手指不存在或无权访问");
        }
        if (!Objects.equals(oldEntity.getProjectId(), updateForm.getProjectId())) {
            return ResponseDTO.userErrorParam("金手指不能移动到其他项目");
        }

        NovelCheatEntity updateEntity = SmartBeanUtil.copy(updateForm, NovelCheatEntity.class);
        updateEntity.setId(updateForm.getCheatId());
        updateEntity.setProjectId(oldEntity.getProjectId());
        updateEntity.setCreateUserId(requestUserId);
        updateEntity.setDeletedFlag(Boolean.FALSE);
        updateEntity.setCurrentStage(oldEntity.getCurrentStage());
        novelCheatDao.updateById(updateEntity);
        novelGraphService.updateNodeProps(NovelGraphNodeEnum.Cheat, oldEntity.getProjectId(), oldEntity.getId(), buildCheatGraphProps(updateEntity));
        return ResponseDTO.ok();
    }

    /**
     * 归档金手指，并同步 Neo4j Cheat 节点归档标记。
     *
     * @param cheatId 金手指ID
     * @param requestUserId 当前登录用户ID
     * @return 归档结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> archiveCheat(Long cheatId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelCheatEntity oldEntity = getOwnedCheat(cheatId, requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("金手指不存在或无权访问");
        }

        NovelCheatEntity archiveEntity = new NovelCheatEntity();
        archiveEntity.setId(cheatId);
        archiveEntity.setDeletedFlag(Boolean.TRUE);
        novelCheatDao.updateById(archiveEntity);
        novelGraphService.archiveNode(NovelGraphNodeEnum.Cheat, oldEntity.getProjectId(), cheatId);
        return ResponseDTO.ok();
    }

    /**
     * 分页查询当前用户某个项目下的马甲。
     *
     * @param queryForm 查询条件和分页参数
     * @param requestUserId 当前登录用户ID
     * @return 当前用户可见的马甲分页结果
     */
    public ResponseDTO<PageResult<NovelAliasVO>> queryAliasByPage(NovelAliasQueryForm queryForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(queryForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        LambdaQueryWrapper<NovelAliasEntity> queryWrapper = buildAliasPageWrapper(queryForm, requestUserId);
        Page<NovelAliasEntity> page = convertAliasPage(queryForm);
        novelAliasDao.selectPage(page, queryWrapper);
        PageResult<NovelAliasVO> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords(), NovelAliasVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询当前用户可见的马甲详情。
     *
     * @param aliasId 马甲ID
     * @param requestUserId 当前登录用户ID
     * @return 马甲详情
     */
    public ResponseDTO<NovelAliasVO> getAliasDetail(Long aliasId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        NovelAliasEntity entity = getOwnedAlias(aliasId, requestUserId);
        if (Objects.isNull(entity)) {
            return ResponseDTO.userErrorParam("马甲不存在或无权访问");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, NovelAliasVO.class));
    }

    /**
     * 创建马甲，并同步 Neo4j Alias 节点。
     *
     * @param addForm 马甲创建表单
     * @param requestUserId 当前登录用户ID
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createAlias(NovelAliasAddForm addForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(addForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        NovelAliasEntity entity = SmartBeanUtil.copy(addForm, NovelAliasEntity.class);
        applyAliasCreateDefaults(entity, requestUserId);
        novelAliasDao.insert(entity);
        // 马甲创建时默认未识破；暴露事实后续由写作流程确认，避免管理页随手改坏剧情关键点。
        novelGraphService.mergeAlias(entity.getProjectId(), entity.getId(), buildAliasGraphProps(entity));
        return ResponseDTO.ok();
    }

    /**
     * 编辑马甲设定字段，并同步 Neo4j Alias 节点。
     *
     * @param updateForm 马甲编辑表单
     * @param requestUserId 当前登录用户ID
     * @return 编辑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateAlias(NovelAliasUpdateForm updateForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelAliasEntity oldEntity = getOwnedAlias(updateForm.getAliasId(), requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("马甲不存在或无权访问");
        }
        if (!Objects.equals(oldEntity.getProjectId(), updateForm.getProjectId())) {
            return ResponseDTO.userErrorParam("马甲不能移动到其他项目");
        }

        NovelAliasEntity updateEntity = SmartBeanUtil.copy(updateForm, NovelAliasEntity.class);
        updateEntity.setId(updateForm.getAliasId());
        updateEntity.setProjectId(oldEntity.getProjectId());
        updateEntity.setCreateUserId(requestUserId);
        updateEntity.setDeletedFlag(Boolean.FALSE);
        updateEntity.setRevealed(oldEntity.getRevealed());
        updateEntity.setRevealedTo(oldEntity.getRevealedTo());
        novelAliasDao.updateById(updateEntity);
        novelGraphService.updateNodeProps(NovelGraphNodeEnum.Alias, oldEntity.getProjectId(), oldEntity.getId(), buildAliasGraphProps(updateEntity));
        return ResponseDTO.ok();
    }

    /**
     * 归档马甲，并同步 Neo4j Alias 节点归档标记。
     *
     * @param aliasId 马甲ID
     * @param requestUserId 当前登录用户ID
     * @return 归档结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> archiveAlias(Long aliasId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelAliasEntity oldEntity = getOwnedAlias(aliasId, requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("马甲不存在或无权访问");
        }

        NovelAliasEntity archiveEntity = new NovelAliasEntity();
        archiveEntity.setId(aliasId);
        archiveEntity.setDeletedFlag(Boolean.TRUE);
        novelAliasDao.updateById(archiveEntity);
        novelGraphService.archiveNode(NovelGraphNodeEnum.Alias, oldEntity.getProjectId(), aliasId);
        return ResponseDTO.ok();
    }

    /**
     * 分页查询当前用户某个项目下的叙事规则。
     *
     * @param queryForm 查询条件和分页参数
     * @param requestUserId 当前登录用户ID
     * @return 当前用户可见的叙事规则分页结果
     */
    public ResponseDTO<PageResult<NovelNarrativeRuleVO>> queryNarrativeRuleByPage(NovelNarrativeRuleQueryForm queryForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(queryForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        LambdaQueryWrapper<NovelNarrativeRuleEntity> queryWrapper = buildNarrativeRulePageWrapper(queryForm, requestUserId);
        Page<NovelNarrativeRuleEntity> page = convertNarrativeRulePage(queryForm);
        novelNarrativeRuleDao.selectPage(page, queryWrapper);
        PageResult<NovelNarrativeRuleVO> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords(), NovelNarrativeRuleVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询当前用户可见的叙事规则详情。
     *
     * @param ruleId 叙事规则ID
     * @param requestUserId 当前登录用户ID
     * @return 叙事规则详情
     */
    public ResponseDTO<NovelNarrativeRuleVO> getNarrativeRuleDetail(Long ruleId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        NovelNarrativeRuleEntity entity = getOwnedNarrativeRule(ruleId, requestUserId);
        if (Objects.isNull(entity)) {
            return ResponseDTO.userErrorParam("叙事规则不存在或无权访问");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, NovelNarrativeRuleVO.class));
    }

    /**
     * 创建叙事规则。
     *
     * @param addForm 叙事规则创建表单
     * @param requestUserId 当前登录用户ID
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createNarrativeRule(NovelNarrativeRuleAddForm addForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(addForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        NovelNarrativeRuleEntity entity = SmartBeanUtil.copy(addForm, NovelNarrativeRuleEntity.class);
        applyNarrativeRuleCreateDefaults(entity, requestUserId);
        novelNarrativeRuleDao.insert(entity);
        // 叙事规则只参与 Prompt 组装，阶段一明确不进入 Neo4j。
        return ResponseDTO.ok();
    }

    /**
     * 编辑叙事规则。
     *
     * @param updateForm 叙事规则编辑表单
     * @param requestUserId 当前登录用户ID
     * @return 编辑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateNarrativeRule(NovelNarrativeRuleUpdateForm updateForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelNarrativeRuleEntity oldEntity = getOwnedNarrativeRule(updateForm.getRuleId(), requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("叙事规则不存在或无权访问");
        }
        if (!Objects.equals(oldEntity.getProjectId(), updateForm.getProjectId())) {
            return ResponseDTO.userErrorParam("叙事规则不能移动到其他项目");
        }

        NovelNarrativeRuleEntity updateEntity = SmartBeanUtil.copy(updateForm, NovelNarrativeRuleEntity.class);
        updateEntity.setId(updateForm.getRuleId());
        updateEntity.setProjectId(oldEntity.getProjectId());
        updateEntity.setCreateUserId(requestUserId);
        updateEntity.setDeletedFlag(Boolean.FALSE);
        novelNarrativeRuleDao.updateById(updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 归档叙事规则。
     *
     * @param ruleId 叙事规则ID
     * @param requestUserId 当前登录用户ID
     * @return 归档结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> archiveNarrativeRule(Long ruleId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelNarrativeRuleEntity oldEntity = getOwnedNarrativeRule(ruleId, requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("叙事规则不存在或无权访问");
        }

        NovelNarrativeRuleEntity archiveEntity = new NovelNarrativeRuleEntity();
        archiveEntity.setId(ruleId);
        archiveEntity.setDeletedFlag(Boolean.TRUE);
        novelNarrativeRuleDao.updateById(archiveEntity);
        return ResponseDTO.ok();
    }

    /**
     * 分页查询当前用户某个项目下的卷。
     *
     * @param queryForm 查询条件和分页参数
     * @param requestUserId 当前登录用户ID
     * @return 当前用户可见的卷分页结果
     */
    public ResponseDTO<PageResult<NovelVolumeVO>> queryVolumeByPage(NovelVolumeQueryForm queryForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(queryForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        LambdaQueryWrapper<NovelVolumeEntity> queryWrapper = buildVolumePageWrapper(queryForm, requestUserId);
        Page<NovelVolumeEntity> page = convertVolumePage(queryForm);
        novelVolumeDao.selectPage(page, queryWrapper);
        PageResult<NovelVolumeVO> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords(), NovelVolumeVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询当前用户可见的卷详情。
     *
     * @param volumeId 卷ID
     * @param requestUserId 当前登录用户ID
     * @return 卷详情
     */
    public ResponseDTO<NovelVolumeVO> getVolumeDetail(Long volumeId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        NovelVolumeEntity entity = getOwnedVolume(volumeId, requestUserId);
        if (Objects.isNull(entity)) {
            return ResponseDTO.userErrorParam("卷不存在或无权访问");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, NovelVolumeVO.class));
    }

    /**
     * 创建卷，并同步 Neo4j Volume 节点和项目包含关系。
     *
     * @param addForm 卷创建表单
     * @param requestUserId 当前登录用户ID
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createVolume(NovelVolumeAddForm addForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(addForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        NovelVolumeEntity entity = SmartBeanUtil.copy(addForm, NovelVolumeEntity.class);
        applyVolumeCreateDefaults(entity, requestUserId);
        novelVolumeDao.insert(entity);
        novelGraphService.mergeVolume(entity.getProjectId(), entity.getId(), buildVolumeGraphProps(entity));
        // 卷是项目结构层的一部分，创建后补上 Project -> Volume 的包含关系，方便后续按卷检索。
        novelGraphService.mergeRelation(NovelGraphRelationEnum.CONTAINS, entity.getProjectId(), NovelGraphNodeEnum.Project, entity.getProjectId(),
                NovelGraphNodeEnum.Volume, entity.getId(), Collections.emptyMap());
        return ResponseDTO.ok();
    }

    /**
     * 编辑卷设定字段，并同步 Neo4j Volume 节点。
     *
     * @param updateForm 卷编辑表单
     * @param requestUserId 当前登录用户ID
     * @return 编辑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateVolume(NovelVolumeUpdateForm updateForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelVolumeEntity oldEntity = getOwnedVolume(updateForm.getVolumeId(), requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("卷不存在或无权访问");
        }
        if (!Objects.equals(oldEntity.getProjectId(), updateForm.getProjectId())) {
            return ResponseDTO.userErrorParam("卷不能移动到其他项目");
        }

        NovelVolumeEntity updateEntity = SmartBeanUtil.copy(updateForm, NovelVolumeEntity.class);
        updateEntity.setId(updateForm.getVolumeId());
        updateEntity.setProjectId(oldEntity.getProjectId());
        updateEntity.setCreateUserId(requestUserId);
        updateEntity.setDeletedFlag(Boolean.FALSE);
        novelVolumeDao.updateById(updateEntity);
        novelGraphService.updateNodeProps(NovelGraphNodeEnum.Volume, oldEntity.getProjectId(), oldEntity.getId(), buildVolumeGraphProps(updateEntity));
        return ResponseDTO.ok();
    }

    /**
     * 归档卷，并同步 Neo4j Volume 节点归档标记。
     *
     * @param volumeId 卷ID
     * @param requestUserId 当前登录用户ID
     * @return 归档结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> archiveVolume(Long volumeId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelVolumeEntity oldEntity = getOwnedVolume(volumeId, requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("卷不存在或无权访问");
        }

        NovelVolumeEntity archiveEntity = new NovelVolumeEntity();
        archiveEntity.setId(volumeId);
        archiveEntity.setDeletedFlag(Boolean.TRUE);
        novelVolumeDao.updateById(archiveEntity);
        novelGraphService.archiveNode(NovelGraphNodeEnum.Volume, oldEntity.getProjectId(), volumeId);
        return ResponseDTO.ok();
    }

    /**
     * 分页查询当前用户某个项目下的角色关系。
     * <p>
     * 角色关系是叙事图谱的边数据，这里只查询 MySQL 主表；Neo4j 负责检索和推理，不作为管理页分页源。
     *
     * @param queryForm 查询条件和分页参数
     * @param requestUserId 当前登录用户ID
     * @return 当前用户可见的角色关系分页结果
     */
    public ResponseDTO<PageResult<NovelCharacterRelationVO>> queryCharacterRelationByPage(NovelCharacterRelationQueryForm queryForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(queryForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        LambdaQueryWrapper<NovelCharacterRelationEntity> queryWrapper = buildCharacterRelationPageWrapper(queryForm, requestUserId);
        Page<NovelCharacterRelationEntity> page = convertCharacterRelationPage(queryForm);
        novelCharacterRelationDao.selectPage(page, queryWrapper);
        PageResult<NovelCharacterRelationVO> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords(), NovelCharacterRelationVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询当前用户可见的角色关系详情。
     *
     * @param relationId 角色关系ID
     * @param requestUserId 当前登录用户ID
     * @return 角色关系详情
     */
    public ResponseDTO<NovelCharacterRelationVO> getCharacterRelationDetail(Long relationId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        NovelCharacterRelationEntity entity = getOwnedCharacterRelation(relationId, requestUserId);
        if (Objects.isNull(entity)) {
            return ResponseDTO.userErrorParam("角色关系不存在或无权访问");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, NovelCharacterRelationVO.class));
    }

    /**
     * 创建角色关系，并同步 Neo4j 中 Character -> Character 的关系边。
     *
     * @param addForm 角色关系创建表单
     * @param requestUserId 当前登录用户ID
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createCharacterRelation(NovelCharacterRelationAddForm addForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(addForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        NovelGraphRelationEnum graphRelationType = resolveCharacterRelationType(addForm.getRelationType());
        ResponseDTO<String> relationValidateResult = validateCharacterRelationPayload(addForm, graphRelationType, requestUserId);
        if (!relationValidateResult.getOk()) {
            return relationValidateResult;
        }

        NovelCharacterRelationEntity entity = SmartBeanUtil.copy(addForm, NovelCharacterRelationEntity.class);
        applyCharacterRelationCreateDefaults(entity, requestUserId);
        normalizeCharacterRelationFields(entity, graphRelationType);
        novelCharacterRelationDao.insert(entity);
        syncCharacterRelationToGraph(entity, graphRelationType);
        return ResponseDTO.ok();
    }

    /**
     * 编辑角色关系，并同步 Neo4j 关系边。
     * <p>
     * 如果关系大类或起止角色变化，先删除旧边再合并新边；只改属性时直接合并即可覆盖属性。
     *
     * @param updateForm 角色关系编辑表单
     * @param requestUserId 当前登录用户ID
     * @return 编辑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateCharacterRelation(NovelCharacterRelationUpdateForm updateForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelCharacterRelationEntity oldEntity = getOwnedCharacterRelation(updateForm.getRelationId(), requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("角色关系不存在或无权访问");
        }
        if (!Objects.equals(oldEntity.getProjectId(), updateForm.getProjectId())) {
            return ResponseDTO.userErrorParam("角色关系不能移动到其他项目");
        }

        NovelGraphRelationEnum graphRelationType = resolveCharacterRelationType(updateForm.getRelationType());
        ResponseDTO<String> relationValidateResult = validateCharacterRelationPayload(updateForm, graphRelationType, requestUserId);
        if (!relationValidateResult.getOk()) {
            return relationValidateResult;
        }

        NovelCharacterRelationEntity updateEntity = SmartBeanUtil.copy(updateForm, NovelCharacterRelationEntity.class);
        updateEntity.setId(updateForm.getRelationId());
        updateEntity.setProjectId(oldEntity.getProjectId());
        updateEntity.setCreateUserId(requestUserId);
        updateEntity.setDeletedFlag(Boolean.FALSE);
        normalizeCharacterRelationFields(updateEntity, graphRelationType);
        novelCharacterRelationDao.updateById(updateEntity);

        if (isCharacterRelationEndpointChanged(oldEntity, updateEntity)) {
            NovelGraphRelationEnum oldGraphRelationType = resolveCharacterRelationType(oldEntity.getRelationType());
            deleteCharacterRelationFromGraph(oldEntity, oldGraphRelationType);
        }
        syncCharacterRelationToGraph(updateEntity, graphRelationType);
        return ResponseDTO.ok();
    }

    /**
     * 归档角色关系，并删除 Neo4j 中对应的关系边。
     *
     * @param relationId 角色关系ID
     * @param requestUserId 当前登录用户ID
     * @return 归档结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> archiveCharacterRelation(Long relationId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelCharacterRelationEntity oldEntity = getOwnedCharacterRelation(relationId, requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("角色关系不存在或无权访问");
        }

        NovelCharacterRelationEntity archiveEntity = new NovelCharacterRelationEntity();
        archiveEntity.setId(relationId);
        archiveEntity.setDeletedFlag(Boolean.TRUE);
        novelCharacterRelationDao.updateById(archiveEntity);
        deleteCharacterRelationFromGraph(oldEntity, resolveCharacterRelationType(oldEntity.getRelationType()));
        return ResponseDTO.ok();
    }

    /**
     * 校验当前请求是否携带登录用户ID。
     *
     * @param requestUserId 当前登录用户ID
     * @return 校验结果
     */
    private ResponseDTO<String> validateRequestUser(Long requestUserId) {
        if (Objects.isNull(requestUserId)) {
            return ResponseDTO.userErrorParam("未获取到当前登录用户");
        }
        return ResponseDTO.ok();
    }

    /**
     * 校验项目是否归属于当前用户。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前登录用户ID
     * @return 项目实体；校验失败时返回错误结果
     */
    private ResponseDTO<NovelProjectEntity> validateOwnedProject(Long projectId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        NovelProjectEntity projectEntity = novelProjectDao.selectOne(new LambdaQueryWrapper<NovelProjectEntity>()
                .eq(NovelProjectEntity::getId, projectId)
                .eq(NovelProjectEntity::getCreateUserId, requestUserId)
                .eq(NovelProjectEntity::getDeletedFlag, Boolean.FALSE));
        if (Objects.isNull(projectEntity)) {
            return ResponseDTO.userErrorParam("项目不存在或无权访问");
        }
        return ResponseDTO.ok(projectEntity);
    }

    /**
     * 创建角色时填充服务端维护字段。
     *
     * @param entity 待写入数据库的角色实体
     * @param requestUserId 当前登录用户ID
     */
    private void applyCharacterCreateDefaults(NovelCharacterEntity entity, Long requestUserId) {
        entity.setCreateUserId(requestUserId);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCurrentStatus(NovelCharacterStatusEnum.ACTIVE.getValue());
    }

    /**
     * 创建地点时填充服务端维护字段。
     *
     * @param entity 待写入数据库的地点实体
     * @param requestUserId 当前登录用户ID
     */
    private void applyLocationCreateDefaults(NovelLocationEntity entity, Long requestUserId) {
        entity.setCreateUserId(requestUserId);
        entity.setDeletedFlag(Boolean.FALSE);
    }

    /**
     * 创建线索时填充服务端维护字段。
     *
     * @param entity 待写入数据库的线索实体
     * @param requestUserId 当前登录用户ID
     */
    private void applyClueCreateDefaults(NovelClueEntity entity, Long requestUserId) {
        entity.setCreateUserId(requestUserId);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setClueStatus(NovelClueStatusEnum.DORMANT.getValue());
        entity.setRevealLevel(BigDecimal.ZERO);
        if (Objects.isNull(entity.getPriority())) {
            entity.setPriority(3);
        }
    }

    /**
     * 创建物品时填充服务端维护字段。
     *
     * @param entity 待写入数据库的物品实体
     * @param requestUserId 当前登录用户ID
     */
    private void applyItemCreateDefaults(NovelItemEntity entity, Long requestUserId) {
        entity.setCreateUserId(requestUserId);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setItemStatus(NovelItemStatusEnum.INTACT.getValue());
    }

    /**
     * 创建事件时填充服务端维护字段。
     *
     * @param entity 待写入数据库的事件实体
     * @param requestUserId 当前登录用户ID
     */
    private void applyEventCreateDefaults(NovelEventEntity entity, Long requestUserId) {
        entity.setCreateUserId(requestUserId);
        entity.setDeletedFlag(Boolean.FALSE);
    }

    /**
     * 创建金手指时填充服务端维护字段。
     *
     * @param entity 待写入数据库的金手指实体
     * @param requestUserId 当前登录用户ID
     */
    private void applyCheatCreateDefaults(NovelCheatEntity entity, Long requestUserId) {
        entity.setCreateUserId(requestUserId);
        entity.setDeletedFlag(Boolean.FALSE);
        if (StringUtils.isBlank(entity.getCurrentStage())) {
            entity.setCurrentStage("无副作用");
        }
    }

    /**
     * 创建马甲时填充服务端维护字段。
     *
     * @param entity 待写入数据库的马甲实体
     * @param requestUserId 当前登录用户ID
     */
    private void applyAliasCreateDefaults(NovelAliasEntity entity, Long requestUserId) {
        entity.setCreateUserId(requestUserId);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setRevealed(Boolean.FALSE);
    }

    /**
     * 创建叙事规则时填充服务端维护字段。
     *
     * @param entity 待写入数据库的叙事规则实体
     * @param requestUserId 当前登录用户ID
     */
    private void applyNarrativeRuleCreateDefaults(NovelNarrativeRuleEntity entity, Long requestUserId) {
        entity.setCreateUserId(requestUserId);
        entity.setDeletedFlag(Boolean.FALSE);
    }

    /**
     * 创建卷时填充服务端维护字段。
     *
     * @param entity 待写入数据库的卷实体
     * @param requestUserId 当前登录用户ID
     */
    private void applyVolumeCreateDefaults(NovelVolumeEntity entity, Long requestUserId) {
        entity.setCreateUserId(requestUserId);
        entity.setDeletedFlag(Boolean.FALSE);
    }

    /**
     * 创建角色关系时填充服务端维护字段。
     *
     * @param entity 待写入数据库的角色关系实体
     * @param requestUserId 当前登录用户ID
     */
    private void applyCharacterRelationCreateDefaults(NovelCharacterRelationEntity entity, Long requestUserId) {
        entity.setCreateUserId(requestUserId);
        entity.setDeletedFlag(Boolean.FALSE);
    }

    /**
     * 编辑线索时保留写作流程维护的动态字段。
     *
     * @param oldEntity 数据库中的原线索实体
     * @param updateEntity 待更新的线索实体
     */
    private void copyClueDynamicFields(NovelClueEntity oldEntity, NovelClueEntity updateEntity) {
        updateEntity.setSummary(oldEntity.getSummary());
        updateEntity.setRevealLevel(oldEntity.getRevealLevel());
        updateEntity.setCurrentStage(oldEntity.getCurrentStage());
        updateEntity.setClueStatus(oldEntity.getClueStatus());
        updateEntity.setLastAlertedChapter(oldEntity.getLastAlertedChapter());
    }

    /**
     * 查询当前用户未归档的角色。
     *
     * @param characterId 角色ID
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档角色，查不到返回 null
     */
    private NovelCharacterEntity getOwnedCharacter(Long characterId, Long requestUserId) {
        return novelCharacterDao.selectOne(new LambdaQueryWrapper<NovelCharacterEntity>()
                .eq(NovelCharacterEntity::getId, characterId)
                .eq(NovelCharacterEntity::getCreateUserId, requestUserId)
                .eq(NovelCharacterEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 查询当前用户未归档的地点。
     *
     * @param locationId 地点ID
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档地点，查不到返回 null
     */
    private NovelLocationEntity getOwnedLocation(Long locationId, Long requestUserId) {
        return novelLocationDao.selectOne(new LambdaQueryWrapper<NovelLocationEntity>()
                .eq(NovelLocationEntity::getId, locationId)
                .eq(NovelLocationEntity::getCreateUserId, requestUserId)
                .eq(NovelLocationEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 查询当前用户未归档的线索。
     *
     * @param clueId 线索ID
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档线索，查不到返回 null
     */
    private NovelClueEntity getOwnedClue(Long clueId, Long requestUserId) {
        return novelClueDao.selectOne(new LambdaQueryWrapper<NovelClueEntity>()
                .eq(NovelClueEntity::getId, clueId)
                .eq(NovelClueEntity::getCreateUserId, requestUserId)
                .eq(NovelClueEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 查询当前用户未归档的物品。
     *
     * @param itemId 物品ID
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档物品，查不到返回 null
     */
    private NovelItemEntity getOwnedItem(Long itemId, Long requestUserId) {
        return novelItemDao.selectOne(new LambdaQueryWrapper<NovelItemEntity>()
                .eq(NovelItemEntity::getId, itemId)
                .eq(NovelItemEntity::getCreateUserId, requestUserId)
                .eq(NovelItemEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 查询当前用户未归档的事件。
     *
     * @param eventId 事件ID
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档事件，查不到返回 null
     */
    private NovelEventEntity getOwnedEvent(Long eventId, Long requestUserId) {
        return novelEventDao.selectOne(new LambdaQueryWrapper<NovelEventEntity>()
                .eq(NovelEventEntity::getId, eventId)
                .eq(NovelEventEntity::getCreateUserId, requestUserId)
                .eq(NovelEventEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 查询当前用户未归档的金手指。
     *
     * @param cheatId 金手指ID
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档金手指，查不到返回 null
     */
    private NovelCheatEntity getOwnedCheat(Long cheatId, Long requestUserId) {
        return novelCheatDao.selectOne(new LambdaQueryWrapper<NovelCheatEntity>()
                .eq(NovelCheatEntity::getId, cheatId)
                .eq(NovelCheatEntity::getCreateUserId, requestUserId)
                .eq(NovelCheatEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 查询当前用户未归档的马甲。
     *
     * @param aliasId 马甲ID
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档马甲，查不到返回 null
     */
    private NovelAliasEntity getOwnedAlias(Long aliasId, Long requestUserId) {
        return novelAliasDao.selectOne(new LambdaQueryWrapper<NovelAliasEntity>()
                .eq(NovelAliasEntity::getId, aliasId)
                .eq(NovelAliasEntity::getCreateUserId, requestUserId)
                .eq(NovelAliasEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 查询当前用户未归档的叙事规则。
     *
     * @param ruleId 叙事规则ID
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档叙事规则，查不到返回 null
     */
    private NovelNarrativeRuleEntity getOwnedNarrativeRule(Long ruleId, Long requestUserId) {
        return novelNarrativeRuleDao.selectOne(new LambdaQueryWrapper<NovelNarrativeRuleEntity>()
                .eq(NovelNarrativeRuleEntity::getId, ruleId)
                .eq(NovelNarrativeRuleEntity::getCreateUserId, requestUserId)
                .eq(NovelNarrativeRuleEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 查询当前用户未归档的卷。
     *
     * @param volumeId 卷ID
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档卷，查不到返回 null
     */
    private NovelVolumeEntity getOwnedVolume(Long volumeId, Long requestUserId) {
        return novelVolumeDao.selectOne(new LambdaQueryWrapper<NovelVolumeEntity>()
                .eq(NovelVolumeEntity::getId, volumeId)
                .eq(NovelVolumeEntity::getCreateUserId, requestUserId)
                .eq(NovelVolumeEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 查询当前用户未归档的角色关系。
     *
     * @param relationId 角色关系ID
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档角色关系，查不到返回 null
     */
    private NovelCharacterRelationEntity getOwnedCharacterRelation(Long relationId, Long requestUserId) {
        return novelCharacterRelationDao.selectOne(new LambdaQueryWrapper<NovelCharacterRelationEntity>()
                .eq(NovelCharacterRelationEntity::getId, relationId)
                .eq(NovelCharacterRelationEntity::getCreateUserId, requestUserId)
                .eq(NovelCharacterRelationEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 构建角色分页查询条件，用户隔离条件在这里强制追加。
     *
     * @param queryForm 查询表单
     * @param requestUserId 当前登录用户ID
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<NovelCharacterEntity> buildCharacterPageWrapper(NovelCharacterQueryForm queryForm, Long requestUserId) {
        LambdaQueryWrapper<NovelCharacterEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelCharacterEntity::getProjectId, queryForm.getProjectId());
        queryWrapper.eq(NovelCharacterEntity::getCreateUserId, requestUserId);
        queryWrapper.eq(NovelCharacterEntity::getDeletedFlag, Boolean.FALSE);
        queryWrapper.like(StringUtils.isNotBlank(queryForm.getName()), NovelCharacterEntity::getName, queryForm.getName());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getRoleType()), NovelCharacterEntity::getRoleType, queryForm.getRoleType());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getCurrentStatus()), NovelCharacterEntity::getCurrentStatus, queryForm.getCurrentStatus());
        queryWrapper.orderByDesc(NovelCharacterEntity::getCreateTime);
        return queryWrapper;
    }

    /**
     * 构建地点分页查询条件，用户隔离条件在这里强制追加。
     *
     * @param queryForm 查询表单
     * @param requestUserId 当前登录用户ID
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<NovelLocationEntity> buildLocationPageWrapper(NovelLocationQueryForm queryForm, Long requestUserId) {
        LambdaQueryWrapper<NovelLocationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelLocationEntity::getProjectId, queryForm.getProjectId());
        queryWrapper.eq(NovelLocationEntity::getCreateUserId, requestUserId);
        queryWrapper.eq(NovelLocationEntity::getDeletedFlag, Boolean.FALSE);
        queryWrapper.like(StringUtils.isNotBlank(queryForm.getName()), NovelLocationEntity::getName, queryForm.getName());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getType()), NovelLocationEntity::getType, queryForm.getType());
        queryWrapper.orderByDesc(NovelLocationEntity::getCreateTime);
        return queryWrapper;
    }

    /**
     * 构建线索分页查询条件，用户隔离条件在这里强制追加。
     *
     * @param queryForm 查询表单
     * @param requestUserId 当前登录用户ID
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<NovelClueEntity> buildCluePageWrapper(NovelClueQueryForm queryForm, Long requestUserId) {
        LambdaQueryWrapper<NovelClueEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelClueEntity::getProjectId, queryForm.getProjectId());
        queryWrapper.eq(NovelClueEntity::getCreateUserId, requestUserId);
        queryWrapper.eq(NovelClueEntity::getDeletedFlag, Boolean.FALSE);
        queryWrapper.like(StringUtils.isNotBlank(queryForm.getName()), NovelClueEntity::getName, queryForm.getName());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getType()), NovelClueEntity::getType, queryForm.getType());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getSubType()), NovelClueEntity::getSubType, queryForm.getSubType());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getClueStatus()), NovelClueEntity::getClueStatus, queryForm.getClueStatus());
        queryWrapper.orderByDesc(NovelClueEntity::getCreateTime);
        return queryWrapper;
    }

    /**
     * 构建物品分页查询条件，用户隔离条件在这里强制追加。
     *
     * @param queryForm 查询表单
     * @param requestUserId 当前登录用户ID
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<NovelItemEntity> buildItemPageWrapper(NovelItemQueryForm queryForm, Long requestUserId) {
        LambdaQueryWrapper<NovelItemEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelItemEntity::getProjectId, queryForm.getProjectId());
        queryWrapper.eq(NovelItemEntity::getCreateUserId, requestUserId);
        queryWrapper.eq(NovelItemEntity::getDeletedFlag, Boolean.FALSE);
        queryWrapper.like(StringUtils.isNotBlank(queryForm.getName()), NovelItemEntity::getName, queryForm.getName());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getType()), NovelItemEntity::getType, queryForm.getType());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getItemStatus()), NovelItemEntity::getItemStatus, queryForm.getItemStatus());
        queryWrapper.orderByDesc(NovelItemEntity::getCreateTime);
        return queryWrapper;
    }

    /**
     * 构建事件分页查询条件，用户隔离条件在这里强制追加。
     *
     * @param queryForm 查询表单
     * @param requestUserId 当前登录用户ID
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<NovelEventEntity> buildEventPageWrapper(NovelEventQueryForm queryForm, Long requestUserId) {
        LambdaQueryWrapper<NovelEventEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelEventEntity::getProjectId, queryForm.getProjectId());
        queryWrapper.eq(NovelEventEntity::getCreateUserId, requestUserId);
        queryWrapper.eq(NovelEventEntity::getDeletedFlag, Boolean.FALSE);
        queryWrapper.like(StringUtils.isNotBlank(queryForm.getName()), NovelEventEntity::getName, queryForm.getName());
        queryWrapper.eq(Objects.nonNull(queryForm.getChapterOccurred()), NovelEventEntity::getChapterOccurred, queryForm.getChapterOccurred());
        queryWrapper.orderByDesc(NovelEventEntity::getCreateTime);
        return queryWrapper;
    }

    /**
     * 构建金手指分页查询条件，用户隔离条件在这里强制追加。
     *
     * @param queryForm 查询表单
     * @param requestUserId 当前登录用户ID
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<NovelCheatEntity> buildCheatPageWrapper(NovelCheatQueryForm queryForm, Long requestUserId) {
        LambdaQueryWrapper<NovelCheatEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelCheatEntity::getProjectId, queryForm.getProjectId());
        queryWrapper.eq(NovelCheatEntity::getCreateUserId, requestUserId);
        queryWrapper.eq(NovelCheatEntity::getDeletedFlag, Boolean.FALSE);
        queryWrapper.like(StringUtils.isNotBlank(queryForm.getName()), NovelCheatEntity::getName, queryForm.getName());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getType()), NovelCheatEntity::getType, queryForm.getType());
        queryWrapper.orderByDesc(NovelCheatEntity::getCreateTime);
        return queryWrapper;
    }

    /**
     * 构建马甲分页查询条件，用户隔离条件在这里强制追加。
     *
     * @param queryForm 查询表单
     * @param requestUserId 当前登录用户ID
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<NovelAliasEntity> buildAliasPageWrapper(NovelAliasQueryForm queryForm, Long requestUserId) {
        LambdaQueryWrapper<NovelAliasEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelAliasEntity::getProjectId, queryForm.getProjectId());
        queryWrapper.eq(NovelAliasEntity::getCreateUserId, requestUserId);
        queryWrapper.eq(NovelAliasEntity::getDeletedFlag, Boolean.FALSE);
        queryWrapper.like(StringUtils.isNotBlank(queryForm.getName()), NovelAliasEntity::getName, queryForm.getName());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getType()), NovelAliasEntity::getType, queryForm.getType());
        queryWrapper.eq(Objects.nonNull(queryForm.getRevealed()), NovelAliasEntity::getRevealed, queryForm.getRevealed());
        queryWrapper.orderByDesc(NovelAliasEntity::getCreateTime);
        return queryWrapper;
    }

    /**
     * 构建叙事规则分页查询条件，用户隔离条件在这里强制追加。
     *
     * @param queryForm 查询表单
     * @param requestUserId 当前登录用户ID
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<NovelNarrativeRuleEntity> buildNarrativeRulePageWrapper(NovelNarrativeRuleQueryForm queryForm, Long requestUserId) {
        LambdaQueryWrapper<NovelNarrativeRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelNarrativeRuleEntity::getProjectId, queryForm.getProjectId());
        queryWrapper.eq(NovelNarrativeRuleEntity::getCreateUserId, requestUserId);
        queryWrapper.eq(NovelNarrativeRuleEntity::getDeletedFlag, Boolean.FALSE);
        queryWrapper.like(StringUtils.isNotBlank(queryForm.getName()), NovelNarrativeRuleEntity::getName, queryForm.getName());
        queryWrapper.eq(Objects.nonNull(queryForm.getPriority()), NovelNarrativeRuleEntity::getPriority, queryForm.getPriority());
        queryWrapper.orderByDesc(NovelNarrativeRuleEntity::getPriority);
        queryWrapper.orderByDesc(NovelNarrativeRuleEntity::getCreateTime);
        return queryWrapper;
    }

    /**
     * 构建卷分页查询条件，用户隔离条件在这里强制追加。
     *
     * @param queryForm 查询表单
     * @param requestUserId 当前登录用户ID
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<NovelVolumeEntity> buildVolumePageWrapper(NovelVolumeQueryForm queryForm, Long requestUserId) {
        LambdaQueryWrapper<NovelVolumeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelVolumeEntity::getProjectId, queryForm.getProjectId());
        queryWrapper.eq(NovelVolumeEntity::getCreateUserId, requestUserId);
        queryWrapper.eq(NovelVolumeEntity::getDeletedFlag, Boolean.FALSE);
        queryWrapper.like(StringUtils.isNotBlank(queryForm.getTitle()), NovelVolumeEntity::getTitle, queryForm.getTitle());
        queryWrapper.eq(Objects.nonNull(queryForm.getNumber()), NovelVolumeEntity::getNumber, queryForm.getNumber());
        queryWrapper.orderByAsc(NovelVolumeEntity::getNumber);
        queryWrapper.orderByDesc(NovelVolumeEntity::getCreateTime);
        return queryWrapper;
    }

    /**
     * 构建角色关系分页查询条件，用户隔离条件在这里强制追加。
     *
     * @param queryForm 查询表单
     * @param requestUserId 当前登录用户ID
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<NovelCharacterRelationEntity> buildCharacterRelationPageWrapper(NovelCharacterRelationQueryForm queryForm, Long requestUserId) {
        LambdaQueryWrapper<NovelCharacterRelationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelCharacterRelationEntity::getProjectId, queryForm.getProjectId());
        queryWrapper.eq(NovelCharacterRelationEntity::getCreateUserId, requestUserId);
        queryWrapper.eq(NovelCharacterRelationEntity::getDeletedFlag, Boolean.FALSE);
        queryWrapper.eq(Objects.nonNull(queryForm.getCharacterId()), NovelCharacterRelationEntity::getCharacterId, queryForm.getCharacterId());
        queryWrapper.eq(Objects.nonNull(queryForm.getTargetCharacterId()), NovelCharacterRelationEntity::getTargetCharacterId, queryForm.getTargetCharacterId());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getRelationType()), NovelCharacterRelationEntity::getRelationType, queryForm.getRelationType());
        queryWrapper.orderByDesc(NovelCharacterRelationEntity::getCreateTime);
        return queryWrapper;
    }

    /**
     * 把 SmartAdmin 分页参数转换成角色实体分页对象。
     *
     * @param queryForm 查询表单
     * @return 角色实体分页对象
     */
    @SuppressWarnings("unchecked")
    private Page<NovelCharacterEntity> convertCharacterPage(NovelCharacterQueryForm queryForm) {
        return (Page<NovelCharacterEntity>) SmartPageUtil.convert2PageQuery(queryForm);
    }

    /**
     * 把 SmartAdmin 分页参数转换成地点实体分页对象。
     *
     * @param queryForm 查询表单
     * @return 地点实体分页对象
     */
    @SuppressWarnings("unchecked")
    private Page<NovelLocationEntity> convertLocationPage(NovelLocationQueryForm queryForm) {
        return (Page<NovelLocationEntity>) SmartPageUtil.convert2PageQuery(queryForm);
    }

    /**
     * 把 SmartAdmin 分页参数转换成线索实体分页对象。
     *
     * @param queryForm 查询表单
     * @return 线索实体分页对象
     */
    @SuppressWarnings("unchecked")
    private Page<NovelClueEntity> convertCluePage(NovelClueQueryForm queryForm) {
        return (Page<NovelClueEntity>) SmartPageUtil.convert2PageQuery(queryForm);
    }

    /**
     * 把 SmartAdmin 分页参数转换成物品实体分页对象。
     *
     * @param queryForm 查询表单
     * @return 物品实体分页对象
     */
    @SuppressWarnings("unchecked")
    private Page<NovelItemEntity> convertItemPage(NovelItemQueryForm queryForm) {
        return (Page<NovelItemEntity>) SmartPageUtil.convert2PageQuery(queryForm);
    }

    /**
     * 把 SmartAdmin 分页参数转换成事件实体分页对象。
     *
     * @param queryForm 查询表单
     * @return 事件实体分页对象
     */
    @SuppressWarnings("unchecked")
    private Page<NovelEventEntity> convertEventPage(NovelEventQueryForm queryForm) {
        return (Page<NovelEventEntity>) SmartPageUtil.convert2PageQuery(queryForm);
    }

    /**
     * 把 SmartAdmin 分页参数转换成金手指实体分页对象。
     *
     * @param queryForm 查询表单
     * @return 金手指实体分页对象
     */
    @SuppressWarnings("unchecked")
    private Page<NovelCheatEntity> convertCheatPage(NovelCheatQueryForm queryForm) {
        return (Page<NovelCheatEntity>) SmartPageUtil.convert2PageQuery(queryForm);
    }

    /**
     * 把 SmartAdmin 分页参数转换成马甲实体分页对象。
     *
     * @param queryForm 查询表单
     * @return 马甲实体分页对象
     */
    @SuppressWarnings("unchecked")
    private Page<NovelAliasEntity> convertAliasPage(NovelAliasQueryForm queryForm) {
        return (Page<NovelAliasEntity>) SmartPageUtil.convert2PageQuery(queryForm);
    }

    /**
     * 把 SmartAdmin 分页参数转换成叙事规则实体分页对象。
     *
     * @param queryForm 查询表单
     * @return 叙事规则实体分页对象
     */
    @SuppressWarnings("unchecked")
    private Page<NovelNarrativeRuleEntity> convertNarrativeRulePage(NovelNarrativeRuleQueryForm queryForm) {
        return (Page<NovelNarrativeRuleEntity>) SmartPageUtil.convert2PageQuery(queryForm);
    }

    /**
     * 把 SmartAdmin 分页参数转换成卷实体分页对象。
     *
     * @param queryForm 查询表单
     * @return 卷实体分页对象
     */
    @SuppressWarnings("unchecked")
    private Page<NovelVolumeEntity> convertVolumePage(NovelVolumeQueryForm queryForm) {
        return (Page<NovelVolumeEntity>) SmartPageUtil.convert2PageQuery(queryForm);
    }

    /**
     * 把 SmartAdmin 分页参数转换成角色关系实体分页对象。
     *
     * @param queryForm 查询表单
     * @return 角色关系实体分页对象
     */
    @SuppressWarnings("unchecked")
    private Page<NovelCharacterRelationEntity> convertCharacterRelationPage(NovelCharacterRelationQueryForm queryForm) {
        return (Page<NovelCharacterRelationEntity>) SmartPageUtil.convert2PageQuery(queryForm);
    }

    /**
     * 把前端传入的角色关系大类转换为允许同步到 Neo4j 的关系枚举。
     *
     * @param relationType 角色关系大类
     * @return 可管理的角色关系枚举；不在白名单时返回 null
     */
    private NovelGraphRelationEnum resolveCharacterRelationType(String relationType) {
        String normalizedRelationType = StringUtils.trimToEmpty(relationType).toUpperCase();
        return switch (normalizedRelationType) {
            case "KNOWS" -> NovelGraphRelationEnum.KNOWS;
            case "LOVES" -> NovelGraphRelationEnum.LOVES;
            case "HATES" -> NovelGraphRelationEnum.HATES;
            case "IS_FAMILY_OF" -> NovelGraphRelationEnum.IS_FAMILY_OF;
            default -> null;
        };
    }

    /**
     * 校验角色关系请求体中的业务字段。
     * <p>
     * 每种关系只允许使用自己的属性位，避免一个关系同时带上爱慕状态、仇恨强度等互斥信息。
     *
     * @param form 角色关系创建或编辑表单
     * @param graphRelationType 已解析的关系大类
     * @param requestUserId 当前登录用户ID
     * @return 校验结果
     */
    private ResponseDTO<String> validateCharacterRelationPayload(NovelCharacterRelationAddForm form, NovelGraphRelationEnum graphRelationType, Long requestUserId) {
        if (Objects.isNull(graphRelationType)) {
            return ResponseDTO.userErrorParam("角色关系大类错误");
        }

        ResponseDTO<String> fieldValidateResult = validateCharacterRelationFields(form, graphRelationType);
        if (!fieldValidateResult.getOk()) {
            return fieldValidateResult;
        }
        return validateCharacterRelationCharacters(form, requestUserId);
    }

    /**
     * 校验角色关系大类对应的专属字段。
     *
     * @param form 角色关系表单
     * @param graphRelationType 角色关系大类
     * @return 校验结果
     */
    private ResponseDTO<String> validateCharacterRelationFields(NovelCharacterRelationAddForm form, NovelGraphRelationEnum graphRelationType) {
        return switch (graphRelationType) {
            case KNOWS -> validateEnumField(form.getKnowsRelationType(), NovelRelationTypeEnum.class, "KNOWS子类型错误");
            case LOVES -> validateEnumField(form.getLoveStatus(), NovelLoveStatusEnum.class, "爱慕状态错误");
            case HATES -> validateHateIntensity(form.getHateIntensity());
            case IS_FAMILY_OF -> validateEnumField(form.getFamilyType(), NovelFamilyTypeEnum.class, "亲缘类型错误");
            default -> ResponseDTO.userErrorParam("角色关系大类错误");
        };
    }

    /**
     * 校验枚举字段必须有值且必须在指定枚举内。
     *
     * @param value 待校验的枚举值
     * @param enumClass 允许值所在枚举
     * @param errorMessage 校验失败提示
     * @return 校验结果
     */
    private ResponseDTO<String> validateEnumField(String value, Class<? extends BaseEnum> enumClass, String errorMessage) {
        if (StringUtils.isBlank(value) || !containsBaseEnumValue(enumClass, value)) {
            return ResponseDTO.userErrorParam(errorMessage);
        }
        return ResponseDTO.ok();
    }

    /**
     * 校验仇恨强度范围。
     *
     * @param hateIntensity 仇恨强度，范围 1~5
     * @return 校验结果
     */
    private ResponseDTO<String> validateHateIntensity(Integer hateIntensity) {
        if (Objects.isNull(hateIntensity) || hateIntensity < 1 || hateIntensity > 5) {
            return ResponseDTO.userErrorParam("仇恨强度必须在1到5之间");
        }
        return ResponseDTO.ok();
    }

    /**
     * 校验角色关系的起点和终点都属于当前用户和同一个项目。
     *
     * @param form 角色关系表单
     * @param requestUserId 当前登录用户ID
     * @return 校验结果
     */
    private ResponseDTO<String> validateCharacterRelationCharacters(NovelCharacterRelationAddForm form, Long requestUserId) {
        NovelCharacterEntity sourceCharacter = getOwnedCharacter(form.getCharacterId(), requestUserId);
        if (Objects.isNull(sourceCharacter)) {
            return ResponseDTO.userErrorParam("源角色不存在或无权访问");
        }

        NovelCharacterEntity targetCharacter = getOwnedCharacter(form.getTargetCharacterId(), requestUserId);
        if (Objects.isNull(targetCharacter)) {
            return ResponseDTO.userErrorParam("目标角色不存在或无权访问");
        }

        if (!Objects.equals(sourceCharacter.getProjectId(), form.getProjectId()) || !Objects.equals(targetCharacter.getProjectId(), form.getProjectId())) {
            return ResponseDTO.userErrorParam("角色关系只能连接同一项目内的角色");
        }
        return ResponseDTO.ok();
    }

    /**
     * 规范化角色关系实体的互斥字段。
     *
     * @param entity 角色关系实体
     * @param graphRelationType 角色关系大类
     */
    private void normalizeCharacterRelationFields(NovelCharacterRelationEntity entity, NovelGraphRelationEnum graphRelationType) {
        entity.setRelationType(graphRelationType.getType());
        if (!NovelGraphRelationEnum.KNOWS.equals(graphRelationType)) {
            entity.setKnowsRelationType(null);
        }
        if (!NovelGraphRelationEnum.LOVES.equals(graphRelationType)) {
            entity.setLoveStatus(null);
        }
        if (!NovelGraphRelationEnum.HATES.equals(graphRelationType)) {
            entity.setHateIntensity(null);
        }
        if (!NovelGraphRelationEnum.IS_FAMILY_OF.equals(graphRelationType)) {
            entity.setFamilyType(null);
        }
    }

    /**
     * 判断角色关系的图谱边定位信息是否发生变化。
     *
     * @param oldEntity 数据库中的原角色关系
     * @param updateEntity 待写入的新角色关系
     * @return true 表示需要删除旧图谱边后再创建新边
     */
    private boolean isCharacterRelationEndpointChanged(NovelCharacterRelationEntity oldEntity, NovelCharacterRelationEntity updateEntity) {
        return !Objects.equals(oldEntity.getRelationType(), updateEntity.getRelationType())
                || !Objects.equals(oldEntity.getCharacterId(), updateEntity.getCharacterId())
                || !Objects.equals(oldEntity.getTargetCharacterId(), updateEntity.getTargetCharacterId());
    }

    /**
     * 同步角色关系到 Neo4j。
     *
     * @param entity 角色关系实体
     * @param graphRelationType 角色关系大类
     */
    private void syncCharacterRelationToGraph(NovelCharacterRelationEntity entity, NovelGraphRelationEnum graphRelationType) {
        novelGraphService.mergeRelation(graphRelationType, entity.getProjectId(), NovelGraphNodeEnum.Character, entity.getCharacterId(),
                NovelGraphNodeEnum.Character, entity.getTargetCharacterId(), buildCharacterRelationGraphProps(entity, graphRelationType));
    }

    /**
     * 从 Neo4j 删除角色关系边。
     *
     * @param entity 角色关系实体
     * @param graphRelationType 角色关系大类
     */
    private void deleteCharacterRelationFromGraph(NovelCharacterRelationEntity entity, NovelGraphRelationEnum graphRelationType) {
        if (Objects.isNull(graphRelationType)) {
            return;
        }
        novelGraphService.deleteRelation(graphRelationType, entity.getProjectId(), NovelGraphNodeEnum.Character, entity.getCharacterId(),
                NovelGraphNodeEnum.Character, entity.getTargetCharacterId());
    }

    /**
     * 组装角色关系同步到 Neo4j 的关系属性。
     *
     * @param entity 角色关系实体
     * @param graphRelationType 角色关系大类
     * @return Neo4j 关系属性
     */
    private Map<String, Object> buildCharacterRelationGraphProps(NovelCharacterRelationEntity entity, NovelGraphRelationEnum graphRelationType) {
        Map<String, Object> props = new LinkedHashMap<>();
        switch (graphRelationType) {
            case KNOWS -> putIfNotNull(props, "relationType", entity.getKnowsRelationType());
            case LOVES -> putIfNotNull(props, "status", entity.getLoveStatus());
            case HATES -> putIfNotNull(props, "intensity", entity.getHateIntensity());
            case IS_FAMILY_OF -> putIfNotNull(props, "familyType", entity.getFamilyType());
            default -> {
            }
        }
        return props;
    }

    /**
     * 判断某个值是否存在于 SmartAdmin BaseEnum 枚举中。
     *
     * @param enumClass 枚举类型
     * @param value 待检查的值
     * @return true 表示枚举值合法
     */
    private boolean containsBaseEnumValue(Class<? extends BaseEnum> enumClass, Object value) {
        return Arrays.stream(enumClass.getEnumConstants()).anyMatch(enumValue -> Objects.equals(enumValue.getValue(), value));
    }

    /**
     * 组装可同步到 Neo4j Character 节点的白名单字段。
     *
     * @param entity 角色实体
     * @return Character 节点属性
     */
    private Map<String, Object> buildCharacterGraphProps(NovelCharacterEntity entity) {
        Map<String, Object> props = new LinkedHashMap<>();
        putIfNotNull(props, "name", entity.getName());
        putIfNotNull(props, "roleType", entity.getRoleType());
        putIfNotNull(props, "description", entity.getDescription());
        putIfNotNull(props, "currentGoal", entity.getCurrentGoal());
        putIfNotNull(props, "goalProgress", entity.getGoalProgress());
        putIfNotNull(props, "goalStatus", entity.getGoalStatus());
        putIfNotNull(props, "currentEmotion", entity.getCurrentEmotion());
        putIfNotNull(props, "emotionIntensity", entity.getEmotionIntensity());
        putIfNotNull(props, "secondaryEmotion", entity.getSecondaryEmotion());
        putIfNotNull(props, "powerLevel", entity.getPowerLevel());
        putIfNotNull(props, "currentStatus", entity.getCurrentStatus());
        return props;
    }

    /**
     * 组装可同步到 Neo4j Location 节点的白名单字段。
     *
     * @param entity 地点实体
     * @return Location 节点属性
     */
    private Map<String, Object> buildLocationGraphProps(NovelLocationEntity entity) {
        Map<String, Object> props = new LinkedHashMap<>();
        putIfNotNull(props, "name", entity.getName());
        putIfNotNull(props, "type", entity.getType());
        putIfNotNull(props, "summary", entity.getSummary());
        return props;
    }

    /**
     * 组装可同步到 Neo4j Clue 节点的白名单字段。
     *
     * @param entity 线索实体
     * @return Clue 节点属性
     */
    private Map<String, Object> buildClueGraphProps(NovelClueEntity entity) {
        Map<String, Object> props = new LinkedHashMap<>();
        putIfNotNull(props, "name", entity.getName());
        putIfNotNull(props, "type", entity.getType());
        putIfNotNull(props, "subType", entity.getSubType());
        putIfNotNull(props, "description", entity.getDescription());
        putIfNotNull(props, "priority", entity.getPriority());
        putIfNotNull(props, "targetChapter", entity.getTargetChapter());
        putIfNotNull(props, "tone", entity.getTone());
        putIfNotNull(props, "summary", entity.getSummary());
        putIfNotNull(props, "revealLevel", entity.getRevealLevel());
        putIfNotNull(props, "currentStage", entity.getCurrentStage());
        putIfNotNull(props, "clueStatus", entity.getClueStatus());
        putIfNotNull(props, "lastAlertedChapter", entity.getLastAlertedChapter());
        return props;
    }

    /**
     * 组装可同步到 Neo4j Item 节点的白名单字段。
     *
     * @param entity 物品实体
     * @return Item 节点属性
     */
    private Map<String, Object> buildItemGraphProps(NovelItemEntity entity) {
        Map<String, Object> props = new LinkedHashMap<>();
        putIfNotNull(props, "name", entity.getName());
        putIfNotNull(props, "type", entity.getType());
        putIfNotNull(props, "summary", entity.getSummary());
        putIfNotNull(props, "quantity", entity.getQuantity());
        putIfNotNull(props, "itemStatus", entity.getItemStatus());
        return props;
    }

    /**
     * 组装可同步到 Neo4j Event 节点的白名单字段。
     *
     * @param entity 事件实体
     * @return Event 节点属性
     */
    private Map<String, Object> buildEventGraphProps(NovelEventEntity entity) {
        Map<String, Object> props = new LinkedHashMap<>();
        putIfNotNull(props, "name", entity.getName());
        putIfNotNull(props, "summary", entity.getSummary());
        putIfNotNull(props, "chapterOccurred", entity.getChapterOccurred());
        return props;
    }

    /**
     * 组装可同步到 Neo4j Cheat 节点的白名单字段。
     *
     * @param entity 金手指实体
     * @return Cheat 节点属性
     */
    private Map<String, Object> buildCheatGraphProps(NovelCheatEntity entity) {
        Map<String, Object> props = new LinkedHashMap<>();
        putIfNotNull(props, "name", entity.getName());
        putIfNotNull(props, "type", entity.getType());
        putIfNotNull(props, "summary", entity.getSummary());
        putIfNotNull(props, "origin", entity.getOrigin());
        putIfNotNull(props, "limitation", entity.getLimitation());
        putIfNotNull(props, "evolution", entity.getEvolution());
        putIfNotNull(props, "currentStage", entity.getCurrentStage());
        return props;
    }

    /**
     * 组装可同步到 Neo4j Alias 节点的白名单字段。
     *
     * @param entity 马甲实体
     * @return Alias 节点属性
     */
    private Map<String, Object> buildAliasGraphProps(NovelAliasEntity entity) {
        Map<String, Object> props = new LinkedHashMap<>();
        putIfNotNull(props, "name", entity.getName());
        putIfNotNull(props, "type", entity.getType());
        putIfNotNull(props, "aliasContext", entity.getAliasContext());
        putIfNotNull(props, "summary", entity.getSummary());
        putIfNotNull(props, "revealed", entity.getRevealed());
        putIfNotNull(props, "revealedTo", entity.getRevealedTo());
        return props;
    }

    /**
     * 组装可同步到 Neo4j Volume 节点的白名单字段。
     *
     * @param entity 卷实体
     * @return Volume 节点属性
     */
    private Map<String, Object> buildVolumeGraphProps(NovelVolumeEntity entity) {
        Map<String, Object> props = new LinkedHashMap<>();
        putIfNotNull(props, "number", entity.getNumber());
        putIfNotNull(props, "title", entity.getTitle());
        putIfNotNull(props, "summary", entity.getSummary());
        return props;
    }

    /**
     * 空值不写入图谱属性，避免 Neo4j Driver 收到 null 属性后写入失败。
     *
     * @param props 属性容器
     * @param key 属性名
     * @param value 属性值
     */
    private void putIfNotNull(Map<String, Object> props, String key, Object value) {
        if (Objects.nonNull(value)) {
            props.put(key, value);
        }
    }
}
