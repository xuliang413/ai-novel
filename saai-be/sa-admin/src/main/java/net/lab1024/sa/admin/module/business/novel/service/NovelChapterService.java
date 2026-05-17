package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.constant.NovelGraphNodeEnum;
import net.lab1024.sa.admin.module.business.novel.dao.ChapterOutlineDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelChapterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelProjectDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelVolumeDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.ChapterOutlineEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelVolumeEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.ChapterOutlineAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.ChapterOutlineQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.ChapterOutlineUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelChapterQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelChapterUpdateForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.ChapterOutlineVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelChapterVO;
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
 * 小说章节管理服务。
 * <p>
 * 负责章节列表、详情、正文编辑和章节细纲 CRUD；章节正文只存 MySQL，章节细纲也只存 MySQL。
 *
 * @Author AI-Novel
 */
@Slf4j
@Service
public class NovelChapterService {

    /**
     * 项目 DAO，所有章节和细纲操作前都要确认项目归属。
     */
    @Resource
    private NovelProjectDao novelProjectDao;

    /**
     * 卷 DAO，章节关联卷时用于校验卷归属。
     */
    @Resource
    private NovelVolumeDao novelVolumeDao;

    /**
     * 章节 DAO，负责章节表查询和编辑。
     */
    @Resource
    private NovelChapterDao novelChapterDao;

    /**
     * 章节细纲 DAO，负责细纲表增删改查。
     */
    @Resource
    private ChapterOutlineDao chapterOutlineDao;

    /**
     * 图谱服务，只同步章节节点的检索字段，不同步正文和细纲。
     */
    @Resource
    private NovelGraphService novelGraphService;

    /**
     * 分页查询当前用户某个项目下的章节。
     *
     * @param queryForm 查询条件和分页参数
     * @param requestUserId 当前登录用户ID
     * @return 当前用户可见的章节分页结果
     */
    public ResponseDTO<PageResult<NovelChapterVO>> queryChapterByPage(NovelChapterQueryForm queryForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(queryForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        LambdaQueryWrapper<NovelChapterEntity> queryWrapper = buildChapterPageWrapper(queryForm, requestUserId);
        Page<NovelChapterEntity> page = convertChapterPage(queryForm);
        novelChapterDao.selectPage(page, queryWrapper);
        PageResult<NovelChapterVO> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords(), NovelChapterVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询当前用户可见的章节详情。
     *
     * @param chapterId 章节ID
     * @param requestUserId 当前登录用户ID
     * @return 章节详情
     */
    public ResponseDTO<NovelChapterVO> getChapterDetail(Long chapterId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        NovelChapterEntity entity = getOwnedChapter(chapterId, requestUserId);
        if (Objects.isNull(entity)) {
            return ResponseDTO.userErrorParam("章节不存在或无权访问");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, NovelChapterVO.class));
    }

    /**
     * 编辑章节正文和检索字段。
     * <p>
     * 正文只写 MySQL；Neo4j 只同步章节节点的白名单检索字段，避免把全文塞进图数据库。
     *
     * @param updateForm 章节编辑表单
     * @param requestUserId 当前登录用户ID
     * @return 编辑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateChapter(NovelChapterUpdateForm updateForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        NovelChapterEntity oldEntity = getOwnedChapter(updateForm.getChapterId(), requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("章节不存在或无权访问");
        }
        if (!Objects.equals(oldEntity.getProjectId(), updateForm.getProjectId())) {
            return ResponseDTO.userErrorParam("章节不能移动到其他项目");
        }

        ResponseDTO<String> volumeValidateResult = validateChapterVolume(updateForm.getVolumeId(), oldEntity.getProjectId(), requestUserId);
        if (!volumeValidateResult.getOk()) {
            return volumeValidateResult;
        }

        NovelChapterEntity updateEntity = SmartBeanUtil.copy(updateForm, NovelChapterEntity.class);
        updateEntity.setId(updateForm.getChapterId());
        updateEntity.setProjectId(oldEntity.getProjectId());
        updateEntity.setChapterNumber(oldEntity.getChapterNumber());
        updateEntity.setWordCount(StringUtils.length(updateForm.getContent()));
        updateEntity.setStatus(oldEntity.getStatus());
        updateEntity.setEmbedding(oldEntity.getEmbedding());
        updateEntity.setCreateUserId(requestUserId);
        updateEntity.setDeletedFlag(Boolean.FALSE);
        novelChapterDao.updateById(updateEntity);
        novelGraphService.updateNodeProps(NovelGraphNodeEnum.Chapter, oldEntity.getProjectId(), oldEntity.getChapterNumber(), buildChapterGraphProps(updateEntity));
        return ResponseDTO.ok();
    }

    /**
     * 分页查询当前用户某个项目下的章节细纲。
     *
     * @param queryForm 查询条件和分页参数
     * @param requestUserId 当前登录用户ID
     * @return 当前用户可见的章节细纲分页结果
     */
    public ResponseDTO<PageResult<ChapterOutlineVO>> queryOutlineByPage(ChapterOutlineQueryForm queryForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(queryForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        LambdaQueryWrapper<ChapterOutlineEntity> queryWrapper = buildOutlinePageWrapper(queryForm, requestUserId);
        Page<ChapterOutlineEntity> page = convertOutlinePage(queryForm);
        chapterOutlineDao.selectPage(page, queryWrapper);
        PageResult<ChapterOutlineVO> pageResult = SmartPageUtil.convert2PageResult(page, page.getRecords(), ChapterOutlineVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询当前用户可见的章节细纲详情。
     *
     * @param outlineId 章节细纲ID
     * @param requestUserId 当前登录用户ID
     * @return 章节细纲详情
     */
    public ResponseDTO<ChapterOutlineVO> getOutlineDetail(Long outlineId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return ResponseDTO.error(userValidateResult);
        }

        ChapterOutlineEntity entity = getOwnedOutline(outlineId, requestUserId);
        if (Objects.isNull(entity)) {
            return ResponseDTO.userErrorParam("章节细纲不存在或无权访问");
        }
        return ResponseDTO.ok(SmartBeanUtil.copy(entity, ChapterOutlineVO.class));
    }

    /**
     * 创建章节细纲。
     * <p>
     * 细纲是写作前规划，只写 MySQL，不进入 Neo4j。
     *
     * @param addForm 章节细纲创建表单
     * @param requestUserId 当前登录用户ID
     * @return 创建结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> createOutline(ChapterOutlineAddForm addForm, Long requestUserId) {
        ResponseDTO<NovelProjectEntity> projectValidateResult = validateOwnedProject(addForm.getProjectId(), requestUserId);
        if (!projectValidateResult.getOk()) {
            return ResponseDTO.error(projectValidateResult);
        }

        ChapterOutlineEntity oldEntity = getOwnedOutlineByProjectAndChapter(addForm.getProjectId(), addForm.getChapterNumber(), requestUserId);
        if (Objects.nonNull(oldEntity)) {
            return ResponseDTO.userErrorParam("该章节已存在细纲");
        }

        ChapterOutlineEntity entity = SmartBeanUtil.copy(addForm, ChapterOutlineEntity.class);
        entity.setCreateUserId(requestUserId);
        entity.setDeletedFlag(Boolean.FALSE);
        chapterOutlineDao.insert(entity);
        return ResponseDTO.ok();
    }

    /**
     * 编辑章节细纲。
     *
     * @param updateForm 章节细纲编辑表单
     * @param requestUserId 当前登录用户ID
     * @return 编辑结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> updateOutline(ChapterOutlineUpdateForm updateForm, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        ChapterOutlineEntity oldEntity = getOwnedOutline(updateForm.getOutlineId(), requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("章节细纲不存在或无权访问");
        }
        if (!Objects.equals(oldEntity.getProjectId(), updateForm.getProjectId())) {
            return ResponseDTO.userErrorParam("章节细纲不能移动到其他项目");
        }

        ChapterOutlineEntity updateEntity = SmartBeanUtil.copy(updateForm, ChapterOutlineEntity.class);
        updateEntity.setId(updateForm.getOutlineId());
        updateEntity.setProjectId(oldEntity.getProjectId());
        updateEntity.setCreateUserId(requestUserId);
        updateEntity.setDeletedFlag(Boolean.FALSE);
        chapterOutlineDao.updateById(updateEntity);
        return ResponseDTO.ok();
    }

    /**
     * 归档章节细纲。
     *
     * @param outlineId 章节细纲ID
     * @param requestUserId 当前登录用户ID
     * @return 归档结果
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> archiveOutline(Long outlineId, Long requestUserId) {
        ResponseDTO<String> userValidateResult = validateRequestUser(requestUserId);
        if (!userValidateResult.getOk()) {
            return userValidateResult;
        }

        ChapterOutlineEntity oldEntity = getOwnedOutline(outlineId, requestUserId);
        if (Objects.isNull(oldEntity)) {
            return ResponseDTO.userErrorParam("章节细纲不存在或无权访问");
        }

        ChapterOutlineEntity archiveEntity = new ChapterOutlineEntity();
        archiveEntity.setId(outlineId);
        archiveEntity.setDeletedFlag(Boolean.TRUE);
        chapterOutlineDao.updateById(archiveEntity);
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
     * 校验章节关联的卷是否属于同一项目和同一用户。
     *
     * @param volumeId 卷ID，可为空
     * @param projectId 章节所属项目ID
     * @param requestUserId 当前登录用户ID
     * @return 校验结果
     */
    private ResponseDTO<String> validateChapterVolume(Long volumeId, Long projectId, Long requestUserId) {
        if (Objects.isNull(volumeId)) {
            return ResponseDTO.ok();
        }

        NovelVolumeEntity volumeEntity = novelVolumeDao.selectOne(new LambdaQueryWrapper<NovelVolumeEntity>()
                .eq(NovelVolumeEntity::getId, volumeId)
                .eq(NovelVolumeEntity::getCreateUserId, requestUserId)
                .eq(NovelVolumeEntity::getDeletedFlag, Boolean.FALSE));
        if (Objects.isNull(volumeEntity) || !Objects.equals(volumeEntity.getProjectId(), projectId)) {
            return ResponseDTO.userErrorParam("卷不存在或无权访问");
        }
        return ResponseDTO.ok();
    }

    /**
     * 查询当前用户未归档的章节。
     *
     * @param chapterId 章节ID
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档章节，查不到返回 null
     */
    private NovelChapterEntity getOwnedChapter(Long chapterId, Long requestUserId) {
        return novelChapterDao.selectOne(new LambdaQueryWrapper<NovelChapterEntity>()
                .eq(NovelChapterEntity::getId, chapterId)
                .eq(NovelChapterEntity::getCreateUserId, requestUserId)
                .eq(NovelChapterEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 查询当前用户未归档的章节细纲。
     *
     * @param outlineId 章节细纲ID
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档细纲，查不到返回 null
     */
    private ChapterOutlineEntity getOwnedOutline(Long outlineId, Long requestUserId) {
        return chapterOutlineDao.selectOne(new LambdaQueryWrapper<ChapterOutlineEntity>()
                .eq(ChapterOutlineEntity::getId, outlineId)
                .eq(ChapterOutlineEntity::getCreateUserId, requestUserId)
                .eq(ChapterOutlineEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 按项目和章节号查询当前用户未归档的章节细纲。
     *
     * @param projectId 项目ID
     * @param chapterNumber 章节号
     * @param requestUserId 当前登录用户ID
     * @return 当前用户拥有的未归档细纲，查不到返回 null
     */
    private ChapterOutlineEntity getOwnedOutlineByProjectAndChapter(Long projectId, Integer chapterNumber, Long requestUserId) {
        return chapterOutlineDao.selectOne(new LambdaQueryWrapper<ChapterOutlineEntity>()
                .eq(ChapterOutlineEntity::getProjectId, projectId)
                .eq(ChapterOutlineEntity::getChapterNumber, chapterNumber)
                .eq(ChapterOutlineEntity::getCreateUserId, requestUserId)
                .eq(ChapterOutlineEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 构建章节分页查询条件，用户隔离条件在这里强制追加。
     *
     * @param queryForm 查询表单
     * @param requestUserId 当前登录用户ID
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<NovelChapterEntity> buildChapterPageWrapper(NovelChapterQueryForm queryForm, Long requestUserId) {
        LambdaQueryWrapper<NovelChapterEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NovelChapterEntity::getProjectId, queryForm.getProjectId());
        queryWrapper.eq(NovelChapterEntity::getCreateUserId, requestUserId);
        queryWrapper.eq(NovelChapterEntity::getDeletedFlag, Boolean.FALSE);
        queryWrapper.eq(Objects.nonNull(queryForm.getVolumeId()), NovelChapterEntity::getVolumeId, queryForm.getVolumeId());
        queryWrapper.eq(Objects.nonNull(queryForm.getChapterNumber()), NovelChapterEntity::getChapterNumber, queryForm.getChapterNumber());
        queryWrapper.like(StringUtils.isNotBlank(queryForm.getTitle()), NovelChapterEntity::getTitle, queryForm.getTitle());
        queryWrapper.eq(StringUtils.isNotBlank(queryForm.getStatus()), NovelChapterEntity::getStatus, queryForm.getStatus());
        queryWrapper.orderByAsc(NovelChapterEntity::getChapterNumber);
        return queryWrapper;
    }

    /**
     * 构建章节细纲分页查询条件，用户隔离条件在这里强制追加。
     *
     * @param queryForm 查询表单
     * @param requestUserId 当前登录用户ID
     * @return MyBatis-Plus 查询条件
     */
    private LambdaQueryWrapper<ChapterOutlineEntity> buildOutlinePageWrapper(ChapterOutlineQueryForm queryForm, Long requestUserId) {
        LambdaQueryWrapper<ChapterOutlineEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChapterOutlineEntity::getProjectId, queryForm.getProjectId());
        queryWrapper.eq(ChapterOutlineEntity::getCreateUserId, requestUserId);
        queryWrapper.eq(ChapterOutlineEntity::getDeletedFlag, Boolean.FALSE);
        queryWrapper.eq(Objects.nonNull(queryForm.getChapterNumber()), ChapterOutlineEntity::getChapterNumber, queryForm.getChapterNumber());
        queryWrapper.orderByAsc(ChapterOutlineEntity::getChapterNumber);
        return queryWrapper;
    }

    /**
     * 把 SmartAdmin 分页参数转换成章节实体分页对象。
     *
     * @param queryForm 查询表单
     * @return 章节实体分页对象
     */
    @SuppressWarnings("unchecked")
    private Page<NovelChapterEntity> convertChapterPage(NovelChapterQueryForm queryForm) {
        return (Page<NovelChapterEntity>) SmartPageUtil.convert2PageQuery(queryForm);
    }

    /**
     * 把 SmartAdmin 分页参数转换成章节细纲实体分页对象。
     *
     * @param queryForm 查询表单
     * @return 章节细纲实体分页对象
     */
    @SuppressWarnings("unchecked")
    private Page<ChapterOutlineEntity> convertOutlinePage(ChapterOutlineQueryForm queryForm) {
        return (Page<ChapterOutlineEntity>) SmartPageUtil.convert2PageQuery(queryForm);
    }

    /**
     * 组装可同步到 Neo4j Chapter 节点的白名单字段。
     *
     * @param entity 章节实体
     * @return Chapter 节点属性
     */
    private Map<String, Object> buildChapterGraphProps(NovelChapterEntity entity) {
        Map<String, Object> props = new LinkedHashMap<>();
        putIfNotNull(props, "volumeId", entity.getVolumeId());
        putIfNotNull(props, "title", entity.getTitle());
        putIfNotNull(props, "summary", entity.getSummary());
        putIfNotNull(props, "pov", entity.getPov());
        putIfNotNull(props, "wordCount", entity.getWordCount());
        putIfNotNull(props, "status", entity.getStatus());
        putIfNotNull(props, "embedding", entity.getEmbedding());
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
