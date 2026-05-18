package net.lab1024.sa.admin.module.business.novel.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;

import java.util.Arrays;
import java.util.List;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.business.novel.config.NovelLLMConfig;
import org.springframework.stereotype.Service;

/**
 * 小说LLM调用服务 —— 写作引擎的唯一LLM出入口。
 * <p>
 * 负责封装阻塞式章节生成、流式章节生成和GraphPatch抽取三种调用场景。
 * 用户未配置API Key时返回null，由调用方降级Mock。
 *
 * @Author AI-Novel
 */
@Slf4j
@Service
public class NovelLLMService {

    /**
     * LLM配置工厂，按用户ID创建对应的模型实例。
     */
    @Resource
    private NovelLLMConfig novelLLMConfig;

    /**
     * 阻塞式生成章节正文。
     * <p>
     * 把System Prompt和User Prompt发给DeepSeek，等待完整回复后解析title/summary/content三个片段。
     * 调用方负责处理null返回值（表示用户未配Key，应降级Mock）。
     *
     * @param systemPrompt 系统提示词，包含叙事规则、世界观、文风约束
     * @param userPrompt 用户提示词，包含上下文检索结果和写作指令
     * @param userId 当前用户ID，用于获取该用户的API Key
     * @return 解析后的章节生成结果；用户未配Key时返回null
     */
    public NovelGenerationResult generateChapter(String systemPrompt, String userPrompt, Long userId) {
        ChatLanguageModel model = novelLLMConfig.createChatModel(userId);
        if (model == null) {
            log.info("用户{}未配置CHAT模型Key，跳过LLM调用", userId);
            return null;
        }

        try {
            log.info("开始调用LLM生成章节, userId={}, promptLength={}", userId, (systemPrompt + userPrompt).length());
            long startTime = System.currentTimeMillis();

            Response<AiMessage> response = model.generate(
                    SystemMessage.from(systemPrompt),
                    UserMessage.from(userPrompt)
            );

            long durationMs = System.currentTimeMillis() - startTime;
            String fullText = response.content().text();
            int promptTokens = response.tokenUsage() != null ? response.tokenUsage().inputTokenCount() : 0;
            int completionTokens = response.tokenUsage() != null ? response.tokenUsage().outputTokenCount() : 0;
            log.info("LLM生成完成, userId={}, 字数={}, 耗时={}ms, promptTokens={}, completionTokens={}",
                    userId, fullText.length(), durationMs, promptTokens, completionTokens);

            return parseChapterResponse(fullText, durationMs, promptTokens, completionTokens);
        } catch (Exception e) {
            log.error("LLM生成章节失败, userId={}", userId, e);
            throw new RuntimeException("LLM生成章节失败: " + e.getMessage(), e);
        }
    }

    /**
     * 流式生成章节正文。
     * <p>
     * 通过WebSocket逐token推送到前端，断线后后端继续生成完成。
     * handler负责处理每个token和最终结果。
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @param userId 当前用户ID
     * @param handler 流式响应处理器，由调用方实现onNext/onComplete/onError
     */
    public void generateChapterStream(String systemPrompt, String userPrompt, Long userId,
                                       StreamingResponseHandler<AiMessage> handler) {
        StreamingChatLanguageModel model = novelLLMConfig.createStreamingChatModel(userId);
        if (model == null) {
            log.info("用户{}未配置CHAT模型Key，跳过流式LLM调用", userId);
            return;
        }

        try {
            log.info("开始流式调用LLM生成章节, userId={}", userId);
            List<ChatMessage> messages = Arrays.asList(
                    SystemMessage.from(systemPrompt),
                    UserMessage.from(userPrompt)
            );
            model.generate(messages, handler);
        } catch (Exception e) {
            log.error("流式LLM生成章节失败, userId={}", userId, e);
            handler.onError(e);
        }
    }

    /**
     * 抽取GraphPatch —— 把章节正文发给LLM分析角色/线索/关系变化。
     * <p>
     * 要求LLM按JSON格式返回GraphPatch操作列表，只允许输出38种枚举操作。
     * 内容定义见 NovelGraphPatchService 的抽取Prompt模板。
     *
     * @param chapterContent 章节正文
     * @param extractionPrompt GraphPatch抽取的System+User Prompt
     * @param userId 当前用户ID
     * @return LLM返回的原始JSON文本，由NovelGraphPatchService解析
     */
    public String extractGraphPatch(String chapterContent, String extractionPrompt, Long userId) {
        ChatLanguageModel model = novelLLMConfig.createChatModel(userId);
        if (model == null) {
            log.info("用户{}未配置CHAT模型Key，跳过GraphPatch抽取", userId);
            return null;
        }

        try {
            log.info("开始调用LLM抽取GraphPatch, userId={}, contentLength={}", userId, chapterContent.length());

            Response<AiMessage> response = model.generate(
                    SystemMessage.from(extractionPrompt),
                    UserMessage.from(chapterContent)
            );

            String result = response.content().text();
            log.info("GraphPatch抽取完成, userId={}, resultLength={}", userId, result.length());
            return result;
        } catch (Exception e) {
            log.error("GraphPatch抽取失败, userId={}", userId, e);
            throw new RuntimeException("GraphPatch抽取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析LLM返回的章节文本，提取标题、摘要和正文。
     *
     * @param fullText LLM原始返回文本
     * @param durationMs 生成耗时
     * @param promptTokens Prompt Token数
     * @param completionTokens 生成Token数
     * @return 解析后的章节结果
     */
    private NovelGenerationResult parseChapterResponse(String fullText, long durationMs, int promptTokens, int completionTokens) {
        String title = null;
        String summary = null;
        String content = fullText;

        // 尝试从第一行提取标题：匹配【标题】或## 标题 格式
        String[] lines = fullText.split("\\n", 4);
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            // 匹配 Markdown 标题 ## 或 # 或 【标题】
            if (line.startsWith("## ") || line.startsWith("# ")) {
                title = line.replaceAll("^#{1,2}\\s*", "").trim();
                break;
            }
            if (line.startsWith("【") && line.contains("】")) {
                int start = line.indexOf("【") + 1;
                int end = line.indexOf("】");
                if (end > start) {
                    title = line.substring(start, end).trim();
                    break;
                }
            }
            // 第一行非空且不是标记，当作标题
            if (title == null) {
                title = line.length() > 50 ? line.substring(0, 50) : line;
                break;
            }
        }

        // 摘要从正文开头截取前300字
        if (content != null && !content.isEmpty()) {
            summary = content.length() > 300 ? content.substring(0, 300) : content;
            // 去掉摘要中的标题行
            if (title != null && summary.startsWith(title)) {
                summary = summary.substring(title.length()).trim();
                if (summary.length() > 300) {
                    summary = summary.substring(0, 300);
                }
            }
        }

        return new NovelGenerationResult(title, summary, content, fullText.length(), durationMs, promptTokens, completionTokens);
    }

    /**
     * 章节生成结果 —— 封装LLM返回的标题、摘要、正文和统计信息。
     */
    public static class NovelGenerationResult {
        /** 章节标题，LLM返回第一行 */
        private final String title;
        /** 章节摘要，正文前300字，用于Neo4j Chapter节点 */
        private final String summary;
        /** 章节正文全文，写入MySQL */
        private final String content;
        /** 生成字符数 */
        private final int charCount;
        /** 生成耗时毫秒 */
        private final long durationMs;
        /** Prompt送入Token数 */
        private final int promptTokens;
        /** AI生成Token数 */
        private final int completionTokens;

        public NovelGenerationResult(String title, String summary, String content, int charCount,
                                      long durationMs, int promptTokens, int completionTokens) {
            this.title = title;
            this.summary = summary;
            this.content = content;
            this.charCount = charCount;
            this.durationMs = durationMs;
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
        }

        public String getTitle() { return title; }
        public String getSummary() { return summary; }
        public String getContent() { return content; }
        public int getCharCount() { return charCount; }
        public long getDurationMs() { return durationMs; }
        public int getPromptTokens() { return promptTokens; }
        public int getCompletionTokens() { return completionTokens; }
    }
}
