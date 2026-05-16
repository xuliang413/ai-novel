package net.lab1024.sa.admin.module.business.novel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.business.novel.dao.*;
import net.lab1024.sa.admin.module.business.novel.domain.entity.*;
import net.lab1024.sa.admin.module.business.novel.domain.form.NovelIdForm;
import net.lab1024.sa.admin.module.business.novel.domain.vo.ChapterProgressItemVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.PendingSessionItemVO;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 工作台仪表盘接口 —— 项目总览、进度统计、待处理事项
 */
@RestController
@RequestMapping("/novel/dashboard")
@Tag(name = "AI 小说 - 仪表盘")
public class NovelDashboardController {

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

    @Operation(summary = "项目资产统计")
    @PostMapping("/asset-summary")
    public ResponseDTO<Map<String, Object>> assetSummary(@RequestBody @Valid NovelIdForm form) {
        Long pid = form.getId();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("characterCount", characterDao.selectCount(wrapChar(pid)).intValue());
        map.put("locationCount", locationDao.selectCount(wrapLoc(pid)).intValue());
        map.put("clueCount", clueDao.selectCount(wrapClue(pid)).intValue());
        map.put("itemCount", itemDao.selectCount(wrapItem(pid)).intValue());
        map.put("eventCount", eventDao.selectCount(wrapEvent(pid)).intValue());
        map.put("cheatCount", cheatDao.selectCount(wrapCheat(pid)).intValue());
        map.put("aliasCount", aliasDao.selectCount(wrapAlias(pid)).intValue());
        map.put("ruleCount", ruleDao.selectCount(wrapRule(pid)).intValue());
        map.put("volumeCount", volumeDao.selectCount(wrapVol(pid)).intValue());
        map.put("chapterCount", chapterDao.selectCount(wrapCh(pid)).intValue());
        map.put("totalWords", totalWords(pid));
        return ResponseDTO.ok(map);
    }

    @Operation(summary = "章节写作进度")
    @PostMapping("/chapter-progress")
    public ResponseDTO<List<ChapterProgressItemVO>> chapterProgress(@RequestBody @Valid NovelIdForm form) {
        List<NovelChapterEntity> chapters = chapterDao.selectList(wrapCh(form.getId()).orderByAsc(NovelChapterEntity::getChapterNo));
        List<ChapterProgressItemVO> list = new ArrayList<>();
        for (NovelChapterEntity ch : chapters) {
            list.add(ChapterProgressItemVO.builder()
                    .chapterId(ch.getChapterId())
                    .chapterNo(ch.getChapterNo())
                    .title(ch.getTitle())
                    .status(ch.getStatus())
                    .wordCount(ch.getContent() != null ? ch.getContent().length() : 0)
                    .build());
        }
        return ResponseDTO.ok(list);
    }

    @Operation(summary = "最近写作日志")
    @PostMapping("/recent-logs")
    public ResponseDTO<List<WritingLogEntity>> recentLogs(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(writingLogDao.selectList(new LambdaQueryWrapper<WritingLogEntity>()
                .eq(WritingLogEntity::getProjectId, form.getId())
                .orderByDesc(WritingLogEntity::getCreateTime)
                .last("limit 10")));
    }

    @Operation(summary = "最近图谱变更")
    @PostMapping("/recent-patches")
    public ResponseDTO<List<GraphChangeLogEntity>> recentPatches(@RequestBody @Valid NovelIdForm form) {
        return ResponseDTO.ok(graphChangeLogDao.selectList(new LambdaQueryWrapper<GraphChangeLogEntity>()
                .eq(GraphChangeLogEntity::getProjectId, form.getId())
                .orderByDesc(GraphChangeLogEntity::getCreateTime)
                .last("limit 10")));
    }

    @Operation(summary = "待处理会话")
    @PostMapping("/pending-sessions")
    public ResponseDTO<List<PendingSessionItemVO>> pendingSessions(@RequestBody @Valid NovelIdForm form) {
        List<ChapterGenerationSessionEntity> sessions = sessionDao.selectList(new LambdaQueryWrapper<ChapterGenerationSessionEntity>()
                .eq(ChapterGenerationSessionEntity::getProjectId, form.getId())
                .notIn(ChapterGenerationSessionEntity::getStatus, "SUCCESS", "FAILED", "CANCELED", "INTERRUPTED")
                .orderByDesc(ChapterGenerationSessionEntity::getCreateTime));
        List<PendingSessionItemVO> list = new ArrayList<>();
        for (ChapterGenerationSessionEntity s : sessions) {
            list.add(PendingSessionItemVO.builder()
                    .sessionId(s.getSessionId())
                    .chapterNo(s.getChapterNo())
                    .status(s.getStatus())
                    .provider(s.getProvider())
                    .createdAt(s.getCreateTime())
                    .build());
        }
        return ResponseDTO.ok(list);
    }

    @Operation(summary = "项目总字数")
    @PostMapping("/total-words")
    public ResponseDTO<Map<String, Object>> totalWordCount(@RequestBody @Valid NovelIdForm form) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalWords", totalWords(form.getId()));
        return ResponseDTO.ok(result);
    }

    private Integer totalWords(Long projectId) {
        List<NovelChapterEntity> chapters = chapterDao.selectList(wrapCh(projectId));
        return chapters.stream().mapToInt(ch -> ch.getContent() != null ? ch.getContent().length() : 0).sum();
    }

    private LambdaQueryWrapper<NovelCharacterEntity> wrapChar(Long pid) {
        return new LambdaQueryWrapper<NovelCharacterEntity>().eq(NovelCharacterEntity::getProjectId, pid).eq(NovelCharacterEntity::getDeletedFlag, false);
    }
    private LambdaQueryWrapper<NovelLocationEntity> wrapLoc(Long pid) {
        return new LambdaQueryWrapper<NovelLocationEntity>().eq(NovelLocationEntity::getProjectId, pid).eq(NovelLocationEntity::getDeletedFlag, false);
    }
    private LambdaQueryWrapper<NovelClueEntity> wrapClue(Long pid) {
        return new LambdaQueryWrapper<NovelClueEntity>().eq(NovelClueEntity::getProjectId, pid).eq(NovelClueEntity::getDeletedFlag, false);
    }
    private LambdaQueryWrapper<NovelItemEntity> wrapItem(Long pid) {
        return new LambdaQueryWrapper<NovelItemEntity>().eq(NovelItemEntity::getProjectId, pid).eq(NovelItemEntity::getDeletedFlag, false);
    }
    private LambdaQueryWrapper<NovelEventEntity> wrapEvent(Long pid) {
        return new LambdaQueryWrapper<NovelEventEntity>().eq(NovelEventEntity::getProjectId, pid).eq(NovelEventEntity::getDeletedFlag, false);
    }
    private LambdaQueryWrapper<NovelCheatEntity> wrapCheat(Long pid) {
        return new LambdaQueryWrapper<NovelCheatEntity>().eq(NovelCheatEntity::getProjectId, pid).eq(NovelCheatEntity::getDeletedFlag, false);
    }
    private LambdaQueryWrapper<NovelAliasEntity> wrapAlias(Long pid) {
        return new LambdaQueryWrapper<NovelAliasEntity>().eq(NovelAliasEntity::getProjectId, pid).eq(NovelAliasEntity::getDeletedFlag, false);
    }
    private LambdaQueryWrapper<NovelNarrativeRuleEntity> wrapRule(Long pid) {
        return new LambdaQueryWrapper<NovelNarrativeRuleEntity>().eq(NovelNarrativeRuleEntity::getProjectId, pid).eq(NovelNarrativeRuleEntity::getDeletedFlag, false);
    }
    private LambdaQueryWrapper<NovelVolumeEntity> wrapVol(Long pid) {
        return new LambdaQueryWrapper<NovelVolumeEntity>().eq(NovelVolumeEntity::getProjectId, pid).eq(NovelVolumeEntity::getDeletedFlag, false);
    }
    private LambdaQueryWrapper<NovelChapterEntity> wrapCh(Long pid) {
        return new LambdaQueryWrapper<NovelChapterEntity>().eq(NovelChapterEntity::getProjectId, pid).eq(NovelChapterEntity::getDeletedFlag, false);
    }
}
