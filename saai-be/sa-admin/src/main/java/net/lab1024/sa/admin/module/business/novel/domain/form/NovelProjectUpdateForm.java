package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectStatusEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectGenreEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 编辑项目表单。
 *
 * 归档操作单独走 /archive 接口，不通过编辑 status 间接处理。
 * 这样做是防止编辑界面不小心把 ACTIVE 改成 ARCHIVED。
 *
 * 但 ACTIVE ↔ PAUSED 的切换是常见需求（作者可能暂停再恢复写作），
 * 所以 status 字段接受 ACTIVE 和 PAUSED，ARCHIVED 被排除在外。
 */
@Data
public class NovelProjectUpdateForm {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    @Length(max = 200, message = "项目名称最多 200 个字符")
    private String projectName;

    @SchemaEnum(value = NovelProjectGenreEnum.class, required = false)
    @CheckEnum(value = NovelProjectGenreEnum.class, message = "小说类型错误")
    @Length(max = 50, message = "小说类型最多 50 个字符")
    private String genre;

    @Length(max = 2000, message = "项目简介最多 2000 个字符")
    private String summary;

    @Length(max = 100, message = "主角名称最多 100 个字符")
    private String protagonist;

    /**
     * 目标字数 —— 仅供进度百分比计算，不强制截断生成
     */
    private Integer targetWords;

    /**
     * 项目状态 —— 允许在 ACTIVE 和 PAUSED 之间切换
     *
     * ARCHIVED 不在此列，归档只能通过 /novel/project/archive 接口。
     */
    @SchemaEnum(value = NovelProjectStatusEnum.class, required = false)
    @CheckEnum(value = NovelProjectStatusEnum.class, message = "项目状态错误")
    @Length(max = 50, message = "项目状态最多 50 个字符")
    private String status;
}
