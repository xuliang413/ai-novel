package net.lab1024.sa.admin.module.business.novel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 小说提示词模板配置。
 * <p>
 * 只保存 Prompt 组装时需要的模板片段和分节标题，方便后续在 application.yaml 中调整语气和结构，而不用修改服务代码。
 *
 * @Author AI-Novel
 */
@Data
@Component
@ConfigurationProperties(prefix = "novel.prompt")
public class NovelPromptProperties {

    /**
     * System Prompt 开头模板，放角色定位和总体写作要求。
     * 默认: "你是一位专业网文作家，请严格遵循以下规则进行续写："
     */
    private String systemTemplatePrefix = "你是一位专业网文作家，请严格遵循以下规则进行续写：";

    /**
     * System Prompt 角色描述段落，定义AI写作人设和行为准则。
     * 默认: 经验丰富的网文作家，必须严格遵循设定。
     */
    private String systemRoleDescription = "你是一位经验丰富的网络小说作家，擅长根据已有剧情和角色设定进行精准续写。你必须严格遵循提供的世界观、角色设定和叙事规则，确保故事前后连贯、角色行为一致。";

    /**
     * System Prompt 输出格式要求，约束AI的章节标题格式和正文排版。
     * 默认: 第一行为Markdown标题，后续为分段正文。
     */
    private String systemOutputFormat = "输出格式要求：第一行为章节标题（## 第X章 标题），之后为正文内容。正文要求分段清晰，对话使用引号，适当使用环境描写烘托氛围。";

    /**
     * User Prompt 开头模板，支持 {chapterNumber}、{pov}、{targetChapterWords} 占位符。
     * 运行时由 NovelPromptService.replaceUserTemplate() 替换为实际值。
     */
    private String userTemplatePrefix = "请续写以下小说的第{chapterNumber}章，POV视角：{pov}，目标字数约{targetChapterWords}字。";

    /**
     * User Prompt 写作技巧要求，注入每章生成指令末尾，约束AI的写作质量。
     * 默认: 字数控制±20%、人物一致性、线索自然推进、悬念钩子、对话真实。
     */
    private String userWritingTips = "写作要求：1)严格控制字数在目标字数±20%以内；2)保持人物性格一致性；3)线索推进自然不突兀；4)章节结尾留有悬念或钩子；5)对话真实自然，符合角色身份。";

    /**
     * 叙事规则分节标题。
     */
    private String narrativeRuleSectionTitle = "【叙事规则】";

    /**
     * 项目约束分节标题。
     */
    private String projectConstraintSectionTitle = "【项目约束】";

    /**
     * 写作上下文分节标题。
     */
    private String contextSectionTitle = "【写作上下文】";

    /**
     * 写作指令分节标题。
     */
    private String instructionSectionTitle = "【写作指令】";

    /**
     * 检索上下文被压缩或丢弃时追加到 User Prompt 的提示语。
     */
    private String truncationNotice = "注意：部分上下文因Token限制已被压缩，请优先遵守已保留的高优先级信息。如有疑问请按既有剧情逻辑推理。";
}
