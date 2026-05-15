package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectGenreEnum;
import net.lab1024.sa.admin.module.business.novel.constant.NovelProjectStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 新建小说项目表单。
 */
@Data
public class NovelProjectAddForm {

    /**
     * 项目名称，也就是书名或临时书名。
     */
    @NotBlank(message = "项目名称不能为空")
    @Length(max = 100, message = "项目名称最多 100 个字符")
    private String projectName;

    /**
     * 小说类型，取值来自技术方案中的 Project.genre 枚举。
     */
    @SchemaEnum(value = NovelProjectGenreEnum.class, required = false)
    @CheckEnum(value = NovelProjectGenreEnum.class, message = "小说类型错误")
    @Length(max = 50, message = "小说类型最多 50 个字符")
    private String genre;

    /**
     * 项目简介，用于后续生成上下文。
     */
    @Size(max = 2000, message = "项目简介最多 2000 个字符")
    private String summary;

    /**
     * 主角名称；未填写时 mock 生成会退回使用第一个角色。
     */
    @Length(max = 100, message = "主角名称最多 100 个字符")
    private String protagonist;

    /**
     * 目标字数。
     */
    private Integer targetWords;

    /**
     * 项目状态；未填写时默认为 ACTIVE。
     */
    @SchemaEnum(value = NovelProjectStatusEnum.class, required = false)
    @CheckEnum(value = NovelProjectStatusEnum.class, message = "项目状态错误")
    @Length(max = 30, message = "项目状态最多 30 个字符")
    private String status;
}
