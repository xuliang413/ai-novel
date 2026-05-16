package net.lab1024.sa.admin.module.business.novel.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.code.ErrorCode;

/**
 * 小说模块错误码枚举
 *
 * 为什么需要统一的错误码：
 * 一个 API 调下去，不一定每次都成功。可能是参数写错了、权限不够、LLM 挂了、Neo4j
 * 连不上、Patch 校验冲突等等。如果每种错误都返回"操作失败"四个字，前端和排错的人
 * 只能靠猜。现在把所有小说模块可能遇到的错误集中定义，每种错误一个唯一码，前端可以
 * 针对不同错误码给出不同的提示（比如 LLM 失败提示用户检查 Key 余额，Neo4j 失败提示
 * 系统维护中等），后端排错也能通过日志里的错误码快速定位。
 *
 * 错误码范围：40000-40999（业务模块 - 小说）
 * 规则：每个错误码的 msg 既要技术人员能看懂，也要能让最终用户大致理解问题在哪。
 */
@Getter
@AllArgsConstructor
public enum NovelErrorCodeEnum implements ErrorCode {

    // ==================== 通用 ====================

    /**
     * 请求参数不符合接口约定。
     * 场景举例：必填字段没填、枚举值传了不在范围内的值、分页参数非法。
     */
    PARAM_ERROR(40001, "参数错误，请检查输入"),

    /**
     * 数据不存在，可能是 id 写错了或者数据已被删除/归档。
     */
    DATA_NOT_EXIST(40002, "数据不存在或已被删除"),

    /**
     * 没有权限操作该资源。小说模块下，通常意味着跨项目操作。
     * 场景举例：用户 A 想编辑用户 B 的项目下的角色。
     */
    NO_PERMISSION(40003, "没有权限操作该资源"),

    /**
     * 当前操作不被允许，比如在草稿状态下执行确认图谱变更。
     * 前端应该根据章节/会话的当前状态判断按钮可用性，这个错误码是最后的兜底拦截。
     */
    STATUS_NOT_ALLOWED(40004, "当前状态不允许执行该操作"),

    // ==================== 项目 ====================

    /**
     * 小说项目不存在。
     */
    PROJECT_NOT_FOUND(40101, "小说项目不存在"),

    /**
     * 项目已被归档，无法继续写作。
     */
    PROJECT_ARCHIVED(40102, "项目已归档，无法继续写作"),

    // ==================== 章节 / 卷 ====================

    /**
     * 章节不存在。
     */
    CHAPTER_NOT_FOUND(40201, "章节不存在"),

    /**
     * 卷不存在。
     */
    VOLUME_NOT_FOUND(40202, "卷不存在"),

    /**
     * 同一章节同一时间只允许一个活跃写作会话。
     * 场景举例：用户可能开了两个窗口写同一章，第二个窗口会收到这个错误。
     */
    CHAPTER_HAS_ACTIVE_SESSION(40203, "该章节已有活跃的写作会话，请先完成或取消当前会话"),

    // ==================== 写作会话 ====================

    /**
     * 写作会话不存在。
     */
    SESSION_NOT_FOUND(40301, "写作会话不存在"),

    /**
     * 操作批次 ID 重复，同一批变更不允许重复执行。
     * 这是幂等保护：防止网络重试导致同一批 GraphPatch 被写两遍。
     */
    OPERATION_BATCH_DUPLICATE(40302, "该操作批次已执行，请勿重复提交"),

    // ==================== LLM 相关 ====================

    /**
     * 未配置 API Key，无法调用大模型。
     * 提示用户去设置页面配置 DeepSeek 或通义千问的 Key。
     */
    LLM_NO_API_KEY(40401, "未配置 API Key，请先在设置中配置大模型 Key"),

    /**
     * API Key 无效或已过期。
     */
    LLM_INVALID_API_KEY(40402, "API Key 无效或已过期，请重新配置"),

    /**
     * LLM 调用失败，可能是网络问题、模型超时、余额不足等。
     * 具体原因在 msg 的补充说明里。
     */
    LLM_REQUEST_FAILED(40403, "大模型调用失败"),

    /**
     * LLM 返回结果无法解析为 JSON。
     * 这通常是模型输出了非预期格式，系统会尝试兜底或让用户重试。
     */
    LLM_PARSE_FAILED(40404, "大模型返回格式异常，请重试"),

    /**
     * 用户取消了生成。
     */
    GENERATION_CANCELED(40405, "生成已取消"),

    // ==================== Neo4j 图谱相关 ====================

    /**
     * Neo4j 操作失败。
     */
    NEO4J_OPERATION_FAILED(40501, "图谱操作失败，请稍后重试"),

    /**
     * GraphPatch 校验失败，存在冲突或不允许的变更。
     * 具体哪些操作被阻断会在返回数据里列出详细信息。
     */
    PATCH_VALIDATION_FAILED(40502, "图谱变更校验不通过"),

    /**
     * 图谱变更撤销失败。
     */
    UNDO_FAILED(40503, "撤销图谱变更失败"),

    /**
     * 图谱恢复失败。
     */
    RECOVER_FAILED(40504, "恢复图谱变更失败"),

    // ==================== 资产相关 ====================

    /**
     * 角色不存在。
     */
    CHARACTER_NOT_FOUND(40601, "角色不存在"),

    /**
     * 地点不存在。
     */
    LOCATION_NOT_FOUND(40602, "地点不存在"),

    /**
     * 线索不存在。
     */
    CLUE_NOT_FOUND(40603, "线索不存在"),

    /**
     * 物品不存在。
     */
    ITEM_NOT_FOUND(40604, "物品不存在"),

    /**
     * 事件不存在。
     */
    EVENT_NOT_FOUND(40605, "事件不存在"),

    /**
     * 金手指不存在。
     */
    CHEAT_NOT_FOUND(40606, "金手指不存在"),

    /**
     * 马甲不存在。
     */
    ALIAS_NOT_FOUND(40607, "马甲不存在"),

    /**
     * 叙事规则不存在。
     */
    NARRATIVE_RULE_NOT_FOUND(40608, "叙事规则不存在");

    private final int code;
    private final String msg;
    private final String level;

    NovelErrorCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.level = LEVEL_USER;
    }
}
