package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueStatusEnum;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelClueDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelLocationDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelCharacterAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelClueAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelLocationAddForm;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
     * 新增资产前校验项目存在，返回项目对象用于补写图谱 Project 节点。
     */
    private NovelProjectEntity getProjectOrNull(Long projectId) {
        return novelProjectService.getAvailableProject(projectId);
    }
}
