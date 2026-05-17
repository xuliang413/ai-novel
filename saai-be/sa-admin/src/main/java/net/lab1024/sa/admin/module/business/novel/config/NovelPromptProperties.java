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
     */
    private String systemTemplatePrefix = "你是一位专业网文作者，请遵循以下规则：";

    /**
     * User Prompt 开头模板，支持 {chapterNumber}、{pov}、{targetChapterWords} 占位符。
     */
    private String userTemplatePrefix = "请续写第{chapterNumber}章，POV：{pov}，目标字数：{targetChapterWords}。";

    /**
     * 叙事规则分节标题。
     */
    private String narrativeRuleSectionTitle = "叙事规则";

    /**
     * 项目约束分节标题。
     */
    private String projectConstraintSectionTitle = "项目约束";

    /**
     * 写作上下文分节标题。
     */
    private String contextSectionTitle = "写作上下文";

    /**
     * 写作指令分节标题。
     */
    private String instructionSectionTitle = "写作指令";

    /**
     * 检索上下文被压缩或丢弃时追加到 User Prompt 的提示语。
     */
    private String truncationNotice = "注意：部分上下文已被压缩或丢弃，请优先遵守已保留的高优先级信息。";
}
