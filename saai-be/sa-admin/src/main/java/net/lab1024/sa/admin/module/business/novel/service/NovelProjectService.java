package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphNodeEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectStatusEnum;
import net.lab1024.sa.admin.module.business.novel.dao.NovelProjectDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelProjectAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelProjectQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelProjectUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelProjectVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 小说项目管理服务。
 * <p>
 * 项目是第一阶段所有角色、地点、章节和图谱数据的顶层容器，所以这里统一处理用户隔离、归档和 Project 节点同步。
 *
 * @Author AI-Novel
 */
@Slf4j
@Service
public class NovelProjectService {

    /**
     * 默认每章目标字数，用户创建项目时未填写则使用该值。
     */
    private static final Integer DEFAULT_TARGET_CHAPTER_WORDS = 3000;

    /**
     * 默认上下文 Token 软预算，后续上下文检索会优先按这个预算裁剪材料。
     */
    private static final Integer DEFAULT_TOKEN_BUDGET = 6000;

    /**
     * 默认上下文 Token 硬上限，Prompt 组装结果不能越过这个上限。
     */
    private static final Integer DEFAULT_TOKEN_HARD_LIMIT = 8000;

    /**
     * 项目 DAO，负责 MySQL 项目表的增删改查。
     */
    @Resource
    private NovelProjectDao novelProjectDao;

    /**
     * 图谱服务，负责把项目主数据同步到 Neo4j Project 节点。
     */
    @Resource
    private NovelGraphService novelGraphService;

    /**
     * 分页查询当前用户的小说项目。
     *
     * @param queryForm 查询条件和分页参数
     * @param requestUserId 当前登录用户ID
     * @return 当前用户可见的项目分页结果
     */
    public ResponseDTO<PageResult<NovelProjectVO>> queryByPage(NovelProjectQueryForm queryForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        LambdaQueryWrapper<NovelProjectEntity> queryWrapper = buildProjectPageWrapper(queryForm, requestUserId);
        Page<NovelProjectEntity> page = convertProjectPage(queryForm);
        novelProjectDao.selectPage(page, queryWrapper);
        PageResult<NovelProjectVO> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords(), NovelProjectVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询当前用户某个项目的详情。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前登录用户ID
     * @return 项目详情，查不到时返回参数错误
     */
    public ResponseDTO<NovelProjectVO> getDetail(Long projectId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        NovelProjectEntity entity = getOwnedActiveProject(projectId, requestUserId);
        if (Objects.isNull(entity)) {
            return ResponseDTO.userErrorParam("项目不存在或无权访问");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, NovelProjectVO.class));
    }

    /**
     * 创建小说项目，并同步 Neo4j Project 节点。
     *
     * @param addForm 项目创建表单
     * @param requestUserId 当前登录用户ID
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createProject(NovelProjectAddForm addForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelProjectEntity entity = SmartBeanUtil.copy(addForm, NovelProjectEntity.class);
        applyCreateDefaults(entity, requestUserId);
        ResponseDTO<String> tokenValidateResult = validateTokenBudget(entity.getTokenBudget(), entity.getTokenHardLimit());
        if (!tokenValidateResult.getOk()) {
            return tokenValidateResult;
        }

        novelProjectDao.insert(entity);
        // Project 节点使用项目ID作为业务键，MySQL插入后才能拿到自增ID。
        novelGraphService.mergeProject(entity.getId(), buildProjectGraphProps(entity));
        return ResponseDTO.ok();
    }

    /**
     * 编辑当前用户的小说项目，并把可进入图谱的字段同步到 Neo4j。
     *
     * @param updateForm 项目编辑表单
     * @param requestUserId 当前登录用户ID
     * @return 编辑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateProject(NovelProjectUpdateForm updateForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }
        if (NovelProjectStatusEnum.ARCHIVED.getValue().equals(updateForm.getStatus())) {
            return ResponseDTO.userErrorParam("项目归档请使用归档接口");
        }

        NovelProjectEntity oldEntity = getOwnedActiveProject(updateForm.getProjectId(), requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("项目不存在或无权访问");
        }

        NovelProjectEntity updateEntity = SmartBeanUtil.copy(updateForm, NovelProjectEntity.class);
        updateEntity.setId(updateForm.getProjectId());
        updateEntity.setCreateUserId(requestUserId);
        applyUpdateDefaults(updateEntity, oldEntity);
        ResponseDTO<String> tokenValidateResult = validateTokenBudget(updateEntity.getTokenBudget(), updateEntity.getTokenHardLimit());
        if (!tokenValidateResult.getOk()) {
            return tokenValidateResult;
        }

        novelProjectDao.updateById(updateEntity);
        novelGraphService.updateNodeProps(NovelGraphNodeEnum.Project, updateEntity.getId(), updateEntity.getId(), buildProjectGraphProps(updateEntity));
        return ResponseDTO.ok();
    }

    /**
     * 归档当前用户的小说项目。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前登录用户ID
     * @return 归档结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> archiveProject(Long projectId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelProjectEntity oldEntity = getOwnedActiveProject(projectId, requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("项目不存在或无权访问");
        }

        NovelProjectEntity archiveEntity = new NovelProjectEntity();
        archiveEntity.setId(projectId);
        archiveEntity.setDeletedFlag(Boolean.TRUE);
        archiveEntity.setStatus(NovelProjectStatusEnum.ARCHIVED.getValue());
        novelProjectDao.updateById(archiveEntity);
        novelGraphService.archiveNode(NovelGraphNodeEnum.Project, projectId, projectId);
        return ResponseDTO.ok();
    }

    /**
     * 校验当前请求用户是否存在。
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
     * 创建项目时填充服务端默认字段。
     *
     * @param entity 待写入数据库的项目实体
     * @param requestUserId 当前登录用户ID
     */
    private void applyCreateDefaults(NovelProjectEntity entity, Long requestUserId) {
        entity.setCreateUserId(requestUserId);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setStatus(NovelProjectStatusEnum.ACTIVE.getValue());
        entity.setTargetChapterWords(defaultIfNull(entity.getTargetChapterWords(), DEFAULT_TARGET_CHAPTER_WORDS));
        entity.setTokenBudget(defaultIfNull(entity.getTokenBudget(), DEFAULT_TOKEN_BUDGET));
        entity.setTokenHardLimit(defaultIfNull(entity.getTokenHardLimit(), DEFAULT_TOKEN_HARD_LIMIT));
    }

    /**
     * 编辑项目时补齐未提交但业务上必须保留的默认字段。
     *
     * @param updateEntity 待更新的项目实体
     * @param oldEntity 数据库中的原项目实体
     */
    private void applyUpdateDefaults(NovelProjectEntity updateEntity, NovelProjectEntity oldEntity) {
        updateEntity.setDeletedFlag(Boolean.FALSE);
        updateEntity.setTargetChapterWords(defaultIfNull(updateEntity.getTargetChapterWords(), DEFAULT_TARGET_CHAPTER_WORDS));
        updateEntity.setTokenBudget(defaultIfNull(updateEntity.getTokenBudget(), DEFAULT_TOKEN_BUDGET));
        updateEntity.setTokenHardLimit(defaultIfNull(updateEntity.getTokenHardLimit(), DEFAULT_TOKEN_HARD_LIMIT));
        if (StringUtils.isBlank(updateEntity.getStatus())) {
            updateEntity.setStatus(oldEntity.getStatus());
        }
    }

    /**
     * 校验 Token 预算关系是否合理。
     *
     * @param tokenBudget Token 软预算
     * @param tokenHardLimit Token 硬上限
     * @return 校验结果
     */
    private ResponseDTO<String> validateTokenBudget(Integer tokenBudget, Integer tokenHardLimit) {
        if (tokenBudget != null && tokenHardLimit != null && tokenHardLimit < tokenBudget) {
            return ResponseDTO.userErrorParam("Token硬上限不能小于Token目标预算");
        }
        return ResponseDTO.ok();
    }

    /**
     * 查询当前用户未归档的项目。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档项目，查不到返回 null
     */
    private NovelProjectEntity getOwnedActiveProject(Long projectId, Long requestUserId) {
        return novelProjectDao.selectOne(new LambdaQueryWrapper<NovelProjectEntity>()
                .eq(NovelProjectEntity::getId, projectId)
                .eq(NovelProjectEntity::getCreateUserId, requestUserId)
                .eq(NovelProjectEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 构建项目分页查询条件，用户隔离条件在这里强制追加。
     *
     * @param queryForm 查询表单
     * @param requestUserId 当前登录用户ID
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<NovelProjectEntity> buildProjectPageWrapper(NovelProjectQueryForm queryForm, Long requestUserId) {
        LambdaQueryWrapper<NovelProjectEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelProjectEntity::getCreateUserId, requestUserId);
        if (!Boolean.TRUE.equals(queryForm.getIncludeArchived())) {
            queryWrapper.eq(NovelProjectEntity::getDeletedFlag, Boolean.FALSE);
        }
        queryWrapper.like(StringUtils.isNotBlank(queryForm.getName()), NovelProjectEntity::getName, queryForm.getName());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getGenre()), NovelProjectEntity::getGenre, queryForm.getGenre());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getPlatform()), NovelProjectEntity::getPlatform, queryForm.getPlatform());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getStatus()), NovelProjectEntity::getStatus, queryForm.getStatus());
        queryWrapper.orderByDesc(NovelProjectEntity::getCreateTime);
        return queryWrapper;
    }

    /**
     * 把 SmartAdmin 分页参数转换成项目实体分页对象。
     *
     * @param queryForm 查询表单
     * @return 项目实体分页对象
     */
    @SuppressWarnings("unchecked")
    private Page<NovelProjectEntity> convertProjectPage(NovelProjectQueryForm queryForm) {
        return (Page<NovelProjectEntity>) SmartPageUtil.convert2PageQuery(queryForm);
    }

    /**
     * 组装可同步到 Neo4j Project 节点的白名单字段。
     *
     * @param entity 项目实体
     * @return Project 节点属性
     */
    private Map<String, Object> buildProjectGraphProps(NovelProjectEntity entity) {
        Map<String, Object> props = new LinkedHashMap<>();
        putIfNotNull(props, "name", entity.getName());
        putIfNotNull(props, "genre", entity.getGenre());
        putIfNotNull(props, "worldBuilding", entity.getWorldBuilding());
        putIfNotNull(props, "protagonistName", entity.getProtagonistName());
        putIfNotNull(props, "styleDescription", entity.getStyleDescription());
        putIfNotNull(props, "platform", entity.getPlatform());
        putIfNotNull(props, "targetTotalWords", entity.getTargetTotalWords());
        putIfNotNull(props, "targetChapterWords", entity.getTargetChapterWords());
        putIfNotNull(props, "tokenBudget", entity.getTokenBudget());
        putIfNotNull(props, "tokenHardLimit", entity.getTokenHardLimit());
        putIfNotNull(props, "status", entity.getStatus());
        putIfNotNull(props, "remark", entity.getRemark());
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

    /**
     * 如果表单没有传值，则使用服务层默认值。
     *
     * @param value 表单传入值
     * @param defaultValue 默认值
     * @return 最终写入值
     */
    private Integer defaultIfNull(Integer value, Integer defaultValue) {
        return Objects.isNull(value) ? defaultValue : value;
    }
}
