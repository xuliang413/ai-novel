package net.lab1024.sa.admin.module.business.novel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典项 VO —— 前端下拉框 / 状态标签的数据来源
 *
 * 为什么要有这个 VO：
 * 后端有很多枚举（项目状态、角色定位、线索类型等），前端以前只能写死映射，
 * 一旦后端加一个枚举值，前端也要改代码。有了字典接口后，前端只需要调用
 * /novel/dict/list 就能拿到所有枚举的动态列表，不用再耦合后端定义。
 *
 * 字段设计：
 * - code：枚举持久化值，前后端通信用，不要在前端直接展示给用户
 * - name：中文名称，给用户看（比如"写作中"）
 * - description：对此枚举值的说明，帮助用户在表单里理解每个选项是什么意思
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelDictItemVO {

    /**
     * 枚举持久化值 —— 存库 / 接口传参时使用
     *
     * 举例：DRAFT, PUBLISHED, ACTIVE
     */
    @Schema(description = "枚举持久化值，存库 / 接口传参时使用", example = "ACTIVE")
    private String code;

    /**
     * 中文名称 —— 前端下拉框和状态标签展示
     *
     * 举例："写作中"、"草稿"、"已发布"
     */
    @Schema(description = "中文名称，给用户看", example = "写作中")
    private String name;

    /**
     * 补充说明 —— 帮助最终用户理解每个选项的含义
     *
     * 可以比 name 更详细。比如角色状态"主要角色"的 description 可以写"频繁出场、有完整弧光的角色"。
     * 如果枚举值本身已经很直白，可以为空或与 name 相同。
     */
    @Schema(description = "补充说明，帮助用户理解该选项的含义", example = "正在持续创作中的项目")
    private String description;
}
