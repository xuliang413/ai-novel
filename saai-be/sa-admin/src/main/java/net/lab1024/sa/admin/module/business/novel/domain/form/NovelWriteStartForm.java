package net.lab1024.sa.admin.module.business.novel.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开始写作 请求表单 —— 发起一章的AI续写任务
 *
 * @Author AI-Novel
 */
@Data
public class NovelWriteStartForm {

    /**
     * 项目ID, 用于校验用户权限和提取项目配置
     */
    @Schema(description = "项目ID")
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 目标章节号, 可以是已存在章节号(重写)或下一章新号
     */
    @Schema(description = "目标章节号")
    @NotNull(message = "章节号不能为空")
    @Min(value = 1, message = "章节号从1开始")
    private Integer chapterNumber;

    /**
     * POV角色名, 可选, 不填时系统从主角或PROTAGONIST兜底
     */
    @Schema(description = "POV角色名, 可选")
    private String pov;

    /**
     * 用户临时写作方向, 覆盖细纲中的章节意图
     * 如"李四在拍卖会上被揭穿身份, 不得不亮出底牌"
     */
    @Schema(description = "用户临时写作方向, 可选")
    private String chapterGoal;

    /**
     * 是否使用流式模式, true=WebSocket流式推送, false=阻塞等待完整结果
     */
    @Schema(description = "是否流式模式, 默认false阻塞式")
    private Boolean streamMode = false;

    /**
     * 编辑后的正文, 当用户在正文审阅阶段修改后提交时携带
     * 非审阅提交阶段为空
     */
    @Schema(description = "编辑后的正文内容, 审阅时使用")
    private String editedContent;
}
