package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.constant.NovelChapterStatusEnum;
import net.lab1024.sa.admin.module.business.novel.dao.NovelChapterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelClueDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelLocationDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelProjectDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelDashboardVO;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 小说仪表盘服务 —— 项目级统计概览。
 * <p>
 * 全部通过MySQL COUNT聚合查询, 不涉及Neo4j。
 * 所有查询强制过滤createUserId和deletedFlag实现用户隔离。
 *
 * @Author AI-Novel
 */
@Slf4j
@Service
public class NovelDashboardService {

    @Resource
    private NovelProjectDao novelProjectDao;

    @Resource
    private NovelChapterDao novelChapterDao;

    @Resource
    private NovelCharacterDao novelCharacterDao;

    @Resource
    private NovelClueDao novelClueDao;

    @Resource
    private NovelLocationDao novelLocationDao;

    /**
     * 获取项目仪表盘统计数据。
     * <p>
     * 统计维度: 总字数(已发布章节)、章节数(总数/已发布/草稿)、角色数、线索数、地点数。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前用户ID, 用于权限校验
     * @return 仪表盘VO, 包含所有统计维度
     */
    public ResponseDTO<NovelDashboardVO> getDashboard(Long projectId, Long requestUserId) {
        NovelProjectEntity project = novelProjectDao.selectById(projectId);
        if (project == null || !Objects.equals(project.getCreateUserId(), requestUserId)) {
            return ResponseDTO.userErrorParam("项目不存在或无权访问");
        }

        NovelDashboardVO vo = new NovelDashboardVO();
        vo.setProjectId(project.getId());
        vo.setProjectName(project.getName());
        vo.setProjectStatus(project.getStatus());
        vo.setTargetTotalWords(project.getTargetTotalWords());

        // 章节统计: 一次查出该项目下所有未归档章节，在内存中分组计数
        List<NovelChapterEntity> chapters = novelChapterDao.selectList(
                new LambdaQueryWrapper<NovelChapterEntity>()
                        .eq(NovelChapterEntity::getProjectId, projectId)
                        .eq(NovelChapterEntity::getDeletedFlag, false));

        long totalChapters = chapters.size();
        long publishedChapters = chapters.stream()
                .filter(ch -> NovelChapterStatusEnum.PUBLISHED.getValue().equals(ch.getStatus()))
                .count();
        long totalWords = chapters.stream()
                .filter(ch -> NovelChapterStatusEnum.PUBLISHED.getValue().equals(ch.getStatus()))
                .mapToLong(ch -> ch.getWordCount() != null ? ch.getWordCount() : 0)
                .sum();
        long draftChapters = chapters.stream()
                .filter(ch -> NovelChapterStatusEnum.DRAFT.getValue().equals(ch.getStatus()))
                .count();

        vo.setTotalChapters(totalChapters);
        vo.setPublishedChapters(publishedChapters);
        vo.setDraftChapters(draftChapters);
        vo.setTotalWords(totalWords);

        // 角色数
        Long characterCount = novelCharacterDao.selectCount(
                new LambdaQueryWrapper<NovelCharacterEntity>()
                        .eq(NovelCharacterEntity::getProjectId, projectId)
                        .eq(NovelCharacterEntity::getDeletedFlag, false));
        vo.setCharacterCount(characterCount);

        // 线索数
        Long clueCount = novelClueDao.selectCount(
                new LambdaQueryWrapper<NovelClueEntity>()
                        .eq(NovelClueEntity::getProjectId, projectId)
                        .eq(NovelClueEntity::getDeletedFlag, false));
        vo.setClueCount(clueCount);

        // 地点数
        Long locationCount = novelLocationDao.selectCount(
                new LambdaQueryWrapper<NovelLocationEntity>()
                        .eq(NovelLocationEntity::getProjectId, projectId)
                        .eq(NovelLocationEntity::getDeletedFlag, false));
        vo.setLocationCount(locationCount);

        return ResponseDTO.ok(vo);
    }
}
