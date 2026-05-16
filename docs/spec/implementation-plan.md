# Implementation Plan: AI小说第一阶段后端

## Overview

基于《AI小说第一阶段技术方案》，从零构建 Novel 后端模块：MySQL 16 张表 + Neo4j 11 节点 29 关系 + 项目管理/资产 CRUD + AI 写作闭环 + GraphPatch 白名单系统 + 自动化测试。

## Architecture Decisions

- **从零创建 Novel 模块**：所有 Entity/DAO/Service/Controller/Enum/Config 全部新建，不依赖旧代码
- **保留 SmartAdmin 框架壳**：Sa-Token 鉴权、MyBatis-Plus ORM、`sa-base` 模块不动
- **不动 SmartAdmin 自带功能**：登录/菜单/路由/权限/用户管理均不改
- Neo4j 测试用 mock `org.neo4j.driver.Driver`，不用 Testcontainers
- 提示词模板放 `application.yaml` 管理，不硬编码在 Service 里
- 所用内容必须符合《AI小说第一阶段技术方案》一~五章

---

## 依赖图

```
Phase 1: Foundation
  MySQL DDL + Neo4j 约束 ──→ 枚举 ──→ Entity + DAO
                                          │
Phase 2: 基础设施                            │
  LLM 配置（写作+Embedding）  ←──────────────┘
  GraphService（5种Cypher模板+11种节点merge）
         │
Phase 3: 业务CRUD（Task 6~9 可并行）
  项目管理 ──→ 资产管理(角色/地点/线索) ──→ 资产管理(物品/事件/金手指/马甲/规则/卷)
                                      ──→ 角色关系CRUD
                                      ──→ 章节+细纲
         │
Phase 4: 写作引擎
  上下文检索 ──→ 提示词组装 ──→ LLM Service ──→ 写作Service（状态机+流程编排）
                                                     ──→ 流式写作+WebSocket
         │
Phase 5: GraphPatch系统
  GraphPatch模型+映射 ──→ GraphPatch LLM抽取 ──→ 审阅API ──→ 写入+发布+向量化
         │
Phase 6: 辅助
  写后校验  撤销  图谱查询+仪表盘+日志
```

---

## Task List

### Phase 1: Foundation（数据层）

#### Task 1: MySQL DDL + Neo4j 约束脚本

**Description:** 编写完整 `update.sql`（16 张业务表 `CREATE TABLE IF NOT EXISTS`）和 `m0_init.cypher`（11 个 CONSTRAINT）。覆盖方案第四章所有实体属性。DDL 可重复执行不报错。

**Acceptance criteria:**
- [ ] 16 张业务表 DDL 覆盖方案第四章所有实体属性
- [ ] Neo4j 11 个 CONSTRAINT 覆盖 Project/Volume/Chapter/Character/Location/Clue/Item/Event/Cheat/Alias/NarrativeRule
- [ ] `CREATE TABLE IF NOT EXISTS` 可重复执行

**Verification:** MySQL 执行 DDL 无报错；Neo4j 执行 Cypher 无报错

**Dependencies:** None

**Files to create/modify:** `db/update.sql`, `db/neo4j/m0_init.cypher`

**Scope:** S

---

#### Task 2: 枚举类创建

**Description:** 新建所有 Novel 枚举类（SmartAdmin 要求实现 `BaseEnum` 接口）。

| 枚举类 | 关键值 |
|--------|--------|
| `NovelProjectGenreEnum` | XIANXIA/XUANHUAN/URBAN/HISTORY/SCIFI/MYSTERY/WUXIA/FANTASY |
| `NovelProjectStatusEnum` | ACTIVE/PAUSED/ARCHIVED |
| `NovelCharacterRoleEnum` | PROTAGONIST/ANTAGONIST/SUPPORTING/MINOR |
| `NovelCharacterStatusEnum` | ACTIVE/INACTIVE/DEAD/MISSING/UNKNOWN |
| `NovelEmotionEnum` | ANGER/FEAR/DETERMINED/DESPAIR/JOY/SADNESS/CALM/SUSPICIOUS/... |
| `NovelGoalStatusEnum` | IN_PROGRESS/ACHIEVED/ABANDONED/DIVERTED |
| `NovelClueTypeEnum` | MAIN/SUB/HIDDEN |
| `NovelClueSubTypeEnum` | PLOT_THREAD/FORESHADOWING |
| `NovelClueStatusEnum` | DORMANT/ACTIVE/RESOLVED |
| `NovelClueToneEnum` | TRAGIC/TENSE/ROMANTIC/HEROIC/MYSTERIOUS/DARK |
| `NovelItemTypeEnum` | WEAPON/ARMOR/TOOL/CONSUMABLE/TREASURE/DOCUMENT/CURRENCY/OTHER |
| `NovelItemStatusEnum` | INTACT/DAMAGED/DESTROYED/LOST |
| `NovelCheatTypeEnum` | ABILITY/ITEM_BOUND/SPACE/SYSTEM |
| `NovelAliasTypeEnum` | ONLINE_IDENTITY/DISGUISE/ALTER_EGO/OTHER |
| `NovelLocationTypeEnum` | CITY/VILLAGE/BUILDING/SECT/WILDERNESS/REALM/BATTLEFIELD |
| `NovelChapterStatusEnum` | DRAFT/PENDING_GRAPH_CONFIRM/PUBLISHED/PENDING_GRAPH_UPDATE/INTERRUPTED_DRAFT |
| `NovelGenerationStatusEnum` | GENERATING/CONTENT_REVIEW/PATCH_REVIEW/PENDING_GRAPH_UPDATE/SUCCESS/INTERRUPTED/FAILED |
| `NovelGraphPatchOperationTypeEnum` | 38 种业务操作，每种带 RiskLevel（LOW/MEDIUM/HIGH） |
| `NovelRelationTypeEnum` (KNOWS) | FRIEND/ALLY/RIVAL/ACQUAINTANCE/SUBORDINATE/ENEMY |
| `NovelLoveStatusEnum` | UNREQUITED/MUTUAL/PAST |
| `NovelFamilyTypeEnum` | FATHER/MOTHER/BROTHER/SISTER/SON/DAUGHTER/COUSIN/SPOUSE/MASTER/DISCIPLE |
| `NovelGenerationProviderEnum` | DEEPSEEK/TONGYI/MOCK |
| `NovelGraphChangeStatusEnum` | APPLIED/UNDONE/FAILED |
| `NovelGraphNodeEnum` | 11 种节点类型 Label |
| `NovelGraphRelationEnum` | 29 种关系类型 |
| `NovelGraphPropertyEnum` | 系统属性名常量 |

**Acceptance criteria:**
- [ ] 全部实现 `BaseEnum` 接口
- [ ] GraphPatch 操作类型枚举带风险等级标记

**Dependencies:** Task 1

**Files to create:** `constant/` 目录下 ~22 个枚举

**Scope:** M

---

#### Task 3: Entity + DAO 创建

**Description:** 新建所有 Entity（对应 16 张业务表 + 5 张系统表），新建对应 DAO 接口（继承 MyBatis-Plus `BaseMapper`）。每张表一个 Entity + 一个 DAO。

**业务表 Entity（17 个）：**
`NovelProjectEntity`、`NovelCharacterEntity`、`NovelLocationEntity`、`NovelClueEntity`、`NovelItemEntity`、`NovelEventEntity`、`NovelCheatEntity`、`NovelAliasEntity`、`NovelNarrativeRuleEntity`、`NovelVolumeEntity`、`NovelChapterEntity`、`NovelCharacterRelationEntity`、`NovelCharacterLocationEntity`、`NovelCharacterCheatEntity`、`NovelChapterAppearanceEntity`、`NovelClueAdvanceEntity`、`ChapterOutlineEntity`

**系统表 Entity（5 个，可复用部分旧代码）：**
`UserApiKeyEntity`、`ChapterGenerationSessionEntity`、`GraphChangeLogEntity`、`WritingLogEntity`、`WritingCalendarEntity`

**Acceptance criteria:**
- [ ] 所有 Entity 字段 + `@TableName` + `@TableId` 与 DDL 一致
- [ ] 所有 DAO 继承 `BaseMapper<Entity>` + `@Mapper`
- [ ] 编译通过 (`mvn compile`)

**Dependencies:** Task 1, 2

**Files to create:** `domain/entity/*.java` (~17 个), `dao/*.java` (~17 个)

**Scope:** L

---

### Checkpoint: Foundation
- [ ] MySQL DDL + Neo4j 约束可执行
- [ ] `mvn compile` 通过

---

### Phase 2: 基础设施

#### Task 4: LLM 配置

**Description:** 创建 `UserApiKeyEntity`（支持写作模型 + 向量模型 Key/模型名/参数），`NovelLLMConfig` 按 Provider 创建模型对象。

**Acceptance criteria:**
- [ ] 用户可配 DeepSeek Key + 模型名 + temperature/maxTokens/timeout
- [ ] 用户可配通义千问 Key + embedding 模型名/rerank 模型名
- [ ] `createDeepseekModel()` / `createTongyiModel()` / `createDeepseekStreamingModel()` / `createEmbeddingModel()`
- [ ] Key 为空返回 null（不抛异常）
- [ ] Key AES 加密存储 + `ApiEncryptService`

**Dependencies:** Task 3

**Files to create:** `config/NovelLLMConfig.java`, `config/NovelLLMProperties.java`, `entity/UserApiKeyEntity.java`, `dao/UserApiKeyDao.java`, `service/UserApiKeyService.java`, `controller/UserApiKeyController.java`

**Scope:** M

---

#### Task 5: Neo4j GraphService 基础层

**Description:** 实现 5 种 Cypher 白名单模板 + 所有 11 种节点类型的 merge 方法 + 字段白名单校验。所有 Cypher 带 `projectId`。

**Acceptance criteria:**
- [ ] MERGE_NODE / ARCHIVE_NODE / MERGE_REL / DELETE_REL / UPDATE_NODE_PROPS 模板正确拼接
- [ ] mergeProject / mergeVolume / mergeChapter / mergeCharacter / mergeLocation / mergeClue / mergeItem / mergeEvent / mergeCheat / mergeAlias / mergeNarrativeRule 全部实现
- [ ] `$props` 字段白名单校验：每个节点类型只允许对应字段，拒绝 system 字段（`projectId`/`archived`）和未知字段
- [ ] 单元测试覆盖 5 种模板 + 白名单拦截 + 所有 merge 方法

**Dependencies:** Task 3

**Files to create:** `service/NovelGraphService.java`, `test/.../service/NovelGraphServiceTest.java`

**Scope:** L

---

### Checkpoint: 基础设施
- [ ] LLM 配置 + GraphService 白名单测试通过
- [ ] `mvn test -pl sa-admin -am` 相关测试绿色

---

### Phase 3: 业务 CRUD（Task 6~9 可并行）

#### Task 6: 项目管理

**Description:** 项目创建/编辑/归档/分页查询/详情。创建时同步 Neo4j Project 节点。查询强制过滤 `create_user_id`。项目含 Token 预算/硬上限字段。

**Verification:** `NovelProjectServiceTest` 覆盖创建/查询/归档/用户隔离

**Dependencies:** Task 5

**Files to create:** `service/NovelProjectService.java`, `controller/NovelProjectController.java`, Form/VO, `test/.../NovelProjectServiceTest.java`

**Scope:** M

---

#### Task 7a: 资产管理 — 角色/地点/线索

**Description:** 角色、地点、线索增删改查。创建时同步 Neo4j。管理页只暴露设定属性（动态属性不在 Form 里）。

**Verification:** 三种实体 Create + Query 测试，验证用户隔离

**Dependencies:** Task 5

**Files to create:** `service/NovelAssetService.java`（角色/地点/线索部分）、各 Form/VO、测试

**Scope:** M

---

#### Task 7b: 资产管理 — 物品/事件/金手指/马甲/叙事规则/卷

**Description:** 六种实体的增删改查，复用 `NovelAssetService`。全部设定属性可改，动态属性不在 Form 里。

**Verification:** 六种实体 Create + Query 测试

**Dependencies:** Task 7a（共用 Service 基架）

**Files:** 补全 `NovelAssetService.java`、各 Form/VO、测试

**Scope:** M

---

#### Task 8: 角色关系 CRUD

**Description:** KNOWS/LOVES/HATES/IS_FAMILY_OF 四种关系的增删查。KNOWS relationType 枚举严格校验。**其他关系中间表（`t_novel_character_location`/`t_novel_character_cheat`/`t_novel_chapter_appearance`/`t_novel_clue_advance`）不提供独立 CRUD——仅通过 GraphPatch 写作流程写入。**

**Verification:** CRUD + 枚举拒绝测试

**Dependencies:** Task 7a

**Files:** `service/NovelAssetService.java`（补关系部分）、Form/VO、测试

**Scope:** S

---

#### Task 9: 章节管理 + 章节细纲

**Description:** 章节列表/详情/编辑。章节关联卷（可选）。章节细纲 CRUD（只存 MySQL，type=JSON 或纯文本）。

**Verification:** 章节查询 + 细纲 CRUD 测试

**Dependencies:** Task 7a, 7b

**Files to create:** `service/NovelChapterService.java`, `controller/NovelChapterController.java`, `dao/ChapterOutlineDao.java`、Form/VO、测试

**Scope:** M

---

### Checkpoint: 业务 CRUD
- [ ] 所有 CRUD 测试通过
- [ ] 用户隔离校验通过
- [ ] `mvn test -pl sa-admin -am` 绿色

---

### Phase 4: 写作引擎

#### Task 10: 上下文检索 Service

**Description:** 实现三阶段候选池收窄 + Token 计数 + 各种兜底。

- 阶段一：粗筛 7 种来源（卷概要/上章/线索/细纲匹配/候选地点/第1章兜底）
- 阶段二：过滤（死者/路人）+ 排序（POV>上章>驱动线索>同地点>其他）
- 阶段三：按 Token 预算动态分层展示（完整卡→简短→丢弃）
- 兜底：第 1 章候选池空→全量；POV 空→主角→PROTAGONIST；无 CURRENTLY_AT→跳过地点；无卷→跳过卷概要

**Verification:** mock Driver 测试覆盖所有边界

**Dependencies:** Task 5

**Files to create:** `service/NovelRetrieveService.java`, `test/.../NovelRetrieveServiceTest.java`

**Scope:** L

---

#### Task 11: 提示词组装 Service

**Description:** 组装 System Prompt（叙事规则按 priority 排序 + 世界观 + 文风 + 每章目标字数）+ User Prompt（卷概要/前章/角色/关系/线索/金手指/地点/写作指令）。模板用 `application.yaml` 配置。

**Verification:** 模板拼装测试——给定 mock 数据，验证 prompt 内容包含关键字段

**Dependencies:** Task 10

**Files to create:** `service/NovelPromptService.java`, `dev/application.yaml`（补 prompt 配置）, `test/.../NovelPromptServiceTest.java`

**Scope:** S

---

#### Task 12: LLM Service

**Description:** 对接 DeepSeek（`generateChapter` 阻塞式 + `generateChapterStream` 流式），调用后解析 title/summary/content。API Key 为空返回 null，异常降级。

**Verification:** Key 空→null；正常解析测试（mock LLM 或真实 Key）

**Dependencies:** Task 4, 11

**Files to create:** `service/NovelLLMService.java`, `test/.../NovelLLMServiceTest.java`

**Scope:** M

---

#### Task 13: 写作 Service（状态机 + 流程编排）

**Description:** 实现完整写作流程：
- `start()`: 创建 session → ChapterIntent 组装 → 细纲解析 → 检索 → Prompt → LLM → 质检旁注 → 保存草稿 → 返回
- `contentReviewPass()`: 正文审阅通过 → 触发 GraphPatch 抽取
- 无 Key 降级 Mock（正文占位，状态机正常流转）
- Fail 重试 3 次后降级 Mock
- 正文审阅时用户可编辑正文
- 状态机流转 + 单章节互斥

**Verification:** Mock 全流程可跑通，状态机正确

**Dependencies:** Task 9, 10, 11, 12

**Files to create:** `service/NovelWriteService.java`, `controller/NovelWriteController.java`, Form/VO, `test/.../NovelWriteServiceTest.java`

**Scope:** L

---

#### Task 14: 流式写作 + WebSocket

**Description:** `startStream()` 流式写法，WebSocket 逐 token 推送。断线后后端继续生成，P1 阶段 `/recover` 拉回。

**Verification:** WebSocket 连接 + 消息推送测试

**Dependencies:** Task 12, 13

**Files to create:** `config/NovelWebSocketConfig.java`, `ws/NovelWriteWebSocketHandler.java`

**Scope:** M

---

### Checkpoint: 写作引擎
- [ ] Mock 全流程可跑通（创建项目→创建角色→开始写作→正文审阅→GraphPatch）
- [ ] 状态机流转正确

---

### Phase 5: GraphPatch 系统

#### Task 15a: GraphPatch 模型定义 + 38→5 映射 + 分级策略

**Description:** 定义 `NovelGraphPatchModel`（patchId/projectId/chapterNumber/operations[]）+ `NovelGraphPatchOperationModel`（type/opId/characterName/from/to/before/after/confidence）。实现 38 种业务操作→5 种 Cypher 模板的映射表 + 操作分级（READY/高风险/CONFLICT/BLOCKED）。inversePatch 生成逻辑。

**Acceptance criteria:**
- [ ] 38 种操作全部有映射
- [ ] inversePatch 正确：MOVE_CHARACTER forward DELETE_REL+MERGE_REL → inverse 反之；CREATE_CHARACTER inverse = ARCHIVE_NODE；ADVANCE_CLUE inverse = 恢复原值+DELETE_REL
- [ ] 分级：出场/新增/位置→READY；情绪/目标/战力→READY；线索→READY；关系/马甲/消耗→高风险
- [ ] 单元测试覆盖映射表 + inversePatch 生成 + 分级

**Dependencies:** Task 5

**Files to create:** `domain/model/NovelGraphPatchModel.java`, `domain/model/NovelGraphPatchOperationModel.java`, `service/NovelGraphPatchService.java`（映射+分级部分）, 测试

**Scope:** M

---

#### Task 15b: GraphPatch LLM 抽取

**Description:** 把正文发给 LLM → 解析 JSON → 调用 15a 的模型生成 GraphPatch（backend 补 before 值）。抽取失败时只允许「重试抽取」（不允许跳过图谱更新直接发布）。

**Verification:** 抽取 Prompt + JSON 解析测试

**Dependencies:** Task 12, 15a

**Files:** 补全 `service/NovelGraphPatchService.java`, 测试

**Scope:** M

---

#### Task 16: GraphPatch 审阅 API

**Description:** `patchConfirm`（确认变更→调用 5.11 写入流程）+ `patchBack`（返回审阅→丢弃候选 Patch）。

**Dependencies:** Task 13, 15b

**Files to create:** 补 `controller/NovelWriteController.java`, `service/NovelWriteService.java`（审阅部分）

**Scope:** S

---

#### Task 17: 图谱写入 + 章节发布 + 向量化

**Description:** 三步写入顺序：① 章节状态→PUBLISHED + writing_log → ② 白名单执行器写 Neo4j → ③ Neo4j 成功后更新 MySQL 动态属性 + graph_change_log。`operation_batch_id` 幂等。Neo4j 失败→标记 PENDING_GRAPH_UPDATE→允许重试。向量化：图谱写入成功后调用 Embedding 模型→Chapter.embedding。未配向量 Key 跳过。

**Verification:** Neo4j 写入成功/失败 + 幂等 + 向量化跳过测试

**Dependencies:** Task 4, 15a, 15b

**Files:** `service/NovelWriteService.java`（写入流程）, `service/NovelGraphService.java`

**Scope:** M

---

### Checkpoint: GraphPatch
- [ ] 抽取→审阅→确认→写入全流程测试通过
- [ ] Neo4j 失败重试链路测试通过

---

### Phase 6: 辅助功能

#### Task 18: 写后校验

**Description:** DORMANT 伏笔回溯 + 线索停滞（10 章）+ 角色失踪（15 章）。纯 Cypher。重复提醒抑制（`lastAlertedChapter`）。

**Dependencies:** Task 5

**Files:** `service/NovelWriteService.java`（补写后校验）

**Scope:** S

---

#### Task 19: 撤销

**Description:** `/undo` 读取最近 APPLIED 的 graph_change_log → 执行 inversePatch。只撤图谱不删正文日志，只撤全部不支持单条。

**Dependencies:** Task 17

**Files:** `service/NovelWriteService.java`（补 undo）, `controller/NovelWriteController.java`

**Scope:** S

---

#### Task 20: 图谱查询 + 仪表盘 + 写作日志

**Description:** 项目概览/角色关系网/线索推进历史 API + 仪表盘统计 API + 写作日志写入。

**Dependencies:** Task 5, 7a

**Files to create:** `service/NovelGraphService.java`（补查询）, `controller/NovelGraphController.java`, `service/NovelDashboardService.java`, `controller/NovelDashboardController.java`

**Scope:** M

---

### Checkpoint: Complete
- [ ] 全流程从创建项目到写作闭环通过测试
- [ ] `mvn test` 全绿
- [ ] 无 Key Mock 降级全流程可跑

---

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Neo4j 不在本地环境 | High | mock Driver 跑测试；DDL/Cypher 脚本单独在目标环境验证 |
| DeepSeek API 不可用 | Medium | Mock 降级保证核心流程可测试 |
| AI GraphPatch 抽取准确率不够 | Medium | 审阅环节兜底 + 高风险默认不勾选 |
| Entity/DDL 字段遗漏 | Low | 一一对照方案第四章逐实体校验 |
| 从零创建代码量大 | Medium | 按 Phase 推进，每个 Checkpoint 验证通过再继续 |

## Scope Summary

| Task | Scope | Task | Scope | Task | Scope |
|------|:-----:|------|:-----:|------|:-----:|
| 1 DDL | S | 8 角色关系 | S | 15a Patch模型 | M |
| 2 枚举 | M | 9 章节+细纲 | M | 15b Patch抽取 | M |
| 3 Entity+DAO | L | 10 上下文检索 | L | 16 审阅API | S |
| 4 LLM配置 | M | 11 提示词 | S | 17 写入+发布 | M |
| 5 GraphService | L | 12 LLM | M | 18 写后校验 | S |
| 6 项目管理 | M | 13 写作 | L | 19 撤销 | S |
| 7a 资产(角色/地点/线索) | M | 14 流式+WS | M | 20 图谱查询 | M |
| 7b 资产(物品/事件/金手指/马甲/规则/卷) | M | | | | |

**22 tasks, 6 phases, 4 checkpoints, 0 XL.**
