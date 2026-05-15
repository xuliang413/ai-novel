package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.novel.dao.NovelChapterDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelChapterQueryForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelChapterVO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 小说章节服务。
 *
 * M0 只负责章节查询、编号生成和草稿保存；章节审阅、发布和回滚后续再补。
 */
@Service
public class NovelChapterService {

    @Resource
    private NovelChapterDao novelChapterDao;

    /**
     * 分页查询项目章节，默认按章节序号正序返回。
     */
    public ResponseDTO<PageResult<NovelChapterVO>> query(NovelChapterQueryForm queryForm) {
        Page<NovelChapterEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize(), true);
        LambdaQueryWrapper<NovelChapterEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NovelChapterEntity::getProjectId, queryForm.getProjectId());
        wrapper.eq(StringUtils.isNotBlank(queryForm.getStatus()), NovelChapterEntity::getStatus, queryForm.getStatus());
        wrapper.orderByAsc(NovelChapterEntity::getChapterNo);
        Page<NovelChapterEntity> resultPage = novelChapterDao.selectPage(page, wrapper);
        PageResult<NovelChapterVO> pageResult = SmartPageUtil.convert2PageResult(resultPage, resultPage.getRecords(), NovelChapterVO.class);
        return ResponseDTO.ok(pageResult);
    }

    /**
     * 按项目和章节号查询章节，用于 mock 生成时覆盖同一章节草稿。
     */
    public NovelChapterEntity getByProjectAndNo(Long projectId, Integer chapterNo) {
        return novelChapterDao.selectOne(new LambdaQueryWrapper<NovelChapterEntity>()
                .eq(NovelChapterEntity::getProjectId, projectId)
                .eq(NovelChapterEntity::getChapterNo, chapterNo));
    }

    /**
     * 按章节 ID 查询章节。
     */
    public NovelChapterEntity getById(Long chapterId) {
        return novelChapterDao.selectById(chapterId);
    }

    /**
     * 计算下一个章节号。
     *
     * 当前实现基于数据库已有最大章节号递增；并发生成的严格锁定后续状态机阶段再处理。
     */
    public Integer getNextChapterNo(Long projectId) {
        NovelChapterEntity latest = novelChapterDao.selectOne(new LambdaQueryWrapper<NovelChapterEntity>()
                .eq(NovelChapterEntity::getProjectId, projectId)
                .orderByDesc(NovelChapterEntity::getChapterNo)
                .last("limit 1"));
        return latest == null ? 1 : latest.getChapterNo() + 1;
    }

    /**
     * 保存章节：新增章节插入，已有章节则更新草稿内容。
     */
    public void save(NovelChapterEntity chapter) {
        if (chapter.getChapterId() == null) {
            novelChapterDao.insert(chapter);
        } else {
            novelChapterDao.updateById(chapter);
        }
    }
}
