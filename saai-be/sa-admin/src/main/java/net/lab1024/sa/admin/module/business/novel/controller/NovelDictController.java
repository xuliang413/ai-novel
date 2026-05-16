package net.lab1024.sa.admin.module.business.novel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.admin.module.business.novel.constant.*;
import net.lab1024.sa.admin.module.business.novel.domain.vo.NovelDictItemVO;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.enumeration.BaseEnum;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 小说模块字典接口
 *
 * 为什么需要字典接口：
 * 项目里有很多枚举——项目状态、角色定位、线索类型、图谱节点类型等等。
 * 如果前端把这些枚举值写死在代码里，每次后端新增一个枚举项，前端就要跟着改。现在把所有枚举
 * 统一通过字典接口暴露出去，前端只需要调用对应接口拿到动态列表来渲染下拉框、状态标签即可。
 *
 * 接口设计原则：
 * - 每个枚举类型一个端点，按「资源.子资源」的粒度拆。前端可以按需加载，不用一次性拉全量。
 * - 返回值统一用 ResponseDTO<List<NovelDictItemVO>>，每个字典项包含 code（持久化值）、
 *   name（中文名称）、description（补充说明，帮助用户理解选项含义）。
 * - 枚举值如果有变化（新增/弃用/改名），只需改常量类，接口自动生效，前端不用改。
 */
@RestController
@RequestMapping("/novel/dict")
@Tag(name = "AI 小说 - 字典")
public class NovelDictController {

    // ==================== 项目相关字典 ====================

    /**
     * 项目状态字典
     *
     * 控制项目能否继续写作。ACTIVE=正常写，PAUSED=暂停（还可以恢复），ARCHIVED=归档（不再进入写作流程）。
     * 前端用在下拉框和项目卡片的状态标签上。
     */
    @Operation(summary = "获取项目状态字典")
    @PostMapping("/project-status")
    public ResponseDTO<List<NovelDictItemVO>> projectStatus() {
        return ResponseDTO.ok(toDictItems(NovelProjectStatusEnum.values()));
    }

    /**
     * 小说类型字典
     *
     * 支持：玄幻、武侠、都市、科幻、悬疑、言情等。前端用在新建项目时的分类下拉框。
     */
    @Operation(summary = "获取小说类型字典")
    @PostMapping("/project-genre")
    public ResponseDTO<List<NovelDictItemVO>> projectGenre() {
        return ResponseDTO.ok(toDictItems(NovelProjectGenreEnum.values()));
    }

    // ==================== 章节相关字典 ====================

    /**
     * 章节状态字典
     *
     * 章节在发布前要经过多个步骤——正文审阅、图谱变更确认。这些状态告诉前端当前章节走到了哪一步。
     * DRAFT=草稿可编辑，PENDING_GRAPH_CONFIRM=正文过了等图谱确认，PUBLISHED=正文和图谱都已确认。
     */
    @Operation(summary = "获取章节状态字典")
    @PostMapping("/chapter-status")
    public ResponseDTO<List<NovelDictItemVO>> chapterStatus() {
        return ResponseDTO.ok(toDictItems(NovelChapterStatusEnum.values()));
    }

    // ==================== 写作会话相关字典 ====================

    /**
     * 写作状态字典（新版状态机）
     *
     * IDLE → INTENT_PARSED → CONTEXT_REVIEWING → CONTEXT_CONFIRMED → GENERATING
     * → CONTENT_REVIEWING → PATCH_PENDING → SUCCESS。
     * 详细迁移规则见 NovelChapterWritingStatusEnum。
     */
    @Operation(summary = "获取写作状态字典")
    @PostMapping("/writing-status")
    public ResponseDTO<List<NovelDictItemVO>> writingStatus() {
        return ResponseDTO.ok(toDictItems(NovelChapterWritingStatusEnum.values()));
    }

    /**
     * 模型厂商字典
     *
     * 当前支持的 AI 模型列表。用户需要在个人设置里配置对应模型的 API Key 才能使用。
     * MOCK=本地模拟生成（无需Key），DEEPSEEK=DeepSeek（国产大模型），TONGYI=通义千问（阿里云）。
     * 注意：字典的 code 字段返回的是枚举持久值（TONGYI），不是别名（QWEN）。
     * 前端提交 API Key 时请使用 code 值。
     */
    @Operation(summary = "获取模型厂商字典")
    @PostMapping("/generation-provider")
    public ResponseDTO<List<NovelDictItemVO>> generationProvider() {
        return ResponseDTO.ok(toDictItems(NovelGenerationProviderEnum.values()));
    }

    // ==================== 角色相关字典 ====================

    /**
     * 角色状态字典
     *
     * 区分角色的活跃程度。ALIVE=活跃出场，MISSING=暂时离场但可能回归，DECEASED=已死亡，
     * LEFT=彻底离场不再出现。前端在角色列表的状态筛选和角色卡片上使用。
     */
    @Operation(summary = "获取角色状态字典")
    @PostMapping("/character-status")
    public ResponseDTO<List<NovelDictItemVO>> characterStatus() {
        return ResponseDTO.ok(toDictItems(NovelCharacterStatusEnum.values()));
    }

    /**
     * 角色定位字典
     *
     * 角色的故事功能分类，帮助 AI 在生成时理解每个角色的功能。
     * PROTAGONIST=主角，ANTAGONIST=反派，SUPPORTING=配角，MEMBER=团队/家庭成员，BACKGROUND=背景角色。
     */
    @Operation(summary = "获取角色定位字典")
    @PostMapping("/character-role")
    public ResponseDTO<List<NovelDictItemVO>> characterRole() {
        return ResponseDTO.ok(toDictItems(NovelCharacterRoleEnum.values()));
    }

    // ==================== 线索相关字典 ====================

    /**
     * 线索类型字典
     *
     * 小说里每条线索按功能分为不同类别。MAIN=主线，SUB=支线，FORESHADOW=伏笔，
     * SUSPENSE=悬念，THEME_LINE=主题线。前端在新建线索时选择类型。
     */
    @Operation(summary = "获取线索类型字典")
    @PostMapping("/clue-type")
    public ResponseDTO<List<NovelDictItemVO>> clueType() {
        return ResponseDTO.ok(toDictItems(NovelClueTypeEnum.values()));
    }

    /**
     * 线索状态字典
     *
     * 追踪每条线索的当前推进状态。HIDDEN=尚未埋下，PLANTED=已埋下但未推进，
     * ACTIVATED=正在推进，PARTIALLY_RESOLVED=部分揭示，RESOLVED=完全揭示。
     */
    @Operation(summary = "获取线索状态字典")
    @PostMapping("/clue-status")
    public ResponseDTO<List<NovelDictItemVO>> clueStatus() {
        return ResponseDTO.ok(toDictItems(NovelClueStatusEnum.values()));
    }

    // ==================== 地点相关字典 ====================

    /**
     * 地点类型字典
     *
     * WORLD=世界级设定，REGION=区域，CITY=城市，BUILDING=建筑，ROOM=房间，NATURAL=自然场景。
     * 前端新建地点时选类型。
     */
    @Operation(summary = "获取地点类型字典")
    @PostMapping("/location-type")
    public ResponseDTO<List<NovelDictItemVO>> locationType() {
        return ResponseDTO.ok(toDictItems(NovelLocationTypeEnum.values()));
    }

    // ==================== 物品相关字典 ====================

    /**
     * 物品类型字典
     *
     * WEAPON=武器，ARMOR=防具，CONSUMABLE=消耗品，KEY_ITEM=关键道具，ACCESSORY=配饰，MISC=杂项。
     */
    @Operation(summary = "获取物品类型字典")
    @PostMapping("/item-type")
    public ResponseDTO<List<NovelDictItemVO>> itemType() {
        return ResponseDTO.ok(toDictItems(NovelItemTypeEnum.values()));
    }

    /**
     * 物品状态字典
     *
     * AVAILABLE=可用，BROKEN=损坏，LOST=丢失，DESTROYED=已销毁，SEALED=封印中。
     * 前端在物品详情页的状态标签上使用。
     */
    @Operation(summary = "获取物品状态字典")
    @PostMapping("/item-status")
    public ResponseDTO<List<NovelDictItemVO>> itemStatus() {
        return ResponseDTO.ok(toDictItems(NovelItemStatusEnum.values()));
    }

    // ==================== 金手指相关字典 ====================

    /**
     * 金手指类型字典
     *
     * 网文里常见的金手指分类。SYSTEM=系统面板类，TALENT=天赋异禀，ARTIFACT=宝物类，
     * MEMORY=记忆/重生知识，BLOODLINE=血脉觉醒，MENTOR=导师/老爷爷，TECHNIQUE=功法/秘籍。
     */
    @Operation(summary = "获取金手指类型字典")
    @PostMapping("/cheat-type")
    public ResponseDTO<List<NovelDictItemVO>> cheatType() {
        return ResponseDTO.ok(toDictItems(NovelCheatTypeEnum.values()));
    }

    // ==================== 马甲相关字典 ====================

    /**
     * 马甲类型字典
     *
     * 角色可以有多个身份。ALTER_EGO=化身/小号，DISGUISE=伪装，PAST_IDENTITY=前世身份，
     * SECRET_IDENTITY=隐藏身份。前端在马甲管理和角色关系展示中使用。
     */
    @Operation(summary = "获取马甲类型字典")
    @PostMapping("/alias-type")
    public ResponseDTO<List<NovelDictItemVO>> aliasType() {
        return ResponseDTO.ok(toDictItems(NovelAliasTypeEnum.values()));
    }

    // ==================== 叙事规则相关字典 ====================

    /**
     * 叙事规则类型字典
     *
     * STYLE=风格规则，POV=视角规则，PACING=节奏规则，TONE=基调规则，CONSTRAINT=约束条件。
     * AI 生成时会参考这些规则来控制文风。
     */
    @Operation(summary = "获取叙事规则类型字典")
    @PostMapping("/narrative-rule-type")
    public ResponseDTO<List<NovelDictItemVO>> narrativeRuleType() {
        return ResponseDTO.ok(toDictItems(NovelNarrativeRuleTypeEnum.values()));
    }

    // ==================== 图谱相关字典 ====================

    /**
     * 图谱节点类型字典
     *
     * Neo4j 里使用的 11 类节点标签。前端图谱面板需要知道有哪些节点类型，
     * 才能正确渲染不同形状/颜色的节点。包含：Project、Volume、Chapter、Character、
     * Location、Clue、Item、Event、Cheat、Alias、NarrativeRule。
     */
    @Operation(summary = "获取图谱节点类型字典")
    @PostMapping("/graph-node")
    public ResponseDTO<List<NovelDictItemVO>> graphNode() {
        return ResponseDTO.ok(toDictItems(NovelGraphNodeEnum.values()));
    }

    /**
     * 图谱关系类型字典
     *
     * Neo4j 里使用的所有关系标签。前端图谱面板需要知道有哪些关系类型，
     * 才能渲染不同样式/颜色的边。包含：CONTAINS、PREVIOUS、APPEARS_IN、KNOWS、
     * CURRENTLY_AT、POSSESSES、DRIVES、HAS_CHEAT、HAS_ALIAS 等。
     */
    @Operation(summary = "获取图谱关系类型字典")
    @PostMapping("/graph-relation")
    public ResponseDTO<List<NovelDictItemVO>> graphRelation() {
        return ResponseDTO.ok(toDictItems(NovelGraphRelationEnum.values()));
    }

    /**
     * 图谱变更日志状态字典
     *
     * 每次 GraphPatch 执行后会写入 graph_change_log，状态包括 APPLIED=已执行、
     * UNDONE=已撤销、FAILED=执行失败。前端在变更历史面板中使用。
     */
    @Operation(summary = "获取图谱变更状态字典")
    @PostMapping("/graph-change-status")
    public ResponseDTO<List<NovelDictItemVO>> graphChangeStatus() {
        return ResponseDTO.ok(toDictItems(NovelGraphChangeStatusEnum.values()));
    }

    /**
     * GraphPatch 操作类型字典
     *
     * 所有白名单操作类型的完整列表，每种操作标注了风险等级（LOW/MEDIUM/HIGH）。
     * 前端图谱变更确认面板依据这个字典决定每行操作的默认勾选状态和展示颜色：
     * - LOW：默认勾选，绿色
     * - MEDIUM：默认勾选，黄色
     * - HIGH：默认不勾选，红色
     */
    @Operation(summary = "获取GraphPatch操作类型字典（含风险等级）")
    @PostMapping("/graph-patch-operation-type")
    public ResponseDTO<List<NovelDictItemVO>> graphPatchOperationType() {
        return ResponseDTO.ok(Arrays.stream(NovelGraphPatchOperationTypeEnum.values())
                .map(e -> NovelDictItemVO.builder()
                        .code(e.getValue().toString())
                        .name(e.getDesc())
                        .description("风险等级：" + e.getRiskLevel().getDesc()
                                + "（" + e.getRiskLevel().getValue() + "）")
                        .build())
                .collect(Collectors.toList()));
    }

    // ==================== 通用工具方法 ====================

    /**
     * 将 BaseEnum 数组统一转换为 NovelDictItemVO 列表
     *
     * 每个枚举项的值映射为：
     * - code  ← enum.getValue()   （持久化值，存库 / 接口传参用）
     * - name  ← enum.getDesc()    （中文名称，给用户看）
     * - description ← 暂时留空   （后续可以从枚举类上读补充说明）
     *
     * 为什么不把 description 也塞进枚举类：
     * 枚举类本身是后端业务常量，职责应该单纯——定义值和中文名。更长的解释性文字
     * 适合放在 @Operation 注解和接口注释里。如果以后需要更详细的说明，可以在枚举上
     * 加一个 getDescription() 方法，但当前先保持简洁。
     */
    private List<NovelDictItemVO> toDictItems(BaseEnum[] values) {
        return Arrays.stream(values)
                .map(e -> NovelDictItemVO.builder()
                        .code(String.valueOf(e.getValue()))
                        .name(String.valueOf(e.getDesc()))
                        .description(null)
                        .build())
                .collect(Collectors.toList());
    }
}
