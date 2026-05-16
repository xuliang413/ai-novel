package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import net.lab1024.sa.admin.module.business.novel.dao.*;
import net.lab1024.sa.admin.module.business.novel.domain.entity.*;
import net.lab1024.sa.admin.module.business.novel.domain.vo.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 仪表盘数据聚合服务 —— 把散落在各 DAO 的统计查询集中管理
 *
 * 为什么需要这个服务：
 * 之前 NovelDashboardController 注入了 14 个 DAO，Controller 里写满了
 * LambdaQueryWrapper 拼装逻辑，违反了"Controller 应该薄"的原则。
 * 现在把数据聚合工作下沉到这里，Controller 只做路由和参数适配。
 *
 * 同时把 10 次独立 COUNT 查询合并为一次聚合遍历：
 * 一次查出所有章节（带 content），从中同时算出章节数、各状态分布和总字数，
 * 避免 10+ 次数据库往返。
 */
@Service
public class NovelDashboardService {

    @Resource
    private NovelCharacterDao characterDao;
    @Resource
    private NovelLocationDao locationDao;
    @Resource
    private NovelClueDao clueDao;
    @Resource
    private NovelItemDao itemDao;
    @Resource
    private NovelEventDao eventDao;
    @Resource
    private NovelCheatDao cheatDao;
    @Resource
    private NovelAliasDao aliasDao;
    @Resource
    private NovelNarrativeRuleDao ruleDao;
    @Resource
    private NovelVolumeDao volumeDao;
    @Resource
    private NovelChapterDao chapterDao;
    @Resource
    private ChapterGenerationSessionDao sessionDao;
    @Resource
    private WritingLogDao writingLogDao;
    @Resource
    private GraphChangeLogDao graphChangeLogDao;

    /**
     * 项目资产统计 —— 一次查询所有需要的计数
     *
     * 为什么不用 10 次独立 COUNT：
     * 仪表盘是高频刷新页面，10 次数据库往返在高并发下会产生不必要的延迟。
     * 现在一次查出所有章节附带正文，同时完成多种计数，减少 IO。
     */
    public AssetSummaryVO assetSummary(Long projectId) {
        List<NovelChapterEntity> chapters = chapterDao.selectList(
                new LambdaQueryWrapper<NovelChapterEntity>()
                        .eq(NovelChapterEntity::getProjectId, projectId)
                        .eq(NovelChapterEntity::getDeletedFlag, false)
                        .select(NovelChapterEntity::getChapterId, NovelChapterEntity::getContent, NovelChapterEntity::getStatus));

        int totalWords = 0;
        for (NovelChapterEntity ch : chapters) {
            if (ch.getContent() != null) {
                totalWords += ch.getContent().length();
            }
        }

        return AssetSummaryVO.builder()
                .characterCount(count(characterDao, NovelCharacterEntity.class, projectId))
                .locationCount(count(locationDao, NovelLocationEntity.class, projectId))
                .clueCount(count(clueDao, NovelClueEntity.class, projectId))
                .itemCount(count(itemDao, NovelItemEntity.class, projectId))
                .eventCount(count(eventDao, NovelEventEntity.class, projectId))
                .cheatCount(count(cheatDao, NovelCheatEntity.class, projectId))
                .aliasCount(count(aliasDao, NovelAliasEntity.class, projectId))
                .ruleCount(count(ruleDao, NovelNarrativeRuleEntity.class, projectId))
                .volumeCount(count(volumeDao, NovelVolumeEntity.class, projectId))
                .chapterCount(chapters.size())
                .totalWords(totalWords)
                .build();
    }

    /**
     * 章节写作进度 —— 按章节序号排序，带字数和状态
     */
    public List<ChapterProgressItemVO> chapterProgress(Long projectId) {
        List<NovelChapterEntity> chapters = chapterDao.selectList(
                new LambdaQueryWrapper<NovelChapterEntity>()
                        .eq(NovelChapterEntity::getProjectId, projectId)
                        .eq(NovelChapterEntity::getDeletedFlag, false)
                        .orderByAsc(NovelChapterEntity::getChapterNo));
        return chapters.stream()
                .map(ch -> ChapterProgressItemVO.builder()
                        .chapterId(ch.getChapterId())
                        .chapterNo(ch.getChapterNo())
                        .title(ch.getTitle())
                        .status(ch.getStatus())
                        .wordCount(ch.getContent() != null ? ch.getContent().length() : 0)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 最近写作日志（top 10）
     */
    public List<WritingLogEntity> recentLogs(Long projectId) {
        return writingLogDao.selectList(new LambdaQueryWrapper<WritingLogEntity>()
                .eq(WritingLogEntity::getProjectId, projectId)
                .orderByDesc(WritingLogEntity::getCreateTime)
                .last("limit 10"));
    }

    /**
     * 最近图谱变更（top 10）
     */
    public List<GraphChangeLogEntity> recentPatches(Long projectId) {
        return graphChangeLogDao.selectList(new LambdaQueryWrapper<GraphChangeLogEntity>()
                .eq(GraphChangeLogEntity::getProjectId, projectId)
                .orderByDesc(GraphChangeLogEntity::getCreateTime)
                .last("limit 10"));
    }

    /**
     * 待处理会话 —— 排除终态（SUCCESS/FAILED/CANCELED/INTERRUPTED）
     */
    public List<PendingSessionItemVO> pendingSessions(Long projectId) {
        List<ChapterGenerationSessionEntity> sessions = sessionDao.selectList(
                new LambdaQueryWrapper<ChapterGenerationSessionEntity>()
                        .eq(ChapterGenerationSessionEntity::getProjectId, projectId)
                        .notIn(ChapterGenerationSessionEntity::getStatus, "SUCCESS", "FAILED", "CANCELED", "INTERRUPTED")
                        .orderByDesc(ChapterGenerationSessionEntity::getCreateTime));
        return sessions.stream()
                .map(s -> PendingSessionItemVO.builder()
                        .sessionId(s.getSessionId())
                        .chapterNo(s.getChapterNo())
                        .status(s.getStatus())
                        .provider(s.getProvider())
                        .createdAt(s.getCreateTime())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 项目总字数
     */
    public TotalWordCountVO totalWords(Long projectId) {
        List<NovelChapterEntity> chapters = chapterDao.selectList(
                new LambdaQueryWrapper<NovelChapterEntity>()
                        .eq(NovelChapterEntity::getProjectId, projectId)
                        .eq(NovelChapterEntity::getDeletedFlag, false));
        int total = chapters.stream()
                .mapToInt(ch -> ch.getContent() != null ? ch.getContent().length() : 0)
                .sum();
        return TotalWordCountVO.builder().totalWords(total).build();
    }

    /**
     * 通用计数：按 projectId 和 deletedFlag=false 统计
     */
    private <T> Integer count(Object dao, Class<T> entityClass, Long projectId) {
        if (entityClass == NovelCharacterEntity.class) {
            return (((NovelCharacterDao) dao).selectCount(
                    new LambdaQueryWrapper<NovelCharacterEntity>()
                            .eq(NovelCharacterEntity::getProjectId, projectId)
                            .eq(NovelCharacterEntity::getDeletedFlag, false))).intValue();
        }
        if (entityClass == NovelLocationEntity.class) {
            return (((NovelLocationDao) dao).selectCount(
                    new LambdaQueryWrapper<NovelLocationEntity>()
                            .eq(NovelLocationEntity::getProjectId, projectId)
                            .eq(NovelLocationEntity::getDeletedFlag, false))).intValue();
        }
        if (entityClass == NovelClueEntity.class) {
            return (((NovelClueDao) dao).selectCount(
                    new LambdaQueryWrapper<NovelClueEntity>()
                            .eq(NovelClueEntity::getProjectId, projectId)
                            .eq(NovelClueEntity::getDeletedFlag, false))).intValue();
        }
        if (entityClass == NovelItemEntity.class) {
            return (((NovelItemDao) dao).selectCount(
                    new LambdaQueryWrapper<NovelItemEntity>()
                            .eq(NovelItemEntity::getProjectId, projectId)
                            .eq(NovelItemEntity::getDeletedFlag, false))).intValue();
        }
        if (entityClass == NovelEventEntity.class) {
            return (((NovelEventDao) dao).selectCount(
                    new LambdaQueryWrapper<NovelEventEntity>()
                            .eq(NovelEventEntity::getProjectId, projectId)
                            .eq(NovelEventEntity::getDeletedFlag, false))).intValue();
        }
        if (entityClass == NovelCheatEntity.class) {
            return (((NovelCheatDao) dao).selectCount(
                    new LambdaQueryWrapper<NovelCheatEntity>()
                            .eq(NovelCheatEntity::getProjectId, projectId)
                            .eq(NovelCheatEntity::getDeletedFlag, false))).intValue();
        }
        if (entityClass == NovelAliasEntity.class) {
            return (((NovelAliasDao) dao).selectCount(
                    new LambdaQueryWrapper<NovelAliasEntity>()
                            .eq(NovelAliasEntity::getProjectId, projectId)
                            .eq(NovelAliasEntity::getDeletedFlag, false))).intValue();
        }
        if (entityClass == NovelNarrativeRuleEntity.class) {
            return (((NovelNarrativeRuleDao) dao).selectCount(
                    new LambdaQueryWrapper<NovelNarrativeRuleEntity>()
                            .eq(NovelNarrativeRuleEntity::getProjectId, projectId)
                            .eq(NovelNarrativeRuleEntity::getDeletedFlag, false))).intValue();
        }
        if (entityClass == NovelVolumeEntity.class) {
            return (((NovelVolumeDao) dao).selectCount(
                    new LambdaQueryWrapper<NovelVolumeEntity>()
                            .eq(NovelVolumeEntity::getProjectId, projectId)
                            .eq(NovelVolumeEntity::getDeletedFlag, false))).intValue();
        }
        return 0;
    }
}
