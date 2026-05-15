package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectStatusEnum;
import net.lab1024.sa.admin.module.business.novel.dao.NovelProjectDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelProjectAddForm;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelProjectQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelProjectVO;
import net.lab1024.sa.admin.util.AdminRequestUtil;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 项目服务——角色、地点、线索、章节都必须归属到一个项目下。
 */
@Service
public class NovelProjectService {

    @Resource
    private NovelProjectDao novelProjectDao;

    @Resource
    private NovelGraphService novelGraphService;

    /**
     * 新建小说项目，并同步写入 Neo4j 的 Project 节点。
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> add(NovelProjectAddForm addForm) {
        NovelProjectEntity entity = SmartBeanUtil.copy(addForm, NovelProjectEntity.class);
        entity.setStatus(StringUtils.defaultIfBlank(addForm.getStatus(), NovelProjectStatusEnum.ACTIVE.getValue()));
        entity.setCreateUserId(AdminRequestUtil.getRequestUserId());
        entity.setDeletedFlag(false);
        novelProjectDao.insert(entity);
        novelGraphService.mergeProject(entity);
        return ResponseDTO.ok(entity.getProjectId());
    }

    /**
     * 分页查询未删除的小说项目，供前端项目列表使用。
     */
    public ResponseDTO<PageResult<NovelProjectVO>> query(NovelProjectQueryForm queryForm) {
        Page<NovelProjectEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize(), true);
        LambdaQueryWrapper<NovelProjectEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NovelProjectEntity::getDeletedFlag, false);
        wrapper.like(StringUtils.isNotBlank(queryForm.getProjectName()), NovelProjectEntity::getProjectName, queryForm.getProjectName());
        wrapper.eq(StringUtils.isNotBlank(queryForm.getStatus()), NovelProjectEntity::getStatus, queryForm.getStatus());
        wrapper.orderByDesc(NovelProjectEntity::getCreateTime);
        Page<NovelProjectEntity> resultPage = novelProjectDao.selectPage(page, wrapper);
        PageResult<NovelProjectVO> pageResult = SmartPageUtil.convert2PageResult(resultPage, resultPage.getRecords(), NovelProjectVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 查询可用项目。
     *
     * 业务写入前统一用此方法校验项目是否存在，避免产生孤立的角色、地点、线索和章节。
     */
    public NovelProjectEntity getAvailableProject(Long projectId) {
        return novelProjectDao.selectOne(new LambdaQueryWrapper<NovelProjectEntity>()
                .eq(NovelProjectEntity::getProjectId, projectId)
                .eq(NovelProjectEntity::getDeletedFlag, false));
    }
}
