# AI 小说后端完成度与一口气补齐清单

核对时间：2026-05-15

核对范围：

- 方案文档：[AI小说技术方案.md](../AI小说技术方案.md)
- 后端代码：`saai-be/sa-admin/src/main/java/net/lab1024/sa/admin/module/business/novel`
- 数据库脚本：`saai-be/db/update.sql`、`saai-be/db/neo4j/m0_init.cypher`
- 实际 MySQL：已通过 MCP 查询确认小说相关表已存在

结论先说清楚：当前后端已经有“小说项目 + 资产管理 + 用户 Key + 写作生成 + 内容审阅 + GraphPatch 确认 + Neo4j 白名单写入 + 撤销”的主骨架。它已经不是空壳，但距离方案里的完整“创作外脑系统”还差一截，主要缺在上下文审阅、完整抽取能力、自然语言操作、向量检索、图谱可视化接口、细纲/Wizard、统计仪表盘和稳定性闭环。

下面不按阶段拆，直接按后端能力域列清楚。

## 已经做了

### 1. 多租户与数据隔离

已做：

- Neo4j 节点和关系写入基本都带 `projectId`。
- MySQL 以 `project_id` 作为小说项目隔离字段。
- 用户 API Key 按当前登录用户保存，不是系统全局 Key。
- Neo4j 初始化脚本和初始化器都有 11 类节点的约束。

相关代码：

- `NovelGraphService`
- `NovelNeo4jInitializer`
- `NovelLLMConfig`
- `UserApiKeyService`

还不完整：

- 没有项目级导出、删除、归档时的 Neo4j 全量清理接口。
- 没有检查“所有 Cypher 是否 100% 带 projectId”的自动测试。
- 没有跨项目数据隔离的集成测试。

### 2. MySQL 表结构

已做并通过 MCP 确认存在：

- `t_novel_project`
- `t_novel_volume`
- `t_novel_chapter`
- `t_novel_character`
- `t_novel_location`
- `t_novel_clue`
- `t_novel_item`
- `t_novel_event`
- `t_novel_cheat`
- `t_novel_alias`
- `t_novel_narrative_rule`
- `t_chapter_outline`
- `t_chapter_generation_session`
- `t_graph_change_log`
- `t_writing_log`
- `t_user_api_key`

已覆盖关键字段：

- `t_chapter_generation_session` 已有 `intent_json`、`context_snapshot`、`content_review_json`、`graph_patch_json`、`inverse_patch_json`、`operation_batch_id`。
- `t_graph_change_log` 已有 `patch_json`、`inverse_patch_json`、`operation_batch_id`，支持撤销追踪。

还不完整：

- 方案里的 `writing_calendar` / 写作打卡表未实现。
- 方案中提到章节正文可单独 `chapter_content`，当前实现是放在 `t_novel_chapter.content`，需要明确这是当前设计选择，或者改成正文独立表。
- 图谱变更只有 JSON 日志，没有 operation 明细表，后续做审计、查询、单条重试会不方便。
- 没有模型调用消耗、响应时长、失败类型的细粒度记录。

### 3. Neo4j 节点

方案要求 11 类节点，当前 enum 和约束已覆盖：

- `Project`
- `Volume`
- `Chapter`
- `Character`
- `Location`
- `Clue`
- `Item`
- `Event`
- `Cheat`
- `Alias`
- `NarrativeRule`

已做：

- 项目、角色、地点、线索、卷、章节、物品、事件、金手指、马甲、叙事规则都可以从 MySQL 同步到 Neo4j。
- 节点支持 `archived`、`createdAt`、`updatedAt`、`createdByPatchId`、`updatedByPatchId` 这类系统字段的一部分。

还不完整：

- `Chapter.embedding` 未实现。
- `Chapter.pov` 未完整同步。
- `Character.currentGoal`、`Character.currentEmotion` 当前没有完整写入链路。
- `Clue.revealLevel`、`Clue.subType`、`priority` 等方案字段没有完整落库和同步。
- `Event`、`Cheat`、`Alias` 的部分业务属性已写，但抽取、更新和展示还不完整。
- `WorldRule`、`Scene` 方案里说后续再做，当前未做，这个可以接受，但要在产品范围里明确。

### 4. Neo4j 关系

方案里的核心关系类型，当前 enum 基本覆盖：

- 结构：`CONTAINS`、`PREVIOUS`、`HAS_RULE`
- 出场：`APPEARS_IN`
- 角色关系：`KNOWS`、`LOVES`、`HATES`、`IS_FAMILY_OF`
- 状态关系：`CURRENTLY_AT`、`POSSESSES`、`PARTICIPATES_IN`
- 线索关系：`DRIVES`、`ADVANCES`、`INVOLVES`、`KNOWS_ABOUT`、`INTERSECTS`、`BELONGS_TO`、`TRIGGERS`
- 金手指：`HAS_CHEAT`、`BOUND_TO`
- 马甲：`HAS_ALIAS`、`KNOWS_ALIAS`

当前额外有：

- `OWNS_ASSET`，方案里没有明确列出，需要确认是否保留。

已做：

- 创建资产时会建立 `Project -CONTAINS-> Asset`。
- 章节会建立 `PREVIOUS`。
- 出场会建立 `APPEARS_IN`。
- 线索推进会建立 `Chapter -ADVANCES-> Clue`。
- 白名单里支持角色移动、物品转移、金手指绑定、马甲关系、线索关联、事件触发线索等关系写入。

还不完整：

- `Volume -CONTAINS-> Chapter` 没有完整实现，当前章节更多是 Project 直连或章节链。
- 关系属性没有完全实现，例如 `HAS_CHEAT.acquiredInChapter`、`HAS_ALIAS.createdInChapter`、`KNOWS_ALIAS.sinceChapter`、`INTERSECTS.intersectChapter/intersectDescription`。
- `DRIVES` 关系 enum 有，但当前写作流程未真正产出。
- 关系撤销依赖 inversePatch，但不是所有关系操作都有精准反向语义。

### 5. 后端接口

已做接口：

项目：

- `POST /novel/project/add`
- `POST /novel/project/query`

章节：

- `POST /novel/chapter/query`

资产：

- `POST /novel/character/add`
- `POST /novel/character/query`
- `POST /novel/location/add`
- `POST /novel/location/query`
- `POST /novel/clue/add`
- `POST /novel/clue/query`
- `POST /novel/volume/add`
- `POST /novel/volume/query`
- `POST /novel/item/add`
- `POST /novel/item/query`
- `POST /novel/event/add`
- `POST /novel/event/query`
- `POST /novel/cheat/add`
- `POST /novel/cheat/query`
- `POST /novel/alias/add`
- `POST /novel/alias/query`
- `POST /novel/rule/add`
- `POST /novel/rule/query`

写作：

- `POST /novel/write/start`
- `POST /novel/write/content/pass`
- `POST /novel/write/patch/confirm`
- `POST /novel/write/patch/back`
- `POST /novel/write/recover`
- `POST /novel/write/undo`
- `POST /novel/write/start/stream`

WebSocket：

- `ws://host/ws/novel/write?token=xxx`
- 支持 `start`、`cancel`
- 推送 `token`、`done`、`error`、`cancelled`

用户 Key：

- `POST /novel/user-api-key/save`
- `POST /novel/user-api-key/get`
- `POST /novel/user-api-key/test`

还不完整：

- 资产没有编辑、删除、详情接口。
- 章节没有详情、修改标题/摘要/正文、重新发布、按卷查询接口。
- 没有上下文预览单独接口，例如 `/novel/write/context/preview`、`/context/confirm`、`/context/retrieve-again`。
- `POST /novel/write/start/stream` 只是提示走 WebSocket，不是真流式 HTTP。
- 没有自然语言查询接口。
- 没有自然语言操作接口。
- 没有图谱查询、节点详情、关系网、图谱面板数据接口。
- 没有仪表盘统计接口。
- 没有 Wizard 立项引导接口。
- 没有大纲粘贴解析、卷章批量创建接口。

### 6. 写作闭环

已做：

- `start`：生成章节草稿。
- 自动构建 `ChapterIntent`。
- 构建 `ContextPreview` 并保存到 session。
- 有 Key 时调用 DeepSeek / 通义千问；没 Key 或失败时降级 mock。
- 生成后做轻量正文质检。
- 保存章节草稿。
- 正文审阅通过后生成 GraphPatch。
- GraphPatch 确认后写 Neo4j。
- 写入 `graph_change_log`。
- 写入 `writing_log`。
- 支持返回正文审阅。
- 支持 recover。
- 支持 undo，默认只撤图谱，不删正文。

还不完整：

- 方案里的 `RETRIEVING -> CONTEXT_REVIEW -> GENERATING -> CONTENT_REVIEW -> PATCH_REVIEW -> APPLYING_PATCH -> SUCCESS` 没完全落成状态机。
- 当前没有真正的上下文审阅停顿。
- `GENERATING` 状态没有在数据库里完整记录，流式生成完成后才保存 session。
- 取消生成只是 WebSocket 不再推 token，底层模型调用不一定真正中断。
- 没有重试策略：API 超时 3 次、Rate Limit 等 10 秒重试等未实现。
- 没有 Redis 10 分钟补流缓存。
- 没有“前端刷新后恢复正在流式生成内容”的能力。
- 没有幂等防重执行保护，例如同一个 `operation_batch_id` 重复确认应直接拒绝或返回已执行结果。

### 7. 上下文检索 / RAG

已做：

- 基于 ChapterIntent 从 Neo4j 检索上下文。
- 检索叙事规则。
- 检索上一章摘要、最近章节摘要。
- 检索候选角色状态卡。
- 检索关键人物关系。
- 检索角色关键资产：金手指、马甲、持有物。
- 检索目标线索进度。
- 检索地点卡。
- 检索 POV 当前位置。
- Prompt 拼装时注入叙事规则、角色、关系、资产、线索、地点。
- 有简单 token 估算和硬截断。

还不完整：

- 没有 EmbeddingService。
- 没有章节摘要向量化。
- 没有向量检索。
- 没有 Rerank。
- 没有按优先级的三级截断策略。
- 没有上下文裁剪明细返回给前端。
- 没有用户手动增删上下文卡片后重新检索的接口。
- 没有伏笔写后校验机制。
- 多 POV 支持不完整，现在更像单 POV。

### 8. LLM 生成与抽取

已做：

- DeepSeek / 通义千问普通生成。
- DeepSeek / 通义千问流式生成。
- 用户 Key 从 MySQL 读取并解密。
- 生成结果要求 JSON：`title`、`summary`、`content`。
- 解析失败时用原始文本兜底。
- 写后抽取 GraphPatch：章节摘要、角色/地点出场、线索推进。
- 抽取失败时走规则兜底 GraphPatch。

还不完整：

- 没有真正的 API Key 远程连通性测试。
- 没有模型调用成本统计。
- 没有模型失败分类。
- 没有重试和退避。
- 抽取能力只覆盖少量操作，离方案里 8 组 GraphPatch 操作还差很多。
- LLM 抽取目前没有覆盖物品状态、事件、金手指、马甲、人物关系、角色状态变化等高价值内容。
- 没有自然语言命令解析模型链路。

### 9. GraphPatch 与白名单执行器

已做：

- `NovelGraphPatchModel`
- `NovelGraphPatchOperationModel`
- `checkBlocked`
- `validatePatch`
- `applyGraphPatch`
- `executePatchOperation` 白名单分发
- `buildInversePatch`
- `filterPatch`
- `graph_change_log` 保存正向和反向 Patch

当前支持的操作包括：

- 章节：`UPDATE_CHAPTER_SUMMARY`、`RESTORE_CHAPTER_SUMMARY`
- 出场：`MARK_APPEARANCE`、`UNMARK_APPEARANCE`、`MARK_LOCATION_APPEARANCE`、`MARK_ITEM_APPEARANCE`、`MARK_EVENT_OCCURRED`、`MARK_ALIAS_APPEARANCE`
- 创建：`CREATE_*`
- 归档：`ARCHIVE_NODE`
- 更新：`UPDATE_CHARACTER_STATE`、`MARK_CHARACTER_STATUS`、`UPDATE_LOCATION`、`UPDATE_ITEM_STATUS`、`UPDATE_EVENT`、`UPDATE_CHEAT`、`UPDATE_ALIAS`、`UPDATE_VOLUME`、`UPDATE_CLUE`、`RESOLVE_CLUE`、`ATTACH_RULE`
- 关系：`UPDATE_CHARACTER_RELATION`、`LINK_EVENT_PARTICIPANT`、`LINK_CLUE_CHARACTER`、`MARK_CHARACTER_KNOWS_CLUE`、`INTERSECT_CLUES`、`TRIGGER_CLUE`、`ASSIGN_CLUE_TO_VOLUME`
- 状态关系：`MOVE_CHARACTER`、`TRANSFER_ITEM`
- 金手指/马甲：`ASSIGN_CHEAT_TO_CHARACTER`、`BIND_CHEAT_TO_ITEM`、`ASSIGN_ALIAS_TO_CHARACTER`、`REVEAL_ALIAS_TO_CHARACTER`
- 线索：`ADVANCE_CLUE`、`RESTORE_CLUE`

还不完整：

- 操作枚举没有独立定义，现在 operationType 是字符串。
- 白名单支持的操作多，但 LLM 抽取不会产出大多数操作。
- 反向 Patch 对复杂关系不够精准，有些只能“反向同操作”或归档节点。
- `validatePatch` 当前主要校验章节摘要和线索 before 值，其他类型冲突校验不足。
- 没有 no-op 判断和重复关系提示。
- 没有 GraphPatch 预览统计接口，例如新增节点几条、关系几条、风险几条。
- 没有高风险操作默认不勾选的统一策略表。

### 10. WebSocket

已做：

- Token 鉴权。
- `start` 启动流式写作。
- `cancel` 标记取消。
- token / done / error 推送。
- WebSocket 线程里手动注入当前用户上下文。

还不完整：

- `cancel` 只阻止继续推送，不一定真正取消 LLM 请求。
- 没有 Redis 缓存已生成 token。
- 没有断线重连补流。
- 没有同一用户多连接协调。
- 没有把生成中状态实时写入 `chapter_generation_session`。

### 11. 管理与仪表盘

已做：

- 项目列表。
- 章节列表。
- 资产分页列表。
- 写作日志表已建，确认发布时会插入基础日志。

还不完整：

- 项目总览统计接口未做。
- 节点统计接口未做。
- 卷章进度接口未做。
- 待处理事项接口未做。
- 角色详情、关系网、出场记录接口未做。
- 线索进度面板接口未做。
- 写作日历、连续写作天数、月统计未做。
- API Key 月用量统计未做。

## 当前最大风险

1. 写作主链路能跑，但“上下文审阅”缺失，方案里的安全闭环少了第一道人工确认。
2. GraphPatch 白名单已经扩了，但 LLM 抽取能力没有跟上，大部分白名单操作只能靠手动构造或未来自然语言操作触发。
3. Neo4j 和 MySQL 没有跨库事务，这是方案允许的，但当前幂等、重试、失败恢复还不够。
4. WebSocket 取消和恢复不够扎实，真实长文本生成时容易出现“前端停了，后端还在跑”。
5. 向量检索、Embedding、Rerank 没做，目前 RAG 只有图查询和硬截断，和方案里的“图查询为主、向量为辅”还差一半。
6. 资产只有新增和查询，没有编辑、删除、详情，后台管理闭环不完整。
7. 自然语言查询 / 操作还没做，方案里的对话式操作能力还没落地。

## 下一步一口气补齐清单

现在先不急着做前端。按当前后端进度，最该先赶的是“前端以后不容易返工”的后端契约：接口地址、请求/响应字段、状态机、枚举、WebSocket 事件名、GraphPatch 数据结构。算法可以先简单，但壳子要稳定。这样前端一旦开工，页面、表单、流程和联调代码就不用因为后端字段反复改。

下面不是分阶段，而是一口气补齐时的优先顺序。越靠前，越能减少前端后面大改。

### A. 先冻结前端契约

这块优先级最高。前端最怕的不是后端功能暂时简单，而是接口字段今天叫 `status`，明天叫 `state`，流程今天三个状态，明天八个状态。

- [ ] 统一接口路由命名：项目、卷、章节、资产、写作会话、上下文、GraphPatch、图谱查询、统计面板。
- [ ] 统一请求 DTO 和响应 VO，前端不要直接依赖实体字段。
- [ ] 统一分页结构、详情结构、保存结构、归档结构。
- [ ] 统一错误码：参数错、权限错、会话不存在、状态不允许、LLM 失败、Neo4j 失败、Patch 校验失败。
- [ ] 统一枚举/字典接口，前端表单下拉、状态颜色、按钮可用性不要写死。
- [ ] 统一时间、状态、ID 字段命名，例如 `projectId`、`chapterId`、`sessionId`、`operationBatchId`。
- [ ] 统一“可编辑、可归档、可回退、可确认”的布尔字段，让前端少写业务猜测。

建议新增或补齐：

- `NovelDictController`
- `NovelWorkbenchProjectVO`
- `NovelWorkbenchChapterVO`
- `NovelAssetDetailVO`
- `NovelOperationResultVO`
- `NovelErrorCodeEnum`

### B. 补齐资产管理接口闭环

现在资产新增和列表已经有基础了，但前端做工作台至少还需要详情、编辑、归档，否则页面会先写死，后面再改成本很高。

- [ ] 项目详情接口：项目、卷、章节、资产数量、最近会话、最近写作日志。
- [ ] 卷详情、编辑、归档。
- [ ] 章节详情、编辑元信息、改标题、改摘要、改正文、归档。
- [ ] 角色详情、编辑、归档。
- [ ] 地点详情、编辑、归档。
- [ ] 线索详情、编辑、归档。
- [ ] 道具详情、编辑、归档。
- [ ] 事件详情、编辑、归档。
- [ ] 金手指详情、编辑、归档。
- [ ] 别名详情、编辑、归档。
- [ ] 叙事规则详情、编辑、启用/停用。

每个详情 VO 不只返回表字段，还要带前端常用的周边信息：

- 角色：出现章节、关系、当前位置、持有物品、已知线索。
- 地点：出现章节、当前在场人物、相关事件。
- 线索：进度、触发事件、相关人物、交叉线索。
- 道具：持有者、状态、出现章节、绑定金手指。
- 事件：参与者、地点、影响线索、影响章节。

### C. 把写作状态机固定下来

写作工作台的页面流转会直接跟状态机绑定。这里不固定，前端后面一定大改。

- [ ] 固定 `INTENT_PARSED`：用户输入已经解析，知道这次要写什么。
- [ ] 固定 `CONTEXT_REVIEWING`：上下文已经准备好，等用户审阅。
- [ ] 固定 `CONTEXT_CONFIRMED`：用户确认了上下文。
- [ ] 固定 `GENERATING`：正文生成中。
- [ ] 固定 `CONTENT_REVIEWING`：正文生成完，等用户确认。
- [ ] 固定 `PATCH_PENDING`：图谱变更已抽取，等用户确认。
- [ ] 固定 `PATCH_APPLIED`：图谱变更已写入。
- [ ] 固定 `RECOVERED`：本次图谱变更已回退。
- [ ] 固定 `CANCELED`：用户取消生成。
- [ ] 固定 `FAILED`：流程失败。

需要补接口：

- [ ] 创建写作会话。
- [ ] 解析意图。
- [ ] 获取上下文预览。
- [ ] 确认上下文。
- [ ] 开始生成。
- [ ] 取消生成。
- [ ] 获取正文结果。
- [ ] 确认正文。
- [ ] 获取 GraphPatch。
- [ ] 确认 GraphPatch。
- [ ] 回退 GraphPatch。
- [ ] 恢复 GraphPatch。
- [ ] 获取会话详情。

关键要求：

- [ ] 流式生成前必须先创建 session，不能等生成完才有记录。
- [ ] 每个接口都要校验当前状态能不能执行这个动作。
- [ ] 失败、取消、重试都要落库。
- [ ] 同一章节同一时间只允许一个活跃写作会话，避免前端开两个窗口把数据冲乱。

### D. 先做上下文审阅契约，算法可以后补

完整 RAG、向量召回、Rerank 可以晚一点，但上下文审阅接口必须先有。前端页面会围绕“生成前看上下文”来设计。

- [ ] 上下文预览接口。
- [ ] 上下文确认接口。
- [ ] 上下文排除/保留接口，用户可以把不想用的材料去掉。
- [ ] 上下文重新检索接口。
- [ ] 上下文低置信度提示字段。
- [ ] 上下文来源证据字段。
- [ ] 上下文 token 预算字段。

`ChapterContextPreviewVO` 建议固定这些块：

- `intent`：本次写作目标。
- `previousChapters`：前文摘要。
- `characters`：本章可能用到的人物。
- `locations`：本章可能用到的地点。
- `clues`：相关线索。
- `items`：相关道具。
- `rules`：叙事规则。
- `warnings`：低置信度或冲突提示。
- `selectedContextIds`：用户确认要带入生成的上下文。

后面就算从简单规则检索升级到向量检索，前端也不用换结构。

### E. 把 GraphPatch 契约固定并补风险信息

GraphPatch 是后面排查 bug 的核心，也是前端确认图谱变更的核心。现在能执行不少操作，但前端需要稳定、可读、可筛选的数据结构。

- [ ] 新增 `NovelGraphPatchOperationTypeEnum`，不要再散落字符串。
- [ ] 新增 GraphPatch 风险配置，低风险默认勾选，高风险默认不勾选。
- [ ] 统一 `GraphPatchPreviewVO`，前端只看这个结构，不直接吃内部 patch model。
- [ ] 每条操作固定 `operationType`、`operationName`、`riskLevel`。
- [ ] 每条操作固定 `targetType`、`targetId`、`targetName`。
- [ ] 每条操作固定 `beforeSnapshot`、`afterSnapshot`。
- [ ] 每条操作固定 `evidence`、`reason`、`confidence`。
- [ ] 每条操作固定 `selected`、`validationStatus`、`validationMessage`、`blockedReason`。
- [ ] 补 GraphPatch 汇总统计：总数、高风险数量、被阻断数量、需要人工确认数量、影响资产数量。
- [ ] 增强 `validatePatch`：角色状态、物品状态、位置、持有人、马甲暴露、关系 before 值都要能查冲突。

这样前端能先把“图谱变更确认面板”做出来，后端抽取能力后面增强也不会改变页面结构。

### F. 把 Neo4j 和 MySQL 同步做严实

这块不是前端直接看到的，但如果不稳，前端会经常遇到“页面显示和图谱不一致”。

- [ ] patch operation batch id 全链路。
- [ ] 每条操作的幂等键。
- [ ] MySQL 变更日志和 Neo4j 写入结果一致性校验。
- [ ] Neo4j 失败后的补偿策略。
- [ ] 同一个章节重复确认时不能重复创建关系。
- [ ] 归档节点后的查询过滤。
- [ ] 图谱健康检查接口。
- [ ] 图谱重建接口：从 MySQL 重放生成 Neo4j。
- [ ] 补 `Volume -CONTAINS-> Chapter`。
- [ ] 补关系属性：`acquiredInChapter`、`createdInChapter`、`sinceChapter`、`intersectChapter`、`intersectDescription`、`level`。

### G. 稳定 WebSocket 协议

WebSocket 事件名一旦前端用了，就不要轻易改。现在 token 推送可用，但写作工作台需要完整事件协议。

- [ ] 固定 `sessionCreated`。
- [ ] 固定 `intentParsed`。
- [ ] 固定 `contextReady`。
- [ ] 固定 `contextConfirmed`。
- [ ] 固定 `generationStarted`。
- [ ] 固定 `token`。
- [ ] 固定 `contentReady`。
- [ ] 固定 `patchReady`。
- [ ] 固定 `patchApplied`。
- [ ] 固定 `recoverDone`。
- [ ] 固定 `canceled`。
- [ ] 固定 `failed`。
- [ ] 固定 `error`。

每个事件都要固定字段：

- `eventType`
- `sessionId`
- `chapterId`
- `requestId`
- `timestamp`
- `payload`
- `message`

还要补：

- [ ] 心跳。
- [ ] 断线重连后按 sessionId 恢复状态。
- [ ] 取消生成时落库。
- [ ] 取消生成时尽量中断底层 LLM 调用。
- [ ] 服务端错误也要推明确事件，不能只断开连接。

### H. 先给图谱和仪表盘稳定查询结构

前端会需要图谱视图和概览面板。这里可以先返回基础数据，但结构要先定。

- [ ] 项目资产统计。
- [ ] 最近写作会话。
- [ ] 最近失败记录。
- [ ] 最近 GraphPatch 记录。
- [ ] 章节写作进度。
- [ ] 角色关系图查询。
- [ ] 线索推进图查询。
- [ ] 地点人物分布查询。
- [ ] 道具流转查询。

图谱查询 VO 建议统一：

- `nodes`
- `edges`
- `groups`
- `legends`
- `filters`
- `warnings`

这样前端图谱组件可以先做，不必等所有智能分析能力完整。

### I. 智能能力增强放后面，但接口先预留

这些能力很重要，但不适合压在前端开工前全部做完。它们可以藏在已经固定的接口后面，后端慢慢升级。

- [ ] 章节向量化。
- [ ] 世界设定向量化。
- [ ] 人物卡向量化。
- [ ] 线索摘要向量化。
- [ ] 查询向量召回。
- [ ] Neo4j 子图召回。
- [ ] 多路召回合并。
- [ ] Rerank。
- [ ] LLM 结构化抽取 GraphPatch。
- [ ] 自然语言查询。
- [ ] 自然语言操作。
- [ ] Wizard 新手引导。
- [ ] 从一句话设定生成初始世界观。
- [ ] 从世界观生成角色、冲突、卷、章节大纲。

重点是：这些后续能力不要改变前端契约，只是让同一个接口返回更聪明的结果。

### J. 补契约测试和排错护栏

为了后面你能审阅、排查 bug，测试重点也要围绕“流程和契约”。

- [ ] 接口响应结构测试。
- [ ] 枚举/字典接口测试。
- [ ] 写作状态机流转测试。
- [ ] 非法状态操作拦截测试。
- [ ] GraphPatch 白名单测试。
- [ ] GraphPatch 风险等级测试。
- [ ] GraphPatch 幂等测试。
- [ ] 回退恢复测试。
- [ ] 资产详情/编辑/归档测试。
- [ ] API Key 加密保存测试。
- [ ] WebSocket 事件顺序测试。
- [ ] Neo4j 重建测试。

排错字段也要统一：

- `sessionId`
- `requestId`
- `operationBatchId`
- `chapterId`
- `projectId`
- `errorCode`
- `errorMessage`

## 最建议先落手的“一口气开发入口”

如果目标是“先赶后端，避免前端后面大改”，最建议先补这条链路：

1. 枚举/字典接口，让前端表单和状态展示不用写死。
2. 项目工作台详情接口，把项目、卷、章节、资产统计、最近会话一次给出来。
3. 所有资产的详情、编辑、归档接口，先保证管理页面闭环。
4. 写作会话状态机接口，固定从上下文审阅到正文确认再到 GraphPatch 确认的完整流程。
5. `ChapterContextPreviewVO`，先把上下文审阅结构定下来。
6. `GraphPatchPreviewVO`，把风险、证据、原因、校验结果、勾选状态定下来。
7. WebSocket 事件协议，固定事件名和通用字段。
8. 图谱查询基础接口，先让前端能画节点和边。
9. 写作日志/会话详情接口，让排错有地方看。
10. 契约测试，防止后端一改字段前端就崩。

这条链路补完后，前端就可以放心开工：

- 左边做项目、章节、资产导航。
- 中间做写作和流式生成。
- 右边做上下文审阅和图谱变更确认。
- 顶部做状态、错误、重试、取消。
- 图谱和仪表盘先接基础数据，后面后端变聪明，前端不用换骨架。
