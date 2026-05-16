package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCheatTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 编辑金手指表单。
 */
@Data
public class NovelCheatUpdateForm {

    @NotNull(message = "金手指ID不能为空")
    private Long cheatId;

    @Length(max = 100, message = "金手指名称最多 100 个字符")
    private String cheatName;

    @SchemaEnum(value = NovelCheatTypeEnum.class, required = false)
    @CheckEnum(value = NovelCheatTypeEnum.class, message = "金手指类型错误")
    @Length(max = 50, message = "金手指类型最多 50 个字符")
    private String cheatType;

    @Length(max = 2000, message = "金手指简介最多 2000 个字符")
    private String summary;

    @Length(max = 2000, message = "来源最多 2000 个字符")
    private String origin;

    @Length(max = 2000, message = "使用限制最多 2000 个字符")
    private String limitation;

    @Length(max = 2000, message = "升级路径最多 2000 个字符")
    private String evolution;
}
