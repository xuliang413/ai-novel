package net.lab1024.sa.admin.module.business.novel.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
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
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 小说提示词组装服务测试。
 * <p>
 * 覆盖 Task 11 的 System Prompt 与 User Prompt 拼装契约，重点验证叙事规则排序、项目约束注入、上下文卡片注入和模板占位符替换。
 *
 * @Author AI-Novel
 */
class NovelPromptServiceTest {

    /**
     * 被测提示词服务。
     */
    private NovelPromptService novelPromptService;

    /**
     * 项目 DAO mock，用来读取世界观、文风和目标字数。
     */
    private NovelProjectDao novelProjectDao;

    /**
     * 叙事规则 DAO mock，用来读取并排序 System Prompt 规则。
     */
    private NovelNarrativeRuleDao novelNarrativeRuleDao;

    /**
     * 金手指 DAO mock，用来把项目级金手指注入 User Prompt。
     */
    private NovelCheatDao novelCheatDao;

    /**
     * 上下文检索服务 mock，用来提供 Task 10 的结构化检索结果。
     */
    private NovelRetrieveService novelRetrieveService;

    /**
     * 提示词模板配置，测试中直接构造，避免依赖 Spring 容器。
     */
    private NovelPromptProperties promptProperties;

    /**
     * 每个用例前重建服务、DAO mock 和 MyBatis 表元信息。
     */
    @BeforeEach
    void setup() {
        novelPromptService = new NovelPromptService();
        novelProjectDao = mock(NovelProjectDao.class);
        novelNarrativeRuleDao = mock(NovelNarrativeRuleDao.class);
        novelCheatDao = mock(NovelCheatDao.class);
        novelRetrieveService = mock(NovelRetrieveService.class);
        promptProperties = new NovelPromptProperties();
        promptProperties.setSystemTemplatePrefix("你是专业网文作者，请严格遵守项目约束。");
        promptProperties.setUserTemplatePrefix("请续写第{chapterNumber}章，POV：{pov}，目标字数：{targetChapterWords}。");

        MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelProjectEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelNarrativeRuleEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, NovelCheatEntity.class);

        ReflectionTestUtils.setField(novelPromptService, "novelProjectDao", novelProjectDao);
        ReflectionTestUtils.setField(novelPromptService, "novelNarrativeRuleDao", novelNarrativeRuleDao);
        ReflectionTestUtils.setField(novelPromptService, "novelCheatDao", novelCheatDao);
        ReflectionTestUtils.setField(novelPromptService, "novelRetrieveService", novelRetrieveService);
        ReflectionTestUtils.setField(novelPromptService, "promptProperties", promptProperties);
    }

    /**
     * System Prompt 应按叙事规则 priority 降序注入，并追加世界观、文风和目标字数。
     */
    @Test
    void buildPromptShouldAssembleSystemPromptWithSortedRulesAndProjectConstraints() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelNarrativeRuleDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(
                buildNarrativeRule(90002L, "文风要求", "白描克制，少用形容词。", 3),
                buildNarrativeRule(90001L, "平台红线", "避免血腥露骨描写。", 5)
        ));
        when(novelCheatDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(novelRetrieveService.buildContextPreview(eq(10001L), eq(12), eq("强化悬疑"), eq(7L))).thenReturn(ResponseDTO.ok(buildRetrieveContext()));

        ResponseDTO<NovelPromptVO> response = novelPromptService.buildPrompt(10001L, 12, "强化悬疑", 7L);

        assertTrue(response.getOk());
        NovelPromptVO prompt = response.getData();
        assertTrue(prompt.getSystemPrompt().contains("你是专业网文作者"));
        assertTrue(prompt.getSystemPrompt().indexOf("平台红线") < prompt.getSystemPrompt().indexOf("文风要求"));
        assertTrue(prompt.getSystemPrompt().contains("末法时代灵气稀薄"));
        assertTrue(prompt.getSystemPrompt().contains("冷峻短句"));
        assertTrue(prompt.getSystemPrompt().contains("每章目标字数：3000"));
        assertEquals(10001L, prompt.getProjectId());
        assertEquals(12, prompt.getChapterNumber());
        assertFalse(prompt.getEstimatedTokens() <= 0);
    }

    /**
     * User Prompt 应注入检索上下文中的卷概要、前章、角色、关系、线索、地点和写作指令。
     */
    @Test
    void buildPromptShouldAssembleUserPromptFromRetrieveContext() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(buildProjectEntity());
        when(novelNarrativeRuleDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(novelCheatDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(buildCheatEntity()));
        when(novelRetrieveService.buildContextPreview(eq(10001L), eq(12), eq("强化悬疑"), eq(7L))).thenReturn(ResponseDTO.ok(buildRetrieveContext()));

        ResponseDTO<NovelPromptVO> response = novelPromptService.buildPrompt(10001L, 12, "强化悬疑", 7L);

        assertTrue(response.getOk());
        String userPrompt = response.getData().getUserPrompt();
        assertTrue(userPrompt.contains("请续写第12章，POV：李四，目标字数：3000。"));
        assertTrue(userPrompt.contains("第二卷：京城风云"));
        assertTrue(userPrompt.contains("上一章：第十一章 暗室密函"));
        assertTrue(userPrompt.contains("李四"));
        assertTrue(userPrompt.contains("王五"));
        assertTrue(userPrompt.contains("KNOWS"));
        assertTrue(userPrompt.contains("灭门真相"));
        assertTrue(userPrompt.contains("青云宗暗室"));
        assertTrue(userPrompt.contains("万倍悟性"));
        assertTrue(userPrompt.contains("强化悬疑"));
        assertTrue(userPrompt.contains("注意：部分上下文已被压缩或丢弃"));
    }

    /**
     * 项目无权访问时，服务应在 Prompt 组装前拒绝，避免泄漏其他用户的世界观和规则。
     */
    @Test
    void buildPromptShouldRejectForeignProject() {
        when(novelProjectDao.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        ResponseDTO<NovelPromptVO> response = novelPromptService.buildPrompt(10001L, 12, "强化悬疑", 7L);

        assertFalse(response.getOk());
    }

    /**
     * 构造一个属于当前用户的项目实体。
     *
     * @return 项目实体
     */
    private NovelProjectEntity buildProjectEntity() {
        NovelProjectEntity entity = new NovelProjectEntity();
        entity.setId(10001L);
        entity.setName("剑道独尊");
        entity.setWorldBuilding("末法时代灵气稀薄，修行者依赖丹药。");
        entity.setStyleDescription("冷峻短句，动作描写清晰。");
        entity.setTargetChapterWords(3000);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造一条叙事规则。
     *
     * @param id 规则ID
     * @param name 规则名
     * @param content 规则内容
     * @param priority 优先级
     * @return 叙事规则实体
     */
    private NovelNarrativeRuleEntity buildNarrativeRule(Long id, String name, String content, Integer priority) {
        NovelNarrativeRuleEntity entity = new NovelNarrativeRuleEntity();
        entity.setId(id);
        entity.setProjectId(10001L);
        entity.setName(name);
        entity.setContent(content);
        entity.setPriority(priority);
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造一个金手指实体。
     *
     * @return 金手指实体
     */
    private NovelCheatEntity buildCheatEntity() {
        NovelCheatEntity entity = new NovelCheatEntity();
        entity.setId(70001L);
        entity.setProjectId(10001L);
        entity.setName("万倍悟性");
        entity.setSummary("主角悟性远超常人。");
        entity.setLimitation("连续使用会透支精神。");
        entity.setDeletedFlag(Boolean.FALSE);
        entity.setCreateUserId(7L);
        return entity;
    }

    /**
     * 构造 Task 10 输出的上下文预览。
     *
     * @return 上下文预览
     */
    private NovelRetrieveContextVO buildRetrieveContext() {
        NovelRetrieveContextVO context = new NovelRetrieveContextVO();
        context.setProjectId(10001L);
        context.setChapterNumber(12);
        context.setPov("李四");
        context.setChapterIntent("李四拆解密函，发现幕后黑手线索。");
        context.setUserDirection("强化悬疑");
        context.setVolumeSummary(buildTextBlock("VOLUME", 100001L, "第二卷：京城风云", "李四进入京城调查灭门真相。"));
        context.setPreviousChapter(buildTextBlock("PREVIOUS_CHAPTER", 30011L, "第十一章 暗室密函", "李四在暗室中发现密函。"));
        context.setOutline(buildTextBlock("OUTLINE", 40012L, "第十二章细纲", "拆解密函，王五赶来提供旁证。"));
        context.getCharacters().add(buildCharacterCard(20001L, "李四", "POV", 10));
        context.getCharacters().add(buildCharacterCard(20002L, "王五", "PREVIOUS_CHAPTER", 20));
        context.getRelations().add(buildRelationCard());
        context.getClues().add(buildClueCard());
        context.getLocations().add(buildLocationCard());
        NovelRetrieveContextVO.TokenStatsVO tokenStats = new NovelRetrieveContextVO.TokenStatsVO();
        tokenStats.setTokenBudget(6000);
        tokenStats.setTokenHardLimit(8000);
        tokenStats.setEstimatedTokens(420);
        tokenStats.setTruncated(Boolean.TRUE);
        tokenStats.setShortenedCount(2);
        tokenStats.setDiscardedCount(1);
        context.setTokenStats(tokenStats);
        return context;
    }

    /**
     * 构造上下文文本块。
     *
     * @param sourceType 来源类型
     * @param sourceId 来源ID
     * @param title 标题
     * @param summary 摘要
     * @return 文本块
     */
    private NovelRetrieveContextVO.TextBlockVO buildTextBlock(String sourceType, Long sourceId, String title, String summary) {
        NovelRetrieveContextVO.TextBlockVO block = new NovelRetrieveContextVO.TextBlockVO();
        block.setSourceType(sourceType);
        block.setSourceId(sourceId);
        block.setTitle(title);
        block.setSummary(summary);
        block.setDetailLevel("FULL");
        return block;
    }

    /**
     * 构造角色卡片。
     *
     * @param id 角色ID
     * @param name 角色名
     * @param source 候选来源
     * @param priority 排序权重
     * @return 角色卡片
     */
    private NovelRetrieveContextVO.CharacterCardVO buildCharacterCard(Long id, String name, String source, Integer priority) {
        NovelRetrieveContextVO.CharacterCardVO card = new NovelRetrieveContextVO.CharacterCardVO();
        card.setId(id);
        card.setName(name);
        card.setRoleType("PROTAGONIST");
        card.setDescription(name + "正在追查灭门真相。");
        card.setCurrentGoal("确认密函来源。");
        card.setCurrentEmotion("克制紧张");
        card.setSource(source);
        card.setPriority(priority);
        card.setDetailLevel("FULL");
        return card;
    }

    /**
     * 构造角色关系卡片。
     *
     * @return 角色关系卡片
     */
    private NovelRetrieveContextVO.RelationCardVO buildRelationCard() {
        NovelRetrieveContextVO.RelationCardVO card = new NovelRetrieveContextVO.RelationCardVO();
        card.setCharacterName("李四");
        card.setTargetCharacterName("王五");
        card.setRelationType("KNOWS");
        card.setRelationDetail("ALLY");
        card.setDetailLevel("FULL");
        return card;
    }

    /**
     * 构造线索卡片。
     *
     * @return 线索卡片
     */
    private NovelRetrieveContextVO.ClueCardVO buildClueCard() {
        NovelRetrieveContextVO.ClueCardVO card = new NovelRetrieveContextVO.ClueCardVO();
        card.setName("灭门真相");
        card.setDescription("李四追查家族灭门背后的真正主谋。");
        card.setSummary("密函显示幕后黑手来自京城。");
        card.setPriority(5);
        card.setDetailLevel("FULL");
        return card;
    }

    /**
     * 构造地点卡片。
     *
     * @return 地点卡片
     */
    private NovelRetrieveContextVO.LocationCardVO buildLocationCard() {
        NovelRetrieveContextVO.LocationCardVO card = new NovelRetrieveContextVO.LocationCardVO();
        card.setName("青云宗暗室");
        card.setSummary("宗门旧案密卷存放处。");
        card.setSource("CURRENT_LOCATION");
        card.setPriority(10);
        card.setDetailLevel("FULL");
        return card;
    }
}
