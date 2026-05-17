package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterRoleEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCharacterStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelClueSubTypeEnum;
import net.lab1024.sa.admin.module.business.novel.dao.ChapterOutlineDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterLocationDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCharacterRelationDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelChapterAppearanceDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelChapterDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelClueDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelLocationDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelProjectDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelVolumeDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.ChapterOutlineEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterRelationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterAppearanceEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelVolumeEntity;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelRetrieveContextVO;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 小说写作前上下文检索服务。
 * <p>
 * 负责把当前项目、章节、角色、地点、线索和关系整理成写作引擎可消费的结构化上下文，并在返回前按项目 Token 预算做分层压缩。
 *
 * @Author AI-Novel
 */
@Slf4j
@Service
public class NovelRetrieveService {

    /**
     * 默认上下文软预算，项目没有配置时使用。
     */
    private static final int DEFAULT_TOKEN_BUDGET = 6000;

    /**
     * 默认上下文硬上限，项目没有配置时使用。
     */
    private static final int DEFAULT_TOKEN_HARD_LIMIT = 8000;

    /**
     * 完整卡片标记，表示字段还没有被 Token 裁剪压缩。
     */
    private static final String DETAIL_FULL = "FULL";

    /**
     * 压缩卡片标记，表示长文本已经被裁剪成短摘要。
     */
    private static final String DETAIL_SHORT = "SHORT";

    /**
     * 项目 DAO，用于校验项目归属并读取预算配置。
     */
    @Resource
    private NovelProjectDao novelProjectDao;

    /**
     * 卷 DAO，用于读取当前章所属卷概要。
     */
    @Resource
    private NovelVolumeDao novelVolumeDao;

    /**
     * 章节 DAO，用于读取当前章元信息和上一章摘要。
     */
    @Resource
    private NovelChapterDao novelChapterDao;

    /**
     * 章节细纲 DAO，用于读取 ChapterIntent 的最高优先级来源。
     */
    @Resource
    private ChapterOutlineDao chapterOutlineDao;

    /**
     * 角色 DAO，用于构建角色候选池和 POV 兜底。
     */
    @Resource
    private NovelCharacterDao novelCharacterDao;

    /**
     * 角色当前位置 DAO，用于读取 POV 的 CURRENTLY_AT 审计记录。
     */
    @Resource
    private NovelCharacterLocationDao novelCharacterLocationDao;

    /**
     * 地点 DAO，用于读取当前位置或首章全量地点兜底。
     */
    @Resource
    private NovelLocationDao novelLocationDao;

    /**
     * 线索 DAO，用于读取写前需要推进的活跃线索。
     */
    @Resource
    private NovelClueDao novelClueDao;

    /**
     * 角色关系 DAO，用于补充候选角色之间的关键关系。
     */
    @Resource
    private NovelCharacterRelationDao novelCharacterRelationDao;

    /**
     * 章节出场 DAO，用于识别上一章已经出现过的实体并提高候选优先级。
     */
    @Resource
    private NovelChapterAppearanceDao novelChapterAppearanceDao;

    /**
     * 构建写作前上下文预览。
     *
     * @param projectId 项目ID
     * @param chapterNumber 目标章节号
     * @param userDirection 用户临时写作方向，可为空
     * @param requestUserId 当前登录用户ID
     * @return 结构化上下文预览
     */
    public ResponseDTO<NovelRetrieveContextVO> buildContextPreview(Long projectId, Integer chapterNumber, String userDirection, Long requestUserId) {
        ResponseDTO<String> validateResult = validateRetrieveRequest(projectId, chapterNumber, requestUserId);
        if (!validateResult.getOk()) {
            return ResponseDTO.error(validateResult);
        }

        NovelProjectEntity projectEntity = getOwnedProject(projectId, requestUserId);
        if (Objects.isNull(projectEntity)) {
            return ResponseDTO.userErrorParam("项目不存在或无权访问");
        }

        NovelChapterEntity currentChapter = getOwnedChapterByNumber(projectId, chapterNumber, requestUserId);
        NovelChapterEntity previousChapter = getPreviousChapter(projectId, chapterNumber, requestUserId);
        ChapterOutlineEntity outlineEntity = getOwnedOutlineByProjectAndChapter(projectId, chapterNumber, requestUserId);
        NovelVolumeEntity volumeEntity = getVolumeForChapter(currentChapter, requestUserId);
        List<NovelCharacterEntity> candidateCharacters = listCandidateCharacters(projectId, requestUserId);
        List<NovelChapterAppearanceEntity> previousAppearances = listPreviousAppearances(projectId, chapterNumber, requestUserId);
        Set<Long> previousCharacterIds = collectAppearanceIds(previousAppearances, "CHARACTER");
        Set<Long> previousLocationIds = collectAppearanceIds(previousAppearances, "LOCATION");

        String pov = resolvePov(currentChapter, projectEntity, candidateCharacters);
        NovelRetrieveContextVO context = new NovelRetrieveContextVO();
        context.setProjectId(projectId);
        context.setChapterNumber(chapterNumber);
        context.setPov(pov);
        context.setUserDirection(userDirection);
        context.setChapterIntent(resolveChapterIntent(outlineEntity, userDirection));
        context.setVolumeSummary(buildVolumeBlock(volumeEntity, context));
        context.setPreviousChapter(buildPreviousChapterBlock(previousChapter));
        context.setOutline(buildOutlineBlock(outlineEntity));

        context.setCharacters(buildCharacterCards(candidateCharacters, pov, previousCharacterIds));
        context.setLocations(buildLocationCards(projectId, requestUserId, chapterNumber, pov, context.getCharacters(), previousLocationIds, context));
        context.setClues(buildClueCards(projectId, requestUserId));
        context.setRelations(buildRelationCards(projectId, requestUserId, context.getCharacters(), candidateCharacters));

        applyTokenBudget(context, projectEntity);
        return ResponseDTO.ok(context);
    }

    /**
     * 校验上下文检索的基础入参。
     *
     * @param projectId 项目ID
     * @param chapterNumber 目标章节号
     * @param requestUserId 当前登录用户ID
     * @return 校验结果
     */
    private ResponseDTO<String> validateRetrieveRequest(Long projectId, Integer chapterNumber, Long requestUserId) {
        if (Objects.isNull(requestUserId)) {
            return ResponseDTO.userErrorParam("未获取到当前登录用户");
        }
        if (Objects.isNull(projectId)) {
            return ResponseDTO.userErrorParam("项目ID不能为空");
        }
        if (Objects.isNull(chapterNumber) || chapterNumber < 1) {
            return ResponseDTO.userErrorParam("章节号必须从1开始");
        }
        return ResponseDTO.ok();
    }

    /**
     * 查询当前用户拥有的未归档项目。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前登录用户ID
     * @return 项目实体，查不到返回 null
     */
    private NovelProjectEntity getOwnedProject(Long projectId, Long requestUserId) {
        return novelProjectDao.selectOne(new LambdaQueryWrapper<NovelProjectEntity>()
                .eq(NovelProjectEntity::getId, projectId)
                .eq(NovelProjectEntity::getCreateUserId, requestUserId)
                .eq(NovelProjectEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 按项目和章节号查询当前用户拥有的未归档章节。
     *
     * @param projectId 项目ID
     * @param chapterNumber 章节号
     * @param requestUserId 当前登录用户ID
     * @return 章节实体，查不到返回 null
     */
    private NovelChapterEntity getOwnedChapterByNumber(Long projectId, Integer chapterNumber, Long requestUserId) {
        return novelChapterDao.selectOne(new LambdaQueryWrapper<NovelChapterEntity>()
                .eq(NovelChapterEntity::getProjectId, projectId)
                .eq(NovelChapterEntity::getChapterNumber, chapterNumber)
                .eq(NovelChapterEntity::getCreateUserId, requestUserId)
                .eq(NovelChapterEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 查询上一章摘要来源。
     *
     * @param projectId 项目ID
     * @param chapterNumber 当前章节号
     * @param requestUserId 当前登录用户ID
     * @return 上一章实体，第一章或查不到时返回 null
     */
    private NovelChapterEntity getPreviousChapter(Long projectId, Integer chapterNumber, Long requestUserId) {
        if (chapterNumber <= 1) {
            return null;
        }
        return getOwnedChapterByNumber(projectId, chapterNumber - 1, requestUserId);
    }

    /**
     * 按项目和章节号查询章节细纲。
     *
     * @param projectId 项目ID
     * @param chapterNumber 章节号
     * @param requestUserId 当前登录用户ID
     * @return 章节细纲，查不到返回 null
     */
    private ChapterOutlineEntity getOwnedOutlineByProjectAndChapter(Long projectId, Integer chapterNumber, Long requestUserId) {
        return chapterOutlineDao.selectOne(new LambdaQueryWrapper<ChapterOutlineEntity>()
                .eq(ChapterOutlineEntity::getProjectId, projectId)
                .eq(ChapterOutlineEntity::getChapterNumber, chapterNumber)
                .eq(ChapterOutlineEntity::getCreateUserId, requestUserId)
                .eq(ChapterOutlineEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 读取当前章所属卷。
     *
     * @param currentChapter 当前章实体，可为空
     * @param requestUserId 当前登录用户ID
     * @return 卷实体；章节未归卷或卷不可访问时返回 null
     */
    private NovelVolumeEntity getVolumeForChapter(NovelChapterEntity currentChapter, Long requestUserId) {
        if (Objects.isNull(currentChapter) || Objects.isNull(currentChapter.getVolumeId())) {
            return null;
        }
        return novelVolumeDao.selectOne(new LambdaQueryWrapper<NovelVolumeEntity>()
                .eq(NovelVolumeEntity::getId, currentChapter.getVolumeId())
                .eq(NovelVolumeEntity::getProjectId, currentChapter.getProjectId())
                .eq(NovelVolumeEntity::getCreateUserId, requestUserId)
                .eq(NovelVolumeEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 查询角色候选池，并在内存中过滤死亡和归档角色。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前登录用户ID
     * @return 有效角色列表
     */
    private List<NovelCharacterEntity> listCandidateCharacters(Long projectId, Long requestUserId) {
        List<NovelCharacterEntity> characters = novelCharacterDao.selectList(new LambdaQueryWrapper<NovelCharacterEntity>()
                .eq(NovelCharacterEntity::getProjectId, projectId)
                .eq(NovelCharacterEntity::getCreateUserId, requestUserId)
                .eq(NovelCharacterEntity::getDeletedFlag, Boolean.FALSE));
        return Optional.ofNullable(characters).orElseGet(ArrayList::new)
                .stream()
                .filter(character -> !StringUtils.equals(character.getCurrentStatus(), NovelCharacterStatusEnum.DEAD.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 查询上一章出场实体，用于给候选池排序加权。
     *
     * @param projectId 项目ID
     * @param chapterNumber 当前章节号
     * @param requestUserId 当前登录用户ID
     * @return 上一章出场记录
     */
    private List<NovelChapterAppearanceEntity> listPreviousAppearances(Long projectId, Integer chapterNumber, Long requestUserId) {
        if (chapterNumber <= 1) {
            return List.of();
        }
        List<NovelChapterAppearanceEntity> appearances = novelChapterAppearanceDao.selectList(new LambdaQueryWrapper<NovelChapterAppearanceEntity>()
                .eq(NovelChapterAppearanceEntity::getProjectId, projectId)
                .eq(NovelChapterAppearanceEntity::getChapterNumber, chapterNumber - 1)
                .eq(NovelChapterAppearanceEntity::getCreateUserId, requestUserId)
                .eq(NovelChapterAppearanceEntity::getDeletedFlag, Boolean.FALSE));
        return Optional.ofNullable(appearances).orElseGet(ArrayList::new);
    }

    /**
     * 从出场记录中收集指定实体类型的业务ID。
     *
     * @param appearances 出场记录列表
     * @param entityType 实体类型
     * @return 去重后的实体ID集合
     */
    private Set<Long> collectAppearanceIds(List<NovelChapterAppearanceEntity> appearances, String entityType) {
        return appearances.stream()
                .filter(appearance -> StringUtils.equals(appearance.getEntityType(), entityType))
                .map(NovelChapterAppearanceEntity::getEntityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 解析本章 POV。
     *
     * @param currentChapter 当前章实体，可为空
     * @param projectEntity 项目实体
     * @param candidateCharacters 候选角色列表
     * @return POV 角色名，无法兜底时返回 null
     */
    private String resolvePov(NovelChapterEntity currentChapter, NovelProjectEntity projectEntity, List<NovelCharacterEntity> candidateCharacters) {
        if (Objects.nonNull(currentChapter) && StringUtils.isNotBlank(currentChapter.getPov())) {
            return currentChapter.getPov();
        }
        if (StringUtils.isNotBlank(projectEntity.getProtagonistName())) {
            return projectEntity.getProtagonistName();
        }
        return candidateCharacters.stream()
                .filter(character -> StringUtils.equals(character.getRoleType(), NovelCharacterRoleEnum.PROTAGONIST.getValue()))
                .map(NovelCharacterEntity::getName)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);
    }

    /**
     * 解析章节意图。
     *
     * @param outlineEntity 章节细纲，可为空
     * @param userDirection 用户临时写作方向，可为空
     * @return 章节意图，查不到时返回 null
     */
    private String resolveChapterIntent(ChapterOutlineEntity outlineEntity, String userDirection) {
        if (Objects.nonNull(outlineEntity)) {
            String outlineSummary = StringUtils.firstNonBlank(outlineEntity.getSummary(), outlineEntity.getSceneBeats());
            if (StringUtils.isNotBlank(outlineSummary)) {
                return outlineSummary;
            }
        }
        return StringUtils.trimToNull(userDirection);
    }

    /**
     * 构建卷概要文本块。
     *
     * @param volumeEntity 卷实体，可为空
     * @param context 上下文对象，用于记录兜底说明
     * @return 卷概要文本块，章节未归卷时返回 null
     */
    private NovelRetrieveContextVO.TextBlockVO buildVolumeBlock(NovelVolumeEntity volumeEntity, NovelRetrieveContextVO context) {
        if (Objects.isNull(volumeEntity)) {
            context.getFallbackNotes().add("NO_VOLUME");
            return null;
        }
        NovelRetrieveContextVO.TextBlockVO textBlock = new NovelRetrieveContextVO.TextBlockVO();
        textBlock.setSourceType("VOLUME");
        textBlock.setSourceId(volumeEntity.getId());
        textBlock.setTitle(volumeEntity.getTitle());
        textBlock.setSummary(volumeEntity.getSummary());
        textBlock.setDetailLevel(DETAIL_FULL);
        return textBlock;
    }

    /**
     * 构建上一章摘要文本块。
     *
     * @param previousChapter 上一章实体，可为空
     * @return 上一章摘要文本块，查不到时返回 null
     */
    private NovelRetrieveContextVO.TextBlockVO buildPreviousChapterBlock(NovelChapterEntity previousChapter) {
        if (Objects.isNull(previousChapter)) {
            return null;
        }
        NovelRetrieveContextVO.TextBlockVO textBlock = new NovelRetrieveContextVO.TextBlockVO();
        textBlock.setSourceType("PREVIOUS_CHAPTER");
        textBlock.setSourceId(previousChapter.getId());
        textBlock.setTitle(previousChapter.getTitle());
        textBlock.setSummary(previousChapter.getSummary());
        textBlock.setDetailLevel(DETAIL_FULL);
        return textBlock;
    }

    /**
     * 构建章节细纲文本块。
     *
     * @param outlineEntity 章节细纲实体，可为空
     * @return 细纲文本块，查不到时返回 null
     */
    private NovelRetrieveContextVO.TextBlockVO buildOutlineBlock(ChapterOutlineEntity outlineEntity) {
        if (Objects.isNull(outlineEntity)) {
            return null;
        }
        NovelRetrieveContextVO.TextBlockVO textBlock = new NovelRetrieveContextVO.TextBlockVO();
        textBlock.setSourceType("OUTLINE");
        textBlock.setSourceId(outlineEntity.getId());
        textBlock.setTitle("第" + outlineEntity.getChapterNumber() + "章细纲");
        textBlock.setSummary(StringUtils.firstNonBlank(outlineEntity.getSummary(), outlineEntity.getSceneBeats()));
        textBlock.setDetailLevel(DETAIL_FULL);
        return textBlock;
    }

    /**
     * 构建角色候选卡片。
     *
     * @param characters 角色实体列表
     * @param pov POV 角色名
     * @param previousCharacterIds 上一章出场角色ID集合
     * @return 排序后的角色卡片
     */
    private List<NovelRetrieveContextVO.CharacterCardVO> buildCharacterCards(List<NovelCharacterEntity> characters, String pov, Set<Long> previousCharacterIds) {
        return characters.stream()
                .map(character -> buildCharacterCard(character, pov, previousCharacterIds))
                .sorted(Comparator
                        .comparing(NovelRetrieveContextVO.CharacterCardVO::getPriority)
                        .thenComparing(card -> Optional.ofNullable(card.getId()).orElse(0L)))
                .collect(Collectors.toList());
    }

    /**
     * 构建单个角色候选卡片。
     *
     * @param character 角色实体
     * @param pov POV 角色名
     * @param previousCharacterIds 上一章出场角色ID集合
     * @return 角色候选卡片
     */
    private NovelRetrieveContextVO.CharacterCardVO buildCharacterCard(NovelCharacterEntity character, String pov, Set<Long> previousCharacterIds) {
        NovelRetrieveContextVO.CharacterCardVO card = new NovelRetrieveContextVO.CharacterCardVO();
        card.setId(character.getId());
        card.setName(character.getName());
        card.setRoleType(character.getRoleType());
        card.setDescription(character.getDescription());
        card.setCurrentGoal(character.getCurrentGoal());
        card.setCurrentEmotion(character.getCurrentEmotion());
        card.setPriority(resolveCharacterPriority(character, pov, previousCharacterIds));
        card.setSource(resolveCharacterSource(character, pov, previousCharacterIds));
        card.setDetailLevel(DETAIL_FULL);
        return card;
    }

    /**
     * 计算角色排序权重。
     *
     * @param character 角色实体
     * @param pov POV 角色名
     * @param previousCharacterIds 上一章出场角色ID集合
     * @return 排序权重，数值越小越靠前
     */
    private int resolveCharacterPriority(NovelCharacterEntity character, String pov, Set<Long> previousCharacterIds) {
        if (StringUtils.equals(character.getName(), pov)) {
            return 10;
        }
        if (previousCharacterIds.contains(character.getId())) {
            return 20;
        }
        if (StringUtils.equals(character.getRoleType(), NovelCharacterRoleEnum.PROTAGONIST.getValue())) {
            return 30;
        }
        if (StringUtils.equals(character.getRoleType(), NovelCharacterRoleEnum.ANTAGONIST.getValue())) {
            return 40;
        }
        if (StringUtils.equals(character.getRoleType(), NovelCharacterRoleEnum.SUPPORTING.getValue())) {
            return 50;
        }
        return 60;
    }

    /**
     * 计算角色候选来源说明。
     *
     * @param character 角色实体
     * @param pov POV 角色名
     * @param previousCharacterIds 上一章出场角色ID集合
     * @return 来源说明
     */
    private String resolveCharacterSource(NovelCharacterEntity character, String pov, Set<Long> previousCharacterIds) {
        if (StringUtils.equals(character.getName(), pov)) {
            return "POV";
        }
        if (previousCharacterIds.contains(character.getId())) {
            return "PREVIOUS_CHAPTER";
        }
        return "ROLE_PRIORITY";
    }

    /**
     * 构建地点候选卡片。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前登录用户ID
     * @param chapterNumber 目标章节号
     * @param pov POV 角色名
     * @param characterCards 已排序的角色卡片
     * @param previousLocationIds 上一章出场地点ID集合
     * @param context 上下文对象，用于记录兜底说明
     * @return 地点候选卡片
     */
    private List<NovelRetrieveContextVO.LocationCardVO> buildLocationCards(Long projectId, Long requestUserId, Integer chapterNumber, String pov,
            List<NovelRetrieveContextVO.CharacterCardVO> characterCards, Set<Long> previousLocationIds, NovelRetrieveContextVO context) {
        List<NovelRetrieveContextVO.LocationCardVO> locations = new ArrayList<>();
        Long povCharacterId = findCharacterIdByName(characterCards, pov);
        if (Objects.nonNull(povCharacterId)) {
            NovelCharacterLocationEntity currentLocation = getCurrentLocation(projectId, povCharacterId, requestUserId);
            if (Objects.nonNull(currentLocation)) {
                NovelLocationEntity locationEntity = getLocationById(projectId, currentLocation.getLocationId(), requestUserId);
                if (Objects.nonNull(locationEntity)) {
                    locations.add(buildLocationCard(locationEntity, "CURRENT_LOCATION", 10));
                }
            } else {
                context.getFallbackNotes().add("NO_CURRENT_LOCATION");
            }
        }

        // 首章没有历史图谱时，用项目全量地点兜底，保证开篇不会拿到空空间上下文。
        if (locations.isEmpty() && Objects.equals(chapterNumber, 1)) {
            listAllLocations(projectId, requestUserId).forEach(location -> locations.add(buildLocationCard(location, "FIRST_CHAPTER_FULL_POOL", 50)));
            if (!locations.isEmpty()) {
                context.getFallbackNotes().add("FIRST_CHAPTER_FULL_POOL");
            }
        }

        addPreviousLocations(projectId, requestUserId, previousLocationIds, locations);
        return locations.stream()
                .sorted(Comparator
                        .comparing(NovelRetrieveContextVO.LocationCardVO::getPriority)
                        .thenComparing(card -> Optional.ofNullable(card.getId()).orElse(0L)))
                .collect(Collectors.toList());
    }

    /**
     * 根据角色名在候选卡片里查找角色ID。
     *
     * @param characterCards 角色卡片列表
     * @param name 角色名
     * @return 角色ID，查不到返回 null
     */
    private Long findCharacterIdByName(List<NovelRetrieveContextVO.CharacterCardVO> characterCards, String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return characterCards.stream()
                .filter(card -> StringUtils.equals(card.getName(), name))
                .map(NovelRetrieveContextVO.CharacterCardVO::getId)
                .findFirst()
                .orElse(null);
    }

    /**
     * 查询角色当前所在地。
     *
     * @param projectId 项目ID
     * @param characterId 角色ID
     * @param requestUserId 当前登录用户ID
     * @return 当前所在地关系，查不到返回 null
     */
    private NovelCharacterLocationEntity getCurrentLocation(Long projectId, Long characterId, Long requestUserId) {
        return novelCharacterLocationDao.selectOne(new LambdaQueryWrapper<NovelCharacterLocationEntity>()
                .eq(NovelCharacterLocationEntity::getProjectId, projectId)
                .eq(NovelCharacterLocationEntity::getCharacterId, characterId)
                .eq(NovelCharacterLocationEntity::getCurrentFlag, Boolean.TRUE)
                .eq(NovelCharacterLocationEntity::getCreateUserId, requestUserId)
                .eq(NovelCharacterLocationEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 按ID查询地点。
     *
     * @param projectId 项目ID
     * @param locationId 地点ID
     * @param requestUserId 当前登录用户ID
     * @return 地点实体，查不到返回 null
     */
    private NovelLocationEntity getLocationById(Long projectId, Long locationId, Long requestUserId) {
        if (Objects.isNull(locationId)) {
            return null;
        }
        return novelLocationDao.selectOne(new LambdaQueryWrapper<NovelLocationEntity>()
                .eq(NovelLocationEntity::getId, locationId)
                .eq(NovelLocationEntity::getProjectId, projectId)
                .eq(NovelLocationEntity::getCreateUserId, requestUserId)
                .eq(NovelLocationEntity::getDeletedFlag, Boolean.FALSE));
    }

    /**
     * 查询项目下所有未归档地点。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前登录用户ID
     * @return 地点列表
     */
    private List<NovelLocationEntity> listAllLocations(Long projectId, Long requestUserId) {
        List<NovelLocationEntity> locations = novelLocationDao.selectList(new LambdaQueryWrapper<NovelLocationEntity>()
                .eq(NovelLocationEntity::getProjectId, projectId)
                .eq(NovelLocationEntity::getCreateUserId, requestUserId)
                .eq(NovelLocationEntity::getDeletedFlag, Boolean.FALSE));
        return Optional.ofNullable(locations).orElseGet(ArrayList::new);
    }

    /**
     * 把上一章地点出场记录补入地点候选池。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前登录用户ID
     * @param previousLocationIds 上一章地点ID集合
     * @param locations 当前地点卡片列表
     */
    private void addPreviousLocations(Long projectId, Long requestUserId, Set<Long> previousLocationIds, List<NovelRetrieveContextVO.LocationCardVO> locations) {
        Set<Long> existingIds = locations.stream()
                .map(NovelRetrieveContextVO.LocationCardVO::getId)
                .collect(Collectors.toSet());
        previousLocationIds.stream()
                .filter(locationId -> !existingIds.contains(locationId))
                .map(locationId -> getLocationById(projectId, locationId, requestUserId))
                .filter(Objects::nonNull)
                .map(location -> buildLocationCard(location, "PREVIOUS_CHAPTER", 30))
                .forEach(locations::add);
    }

    /**
     * 构建单个地点卡片。
     *
     * @param location 地点实体
     * @param source 候选来源
     * @param priority 排序权重
     * @return 地点卡片
     */
    private NovelRetrieveContextVO.LocationCardVO buildLocationCard(NovelLocationEntity location, String source, int priority) {
        NovelRetrieveContextVO.LocationCardVO card = new NovelRetrieveContextVO.LocationCardVO();
        card.setId(location.getId());
        card.setName(location.getName());
        card.setType(location.getType());
        card.setSummary(location.getSummary());
        card.setSource(source);
        card.setPriority(priority);
        card.setDetailLevel(DETAIL_FULL);
        return card;
    }

    /**
     * 构建活跃线索候选卡片。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前登录用户ID
     * @return 线索卡片列表
     */
    private List<NovelRetrieveContextVO.ClueCardVO> buildClueCards(Long projectId, Long requestUserId) {
        List<NovelClueEntity> clues = novelClueDao.selectList(new LambdaQueryWrapper<NovelClueEntity>()
                .eq(NovelClueEntity::getProjectId, projectId)
                .eq(NovelClueEntity::getCreateUserId, requestUserId)
                .eq(NovelClueEntity::getDeletedFlag, Boolean.FALSE)
                .orderByDesc(NovelClueEntity::getPriority));
        return Optional.ofNullable(clues).orElseGet(ArrayList::new)
                .stream()
                .filter(clue -> StringUtils.equals(clue.getClueStatus(), NovelClueStatusEnum.ACTIVE.getValue()))
                .filter(clue -> StringUtils.equals(clue.getSubType(), NovelClueSubTypeEnum.PLOT_THREAD.getValue()))
                .map(this::buildClueCard)
                .sorted(Comparator
                        .comparing((NovelRetrieveContextVO.ClueCardVO card) -> Optional.ofNullable(card.getPriority()).orElse(0))
                        .reversed()
                        .thenComparing(card -> Optional.ofNullable(card.getId()).orElse(0L)))
                .collect(Collectors.toList());
    }

    /**
     * 构建单个线索卡片。
     *
     * @param clue 线索实体
     * @return 线索卡片
     */
    private NovelRetrieveContextVO.ClueCardVO buildClueCard(NovelClueEntity clue) {
        NovelRetrieveContextVO.ClueCardVO card = new NovelRetrieveContextVO.ClueCardVO();
        card.setId(clue.getId());
        card.setName(clue.getName());
        card.setSubType(clue.getSubType());
        card.setDescription(clue.getDescription());
        card.setSummary(clue.getSummary());
        card.setPriority(clue.getPriority());
        card.setDetailLevel(DETAIL_FULL);
        return card;
    }

    /**
     * 构建候选角色之间的关系卡片。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前登录用户ID
     * @param selectedCharacters 已进入上下文的角色卡片
     * @param candidateCharacters 候选角色实体列表
     * @return 关系卡片列表
     */
    private List<NovelRetrieveContextVO.RelationCardVO> buildRelationCards(Long projectId, Long requestUserId,
            List<NovelRetrieveContextVO.CharacterCardVO> selectedCharacters, List<NovelCharacterEntity> candidateCharacters) {
        Set<Long> selectedCharacterIds = selectedCharacters.stream()
                .map(NovelRetrieveContextVO.CharacterCardVO::getId)
                .collect(Collectors.toSet());
        Map<Long, NovelCharacterEntity> characterMap = candidateCharacters.stream()
                .collect(Collectors.toMap(NovelCharacterEntity::getId, Function.identity(), (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        List<NovelCharacterRelationEntity> relations = novelCharacterRelationDao.selectList(new LambdaQueryWrapper<NovelCharacterRelationEntity>()
                .eq(NovelCharacterRelationEntity::getProjectId, projectId)
                .eq(NovelCharacterRelationEntity::getCreateUserId, requestUserId)
                .eq(NovelCharacterRelationEntity::getDeletedFlag, Boolean.FALSE));
        return Optional.ofNullable(relations).orElseGet(ArrayList::new)
                .stream()
                .filter(relation -> selectedCharacterIds.contains(relation.getCharacterId()))
                .filter(relation -> selectedCharacterIds.contains(relation.getTargetCharacterId()))
                .map(relation -> buildRelationCard(relation, characterMap))
                .collect(Collectors.toList());
    }

    /**
     * 构建单个关系卡片。
     *
     * @param relation 角色关系实体
     * @param characterMap 角色实体映射
     * @return 关系卡片
     */
    private NovelRetrieveContextVO.RelationCardVO buildRelationCard(NovelCharacterRelationEntity relation, Map<Long, NovelCharacterEntity> characterMap) {
        NovelRetrieveContextVO.RelationCardVO card = new NovelRetrieveContextVO.RelationCardVO();
        card.setId(relation.getId());
        card.setCharacterId(relation.getCharacterId());
        card.setTargetCharacterId(relation.getTargetCharacterId());
        card.setCharacterName(Optional.ofNullable(characterMap.get(relation.getCharacterId())).map(NovelCharacterEntity::getName).orElse(null));
        card.setTargetCharacterName(Optional.ofNullable(characterMap.get(relation.getTargetCharacterId())).map(NovelCharacterEntity::getName).orElse(null));
        card.setRelationType(relation.getRelationType());
        card.setRelationDetail(resolveRelationDetail(relation));
        card.setDetailLevel(DETAIL_FULL);
        return card;
    }

    /**
     * 解析关系补充说明。
     *
     * @param relation 角色关系实体
     * @return 关系补充说明
     */
    private String resolveRelationDetail(NovelCharacterRelationEntity relation) {
        return StringUtils.firstNonBlank(
                relation.getKnowsRelationType(),
                relation.getLoveStatus(),
                relation.getFamilyType(),
                Objects.nonNull(relation.getHateIntensity()) ? "HATE_INTENSITY_" + relation.getHateIntensity() : null
        );
    }

    /**
     * 根据项目预算对上下文做分层裁剪。
     *
     * @param context 上下文对象
     * @param projectEntity 项目实体
     */
    private void applyTokenBudget(NovelRetrieveContextVO context, NovelProjectEntity projectEntity) {
        int tokenBudget = resolvePositiveInt(projectEntity.getTokenBudget(), DEFAULT_TOKEN_BUDGET);
        int tokenHardLimit = resolvePositiveInt(projectEntity.getTokenHardLimit(), DEFAULT_TOKEN_HARD_LIMIT);
        int hardLimit = Math.max(1, tokenHardLimit);
        int estimatedTokens = refreshTokenEstimates(context);
        int shortenedCount = 0;
        int discardedCount = 0;

        if (estimatedTokens > tokenBudget || estimatedTokens > tokenHardLimit) {
            shortenedCount += shortenContext(context);
            estimatedTokens = refreshTokenEstimates(context);
        }

        while (estimatedTokens > hardLimit && discardLowestPriorityCard(context)) {
            discardedCount++;
            estimatedTokens = refreshTokenEstimates(context);
        }

        if (estimatedTokens > hardLimit) {
            shortenedCount += forceShortenTextBlocks(context);
            estimatedTokens = refreshTokenEstimates(context);
        }

        NovelRetrieveContextVO.TokenStatsVO tokenStats = new NovelRetrieveContextVO.TokenStatsVO();
        tokenStats.setTokenBudget(tokenBudget);
        tokenStats.setTokenHardLimit(tokenHardLimit);
        tokenStats.setEstimatedTokens(estimatedTokens);
        tokenStats.setTruncated(shortenedCount > 0 || discardedCount > 0);
        tokenStats.setShortenedCount(shortenedCount);
        tokenStats.setDiscardedCount(discardedCount);
        context.setTokenStats(tokenStats);
    }

    /**
     * 解析正整数配置。
     *
     * @param value 原始配置值
     * @param defaultValue 默认值
     * @return 正整数配置值
     */
    private int resolvePositiveInt(Integer value, int defaultValue) {
        if (Objects.isNull(value) || value <= 0) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 刷新所有文本块和卡片的估算 Token 数。
     *
     * @param context 上下文对象
     * @return 当前上下文总估算 Token 数
     */
    private int refreshTokenEstimates(NovelRetrieveContextVO context) {
        int total = estimateText(context.getPov()) + estimateText(context.getChapterIntent()) + estimateText(context.getUserDirection());
        total += refreshTextBlockTokens(context.getVolumeSummary());
        total += refreshTextBlockTokens(context.getPreviousChapter());
        total += refreshTextBlockTokens(context.getOutline());
        for (NovelRetrieveContextVO.CharacterCardVO card : context.getCharacters()) {
            int cardTokens = estimateText(card.getName()) + estimateText(card.getRoleType()) + estimateText(card.getDescription())
                    + estimateText(card.getCurrentGoal()) + estimateText(card.getCurrentEmotion());
            card.setEstimatedTokens(cardTokens);
            total += cardTokens;
        }
        for (NovelRetrieveContextVO.LocationCardVO card : context.getLocations()) {
            int cardTokens = estimateText(card.getName()) + estimateText(card.getType()) + estimateText(card.getSummary());
            card.setEstimatedTokens(cardTokens);
            total += cardTokens;
        }
        for (NovelRetrieveContextVO.ClueCardVO card : context.getClues()) {
            int cardTokens = estimateText(card.getName()) + estimateText(card.getSubType()) + estimateText(card.getDescription()) + estimateText(card.getSummary());
            card.setEstimatedTokens(cardTokens);
            total += cardTokens;
        }
        for (NovelRetrieveContextVO.RelationCardVO card : context.getRelations()) {
            int cardTokens = estimateText(card.getCharacterName()) + estimateText(card.getTargetCharacterName())
                    + estimateText(card.getRelationType()) + estimateText(card.getRelationDetail());
            card.setEstimatedTokens(cardTokens);
            total += cardTokens;
        }
        return total;
    }

    /**
     * 刷新单个文本块的估算 Token 数。
     *
     * @param textBlock 文本块，可为空
     * @return 文本块估算 Token 数
     */
    private int refreshTextBlockTokens(NovelRetrieveContextVO.TextBlockVO textBlock) {
        if (Objects.isNull(textBlock)) {
            return 0;
        }
        int tokenCount = estimateText(textBlock.getTitle()) + estimateText(textBlock.getSummary());
        textBlock.setEstimatedTokens(tokenCount);
        return tokenCount;
    }

    /**
     * 轻量估算文本 Token 数。
     *
     * @param value 文本内容
     * @return 估算 Token 数
     */
    private int estimateText(String value) {
        return StringUtils.length(value);
    }

    /**
     * 把上下文里的完整卡片压缩成短卡片。
     *
     * @param context 上下文对象
     * @return 被压缩的卡片和文本块数量
     */
    private int shortenContext(NovelRetrieveContextVO context) {
        int shortenedCount = 0;
        shortenedCount += shortenTopLevelText(context, 80);
        shortenedCount += shortenTextBlock(context.getVolumeSummary(), 80);
        shortenedCount += shortenTextBlock(context.getPreviousChapter(), 80);
        shortenedCount += shortenTextBlock(context.getOutline(), 80);
        for (NovelRetrieveContextVO.CharacterCardVO card : context.getCharacters()) {
            if (StringUtils.equals(card.getDetailLevel(), DETAIL_FULL)) {
                card.setDescription(shorten(card.getDescription(), 48));
                card.setCurrentGoal(shorten(card.getCurrentGoal(), 36));
                card.setCurrentEmotion(shorten(card.getCurrentEmotion(), 20));
                card.setDetailLevel(DETAIL_SHORT);
                shortenedCount++;
            }
        }
        for (NovelRetrieveContextVO.LocationCardVO card : context.getLocations()) {
            if (StringUtils.equals(card.getDetailLevel(), DETAIL_FULL)) {
                card.setSummary(shorten(card.getSummary(), 48));
                card.setDetailLevel(DETAIL_SHORT);
                shortenedCount++;
            }
        }
        for (NovelRetrieveContextVO.ClueCardVO card : context.getClues()) {
            if (StringUtils.equals(card.getDetailLevel(), DETAIL_FULL)) {
                card.setDescription(shorten(card.getDescription(), 48));
                card.setSummary(shorten(card.getSummary(), 36));
                card.setDetailLevel(DETAIL_SHORT);
                shortenedCount++;
            }
        }
        for (NovelRetrieveContextVO.RelationCardVO card : context.getRelations()) {
            if (StringUtils.equals(card.getDetailLevel(), DETAIL_FULL)) {
                card.setRelationDetail(shorten(card.getRelationDetail(), 24));
                card.setDetailLevel(DETAIL_SHORT);
                shortenedCount++;
            }
        }
        return shortenedCount;
    }

    /**
     * 压缩上下文顶层文本，例如章节意图和用户临时方向。
     *
     * @param context 上下文对象
     * @param maxLength 最大保留长度
     * @return 被压缩的顶层字段数量
     */
    private int shortenTopLevelText(NovelRetrieveContextVO context, int maxLength) {
        int shortenedCount = 0;
        String oldChapterIntent = context.getChapterIntent();
        context.setChapterIntent(shorten(context.getChapterIntent(), maxLength));
        if (!StringUtils.equals(oldChapterIntent, context.getChapterIntent())) {
            shortenedCount++;
        }
        String oldUserDirection = context.getUserDirection();
        context.setUserDirection(shorten(context.getUserDirection(), maxLength));
        if (!StringUtils.equals(oldUserDirection, context.getUserDirection())) {
            shortenedCount++;
        }
        return shortenedCount;
    }

    /**
     * 压缩文本块。
     *
     * @param textBlock 文本块，可为空
     * @param maxLength 最大保留长度
     * @return 被压缩返回 1，否则返回 0
     */
    private int shortenTextBlock(NovelRetrieveContextVO.TextBlockVO textBlock, int maxLength) {
        if (Objects.isNull(textBlock) || StringUtils.equals(textBlock.getDetailLevel(), DETAIL_SHORT)) {
            return 0;
        }
        textBlock.setSummary(shorten(textBlock.getSummary(), maxLength));
        textBlock.setDetailLevel(DETAIL_SHORT);
        return 1;
    }

    /**
     * 丢弃最低优先级的可选卡片。
     *
     * @param context 上下文对象
     * @return 丢弃成功返回 true，没有可丢弃卡片返回 false
     */
    private boolean discardLowestPriorityCard(NovelRetrieveContextVO context) {
        if (!context.getClues().isEmpty()) {
            context.getClues().remove(context.getClues().size() - 1);
            return true;
        }
        if (!context.getRelations().isEmpty()) {
            context.getRelations().remove(context.getRelations().size() - 1);
            return true;
        }
        if (!context.getLocations().isEmpty()) {
            context.getLocations().remove(context.getLocations().size() - 1);
            return true;
        }
        if (context.getCharacters().size() > 1) {
            context.getCharacters().remove(context.getCharacters().size() - 1);
            return true;
        }
        return false;
    }

    /**
     * 硬上限仍超限时进一步压缩核心文本块。
     *
     * @param context 上下文对象
     * @return 被进一步压缩的文本块数量
     */
    private int forceShortenTextBlocks(NovelRetrieveContextVO context) {
        int shortenedCount = 0;
        shortenedCount += shortenTopLevelText(context, 10);
        shortenedCount += forceShortenTextBlock(context.getVolumeSummary(), 10);
        shortenedCount += forceShortenTextBlock(context.getPreviousChapter(), 10);
        shortenedCount += forceShortenTextBlock(context.getOutline(), 10);
        shortenedCount += forceShortenRemainingCards(context);
        return shortenedCount;
    }

    /**
     * 硬上限兜底阶段压缩剩余卡片。
     *
     * @param context 上下文对象
     * @return 被进一步压缩的卡片数量
     */
    private int forceShortenRemainingCards(NovelRetrieveContextVO context) {
        int shortenedCount = 0;
        for (NovelRetrieveContextVO.CharacterCardVO card : context.getCharacters()) {
            String oldDescription = card.getDescription();
            String oldGoal = card.getCurrentGoal();
            String oldEmotion = card.getCurrentEmotion();
            card.setDescription(shorten(card.getDescription(), 6));
            card.setCurrentGoal(shorten(card.getCurrentGoal(), 6));
            card.setCurrentEmotion(shorten(card.getCurrentEmotion(), 6));
            if (!StringUtils.equals(oldDescription, card.getDescription())
                    || !StringUtils.equals(oldGoal, card.getCurrentGoal())
                    || !StringUtils.equals(oldEmotion, card.getCurrentEmotion())) {
                shortenedCount++;
            }
        }
        for (NovelRetrieveContextVO.LocationCardVO card : context.getLocations()) {
            String oldSummary = card.getSummary();
            card.setSummary(shorten(card.getSummary(), 6));
            if (!StringUtils.equals(oldSummary, card.getSummary())) {
                shortenedCount++;
            }
        }
        for (NovelRetrieveContextVO.ClueCardVO card : context.getClues()) {
            String oldDescription = card.getDescription();
            String oldSummary = card.getSummary();
            card.setDescription(shorten(card.getDescription(), 6));
            card.setSummary(shorten(card.getSummary(), 6));
            if (!StringUtils.equals(oldDescription, card.getDescription()) || !StringUtils.equals(oldSummary, card.getSummary())) {
                shortenedCount++;
            }
        }
        return shortenedCount;
    }

    /**
     * 强制压缩单个文本块。
     *
     * @param textBlock 文本块，可为空
     * @param maxLength 最大保留长度
     * @return 被压缩返回 1，否则返回 0
     */
    private int forceShortenTextBlock(NovelRetrieveContextVO.TextBlockVO textBlock, int maxLength) {
        if (Objects.isNull(textBlock)) {
            return 0;
        }
        String oldSummary = textBlock.getSummary();
        textBlock.setSummary(shorten(textBlock.getSummary(), maxLength));
        return StringUtils.equals(oldSummary, textBlock.getSummary()) ? 0 : 1;
    }

    /**
     * 截断文本并加省略号。
     *
     * @param value 原始文本
     * @param maxLength 最大长度
     * @return 截断后的文本
     */
    private String shorten(String value, int maxLength) {
        if (StringUtils.isBlank(value) || StringUtils.length(value) <= maxLength) {
            return value;
        }
        int contentLength = Math.max(1, maxLength - 3);
        return value.substring(0, contentLength) + "...";
    }
}
