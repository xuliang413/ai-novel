package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.config.NovelPromptProperties;
import net.lab1024.sa.admin.module.business.novel.dao.NovelCheatDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelNarrativeRuleDao;
import net.lab1024.sa.admin.module.business.novel.dao.NovelProjectDao;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCheatEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelNarrativeRuleEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelPromptVO;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelRetrieveContextVO;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 小说提示词组装服务。
 * <p>
 * 负责把项目约束、叙事规则和上下文检索结果拼成 LLM 可直接使用的 System Prompt 与 User Prompt。
 *
 * @Author AI-Novel
 */
@Slf4j
@Service
public class NovelPromptService {

    /**
     * 项目 DAO，用来校验项目归属并读取世界观、文风和目标字数。
     */
    @Resource
    private NovelProjectDao novelProjectDao;

    /**
     * 叙事规则 DAO，用来读取 System Prompt 中的自然语言规则。
     */
    @Resource
    private NovelNarrativeRuleDao novelNarrativeRuleDao;

    /**
     * 金手指 DAO，用来补充 User Prompt 中的能力边界和限制。
     */
    @Resource
    private NovelCheatDao novelCheatDao;

    /**
     * 上下文检索服务，用来获取 Task 10 的结构化上下文预览。
     */
    @Resource
    private NovelRetrieveService novelRetrieveService;

    /**
     * Prompt 模板配置，来源于 application.yaml。
     */
    @Resource
    private NovelPromptProperties promptProperties;

    /**
     * 构建章节生成 Prompt。
     *
     * @param projectId 项目ID
     * @param chapterNumber 目标章节号
     * @param userDirection 用户临时写作方向，可为空
     * @param requestUserId 当前登录用户ID
     * @return System Prompt、User Prompt 与上下文快照
     */
    public ResponseDTO<NovelPromptVO> buildPrompt(Long projectId, Integer chapterNumber, String userDirection, Long requestUserId) {
        ResponseDTO<String> validateResult = validatePromptRequest(projectId, chapterNumber, requestUserId);
        if (!validateResult.getOk()) {
            return ResponseDTO.error(validateResult);
        }

        NovelProjectEntity projectEntity = getOwnedProject(projectId, requestUserId);
        if (Objects.isNull(projectEntity)) {
            return ResponseDTO.userErrorParam("项目不存在或无权访问");
        }

        ResponseDTO<NovelRetrieveContextVO> contextResponse = novelRetrieveService.buildContextPreview(projectId, chapterNumber, userDirection, requestUserId);
        if (!contextResponse.getOk()) {
            return ResponseDTO.error(contextResponse);
        }

        List<NovelNarrativeRuleEntity> rules = listNarrativeRules(projectId, requestUserId);
        List<NovelCheatEntity> cheats = listCheats(projectId, requestUserId);
        NovelRetrieveContextVO context = contextResponse.getData();
        String systemPrompt = buildSystemPrompt(projectEntity, rules);
        String userPrompt = buildUserPrompt(projectEntity, context, cheats);

        NovelPromptVO promptVO = new NovelPromptVO();
        promptVO.setProjectId(projectId);
        promptVO.setChapterNumber(chapterNumber);
        promptVO.setPov(context.getPov());
        promptVO.setSystemPrompt(systemPrompt);
        promptVO.setUserPrompt(userPrompt);
        promptVO.setSystemEstimatedTokens(estimateText(systemPrompt));
        promptVO.setUserEstimatedTokens(estimateText(userPrompt));
        promptVO.setEstimatedTokens(promptVO.getSystemEstimatedTokens() + promptVO.getUserEstimatedTokens());
        promptVO.setContextPreview(context);
        return ResponseDTO.ok(promptVO);
    }

    /**
     * 校验 Prompt 组装的基础入参。
     *
     * @param projectId 项目ID
     * @param chapterNumber 章节号
     * @param requestUserId 当前登录用户ID
     * @return 校验结果
     */
    private ResponseDTO<String> validatePromptRequest(Long projectId, Integer chapterNumber, Long requestUserId) {
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
     * 查询当前用户拥有的项目。
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
     * 查询项目叙事规则，并按优先级降序排序。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前登录用户ID
     * @return 叙事规则列表
     */
    private List<NovelNarrativeRuleEntity> listNarrativeRules(Long projectId, Long requestUserId) {
        List<NovelNarrativeRuleEntity> rules = novelNarrativeRuleDao.selectList(new LambdaQueryWrapper<NovelNarrativeRuleEntity>()
                .eq(NovelNarrativeRuleEntity::getProjectId, projectId)
                .eq(NovelNarrativeRuleEntity::getCreateUserId, requestUserId)
                .eq(NovelNarrativeRuleEntity::getDeletedFlag, Boolean.FALSE)
                .orderByDesc(NovelNarrativeRuleEntity::getPriority));
        return Optional.ofNullable(rules).orElseGet(ArrayList::new)
                .stream()
                .sorted(Comparator
                        .comparing((NovelNarrativeRuleEntity rule) -> Optional.ofNullable(rule.getPriority()).orElse(0))
                        .reversed()
                        .thenComparing(rule -> Optional.ofNullable(rule.getId()).orElse(0L)))
                .toList();
    }

    /**
     * 查询项目金手指。
     *
     * @param projectId 项目ID
     * @param requestUserId 当前登录用户ID
     * @return 金手指列表
     */
    private List<NovelCheatEntity> listCheats(Long projectId, Long requestUserId) {
        List<NovelCheatEntity> cheats = novelCheatDao.selectList(new LambdaQueryWrapper<NovelCheatEntity>()
                .eq(NovelCheatEntity::getProjectId, projectId)
                .eq(NovelCheatEntity::getCreateUserId, requestUserId)
                .eq(NovelCheatEntity::getDeletedFlag, Boolean.FALSE));
        return Optional.ofNullable(cheats).orElseGet(ArrayList::new);
    }

    /**
     * 组装 System Prompt。
     *
     * @param projectEntity 项目实体
     * @param rules 叙事规则列表
     * @return System Prompt
     */
    private String buildSystemPrompt(NovelProjectEntity projectEntity, List<NovelNarrativeRuleEntity> rules) {
        StringBuilder builder = new StringBuilder();
        appendLine(builder, promptProperties.getSystemTemplatePrefix());
        appendLine(builder, "");
        appendLine(builder, promptProperties.getSystemRoleDescription());
        appendLine(builder, "");
        appendLine(builder, promptProperties.getSystemOutputFormat());
        appendLine(builder, "");
        appendSection(builder, promptProperties.getNarrativeRuleSectionTitle());
        if (rules.isEmpty()) {
            appendLine(builder, "- 暂无额外叙事规则。");
        } else {
            for (NovelNarrativeRuleEntity rule : rules) {
                appendLine(builder, "- [" + safe(rule.getPriority()) + "] " + safe(rule.getName()) + "：" + safe(rule.getContent()));
            }
        }

        appendSection(builder, promptProperties.getProjectConstraintSectionTitle());
        appendLine(builder, "- 世界观：" + defaultText(projectEntity.getWorldBuilding(), "未配置"));
        appendLine(builder, "- 文风：" + defaultText(projectEntity.getStyleDescription(), "未配置"));
        appendLine(builder, "- 每章目标字数：" + defaultText(projectEntity.getTargetChapterWords(), "未配置"));
        return builder.toString();
    }

    /**
     * 组装 User Prompt。
     *
     * @param projectEntity 项目实体
     * @param context 上下文预览
     * @param cheats 金手指列表
     * @return User Prompt
     */
    private String buildUserPrompt(NovelProjectEntity projectEntity, NovelRetrieveContextVO context, List<NovelCheatEntity> cheats) {
        StringBuilder builder = new StringBuilder();
        appendLine(builder, replaceUserTemplate(projectEntity, context));
        appendSection(builder, promptProperties.getContextSectionTitle());
        appendTextBlock(builder, "卷概要", context.getVolumeSummary());
        appendTextBlock(builder, "上一章", context.getPreviousChapter());
        appendTextBlock(builder, "章节细纲", context.getOutline());
        appendCharacters(builder, context.getCharacters());
        appendRelations(builder, context.getRelations());
        appendClues(builder, context.getClues());
        appendCheats(builder, cheats);
        appendLocations(builder, context.getLocations());

        appendSection(builder, promptProperties.getInstructionSectionTitle());
        appendLine(builder, "- 本章意图：" + defaultText(context.getChapterIntent(), "按既有剧情自然推进"));
        appendLine(builder, "- 用户指令：" + defaultText(context.getUserDirection(), "无"));
        appendLine(builder, "- POV：" + defaultText(context.getPov(), "未指定"));
        appendLine(builder, "");
        appendLine(builder, promptProperties.getUserWritingTips());
        if (Objects.nonNull(context.getTokenStats()) && Boolean.TRUE.equals(context.getTokenStats().getTruncated())) {
            appendLine(builder, promptProperties.getTruncationNotice());
        }
        return builder.toString();
    }

    /**
     * 替换 User Prompt 开头模板中的占位符。
     *
     * @param projectEntity 项目实体
     * @param context 上下文预览
     * @return 替换后的模板文本
     */
    private String replaceUserTemplate(NovelProjectEntity projectEntity, NovelRetrieveContextVO context) {
        return promptProperties.getUserTemplatePrefix()
                .replace("{chapterNumber}", defaultText(context.getChapterNumber(), "未知"))
                .replace("{pov}", defaultText(context.getPov(), "未指定"))
                .replace("{targetChapterWords}", defaultText(projectEntity.getTargetChapterWords(), "未配置"));
    }

    /**
     * 追加上下文文本块。
     *
     * @param builder Prompt 构造器
     * @param label 文本块标签
     * @param textBlock 文本块，可为空
     */
    private void appendTextBlock(StringBuilder builder, String label, NovelRetrieveContextVO.TextBlockVO textBlock) {
        if (Objects.isNull(textBlock)) {
            appendLine(builder, "- " + label + "：无");
            return;
        }
        appendLine(builder, "- " + label + "：" + safe(textBlock.getTitle()) + "｜" + safe(textBlock.getSummary()));
    }

    /**
     * 追加角色候选卡片。
     *
     * @param builder Prompt 构造器
     * @param characters 角色候选卡片列表
     */
    private void appendCharacters(StringBuilder builder, List<NovelRetrieveContextVO.CharacterCardVO> characters) {
        appendLine(builder, "- 角色：");
        if (characters.isEmpty()) {
            appendLine(builder, "  - 无");
            return;
        }
        for (NovelRetrieveContextVO.CharacterCardVO character : characters) {
            appendLine(builder, "  - " + safe(character.getName()) + "｜" + safe(character.getRoleType())
                    + "｜目标：" + safe(character.getCurrentGoal())
                    + "｜情绪：" + safe(character.getCurrentEmotion())
                    + "｜描述：" + safe(character.getDescription()));
        }
    }

    /**
     * 追加角色关系卡片。
     *
     * @param builder Prompt 构造器
     * @param relations 角色关系卡片列表
     */
    private void appendRelations(StringBuilder builder, List<NovelRetrieveContextVO.RelationCardVO> relations) {
        appendLine(builder, "- 关系：");
        if (relations.isEmpty()) {
            appendLine(builder, "  - 无");
            return;
        }
        for (NovelRetrieveContextVO.RelationCardVO relation : relations) {
            appendLine(builder, "  - " + safe(relation.getCharacterName()) + " -> " + safe(relation.getTargetCharacterName())
                    + "｜" + safe(relation.getRelationType()) + "｜" + safe(relation.getRelationDetail()));
        }
    }

    /**
     * 追加线索候选卡片。
     *
     * @param builder Prompt 构造器
     * @param clues 线索候选卡片列表
     */
    private void appendClues(StringBuilder builder, List<NovelRetrieveContextVO.ClueCardVO> clues) {
        appendLine(builder, "- 线索：");
        if (clues.isEmpty()) {
            appendLine(builder, "  - 无");
            return;
        }
        for (NovelRetrieveContextVO.ClueCardVO clue : clues) {
            appendLine(builder, "  - " + safe(clue.getName()) + "｜优先级：" + safe(clue.getPriority())
                    + "｜进展：" + safe(clue.getSummary()) + "｜设定：" + safe(clue.getDescription()));
        }
    }

    /**
     * 追加金手指卡片。
     *
     * @param builder Prompt 构造器
     * @param cheats 金手指列表
     */
    private void appendCheats(StringBuilder builder, List<NovelCheatEntity> cheats) {
        appendLine(builder, "- 金手指：");
        if (cheats.isEmpty()) {
            appendLine(builder, "  - 无");
            return;
        }
        for (NovelCheatEntity cheat : cheats) {
            appendLine(builder, "  - " + safe(cheat.getName()) + "｜效果：" + safe(cheat.getSummary())
                    + "｜限制：" + safe(cheat.getLimitation()) + "｜阶段：" + safe(cheat.getCurrentStage()));
        }
    }

    /**
     * 追加地点候选卡片。
     *
     * @param builder Prompt 构造器
     * @param locations 地点候选卡片列表
     */
    private void appendLocations(StringBuilder builder, List<NovelRetrieveContextVO.LocationCardVO> locations) {
        appendLine(builder, "- 地点：");
        if (locations.isEmpty()) {
            appendLine(builder, "  - 无");
            return;
        }
        for (NovelRetrieveContextVO.LocationCardVO location : locations) {
            appendLine(builder, "  - " + safe(location.getName()) + "｜来源：" + safe(location.getSource()) + "｜" + safe(location.getSummary()));
        }
    }

    /**
     * 追加分节标题。
     *
     * @param builder Prompt 构造器
     * @param title 分节标题
     */
    private void appendSection(StringBuilder builder, String title) {
        appendLine(builder, "");
        appendLine(builder, "【" + safe(title) + "】");
    }

    /**
     * 追加一行文本。
     *
     * @param builder Prompt 构造器
     * @param value 文本内容
     */
    private void appendLine(StringBuilder builder, String value) {
        builder.append(value).append(System.lineSeparator());
    }

    /**
     * 把空值转换成人可读的短文本。
     *
     * @param value 原始值
     * @return 非空字符串
     */
    private String safe(Object value) {
        if (Objects.isNull(value)) {
            return "无";
        }
        return StringUtils.defaultIfBlank(String.valueOf(value), "无");
    }

    /**
     * 把空值转换成指定默认文本。
     *
     * @param value 原始值
     * @param defaultValue 默认文本
     * @return 非空字符串
     */
    private String defaultText(Object value, String defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        return StringUtils.defaultIfBlank(String.valueOf(value), defaultValue);
    }

    /**
     * 轻量估算文本 Token 数。
     *
     * @param value 文本
     * @return 估算 Token 数
     */
    private int estimateText(String value) {
        return StringUtils.length(value);
    }
}
