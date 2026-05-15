package net.lab1024.sa.admin.module.business.novel.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import net.lab1024.sa.admin.module.business.novel.constant.NovelCheatTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

/**
 * 新增小说金手指表单。
 *
 * 金手指会显著影响剧情可行性，所以它会进入图谱并被写作检索优先读取。
 */
@Data
public class NovelCheatAddForm {

    /**
     * 所属小说项目 ID。
     */
    @NotNull(message = "项目 ID 不能为空")
    private Long projectId;

    /**
     * 金手指名称。
     */
    @NotBlank(message = "金手指名称不能为空")
    @Length(max = 100, message = "金手指名称最多 100 个字符")
    private String cheatName;

    @SchemaEnum(value = NovelCheatTypeEnum.class, required = false)
    @CheckEnum(value = NovelCheatTypeEnum.class, message = "金手指类型错误")
    @Length(max = 50, message = "金手指类型最多 50 个字符")
    private String cheatType;

    /**
     * 金手指简介。
     */
    @Size(max = 2000, message = "金手指简介最多 2000 个字符")
    private String summary;

    /**
     * 来源。
     *
     * 比如血脉觉醒、系统绑定、祖传法器。排查剧情合理性时很常看。
     */
    @Length(max = 500, message = "来源最多 500 个字符")
    private String origin;

    /**
     * 使用限制。
     *
     * 例如一天一次、必须付出代价。这个字段能防止模型把能力写成无敌外挂。
     */
    @Length(max = 500, message = "限制最多 500 个字符")
    private String limitation;

    /**
     * 升级路径。
     */
    @Length(max = 500, message = "升级路径最多 500 个字符")
    private String evolution;
}
