package net.lab1024.sa.admin.module.business.novel.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.lab1024.sa.admin.module.business.novel.config.NovelLLMConfig;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelChapterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelCharacterEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelClueEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelLocationEntity;
import net.lab1024.sa.admin.module.business.novel.domain.entity.NovelProjectEntity;
import net.lab1024.sa.admin.module.business.novel.domain.model.ChapterIntentModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.ContextPreviewModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchModel;
import net.lab1024.sa.admin.module.business.novel.domain.model.NovelGraphPatchOperationModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * 对接 DeepSeek / 通义千问大模型。
 *
 * 干了三件事：
 * 1. 生成章节 —— 把小说设定 + 检索到的上下文发给 AI，让它写出一章
 * 2. 抽取图谱变更 —— 让 AI 读完刚写的正文，挑出哪些角色出场了、哪条线索被推进了
 * 3. 流式输出 —— 每生成一个字就实时推送，不用等整章写完才能看
 *
 * 两个生成方法：generateChapter 等完整结果 / generateChapterStream 逐字推送
 */
@Slf4j
@Service
public class NovelLLMService {

    @Resource
    private NovelLLMConfig llmConfig;

    @Resource
    private NovelRetrieveService retrieveService;

    /**
     * 等 AI 写完再拿到完整结果，中间不推送。
     *
     * 流程：
     * 1. 从 Neo4j 检索上下文（前章说了什么、角色当前状态、线索进度）
     * 2. 拼一段提示词告诉 AI 要写第几章、什么视角、什么目标
     * 3. 调 AI 接口，等它写完整章再拿回来
     * 4. 解析 AI 返回的 JSON（title + summary + content）
     */
    public LLMChapterResult generateChapter(NovelProjectEntity project,
                                             ChapterIntentModel intent,
                                             ContextPreviewModel context,
                                             String provider) {
        OpenAiChatModel model = resolveModel(provider);
        if (model == null) {
            return null;
        }

        NovelRetrieveService.RetrievalResult retrieved = retrieveService.retrieve(project, intent);
        String systemPrompt = buildSystemPrompt(project, intent);
        String userPrompt = buildUserPrompt(project, intent, retrieved);

        log.info("LLM generate chapter: provider={}, chapterNo={}, pov={}, model={}",
                provider, intent.getChapterNo(), intent.getPov(), model);

        try {
            String response = model.generate(List.of(
                    SystemMessage.from(systemPrompt),
                    UserMessage.from(userPrompt)
            )).content().text();
            log.info("LLM response length: {}", StringUtils.length(response));
            return parseChapterResponse(response, project, intent);
        } catch (Exception e) {
            log.error("LLM generation failed: provider={}, error={}", provider, e.getMessage());
            return new LLMChapterResult(null, null, "【生成失败】" + e.getMessage());
        }
    }

    /**
     * 按 provider 获取阻塞式模型。
     *
     * 模型按需创建，API Key 为空时返回 null（不抛异常）。
     */
    OpenAiChatModel resolveModel(String provider) {
        if ("DEEPSEEK".equals(provider)) {
            return llmConfig.createDeepseekModel();
        }
        if ("TONGYI".equals(provider)) {
            return llmConfig.createTongyiModel();
        }
        return null;
    }

    /**
     * 按 provider 获取流式模型（用于 SSE 实时推送）。
     */
    OpenAiStreamingChatModel resolveStreamingModel(String provider) {
        if ("DEEPSEEK".equals(provider)) {
            return llmConfig.createDeepseekStreamingModel();
        }
        if ("TONGYI".equals(provider)) {
            return llmConfig.createTongyiStreamingModel();
        }
        return null;
    }

    /**
     * AI 写到哪就立即推送哪个字，前端可以逐字渲染。
     *
     * 跟 generateChapter 的区别：不等人写完整章，每写完一个字就通过回调推出去。
     * 收到 onComplete 时才解析完整 JSON 拿到 title + summary。
     *
     * @param onToken    每写完一个字就回调一次，参数就是这个字
     * @param onComplete 整章写完后回调，参数是解析好的结果（标题+摘要+正文）
     * @param onError    出错了回调，参数是错误信息
     */

    public void generateChapterStream(NovelProjectEntity project,
                                       ChapterIntentModel intent,
                                       ContextPreviewModel context,
                                       String provider,
                                       java.util.function.Consumer<String> onToken,
                                       java.util.function.Consumer<LLMChapterResult> onComplete,
                                       java.util.function.Consumer<String> onError) {
        OpenAiStreamingChatModel model = resolveStreamingModel(provider);
        if (model == null) {
            onError.accept("未找到可用的 LLM Provider: " + provider);
            return;
        }

        String systemPrompt = buildSystemPrompt(project, intent);
        NovelRetrieveService.RetrievalResult retrieved = retrieveService.retrieve(project, intent);
        String userPrompt = buildUserPrompt(project, intent, retrieved);

        log.info("LLM stream generate: provider={}, chapterNo={}, pov={}",
                provider, intent.getChapterNo(), intent.getPov());

        StringBuilder fullResponse = new StringBuilder();

        model.generate(
                List.of(SystemMessage.from(systemPrompt), UserMessage.from(userPrompt)),
                new StreamingResponseHandler<AiMessage>() {
                    @Override
                    public void onNext(String token) {
                        fullResponse.append(token);
                        onToken.accept(token);
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        LLMChapterResult result = parseChapterResponse(
                                fullResponse.toString(), project, intent);
                        onComplete.accept(result);
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("LLM stream error: {}", error.getMessage());
                        onError.accept(error.getMessage());
                    }
                });
    }

    /**
     * 告诉 AI 它是谁、要遵守什么规则、返回什么格式。
     *
     * 目前是写死的几条基本要求。以后应该从 NarrativeRule 节点动态加载，
     * 这样不同项目可以有不同文风、字数、平台红线的配置。
     */
    private String buildSystemPrompt(NovelProjectEntity project, ChapterIntentModel intent) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位专业的小说作家，擅长");
        sb.append(StringUtils.defaultIfBlank(project.getGenre(), "文学"));
        sb.append("类写作。\n\n");

        sb.append("## 写作要求\n");
        sb.append("1. 文笔流畅自然，情节有张力，人物对话生动\n");
        sb.append("2. 本章字数目标：1500-3000 字\n");
        sb.append("3. 必须包含清晰的章节标题和内容\n");
        sb.append("4. 章末为下一章留下悬念或推进入口\n\n");

        sb.append("## 返回格式\n");
        sb.append("请严格按照以下 JSON 格式返回，不要包含其他内容：\n");
        sb.append("{\"title\":\"第N章：章节标题\",\"summary\":\"本章摘要（100字内）\",\"content\":\"章节正文\"}\n");
        sb.append("注意：content 字段为 Markdown 格式的正文，不要将 JSON 嵌套在 content 内。");

        return sb.toString();
    }

    /**
     * 告诉 AI 这一章的具体创作要求——写什么。
     *
     * 拼装的内容来自 Neo4j 检索结果，不是直接从 MySQL 查全表：
     * - 前一章发生了什么（提醒 AI 要衔接）
     * - 这章涉及哪些角色、他们现在什么状态/情绪/在哪
     * - 角色之间有啥恩怨情仇（只给能推动剧情的关系）
     * - 这章要推进哪些线索，线索现在到什么程度了
     * - POV 角色目前人在哪里
     *
     * 如果内容太长超过 8000 token，会硬截断。以后要改成按优先级智能裁剪。
     */
    private String buildUserPrompt(NovelProjectEntity project,
                                    ChapterIntentModel intent,
                                    NovelRetrieveService.RetrievalResult r) {
        StringBuilder sb = new StringBuilder();
        sb.append("请为小说《").append(project.getProjectName()).append("》撰写第")
                .append(intent.getChapterNo()).append("章。\n\n");
        sb.append("作品类型：").append(StringUtils.defaultIfBlank(project.getGenre(), "未分类")).append("\n");
        sb.append("主角：").append(StringUtils.defaultIfBlank(project.getProtagonist(), "未指定")).append("\n\n");

        sb.append("本章 POV：").append(intent.getPov()).append("\n");
        sb.append("本章目标：").append(intent.getChapterGoal()).append("\n\n");

        if (StringUtils.isNotBlank(r.previousChapterSummary)) {
            sb.append("## 前一章摘要\n").append(r.previousChapterSummary).append("\n\n");
        }

        if (!r.characterStateCards.isEmpty()) {
            sb.append("## 候选角色\n");
            for (Map.Entry<String, Map<String, Object>> e : r.characterStateCards.entrySet()) {
                Map<String, Object> card = e.getValue();
                sb.append("- ").append(e.getKey()).append(" [").append(toString(card.get("role")))
                        .append("·").append(toString(card.get("status")))
                        .append("·").append(toString(card.get("emotion"))).append("]");
                if (StringUtils.isNotBlank(toString(card.get("goal")))) {
                    sb.append(" 目标：").append(card.get("goal"));
                }
                if (StringUtils.isNotBlank(toString(card.get("currentLocation")))) {
                    sb.append(" 位于").append(card.get("currentLocation"));
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        if (CollectionUtils.isNotEmpty(r.keyRelations)) {
            sb.append("## 关键关系\n");
            for (String rel : r.keyRelations) {
                sb.append("- ").append(rel).append("\n");
            }
            sb.append("\n");
        }

        if (!r.targetClueProgress.isEmpty()) {
            sb.append("## 目标线索\n");
            for (Map.Entry<String, Map<String, Object>> e : r.targetClueProgress.entrySet()) {
                Map<String, Object> card = e.getValue();
                sb.append("- ").append(e.getKey()).append(" [").append(toString(card.get("status")))
                        .append("] ").append(toString(card.get("summary"))).append("\n");
            }
            sb.append("\n");
        }

        if (r.povCurrentLocation != null) {
            sb.append("## POV 当前位置\n");
            sb.append("POV 角色 ").append(r.pov).append(" 当前位于：").append(toString(r.povCurrentLocation.get("name")))
                    .append("（").append(StringUtils.abbreviate(toString(r.povCurrentLocation.get("description")), 60)).append("）\n\n");
        }

        if (CollectionUtils.isNotEmpty(intent.getExtraInstructions())) {
            sb.append("## 额外指示\n");
            intent.getExtraInstructions().forEach(i -> sb.append("- ").append(i).append("\n"));
            sb.append("\n");
        }

        sb.append("请开始生成第").append(intent.getChapterNo()).append("章。");

        String prompt = sb.toString();
        int estimatedTokens = prompt.length() / 2;
        if (estimatedTokens > HARD_TRUNCATION) {
            log.warn("Truncated token from {} to {}", estimatedTokens, HARD_TRUNCATION);
            r.truncatedItems = Math.max(1, (estimatedTokens - HARD_TRUNCATION) / 2);
            prompt = prompt.substring(0, HARD_TRUNCATION * 2);
        }
        log.info("Retrieval prompt tokens: ~{}", prompt.length() / 2);
        return prompt;
    }

    private static String toString(Object value) {
        return value == null ? "" : value.toString();
    }

    /**
     * 简易 token 估算：中文每字符约 0.5 token。
     */
    private static final int HARD_TRUNCATION = 8000;

    /**
     * 把 AI 返回的原始文本拆成 标题 + 摘要 + 正文。
     *
     * AI 有时候不听话，在 JSON 前后多写了一些废话（比如"好的，以下是..."），
     * 所以要找到第一个 { 到最后一个 } 之间的内容来解析。
     * 解析失败也不报错，拿原始文本当正文用。
     */
    LLMChapterResult parseChapterResponse(String response, NovelProjectEntity project, ChapterIntentModel intent) {
        String jsonStr = response.trim();
        if (!jsonStr.startsWith("{")) {
            int idx = jsonStr.indexOf("{");
            if (idx > 0) {
                jsonStr = jsonStr.substring(idx);
            }
        }
        int lastBrace = jsonStr.lastIndexOf("}");
        if (lastBrace > 0 && lastBrace < jsonStr.length() - 1) {
            jsonStr = jsonStr.substring(0, lastBrace + 1);
        }

        try {
            JSONObject json = JSON.parseObject(jsonStr);
            String title = json.getString("title");
            String summary = json.getString("summary");
            String content = json.getString("content");

            if (StringUtils.isBlank(title)) {
                title = "第" + intent.getChapterNo() + "章";
            }
            if (StringUtils.isBlank(summary)) {
                summary = StringUtils.abbreviate(StringUtils.defaultString(content), 200);
            }
            return new LLMChapterResult(title, summary, content);
        } catch (Exception e) {
            log.warn("Failed to parse LLM JSON response, using raw text. error={}", e.getMessage());
            String title = "第" + intent.getChapterNo() + "章";
            return new LLMChapterResult(title, StringUtils.abbreviate(response, 200), response);
        }
    }

    public record LLMChapterResult(String title, String summary, String content) {
    }

    /**
     * 让 AI 读一遍刚写完的正文，自动挑出图谱需要更新的地方。
     *
     * 具体做法：
     * 1. 把"已知角色/地点/线索列表" + "本章正文（前3000字）"一起发给 AI
     * 2. AI 看完后返回 JSON，列出来：这章谁出场了、哪条线索推进了
     * 3. 后端拿 AI 返回的名字去匹配数据库里已有的实体，填上对应的 ID
     *
     * 如果 AI 调用失败或者返回的东西解析不了，这里返回 null，
     * 外面会自动改成用老办法（buildGraphPatch）生成一个默认的变更单。
     */
    public NovelGraphPatchModel extractGraphPatch(NovelProjectEntity project,
                                                   NovelChapterEntity chapter,
                                                   List<NovelCharacterEntity> characters,
                                                   List<NovelLocationEntity> locations,
                                                   List<NovelClueEntity> clues,
                                                   String provider) {
        OpenAiChatModel model = resolveModel(provider);
        if (model == null) {
            return null;
        }

        String systemPrompt = """
                你是一位专业的小说编辑和分析师。你的任务是根据刚写完的章节正文，识别出知识图谱中需要更新的实体变更。

                ## 操作类型
                1. UPDATE_CHAPTER_SUMMARY - 更新本章摘要和状态
                2. MARK_APPEARANCE - 标记角色或地点在本章出场
                3. ADVANCE_CLUE - 标记本章推进了某条线索

                ## 分析规则
                - 只标记正文中确实出现、有实际戏份的角色（不仅仅是提到名字）
                - 地点必须是本章主要场景发生地
                - 线索必须有实质推进（新信息、状态变化），不能只是提到
                - confidence 取 HIGH（充分证据）、MEDIUM（间接证据）、LOW（猜测）
                - 为每个操作提供 evidence 字段，引用正文原文

                ## 返回 JSON 格式
                {"chapterSummary":"100字内本章摘要","operations":[{"operationType":"MARK_APPEARANCE","targetType":"CHARACTER","targetName":"角色名","confidence":"HIGH","evidence":"引用的原文证据"},{"operationType":"ADVANCE_CLUE","targetName":"线索名","afterStatus":"ACTIVE","afterSummary":"推进后的线索状态摘要","confidence":"HIGH","evidence":"引用的原文证据"}]}

                注意：只返回 JSON，不要任何其他文字。
                """;

        String userPrompt = buildExtractionPrompt(project, chapter, characters, locations, clues);

        log.info("LLM extract graph patch: provider={}, chapterNo={}", provider, chapter.getChapterNo());

        try {
            String response = model.generate(List.of(
                    SystemMessage.from(systemPrompt),
                    UserMessage.from(userPrompt)
            )).content().text();
            log.info("LLM extract graph patch response length: {}", StringUtils.length(response));
            return parseExtractionResponse(response, project, chapter, characters, locations, clues);
        } catch (Exception e) {
            log.error("LLM extraction failed, fallback to mock. error={}", e.getMessage());
            return null;
        }
    }

    private String buildExtractionPrompt(NovelProjectEntity project,
                                          NovelChapterEntity chapter,
                                          List<NovelCharacterEntity> characters,
                                          List<NovelLocationEntity> locations,
                                          List<NovelClueEntity> clues) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 作品信息\n");
        sb.append("作品：").append(project.getProjectName()).append("\n");
        sb.append("类型：").append(StringUtils.defaultIfBlank(project.getGenre(), "未知")).append("\n");
        sb.append("主角：").append(StringUtils.defaultIfBlank(project.getProtagonist(), "未指定")).append("\n\n");

        sb.append("## 已知角色\n");
        for (NovelCharacterEntity c : characters) {
            sb.append("- ").append(c.getCharacterName())
                    .append("（").append(StringUtils.defaultString(c.getRoleType(), "角色")).append("）")
                    .append("：").append(StringUtils.defaultString(c.getSummary(), "")).append("\n");
        }
        sb.append("\n");

        sb.append("## 已知地点\n");
        for (NovelLocationEntity l : locations) {
            sb.append("- ").append(l.getLocationName())
                    .append("（").append(StringUtils.defaultString(l.getLocationType(), "")).append("）")
                    .append("：").append(StringUtils.defaultString(l.getSummary(), "")).append("\n");
        }
        sb.append("\n");

        sb.append("## 已知线索\n");
        for (NovelClueEntity cl : clues) {
            sb.append("- ").append(cl.getClueName())
                    .append(" [").append(cl.getClueStatus()).append("]")
                    .append("：").append(StringUtils.defaultString(cl.getSummary(), "")).append("\n");
        }
        sb.append("\n");

        sb.append("## 本章正文（第").append(chapter.getChapterNo()).append("章）\n");
        sb.append(StringUtils.abbreviate(StringUtils.defaultString(chapter.getContent()), 3000));
        sb.append("\n\n");

        sb.append("请分析以上正文，识别本章出场的角色、推进的线索，返回 JSON。");

        return sb.toString();
    }

    /**
     * 把 AI 返回的抽取结果 JSON 转成标准变更单。
     *
     * 处理逻辑：
     * - 总是加一条"更新本章摘要"的操作（不管 AI 有没有返回，这条必做）
     * - 遍历 AI 返回的 operations 数组，碰到 MARK_APPEARANCE 就去匹配角色/地点，碰到 ADVANCE_CLUE 就去匹配线索
     * - AI 可能认错名字，匹配不上的实体 targetId 留空，写入时跳过
     * - JSON 解析失败也不全丢，至少保留"更新本章摘要"这一条
     */
    NovelGraphPatchModel parseExtractionResponse(String response,
                                                  NovelProjectEntity project,
                                                  NovelChapterEntity chapter,
                                                  List<NovelCharacterEntity> characters,
                                                  List<NovelLocationEntity> locations,
                                                  List<NovelClueEntity> clues) {
        String jsonStr = response.trim();
        if (!jsonStr.startsWith("{")) {
            int idx = jsonStr.indexOf("{");
            if (idx > 0) {
                jsonStr = jsonStr.substring(idx);
            }
        }
        int lastBrace = jsonStr.lastIndexOf("}");
        if (lastBrace > 0 && lastBrace < jsonStr.length() - 1) {
            jsonStr = jsonStr.substring(0, lastBrace + 1);
        }

        NovelGraphPatchModel patch = new NovelGraphPatchModel();
        patch.setPatchId(java.util.UUID.randomUUID().toString());
        patch.setOperationBatchId(java.util.UUID.randomUUID().toString());
        patch.setProjectId(project.getProjectId());
        patch.setChapterId(chapter.getChapterId());
        patch.setChapterNo(chapter.getChapterNo());
        patch.setStatus("READY");

        try {
            JSONObject json = JSON.parseObject(jsonStr);
            String chapterSummary = json.getString("chapterSummary");

            NovelGraphPatchOperationModel summaryOp = new NovelGraphPatchOperationModel();
            summaryOp.setOperationId(java.util.UUID.randomUUID().toString());
            summaryOp.setOperationType("UPDATE_CHAPTER_SUMMARY");
            summaryOp.setTargetType("CHAPTER");
            summaryOp.setTargetId(chapter.getChapterId());
            summaryOp.setTargetName("第" + chapter.getChapterNo() + "章");
            summaryOp.setBeforeStatus(chapter.getStatus());
            summaryOp.setAfterStatus("PUBLISHED");
            summaryOp.setBeforeSummary(chapter.getSummary());
            summaryOp.setAfterSummary(StringUtils.defaultIfBlank(chapterSummary, chapter.getSummary()));
            summaryOp.setConfidence("HIGH");
            summaryOp.setValidationStatus("READY");
            summaryOp.setSelected(true);
            summaryOp.setEvidence("AI 抽取本章摘要。");
            summaryOp.setReason("发布章节时同步更新 Chapter 节点摘要和状态。");
            patch.getOperations().add(summaryOp);

            if (json.containsKey("operations")) {
                for (Object item : json.getJSONArray("operations")) {
                    JSONObject opJson = (JSONObject) item;
                    String opType = opJson.getString("operationType");
                    String targetName = opJson.getString("targetName");
                    String confidence = StringUtils.defaultIfBlank(opJson.getString("confidence"), "MEDIUM");
                    String evidence = StringUtils.defaultIfBlank(opJson.getString("evidence"), "");
                    String targetType = StringUtils.defaultIfBlank(opJson.getString("targetType"), "");

                    if ("MARK_APPEARANCE".equals(opType)) {
                        NovelGraphPatchOperationModel op = buildAppearanceOp(patch, targetType, targetName, confidence, evidence, characters, locations);
                        if (op != null) {
                            patch.getOperations().add(op);
                        }
                    } else if ("ADVANCE_CLUE".equals(opType)) {
                        NovelGraphPatchOperationModel op = buildClueOp(patch, targetName, opJson, confidence, evidence, clues);
                        if (op != null) {
                            patch.getOperations().add(op);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse LLM extraction JSON. error={}", e.getMessage());
        }

        if (patch.getOperations().stream().anyMatch(op -> "LOW".equals(op.getValidationStatus()))) {
            patch.getWarnings().add("存在低置信操作，请人工确认。");
        }

        return patch;
    }

    /**
     * 根据名字自动判断这是个角色还是个地点。
     *
     * 先在角色表里找这个名字，找到就是角色；地点表里找到就是地点；都找不到就当角色处理。
     */
    private NovelGraphPatchOperationModel buildAppearanceOp(NovelGraphPatchModel patch,
                                                             String targetType,
                                                             String targetName,
                                                             String confidence,
                                                             String evidence,
                                                             List<NovelCharacterEntity> characters,
                                                             List<NovelLocationEntity> locations) {
        if (StringUtils.isBlank(targetType)) {
            targetType = guessEntityType(targetName, characters, locations);
        }

        Long targetId = null;
        if ("LOCATION".equals(targetType)) {
            for (NovelLocationEntity l : locations) {
                if (l.getLocationName().equals(targetName)) {
                    targetId = l.getLocationId();
                    break;
                }
            }
        } else {
            for (NovelCharacterEntity c : characters) {
                if (c.getCharacterName().equals(targetName)) {
                    targetId = c.getCharacterId();
                    break;
                }
            }
            if (targetId == null) {
                targetType = "CHARACTER";
            }
        }

        NovelGraphPatchOperationModel op = new NovelGraphPatchOperationModel();
        op.setOperationId(java.util.UUID.randomUUID().toString());
        op.setOperationType("MARK_APPEARANCE");
        op.setTargetType(StringUtils.defaultIfBlank(targetType, "CHARACTER"));
        op.setTargetId(targetId);
        op.setTargetName(targetName);
        op.setConfidence(confidence);
        op.setValidationStatus("LOW".equals(confidence) ? "LOW_CONFIDENCE" : "READY");
        op.setSelected(!"LOW".equals(confidence));
        op.setEvidence(evidence);
        op.setReason("AI 从正文抽取的出场记录。");
        return op;
    }

    private NovelGraphPatchOperationModel buildClueOp(NovelGraphPatchModel patch,
                                                       String targetName,
                                                       JSONObject opJson,
                                                       String confidence,
                                                       String evidence,
                                                       List<NovelClueEntity> clues) {
        Long targetId = null;
        String beforeStatus = "DORMANT";
        String beforeSummary = "";
        for (NovelClueEntity cl : clues) {
            if (cl.getClueName().equals(targetName)) {
                targetId = cl.getClueId();
                beforeStatus = cl.getClueStatus();
                beforeSummary = cl.getSummary();
                break;
            }
        }

        String afterStatus = StringUtils.defaultIfBlank(opJson.getString("afterStatus"), "ACTIVE");
        String afterSummary = StringUtils.defaultIfBlank(opJson.getString("afterSummary"), beforeSummary);

        NovelGraphPatchOperationModel op = new NovelGraphPatchOperationModel();
        op.setOperationId(java.util.UUID.randomUUID().toString());
        op.setOperationType("ADVANCE_CLUE");
        op.setTargetType("CLUE");
        op.setTargetId(targetId);
        op.setTargetName(targetName);
        op.setBeforeStatus(beforeStatus);
        op.setAfterStatus(afterStatus);
        op.setBeforeSummary(beforeSummary);
        op.setAfterSummary(afterSummary);
        op.setConfidence(confidence);
        op.setValidationStatus("READY");
        op.setSelected(true);
        op.setEvidence(evidence);
        op.setReason("AI 从正文抽取的线索推进记录。");
        return op;
    }

    private String guessEntityType(String name, List<NovelCharacterEntity> characters, List<NovelLocationEntity> locations) {
        for (NovelCharacterEntity c : characters) {
            if (c.getCharacterName().equals(name)) return "CHARACTER";
        }
        for (NovelLocationEntity l : locations) {
            if (l.getLocationName().equals(name)) return "LOCATION";
        }
        return "CHARACTER";
    }
}
