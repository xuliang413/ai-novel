# AI 小说技术方案

> 基于「LangChain4j + Neo4j + SmartAdmin」的小说创作外脑系统
> 
> **定位**：「工具 + 教练」——帮老手提效，带新手入门

---

## 〇、多租户架构

系统基于 SmartAdmin 的用户体系，采用 **用户 → 项目** 两级隔离。

```
系统（单实例）
  ├── 用户 A（私有，不可见其他用户数据）
  │    ├── 项目 1：剑道独尊
  │    ├── 项目 2：都市神豪
  │    └── 项目 3：废柴逆袭（草稿）
  │
  ├── 用户 B（私有）
  │    └── 项目 1：魔法纪元
  │
  └── 用户 C（私有）
       └── 项目 1：江湖夜雨
```

### 0.1 隔离方案

| 层级 | 存储 | 说明 |
|------|------|------|
| 用户信息 | MySQL（SmartAdmin 自带） | 账号、密码、偏好、API Key |
| 项目元数据 | MySQL | 项目 ID、书名、类型、状态 |
| 图谱数据 | Neo4j | 固定实体标签 + `projectId` 属性隔离，所有业务节点和关系都带 `projectId` |
| 用户 API Key | MySQL（加密存储） | 每位用户用自己的 DeepSeek / 通义千问 Key |

> **图谱隔离原则**：不使用按项目 ID 拼接出来的动态标签。Neo4j 中的标签只表达实体类型（如 `Project`、`Character`、`Chapter`），项目归属统一通过 `projectId` 属性表达。MySQL `project.id` 是权威项目 ID，Neo4j 的 `projectId` 与其保持一致。
>
> 所有 GraphService 查询必须显式接收 `projectId`，并在 Cypher 中过滤项目边界；不允许只按名称、章节号等业务字段裸查。

```cypher
MATCH (c:Character {projectId: $projectId, name: $name})
OPTIONAL MATCH (c)-[r:CURRENTLY_AT {projectId: $projectId}]->(loc:Location {projectId: $projectId})
RETURN c, r, loc
```

**Neo4j 约束示例**：

```cypher
CREATE CONSTRAINT project_id IF NOT EXISTS
FOR (p:Project) REQUIRE p.projectId IS UNIQUE;

CREATE CONSTRAINT character_project_name IF NOT EXISTS
FOR (c:Character) REQUIRE (c.projectId, c.name) IS UNIQUE;

CREATE CONSTRAINT chapter_project_number IF NOT EXISTS
FOR (ch:Chapter) REQUIRE (ch.projectId, ch.number) IS UNIQUE;

CREATE CONSTRAINT clue_project_name IF NOT EXISTS
FOR (cl:Clue) REQUIRE (cl.projectId, cl.name) IS UNIQUE;
```

### 0.2 MySQL 数据表设计

> SmartAdmin 自带 `user`、`role`、`permission` 表，无需改动。以下是业务新增表。

| # | 表 | 用途 | 核心字段 | 排期 |
|---|----|------|---------|:--:|
| 1 | `user_api_key` | 用户 API Key | `user_id`, `deepseek_key`(加密), `qwen_key`(加密) | P0 |
| 2 | `project` | 项目元数据 | `id`, `user_id`, `title`, `genre`, `status` | P0 |
| 3 | `chapter_content` | 章节正文全文 | `chapter_number`, `project_id`, `content`(TEXT), `status`, `updated_at` | P0 |
| 4 | `chapter_outline` | 章节细纲（场景节拍） | `chapter_number`, `project_id`, `scenes`(JSON) | P0 |
| 5 | `writing_log` | 写作记录 | `project_id`, `chapter_number`, `word_count`, `token_used`, `success`, `created_at` | P0 |
| 6 | `graph_change_log` | 图谱变更日志 | `project_id`, `chapter_number`, `operation_batch_id`, `patch_json`, `inverse_patch_json`, `status` | P0 |
| 7 | `chapter_generation_session` | 章节生成会话 | `project_id`, `chapter_number`, `generation_job_id`, `status`, `intent_json`, `pending_patch_id` | P0 |
| 8 | `style_template` | 风格模板 | `user_id`, `name`, `rules`(JSON) | P2 |
| 9 | `inspiration_note` | 灵感便签 | `user_id`, `content`, `project_id`(可空) | P2 |
| 10 | `writing_calendar` | 写作打卡 | `user_id`, `date`, `word_count` | P2 |

`chapter_content.status` 枚举：

| 状态 | 含义 |
|------|------|
| `DRAFT` | 已生成或用户已编辑，但尚未进入图谱确认 |
| `PENDING_GRAPH_CONFIRM` | 正文已通过内容审阅，等待用户确认 GraphPatch |
| `PENDING_GRAPH_UPDATE` | 正文已保存，但图谱更新失败或被 `/undo` 撤回，需要重新同步 |
| `PUBLISHED` | 正文和图谱均已确认完成 |
| `INTERRUPTED_DRAFT` | 生成中断后保存的部分草稿 |

`graph_change_log` 用于支撑审计和 `/undo`：`patch_json` 保存本次实际执行的图谱变更，`inverse_patch_json` 保存反向变更，`operation_batch_id` 绑定一次确认更新内的所有操作，`status` 取 `APPLIED / UNDONE / FAILED`。

### 0.3 Neo4j 与 MySQL 分工

```
MySQL（关系型）：
  · 用户 / 项目元数据 / 章节正文 / 细纲 / 写作记录 / 图谱变更日志 / 生成会话
  · 适合：文本存储、日志统计、加密字段、跨库恢复状态

Neo4j（图数据库）：
  · 11 类节点 + 29 种关系
  · Chapter 节点只存 summary、pov、embedding（不含正文）
  · 所有业务节点和关系带 projectId，适合：关系查询、状态追踪、上下文检索
```

> 章节正文为什么存 MySQL 不放 Neo4j：每章几千字长文本，Neo4j 属性值不适合存大文本；且正文不需要图查询，SQL 直接按章号取即可。

### 0.4 用户层资产（跨项目共享）

| 资产 | 说明 | 排期 |
|------|------|------|
| 写作日历 + 打卡 | 连续写作天数、月统计 | P2 |
| 风格模板 | 预设文风/视角/禁止规则，多项目复用 | P2 |
| 灵感便签 | 不属任何项目的随手记，可移入项目 | P2 |

---

## 一、项目概述

### 1.1 核心痛点

- **上下文爆炸**：小说设定、角色信息、细纲、主线/支线/暗线等数据量巨大，且随故事推进不断膨胀
- **大模型崩溃**：即使对信息进行总结压缩，上下文窗口依然不堪重负，导致 AI 生成内容逻辑断裂、角色行为前后矛盾
- **知识库维护繁琐**：手动提炼章节内容、更新角色状态、追踪线索进度，效率极低
- **创作门槛高**：新手面对空白页面不知从何开始；老手的大纲细纲缺乏结构化管理和追踪

### 1.2 解决思路

- **知识外挂（RAG）**：不再把所有设定塞给模型，而是每次写作时从 Neo4j 知识图谱中按需检索最相关信息
- **结构化小说模型**：将一切元素（角色、地点、章节、线索、事件、物品、金手指、马甲）抽象为节点和关系
- **写作闭环**：检索→生成→审阅→自动更新图谱，无需手动维护
- **滚动生长**：不要求提前画完整蓝图，大纲在写作中与系统一起「发现」
- **创作教练**：Wizard 引导式立项，每步解释「为什么重要」，帮助新手学会搭建故事

---

## 二、技术选型

| 层级 | 技术 | 说明 |
|------|------|------|
| 前端壳 | **SmartAdmin**（MIT 协议） | Vue3 + Ant Design Vue，自带登录/鉴权/布局/移动端模板 |
| 后端框架 | Spring Boot 3.x | SmartAdmin 自带，Java 生态 |
| 鉴权 | Sa-Token + JWT | SmartAdmin 自带 |
| 关系数据库 | MySQL | SmartAdmin 自带，存用户/项目元数据/章节正文/细纲/写作记录 |
| 图数据库 | **Neo4j** | 知识图谱 + 向量存储，一体两用 |
| AI 编排 | **LangChain4j** | Java 版 LangChain，集成 Neo4j 和多种 LLM |
| 对话 / 写作 LLM | **DeepSeek**（用户自带 API Key） | 性价比极高，中文创作能力强 |
| Embedding 模型 | **通义千问**（阿里云 API） | 中文语义向量化效果优秀 |
| Rerank 模型 | 通义千问 qwen3-rerank | 对检索结果精排 |
| 图谱可视化 | @antv/g6 | 与 Ant Design Vue 同生态 |
| 实时通信 | WebSocket | 流式生成推送、图谱联动 |
| 移动端 | UniApp（SmartAdmin 自带） | P2 启用 |

> **SmartAdmin 的使用策略**：只做壳（登录/鉴权/布局/菜单路由），不动其 MyBatis-Plus 业务模块，我们的核心业务（Neo4j + LangChain4j）独立编写。

---

## 三、系统架构

```
┌─────────────────────────────────────────────────────────┐
│                    接入层                                │
│  SmartAdmin 前端壳（Vue3 + AntDV）                       │
│  REST API  +  WebSocket                                 │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────┐
│                  业务编排层                              │
│  WizardService  │  WritingOrchestrator  │  ReviewService │
│  （立项引导）     │  （写作状态机）        │  （审阅）       │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────┐
│                   核心引擎层                              │
│  GraphService       │  RetrieveService  │  LLMService   │
│  （Cypher 图查询）    │  （上下文检索+截断） │  （DeepSeek）  │
│  Neo4jText2Cypher   │  EmbeddingService │  ExtractService│
│  （自然语言→Cypher）  │  （通义千问向量化）  │  （实体抽取）   │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────┴──────────────────────────────────┐
│                   数据与存储层                            │
│  Neo4j（图谱 + 向量）  │  MySQL（用户 + 项目 + 章节正文 + 日志）│
└─────────────────────────────────────────────────────────┘
```

---

## 四、知识图谱设计（Neo4j）

Neo4j 同时承担「知识图谱」和「向量存储」两个角色，是整个系统的核心记忆库。

### 4.1 设计目标

- 将小说中所有元素（角色、地点、章节、线索、事件、物品）抽象为**节点**和**关系**
- AI 写作时，通过 Cypher 精准查询当前章节所需上下文，替代「把全文塞给 LLM」
- 写作完成后，自动从新章节抽取实体变更，回写图谱形成闭环

### 4.2 节点类型（11 类）

#### 4.2.0 通用系统字段

所有业务节点（包括 `Project`、`Volume`、`Chapter`、`Character`、`Location`、`Item`、`Event`、`Clue`、`Cheat`、`Alias`、`NarrativeRule`）都必须具备以下系统字段：

| 属性 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `projectId` | Long | ✅ | 项目隔离字段，与 MySQL `project.id` 一致 |
| `archived` | Boolean | ✅ | 系统归档标记，默认 `false`，查询默认过滤 `archived=true` |
| `createdAt` | DateTime | ✅ | 创建时间 |
| `updatedAt` | DateTime | ✅ | 更新时间 |
| `createdByPatchId` | String | ❌ | 创建该节点的 GraphPatch ID |
| `updatedByPatchId` | String | ❌ | 最近一次更新该节点的 GraphPatch ID |

> `archived` 是系统层归档，不等于小说世界里的业务 `status`。例如 `Character.status = DEAD` 表示角色在故事里死亡，但该角色仍是有效图谱实体；`archived = true` 表示该节点是误创建或被用户归档，默认不参与检索和生成。

**事实源原则**：
- 节点存「当前摘要」（如角色目标、情绪、线索进度）。
- 关系存「当前事实」（如角色当前位置、物品持有者、人物关系）。
- 历史靠 MySQL `graph_change_log` 中的 GraphPatch / inversePatch 追溯，不在 P0 做完整时间图谱。

#### 4.2.1 Project（项目 / 书）

| 属性 | 类型 | 必填 | 枚举值 | 说明 | 示例 |
|------|------|------|--------|------|------|
| `projectId` | Long | ✅ | — | 项目 ID，与 MySQL `project.id` 一致 | 10001 |
| `title` | String | ✅ | — | 书名 | 「剑道独尊」 |
| `genre` | String | ✅ | 见 4.7 枚举 | 小说类型 | XIANXIA |
| `worldSetting` | String | ✅ | — | 世界观概述，一段自然语言 | 「上古神魔大战后，天地灵气稀薄，修仙界分为九域…」 |

#### 4.2.2 Volume（卷）

| 属性 | 类型 | 必填 | 枚举值 | 说明 | 示例 |
|------|------|------|--------|------|------|
| `number` | Int | ✅ | — | 卷序号，从 1 开始 | 1 |
| `title` | String | ✅ | — | 卷标题 | 「少年游」 |
| `summary` | String | ✅ | — | 本卷概要，200 字以内 | 「李四拜入青云宗，从杂役弟子开始踏上修仙之路…」 |

#### 4.2.3 Chapter（章节）

| 属性 | 类型 | 必填 | 枚举值 | 说明 | 示例 |
|------|------|------|--------|------|------|
| `number` | Int | ✅ | — | 全局章序号 | 12 |
| `title` | String | ✅ | — | 章标题 | 「旧敌重逢」 |
| `summary` | String | ✅ | — | 本章摘要，300 字以内 | 「李四在青云城偶遇王五，二人交手，李四发现王五持有断魂刀…」 |
| `pov` | String | ❌ | — | 视角人物名（多视角逗号分隔） | 「李四」 或 「李四, 王五」 |
| `embedding` | Float[] | ❌ | — | 章节摘要的向量（用于语义相似检索） | `[0.12, -0.34, ...]` |

> **正文不存 Neo4j**：章节全文存储在 MySQL `chapter_content` 表，Chapter 节点仅存摘要、POV、向量。正文通过章节号关联查询。

#### 4.2.4 Character（角色）

| 属性 | 类型 | 必填 | 枚举值 | 说明 | 示例 |
|------|------|------|--------|------|------|
| `name` | String | ✅ | — | 角色名（唯一） | 「李四」 |
| `role` | String | ✅ | 见 4.7 枚举 | 角色定位 | PROTAGONIST |
| `currentGoal` | String | ❌ | — | 当前目标，自然语言 | 「查明灭门案真相」 |
| `goalProgress` | Float | ❌ | — | 目标完成度（0.0~1.0），LLM 抽取时估算 | 0.4 |
| `goalStatus` | String | ❌ | 见 4.7 枚举 | 目标状态 | IN_PROGRESS / ACHIEVED / ABANDONED / DIVERTED |
| `currentEmotion` | String | ❌ | 见 4.7 枚举 | 当前主导情绪 | ANGER |
| `emotionIntensity` | Int | ❌ | — | 主导情绪强度（1~5，5 最强烈） | 4 |
| `secondaryEmotion` | String | ❌ | 见 4.7 枚举 | 次生情绪（与主导情绪并存） | FEAR |
| `currentLocation` | String | ❌ | — | 只读缓存/展示冗余，不作为事实源；事实源为 `CURRENTLY_AT` 关系 | 「青云城」 |
| `status` | String | ✅ | 见 4.7 枚举 | 角色存活/活跃状态 | ACTIVE |
| `powerLevel` | String | ❌ | — | 战力/境界（修仙：筑基期·金丹期·元婴期；玄幻：一阶·二阶；都市：无） | 「筑基后期」 |
| `description` | String | ❌ | — | 角色基础描述（外貌、性格等，不常变） | 「二十岁青年，剑眉星目，性格坚韧但偶尔冲动…」 |

> `currentGoal`、`currentEmotion`、`goalProgress`、`goalStatus`、`emotionIntensity`、`secondaryEmotion`、`powerLevel` 为**动态摘要属性**，每次章节更新后同步修改。
>
> **情绪粒度设计**：`currentEmotion` + `secondaryEmotion` 支持表层/深层混合表达（如 `DETERMINED` 主导 + `FEAR` 次生 = 表面坚定实则恐惧）。`emotionIntensity` 辅助 AI 把握描写分寸。
>
> **目标追踪**：`goalProgress`（0~1）和 `goalStatus`（IN_PROGRESS / ACHIEVED / ABANDONED / DIVERTED）解决「目标是否完成」的结构化判断，替代纯自然语言的模糊性。
>
> **战力体系**：`powerLevel` 为自然语言字段，适配修仙/玄幻/武侠/都市异能等多种题材的境界/等级体系，由 LLM 抽取和作者手动设置均可。
>
> `currentLocation` 不作为事实源；角色当前位置统一以 `(:Character)-[:CURRENTLY_AT]->(:Location)` 为准，避免属性和关系出现冲突。

#### 4.2.5 Location（地点）

| 属性 | 类型 | 必填 | 枚举值 | 说明 | 示例 |
|------|------|------|--------|------|------|
| `name` | String | ✅ | — | 地点名（唯一） | 「青云城」 |
| `type` | String | ✅ | 见 4.7 枚举 | 地点类型 | CITY |
| `description` | String | ✅ | — | 地点描述 | 「修仙界东部主城，青云宗驻地，城中有一条灵脉…」 |

> **地点层级**：Location 之间通过 `CONTAINS` 关系表达包含层级（大城市→建筑→房间），最多推荐 3 层。检索时自动向上追溯父级地点，注入上下文。详见 §4.3。

#### 4.2.6 Item（物品）

| 属性 | 类型 | 必填 | 枚举值 | 说明 | 示例 |
|------|------|------|--------|------|------|
| `name` | String | ✅ | — | 物品名（唯一） | 「断魂刀」 |
| `type` | String | ✅ | 见 4.7 枚举 | 物品类型 | WEAPON |
| `description` | String | ✅ | — | 物品描述 | 「上古邪刀，刀身漆黑，出鞘必有亡魂…」 |
| `status` | String | ✅ | 见 4.7 枚举 | 物品状态 | INTACT |
| `quantity` | Int | ❌ | — | 数量（可消耗资源如丹药、灵石；不可消耗的唯一物品不填） | 3 |

> **资源追踪**：`quantity` 用于可消耗物品（丹药、灵石、符箓等）。每章写完后 LLM 可抽取消耗变更（如「消耗了 1 颗筑基丹」→ `quantity` 从 3 变为 2）。唯一物品（如断魂刀）不填此字段。

#### 4.2.7 Event（事件）

| 属性 | 类型 | 必填 | 枚举值 | 说明 | 示例 |
|------|------|------|--------|------|------|
| `name` | String | ✅ | — | 事件名（唯一） | 「青云宗宗门大比」 |
| `description` | String | ✅ | — | 事件描述 | 「三年一度的弟子比武大会，前三名可入内门…」 |
| `chapterOccurred` | Int | ❌ | — | 事件发生的章序号 | 15 |

#### 4.2.8 Clue（线索 / 伏笔）

详见 §4.6.1 完整属性表。

#### 4.2.9 Cheat（金手指）

| 属性 | 类型 | 必填 | 枚举值 | 说明 | 示例 |
|------|------|------|--------|------|------|
| `name` | String | ✅ | — | 金手指名称 | 「万倍悟性」 |
| `type` | String | ✅ | 见 4.7 枚举 | 金手指类型 | ABILITY |
| `description` | String | ✅ | — | 能力描述 | 「修炼任何功法速度提升万倍，但每次使用消耗精神力」 |
| `origin` | String | ❌ | — | 来源 | 「第 1 章山洞中捡到古玉激活」 |
| `currentStage` | String | ❌ | — | 当前副作用/升级阶段（自然语言，如「初期反噬」「经脉逆行」「丹田碎裂」） | 「初期反噬·头痛」 |
| `limitation` | String | ❌ | — | 限制/副作用 | 「每日限用三次，过度使用会昏迷」 |
| `evolution` | String | ❌ | — | 升级路径 | 「吸收灵石可升级，下一阶段：十万倍悟性」 |

> **副作用渐进追踪**：`currentStage` 追踪能力/金手指的副作用演化阶段。配合 Clue 节点的「能力代价」暗线使用，检索时自动联动。初期填「无副作用」，随剧情推进逐步更新。

#### 4.2.10 Alias（马甲 / 分身身份）

| 属性 | 类型 | 必填 | 枚举值 | 说明 | 示例 |
|------|------|------|--------|------|------|
| `name` | String | ✅ | — | 马甲名 | 「暗影」 |
| `type` | String | ✅ | 见 4.7 枚举 | 马甲类型 | ONLINE_IDENTITY |
| `context` | String | ✅ | — | 使用场景 | 「暗网黑客论坛」 |
| `description` | String | ❌ | — | 马甲描述 | 「技术帖之王，没人知道真实身份」 |
| `revealed` | Boolean | ✅ | — | 是否已被识破 | false |
| `revealedTo` | String[] | ❌ | — | 被谁识破（角色名列表） | ["王五"] |

#### 4.2.11 NarrativeRule（叙事规则）

| 属性 | 类型 | 必填 | 枚举值 | 说明 | 示例 |
|------|------|------|--------|------|------|
| `name` | String | ✅ | — | 规则名称 | 「字数限制」 |
| `type` | String | ✅ | 见 4.7 枚举 | 规则类型 | WORD_COUNT |
| `value` | String | ✅ | — | 规则内容（自然语言） | 「每章 2500~3500 字」 |
| `priority` | Int | ✅ | — | 优先级（1~5，5 最高） | 5 |

> **规则类型说明**：PLATFORM_REDLINE（平台红线）优先级最高，AI 绝对不能违反。WORD_COUNT（字数）和 STYLE（文风）是硬约束。FORBIDDEN（禁止项）和 POV（视角）是软约束。
>
> **Prompt 组装时注入**：系统指令（~400 token）优先取自 NarrativeRule，按 priority 降序排列，PLATFORM_REDLINE 永远在最前面。

> **关于 Scene（场景）节点**：第一阶段不做，Chapter 的 `summary` 属性足以覆盖场景信息。后期检索粒度不够再加。
>
> **关于 WorldRule（世界观规则）节点**：预留扩展空间，用户可根据小说类型自由定义规则节点及属性规范。

### 4.3 关系类型（29 类）

#### 4.3.0 关系通用字段

所有业务关系都必须带 `projectId`，用于项目隔离、导出、删除和审计。关系统一字段如下：

| 属性 | 类型 | 必填 | 说明 |
|------|------|:--:|------|
| `projectId` | Long | ✅ | 项目隔离字段，与关系两端节点的 `projectId` 一致 |
| `createdAt` | DateTime | ✅ | 创建时间 |
| `updatedAt` | DateTime | ✅ | 更新时间 |
| `createdByPatchId` | String | ❌ | 创建该关系的 GraphPatch ID |
| `updatedByPatchId` | String | ❌ | 最近一次更新该关系的 GraphPatch ID |

> 关系不统一增加 `archived` 字段。撤销关系变更时，直接删除或恢复关系；历史追溯依赖 MySQL `graph_change_log`。如果要表达故事世界中的过去状态，使用业务字段，例如 `LOVES.status = PAST`、`Clue.status = RESOLVED`，不要用系统归档字段表达剧情状态。

#### 结构关系

| 关系 | 起点 | 终点 | 含义 |
|------|------|------|------|
| `CONTAINS` | Project | Volume | 项目包含卷 |
| `CONTAINS` | Volume | Chapter | 卷包含章 |
| `CONTAINS` | Location | Location | 地点包含子地点（如「京城」包含「国师府」，「国师府」包含「暗室」），最多推荐 3 层 |
| `PREVIOUS` | Chapter | Chapter | 上一章 → 下一章 |
| `HAS_RULE` | Project | NarrativeRule | 项目配置的叙事规则 |

#### 出场关系

| 关系 | 起点 | 终点 | 含义 |
|------|------|------|------|
| `APPEARS_IN` | Character | Chapter | 角色出场于某章 |
| `APPEARS_IN` | Location | Chapter | 地点出现于某章 |
| `APPEARS_IN` | Item | Chapter | 物品出现于某章 |
| `APPEARS_IN` | Event | Chapter | 事件发生于某章 |

#### 角色关系

| 关系 | 起点 | 终点 | 属性 | 含义 |
|------|------|------|------|------|
| `KNOWS` | Character | Character | `relationType`（见 4.7 枚举） | 一般相识 |
| `LOVES` | Character | Character | `status`（UNREQUITED / MUTUAL / PAST） | 爱慕 |
| `HATES` | Character | Character | `intensity`（1~5） | 仇恨 |
| `IS_FAMILY_OF` | Character | Character | `familyType`（FATHER / MOTHER / BROTHER / SISTER / SON / DAUGHTER / COUSIN / SPOUSE / MASTER / DISCIPLE） | 亲属/师门 |

> **折中策略**：核心情感关系（能驱动剧情的）使用独立关系类型；一般社交关系用 `KNOWS` + `relationType` 属性区分。
>
> **关系属性示例**：
> ```
> (李四)-[:HATES {intensity: 5}]->(王五)       // 不共戴天之仇
> (李四)-[:LOVES {status: 'MUTUAL'}]->(师妹)    // 两情相悦
> (李四)-[:IS_FAMILY_OF {familyType: 'DISCIPLE'}]->(师傅)  // 师徒关系
> (李四)-[:KNOWS {relationType: 'ALLY'}]->(张六)  // 盟友
> ```

#### 状态关系

| 关系 | 起点 | 终点 | 含义 |
|------|------|------|------|
| `CURRENTLY_AT` | Character | Location | 角色当前所在地 |
| `POSSESSES` | Character | Item | 角色持有物品 |
| `PARTICIPATES_IN` | Character | Event | 角色参与事件 |

#### 线索关系

| 关系 | 起点 | 终点 | 含义 |
|------|------|------|------|
| `DRIVES` | Character | Clue | 角色推动某条线索 |
| `ADVANCES` | Chapter | Clue | 某章推进了某条线索 |
| `INVOLVES` | Clue | Character | 线索牵连某个角色 |
| `KNOWS_ABOUT` | Character | Clue | 角色知情某条线索（属性 `level`: 0.0~1.0, `isFalse`: Boolean） |
| `INTERSECTS` | Clue | Clue | 线索交汇（属性 `intersectChapter`, `intersectDescription`） |
| `BELONGS_TO` | Clue | Volume | 线索属于哪一卷 |
| `TRIGGERS` | Event | Clue | 事件触发线索 |

> **INTERSECTS 带属性**：记录两条线在哪个章节交汇、交汇的具体内容。
> **KNOWS_ABOUT**：与 `DRIVES`（主动推动）不同，`KNOWS_ABOUT` 表示角色被动知情。对群像剧和悬疑类型至关重要。
> `isFalse` 默认为 `false`，设为 `true` 表示角色**持有错误认知**（如李四以为凶手是国师，但实际是掌门）。悬疑/谋略类型刚需，追踪信息不对称。

#### 物品关系

| 关系 | 起点 | 终点 | 含义 |
|------|------|------|------|
| `ITEM_OF` | Item | Item | 物品是另一个物品的组成部分/碎片（如「断魂刀」是「上古神兵」的碎片） |

#### 金手指关系

| 关系 | 起点 | 终点 | 属性 | 含义 |
|------|------|------|------|------|
| `HAS_CHEAT` | Character | Cheat | `acquiredInChapter`（获得章节） | 角色拥有金手指 |
| `BOUND_TO` | Cheat | Item | — | 金手指绑定于某物品（如「老爷爷戒指」） |
| `RELATES_TO` | Clue | Cheat | — | 线索关联某项金手指（如暗线「能力代价」关联金手指「万倍悟性」，检索时联动） |

> **示例**：
> ```
> (李四)-[:HAS_CHEAT {acquiredInChapter: 1}]->(万倍悟性)
> (万倍悟性)-[:BOUND_TO]->(古玉)
> (能力代价)-[:RELATES_TO]->(万倍悟性)  // 暗线关联金手指，检索时自动带上 Cheat 的 limitation/evolution/currentStage
> ```

#### 马甲关系

| 关系 | 起点 | 终点 | 属性 | 含义 |
|------|------|------|------|------|
| `HAS_ALIAS` | Character | Alias | `createdInChapter`（首现章节） | 角色拥有马甲身份 |
| `KNOWS_ALIAS` | Character | Alias | `sinceChapter`（何时知晓） | 某人知道该马甲背后的真实身份 |

> **示例**：
> ```
> (李四)-[:HAS_ALIAS {createdInChapter: 5}]->(暗影)
> (王五)-[:KNOWS_ALIAS {sinceChapter: 20}]->(暗影)  // 王五在第20章识破了暗影=李四
> (暗网论坛)-[:APPEARS_IN]->(暗影)  // 马甲也可用 APPEARS_IN 记录出场
> ```

### 4.4 图谱示例

以修仙题材为例，第 12 章「旧敌重逢」的图谱结构：

```
(第11章)──PREVIOUS──→(第12章)──PREVIOUS──→(第13章)
                          │
    ┌──────────┬──────────┼──────────┬──────────┐
    │          │          │          │          │
  APPEARS_IN  APPEARS_IN  │       ADVANCES     │
    │          │          │          │          │
  [李四]    [王五]    [青云城]   [暗线:上古神兵]
    │          │          │
  CURRENTLY_AT  │          │
    │          │          │
    └──→[青云城]←─┘        │
    │                      │
  HATES                    │
    │                      │
    └────────→[王五]        │
    │                      │
  DRIVES                   │
    │                      │
    └──────────→[暗线:上古神兵]
                   │
                POSSESSES
                   │
                [王五]──→[断魂刀]
```

### 4.5 核心查询示例

```cypher
// 查询：写第13章时，第12章出场了哪些角色、当前状态如何、推动了哪些线索
MATCH (ch:Chapter {projectId: $projectId, number: 12})
MATCH (c:Character {projectId: $projectId})-[:APPEARS_IN {projectId: $projectId}]->(ch)
OPTIONAL MATCH (c)-[:CURRENTLY_AT {projectId: $projectId}]->(loc:Location {projectId: $projectId})
OPTIONAL MATCH (c)-[:DRIVES {projectId: $projectId}]->(clue:Clue {projectId: $projectId})
OPTIONAL MATCH (ch)-[:ADVANCES {projectId: $projectId}]->(clue2:Clue {projectId: $projectId})
RETURN c.name, c.currentGoal, c.currentEmotion,
       loc.name AS locationName, clue.name AS drivenClue,
       clue2.name AS advancedClue
```

```cypher
// 查询：当前所有活跃的暗线，以及谁在推动、推进到哪一章
MATCH (clue:Clue {projectId: $projectId, type: 'HIDDEN', status: 'ACTIVE'})
OPTIONAL MATCH (c:Character {projectId: $projectId})-[:DRIVES {projectId: $projectId}]->(clue)
OPTIONAL MATCH (ch:Chapter {projectId: $projectId})-[:ADVANCES {projectId: $projectId}]->(clue)
RETURN clue.name, clue.description, c.name AS driver,
       ch.number AS lastAdvancedChapter
ORDER BY ch.number DESC
```

```cypher
// 查询：李四的人际关系网（含关系类型）
MATCH (li:Character {projectId: $projectId, name: '李四'})-[r]-(other:Character {projectId: $projectId})
WHERE r.projectId = $projectId
  AND type(r) IN ['KNOWS', 'LOVES', 'HATES', 'IS_FAMILY_OF']
RETURN li.name, type(r) AS relation, other.name
```

### 4.6 多线叙事建模

#### 4.6.1 Clue 节点属性详解

| 属性 | 类型 | 说明 | 示例 |
|------|------|------|------|
| `name` | String | 线索/伏笔名称 | 「灭门案真相」 |
| `type` | Enum | 主线/支线/暗线 | MAIN / SUB / HIDDEN |
| `**subType**` | Enum | **线索 vs 伏笔** | PLOT_THREAD / FORESHADOWING |
| `status` | Enum | 生命周期状态 | DORMANT / ACTIVE / RESOLVED |
| `description` | String | 完整描述 | 「二十年前李家满门被灭…」 |
| `**revealLevel**` | Float | **揭露程度**（0.0 ~ 1.0） | 0.3（已暴露 30%） |
| `**currentStage**` | String | **当前阶段**（渐进式线索的核心追踪字段） | 「线索碎片」「真相浮现」「证据确凿」 / 「初期反噬」「经脉逆行」「丹田碎裂」 |
| `**targetChapter**` | Int | **计划收束章节** | 65 |
| `**priority**` | Int | **优先级**（1 ~ 5） | 5（核心暗线） |
| `**summary**` | String | **当前进展摘要**（每章更新） | 「李四已发现国师可疑，但无证据」 |

> **subType 的设计动机**：「线索」持续跨章推进，「伏笔」埋下后长期休眠、在未来引爆。区分后，写第 45 章时可通过回溯检索自动发现第 3 章埋下的伏笔，防止 AI「遗忘」。
>
> **currentStage 的设计动机**：`revealLevel` 表达「暴露给读者多少」，`currentStage` 表达「线索当前所处的阶段」。两者正交——暗线可能 `revealLevel=0.2`（读者几乎不知道）但 `currentStage=经脉逆行`（代价已经非常严重）。当前阶段是检索时提示 AI 渐进式描写、代价加重、阶段升级的关键信号。

#### 4.6.2 线索生命周期

```
  [创建] → DORMANT ──→ ACTIVE ──→ RESOLVED
              ↑            │           │
              └── 暂停 ────┘           │
                                       │
                  收束后保留在图中（历史追溯）
```

| 状态 | 含义 | 行为 |
|------|------|------|
| `DORMANT` | 已埋下伏笔，尚未开始推进 | 不入 Prompt，仅在回溯查询时触发 |
| `ACTIVE` | 正在推进中 | 每章写作时检索并注入 Prompt |
| `RESOLVED` | 故事线已收束 | 不注入 Prompt，但保留历史供回顾 |

#### 4.6.3 新增关系说明

**KNOWS_ABOUT — 知情者追踪**

与 `DRIVES`（主动推动线索）不同，`KNOWS_ABOUT` 表示角色被动知情：

```
(李四)-[:DRIVES]->(暗线:灭门真相)        // 李四主动调查
(王五)-[:KNOWS_ABOUT {level: 0.5}]->(暗线:灭门真相)  // 王五知道一半真相
```

Cypher 查询知情者：

```cypher
// 查暗线「灭门真相」的知情者有哪些、各知道多少
MATCH (c:Character {projectId: $projectId})-[k:KNOWS_ABOUT {projectId: $projectId}]->(clue:Clue {projectId: $projectId, name: '灭门真相'})
RETURN c.name, k.level
ORDER BY k.level DESC
```

**INTERSECTS — 线索交汇**

记录两条线在哪个章节交汇、具体内容：

```
[暗线:灭门真相]-[i:INTERSECTS {intersectChapter: 15, intersectDescription: "李四发现灭门案和师门有关"}]->[支线:宗门内斗]
```

```cypher
// 查暗线和所有其他线索的交汇点
MATCH (c1:Clue {projectId: $projectId, name: '灭门真相'})-[i:INTERSECTS {projectId: $projectId}]-(c2:Clue {projectId: $projectId})
RETURN c2.name, i.intersectChapter, i.intersectDescription
ORDER BY i.intersectChapter
```

#### 4.6.4 多线叙事完整查询

写新章节时，系统一次性检索所有叙事上下文：

```cypher
// 1. 当前所有活跃线索（按优先级排序）
MATCH (clue:Clue {projectId: $projectId})
WHERE clue.status = 'ACTIVE'
OPTIONAL MATCH (c:Character {projectId: $projectId})-[:DRIVES {projectId: $projectId}]->(clue)
OPTIONAL MATCH (lastCh:Chapter {projectId: $projectId})-[:ADVANCES {projectId: $projectId}]->(clue)
RETURN clue.name, clue.type, clue.subType, clue.summary, clue.revealLevel,
       c.name AS driver, lastCh.number AS lastAdvancedChapter
ORDER BY clue.priority DESC

// 2. 当前所有未引爆的伏笔（subType = FORESHADOWING 且 status = DORMANT）
MATCH (f:Clue {projectId: $projectId, subType: 'FORESHADOWING', status: 'DORMANT'})
MATCH (ch:Chapter {projectId: $projectId})-[:ADVANCES {projectId: $projectId}]->(f)
RETURN f.name, f.description, ch.number AS plantedChapter
ORDER BY ch.number

// 3. 已记录出场角色涉及的线索交汇（用于章节完成后回顾/校验）
MATCH (ch:Chapter {projectId: $projectId, number: 20})
MATCH (c:Character {projectId: $projectId})-[:APPEARS_IN {projectId: $projectId}]->(ch)
MATCH (c)-[r:DRIVES|KNOWS_ABOUT {projectId: $projectId}]->(clue:Clue {projectId: $projectId})
MATCH (clue)-[i:INTERSECTS {projectId: $projectId}]-(otherClue:Clue {projectId: $projectId})
RETURN clue.name, otherClue.name, i.intersectChapter, i.intersectDescription
```

#### 4.6.5 迭代计划

| # | 扩展点 | 类型 | 说明 |
|---|--------|------|------|
| ② | 线索层级 | 关系 `PARENT_OF` / `CHILD_OF` | 大线套小线，如「灭门真相」下分「凶手身份」「灭门动机」「幸存者」等子线索 |
| ③ | 线索依赖 | 关系 `DEPENDS_ON` | 单向依赖，B 线必须等 A 线推到一定阶段才能触发。悬疑类刚需 |
| ⑤ | 情绪基调 | 属性 `tone` | 每条线的底色（TRAGIC / TENSE / ROMANTIC），用于约束 AI 写相关段落的语气 |
| ⑥ | 揭示计划 | 属性 `plannedRevealPhases` | 结构化规划释放节奏，如 `[{chapterRange: "11-20", phase: "线索碎片"}]` |

### 4.7 枚举值汇总

#### 4.7.1 小说类型（Project.genre / WorldRule 等场景）

| 枚举值 | 中文 | 典型特征 |
|--------|------|----------|
| `XIANXIA` | 修仙 / 仙侠 | 境界体系、灵气、飞升 |
| `XUANHUAN` | 玄幻 | 异世界、斗气/魔法、非传统修仙 |
| `URBAN` | 都市 | 现代背景 |
| `HISTORY` | 历史 | 架空/真实历史背景 |
| `SCIFI` | 科幻 | 科技、未来、星际 |
| `MYSTERY` | 悬疑 / 推理 | 案件、谜题驱动 |
| `WUXIA` | 武侠 | 江湖、内功、无超自然 |
| `FANTASY` | 西方奇幻 | 魔法、龙、骑士 |

#### 4.7.2 角色定位（Character.role）

| 枚举值 | 中文 | 说明 |
|--------|------|------|
| `PROTAGONIST` | 主角 | 故事核心人物 |
| `ANTAGONIST` | 反派 / 对手 | 主要对抗力量 |
| `SUPPORTING` | 重要配角 | 常驻、推动剧情 |
| `MINOR` | 次要角色 | 阶段性出场 |

#### 4.7.3 角色状态（Character.status）

| 枚举值 | 中文 | 说明 |
|--------|------|------|
| `ACTIVE` | 活跃 | 正常出场中 |
| `INACTIVE` | 暂离 | 暂时退场（如闭关、远行） |
| `DEAD` | 已死亡 | 确认死亡 |
| `MISSING` | 失踪 | 下落不明 |
| `UNKNOWN` | 未知 | 存疑（留给悬疑用） |

#### 4.7.3.1 角色目标状态（Character.goalStatus）

| 枚举值 | 中文 | 说明 |
|--------|------|------|
| `IN_PROGRESS` | 推进中 | 角色正在追求该目标 |
| `ACHIEVED` | 已完成 | 目标达成（产生新目标时旧目标标记此状态） |
| `ABANDONED` | 已放弃 | 角色不再追求该目标 |
| `DIVERTED` | 已转向 | 目标因外部事件被迫改变方向 |

#### 4.7.4 角色情绪（Character.currentEmotion）

| 枚举值 | 中文 | 适用场景 |
|--------|------|----------|
| `ANGER` | 愤怒 | 仇恨、被背叛、被挑衅 |
| `JOY` | 喜悦 | 突破、重逢、胜利 |
| `SADNESS` | 悲伤 | 失去、离别、回忆 |
| `FEAR` | 恐惧 | 绝境、强敌、未知 |
| `CALM` | 平静 | 日常、修炼、思考 |
| `ANXIETY` | 焦虑 | 等待、不安、预感 |
| `DETERMINED` | 坚定 | 下定决心、背水一战 |
| `CONFUSED` | 困惑 | 迷茫、发现矛盾信息 |

#### 4.7.5 地点类型（Location.type）

| 枚举值 | 中文 | 说明 |
|--------|------|------|
| `CITY` | 城市 | 人口聚集地 |
| `VILLAGE` | 村庄 | 小型聚居点 |
| `BUILDING` | 建筑 | 单一建筑（宫殿、塔、客栈） |
| `SECT` | 宗门 | 门派驻地 |
| `WILDERNESS` | 野外 | 山林、沙漠、海域 |
| `REALM` | 秘境 / 洞天 | 独立空间、副本 |
| `BATTLEFIELD` | 战场 | 临时战斗地点 |

#### 4.7.6 物品类型（Item.type）

| 枚举值 | 中文 | 说明 |
|--------|------|------|
| `WEAPON` | 武器 | 攻击性装备 |
| `ARMOR` | 防具 | 防御性装备 |
| `ARTIFACT` | 法宝 / 神器 | 有特殊功能的法器 |
| `TOKEN` | 信物 / 凭证 | 身份象征、通行证 |
| `SCROLL` | 秘籍 / 卷轴 | 功法、配方、地图 |
| `ELIXIR` | 丹药 | 用于修炼或治疗的药物 |
| `COMMON` | 普通物品 | 无特殊功能的日常物品 |
| `CHEAT` | 金手指物品 | 带系统的戒指、古玉残魂等（物品型金手指） |

#### 4.7.7 物品状态（Item.status）

| 枚举值 | 中文 | 说明 |
|--------|------|------|
| `INTACT` | 完好 | 正常可用 |
| `DAMAGED` | 受损 | 需要修复 |
| `DESTROYED` | 已毁 | 不可修复 |
| `LOST` | 遗失 | 下落不明 |
| `SEALED` | 被封印 | 能力被锁 |

#### 4.7.8 线索类型（Clue.type）

| 枚举值 | 中文 | 说明 |
|--------|------|------|
| `MAIN` | 主线 | 贯穿全书的核心叙事线 |
| `SUB` | 支线 | 分支剧情线 |
| `HIDDEN` | 暗线 | 隐藏在表层叙事之下，逐步揭示 |

#### 4.7.9 线索子类型（Clue.subType）

| 枚举值 | 中文 | 说明 | 生命周期特征 |
|--------|------|------|-------------|
| `PLOT_THREAD` | 线索 | 持续跨章推进的故事线 | 长期 ACTIVE |
| `FORESHADOWING` | 伏笔 | 提前埋下的钩子，未来引爆 | 长期 DORMANT → 引爆时 ACTIVE |

#### 4.7.10 线索状态（Clue.status）

| 枚举值 | 中文 | 行为 |
|--------|------|------|
| `DORMANT` | 休眠 | 已埋下，不入 Prompt；回溯查询时触发 |
| `ACTIVE` | 活跃 | 每章写作时检索并注入 Prompt |
| `RESOLVED` | 已收束 | 不注入 Prompt，保留在图谱中供历史回顾 |

#### 4.7.11 一般社交关系（KNOWS.relationType）

| 枚举值 | 中文 | 说明 |
|--------|------|------|
| `FRIEND` | 朋友 | 日常友好关系 |
| `ALLY` | 盟友 | 利益共同体 |
| `RIVAL` | 竞争对手 | 非敌对但有竞争 |
| `ACQUAINTANCE` | 熟人 | 认识但不深交 |
| `SUBORDINATE` | 下属 | 上下级关系 |
| `ENEMY` | 敌人 | 弱于 HATES 的对立关系 |

#### 4.7.12 线索情绪基调（迭代计划 ⑤ — Clue.tone）

| 枚举值 | 中文 | AI 写作影响 |
|--------|------|------------|
| `TRAGIC` | 悲剧 | 沉重、压抑的笔调 |
| `TENSE` | 紧张 | 快节奏、悬疑的笔调 |
| `ROMANTIC` | 温情 | 柔和、细腻的笔调 |
| `HEROIC` | 热血 | 激昂、燃向的笔调 |
| `MYSTERIOUS` | 神秘 | 朦胧、暗示性的笔调 |
| `DARK` | 黑暗 | 冷酷、绝望的笔调 |

#### 4.7.13 金手指类型（Cheat.type）

| 枚举值 | 中文 | 说明 | 典型形态 |
|--------|------|------|----------|
| `ABILITY` | 能力型 | 无物理载体的被动/主动能力 | 万倍悟性、过目不忘、血脉觉醒 |
| `ITEM_BOUND` | 物品绑定型 | 绑定在物品上的特殊功能 | 老爷爷戒指、系统面板、神秘古玉 |
| `SPACE` | 空间型 | 随身空间或洞天 | 时间加速洞府、灵田空间 |
| `SYSTEM` | 系统型 | 面板/任务系统 | 加点系统、抽奖轮盘 |
| `INHERITANCE` | 传承型 | 功法记忆传承 | 上古大能灌顶、血脉记忆觉醒 |

#### 4.7.14 马甲类型（Alias.type）

| 枚举值 | 中文 | 说明 | 典型场景 |
|--------|------|------|----------|
| `ONLINE_IDENTITY` | 网络身份 | 论坛/游戏中的虚拟身份 | 暗网技术大神、游戏第一高手 |
| `DISGUISE` | 伪装身份 | 易容/假扮的临时身份 | 蒙面客、潜入敌营的假身份 |
| `ALTER_EGO` | 第二人格/分身 | 独立的人格或实体分身 | 魔化人格、身外化身 |
| `SECRET_IDENTITY` | 隐藏真身 | 不为人知的真实社会身份 | 白天废柴/夜晚高手、双重职业 |

#### 4.7.15 金手指克制（迭代计划 — Cheat 间关系）

| 关系 | 起点 | 终点 | 含义 |
|------|------|------|------|
| `COUNTERS` | Cheat | Cheat | 一个金手指克制另一个 |
| `ENHANCES` | Cheat | Cheat | 一个金手指强化另一个 |

> 当多个角色的金手指产生交互时使用。属于迭代增强功能。

#### 4.7.16 叙事规则类型（NarrativeRule.type）

| 枚举值 | 中文 | 优先级 | 说明 | 示例 |
|--------|------|:------:|------|------|
| `PLATFORM_REDLINE` | 平台红线 | 🔴 5 | 根据发布平台加载的内容安全规则，AI 绝对不能违反 | 「起点标准：不写色情、不写政治敏感」 |
| `WORD_COUNT` | 字数 | 🟡 4 | 每章字数范围，生成约束 + 质检依据 | 「每章 2500~3500 字」 |
| `STYLE` | 文风 | 🟡 4 | 写作风格描述，白描/金庸风/轻小说风等 | 「白描，多用短句，金庸式打斗描写」 |
| `FORBIDDEN` | 禁止项 | 🟡 3 | 作者不想看到的具体表达 | 「不写「恐怖如斯」、不堆砌外貌成语」 |
| `POV` | 视角 | 🟡 3 | 叙事视角约束 | 「第三人称限知，单章不跳视角」 |
| `STRUCTURE` | 结构 | 🟢 2 | 章节结构要求 | 「每章至少一个冲突点，结尾要有钩子」 |
| `LANGUAGE` | 语言 | 🟢 2 | 语言风格约束 | 「简体中文，不用网络梗，不用英文」 |
| `CUSTOM` | 自定义 | 🟢 1 | 作者自定义的其他规则 | 「配角不能抢主角高光时刻」 |

> **Prompt 注入顺序**：优先级高的先注入，PLATFORM_REDLINE（priority=5）永远在系统指令的第一行。

#### 4.7.17 平台红线预设（选配，作者可选加载）

| 平台 | 预设内容（作者可增删） |
|------|---------------------|
| 起点中文网 | 禁止色情描写、禁止政治敏感、禁止宣扬封建迷信 |
| 番茄小说 | 禁止涉黑涉政、禁止色情、禁止校园暴力美化 |
| 七猫 | 禁止涉政涉黑、禁止色情低俗、禁止历史虚无主义 |
| 自定义 | 作者完全手动编写 |

> Wizard 第 1 步选平台后自动加载对应预设为 NarrativeRule（type=PLATFORM_REDLINE）。

---

## 五、核心流程

### 5.1 上下文检索策略

每次写作时，系统从 Neo4j 知识图谱中按需检索最相关的上下文注入 Prompt，替代「把全文塞给 LLM」。

#### 5.1.1 核心原则

```
❌ 塞原始数据 → AI 自己消化
   "李四，男，20 岁，青云宗弟子，筑基期，性格坚韧但偶尔冲动，父亲早亡..."
   → 200 token，信息噪声大

✅ 塞结构化摘要 → 直接喂结论
   "李四[筑基期·复仇中·愤怒·青云城]"
   → 40 token，信息密度高 5 倍
```

#### 5.1.2 基于 ChapterIntent 的检索清单

写第 N 章时，系统不能假设第 N 章已经存在 `APPEARS_IN` 关系；第 N 章尚未生成，出场角色、目标线索和主地点必须先由写作意图推导。系统先构建 `ChapterIntent`，再基于它检索上下文。

**ChapterIntent 来源优先级**：

| 优先级 | 来源 | 示例 | 默认 required |
|------|------|------|:--:|
| S0 | 用户显式指定 | `/write 20 --pov 李四 --chars 李四,王五 --clues 灭门案真相` | ✅ |
| S1 | 章节细纲 | `chapter_outline.scenes` 中提到的角色、地点、线索 | ✅ |
| S2 | 前章延续 | 第 N-1 章出场角色、未解决冲突、章末地点 | ❌ |
| S3 | 活跃线索关联 | `ACTIVE Clue` 的 `DRIVES / INVOLVES / KNOWS_ABOUT` 角色 | ❌ |
| S4 | 项目默认/叙事规则 | 默认 POV、主角兜底、类型规则、平台规则 | 视规则而定 |

**ChapterIntent 示例**：

```json
{
  "projectId": 10001,
  "chapterNumber": 20,
  "pov": "李四",
  "chapterGoal": "潜入国师府暗室，找到灭门案证据",
  "candidateCharacters": [
    {
      "name": "李四",
      "source": ["USER_POV"],
      "required": true
    },
    {
      "name": "王五",
      "source": ["PREVIOUS_CHAPTER"],
      "required": false
    },
    {
      "name": "国师",
      "source": ["OUTLINE", "ACTIVE_CLUE"],
      "required": true
    }
  ],
  "candidateLocations": [
    {
      "name": "国师府暗室",
      "source": ["OUTLINE"],
      "required": true
    }
  ],
  "targetClues": [
    {
      "name": "灭门案真相",
      "source": ["USER_COMMAND", "ACTIVE_CLUE"],
      "required": true
    }
  ],
  "extraInstructions": []
}
```

`ChapterIntent` P0 不写入正式 `chapter_outline`，而是保存在 `chapter_generation_session.intent_json` 中，用于刷新恢复和审计。章节发布后，可把最终使用过的 intent 作为审计信息保留，但不污染作者原始细纲。

**基于 ChapterIntent 的检索内容**：

| # | 检索内容 | 数据来源 | 检索方式 | 预算 |
|---|---------|---------|---------|------|
| ① | 本卷细纲 / 本章目标 | `Volume.summary` + `ChapterIntent.chapterGoal` | 图查询 + 会话对象 | 1 段 |
| ② | 前章摘要 | `Chapter.summary` WHERE `projectId` + `number = N-1` | 图查询 | 1 段 |
| ③ | 候选角色完整状态卡 | `ChapterIntent.candidateCharacters` + `Character` + `CURRENTLY_AT` | 图查询（含新字段 goalProgress/goalStatus/emotionIntensity/secondaryEmotion/powerLevel） | 5 人 |
| ④ | 候选角色简短提示 | 排名 6~10 的候选角色 | 图查询 | 5 人 |
| ⑤ | 关键人物关系 | 候选角色之间的 `KNOWS / LOVES / HATES / IS_FAMILY_OF` | 图查询 | 只保留冲突/驱动剧情关系 |
| ⑥ | 目标线索完整进展 | `ChapterIntent.targetClues` | 图查询（含 currentStage + RELATES_TO 关联 Cheat） | 3 条 |
| ⑦ | 关联线索简短提示 | 与目标线索 `INTERSECTS` 或共享角色的线索 | 图查询 | 3 条 |
| ⑧ | 金手指 + 马甲 | `Cheat` + `Alias` WHERE 属于候选角色；Clue 通过 `RELATES_TO` 关联的 Cheat 自动附带 | 图查询 | 只取候选角色相关 |
| ⑨ | 主地点 + 父级地点 | `ChapterIntent.candidateLocations` + POV 当前所在地 + 前章结尾地点 + `CONTAINS` 向上追溯 2 层 | 图查询 | 1 完整 + 父级简短 |
| ⑩ | 渐进式趋势提示 | 目标线索的 `currentStage` 历史（最近 3 章）→ 计算趋势 | `graph_change_log` 回溯 + 实时计算 | 1~2 行提示 |

> DORMANT 伏笔不进入写前 Prompt，统一走写后校验，避免占用生成预算。向量检索仅作为补充：当用户要求「找类似氛围的打斗场景」这类模糊语义匹配时，对章节摘要进行 Embedding 搜索。

#### 5.1.3 检索策略：图查询为主，向量检索为辅

| 维度 | 图查询 (Cypher) | 向量检索 (Embedding) |
|------|----------------|---------------------|
| **占比** | ~90% | ~10% |
| **适合** | 精确关系、结构化数据 | 模糊语义、相似场景 |
| **优势** | 100% 精准，不会漏 | 能发现未显式定义但语义相关的关联 |
| **劣势** | 只能查已定义的关系 | 可能召回不相关内容 |

#### 5.1.4 Token 预算（总 ~6000）

```
┌────────────────────────┬──────────┬──────────┐
│ 内容                   │ 预算     │ 说明                     │
├────────────────────────┼──────────┼──────────────────────────┤
│ 系统指令               │ ~400     │ 取自 NarrativeRule（§4.2.11），按 priority 排序，P0 固定 │
│ ChapterIntent           │ ~250     │ POV、本章目标、候选实体来源 │
│ 本卷当前细纲段          │ ~200     │ 本章所在卷和当前阶段目标    │
│ 前章摘要               │ ~300     │ 上一章发生了什么          │
│ 候选角色完整状态卡       │ ~700     │ 每人 40~60 token，≤5 人  │
│ 候选角色简短提示         │ ~200     │ 每人 20~30 token，≤5 人  │
│ 角色间关键关系          │ ~250     │ 只给有冲突/驱动剧情的关系  │
│ 目标线索完整进展         │ ~450     │ 每条 80~120 token，≤3 条 │
│ 关联线索简短提示         │ ~180     │ 每条 30~50 token，≤3 条  │
│ 金手指/马甲            │ ~220     │ 只给候选角色相关          │
│ 地点信息               │ ~250     │ 1 个完整主地点 + 2 个简短地点 │
│ AI 生成预留            │ ~2850    │ 实际写作空间             │
├────────────────────────┼──────────┼──────────────────────────┤
│ 合计                   │ ~6000    │                          │
└────────────────────────┴──────────┴──────────────────────────┘
```

**压缩策略**：

| 策略 | 效果 |
|------|------|
| 角色状态卡压缩为一行：`李四[筑基期·复仇中·愤怒·国师府暗室]` | 200 → 40 token |
| 角色分层：5 个完整状态卡 + 5 个简短提示 | 群像章节不断上下文 |
| 关系只给「写入章节可能有冲突的」，相识/熟人跳过 | 减少噪声 |
| 线索分层：3 条目标线索 + 3 条关联线索 | 避免所有 ACTIVE 线索一起入 Prompt |
| `DORMANT` 伏笔不进 Prompt，写后校验接管 | 减半 |
| 地点描述截断为关键特征（200 字 → 50 字） | 省 150 token |

**截断规则**：当检索结果超预算时，优先保留 `required=true` 的 ChapterIntent 内容。用户显式指定、POV 角色、细纲明确提到的角色/地点/线索默认不可截断；推断来源（前章延续、活跃线索关联、项目默认兜底）可按优先级降级为简短提示或丢弃。

##### 详细截断策略

**硬阈值**：Prompt 总上限 **8000 token**。预算目标 6000，允许浮动到 8000，超过则强制截断。

**三级截断流程**：

```
Level 1: 先砍 P2（目标省 ~400 token）
  ├→ 关联地点：2 个→1 个，描述 100 字→30 字
  └→ DORMANT 伏笔：不进入写前 Prompt，写后校验接管

Level 2: 再砍 P1（目标省 ~600 token）
  ├→ 角色关系：只留 HATES/LOVES/关键 KNOWS，砍普通关系
  ├→ ② 前章摘要：300 字→100 字（只留核心事件一句话）
  ├→ ① 卷细纲：200 字→50 字（只留本章目标一句）
  └→ 金手指/马甲：先砍非 POV 角色，再砍 POV 角色的低优信息

Level 3: 最后砍 P0（目标省 ~800 token，尽量不触发）
  ├→ 目标线索：3 条→2 条，保留 required=true 和 priority 最高
  └→ 完整角色状态卡：5 人→3 人，非 required 角色降级为简短提示
```

**逐项截断对照表**：

| # | 内容 | 默认值 | Level 1 | Level 2 | Level 3 | Level 3 警告 |
|---|------|--------|---------|---------|---------|-------------|
| ① | ChapterIntent required 项 | 全保留 | 不变 | 不变 | 不变 | 必须人工确认 |
| ② | 卷细纲 | 200 字 | 不变 | 200→50 | 200→50 | — |
| ③ | 前章摘要 | 300 字 | 不变 | 300→100 | 300→100 | — |
| ④ | 完整角色状态卡 | ≤5 人 | 不变 | 不变 | 5→3，非 required 降级 | ⚠️ |
| ⑤ | 简短角色提示 | ≤5 人 | 5→3 | 3→0 | 0 | — |
| ⑥ | 目标线索 | ≤3 条 | 不变 | 不变 | 3→2，保留 required | ⚠️ |
| ⑦ | 关联线索 | ≤3 条 | 3→2 | 2→0 | 0 | — |
| ⑧ | 金手指/马甲 | 候选角色相关 | 不变 | 先砍非 POV 角色 | 只留 POV 角色关键项 | 系统提示 |
| ⑨ | 地点 | 1 完整 + 2 简短 | 2 简短→1 简短 | 主地点 100 字→50 字 | 主地点 50 字→30 字 | — |

**截断后通知**：

截断完成后，在 Prompt 末尾追加：
```
[系统提示] 本次上下文已完成自动裁剪：省略了 N 条低优线索、M 个角色关系。
```

Level 3 触发时改为：
```
⚠️ [警告] 上下文严重超限，仅保留了最核心的角色和线索信息。建议拆分本章或精简细纲。
```

#### 5.1.5 检索时机

分四个阶段：

```
阶段 0：构建 ChapterIntent
  └→ 解析「写第 20 章，视角李四」
  └→ 合并用户命令、章节细纲、前章延续、活跃线索、项目默认
  └→ 写入 chapter_generation_session.intent_json

阶段 1：基于 ChapterIntent 预检索
  └→ 获取候选角色、目标线索、主地点、金手指马甲
  └→ 按 source / required / priority 排序

阶段 2：Prompt 组装前（最终检索 + 截断）
  └→ 拉取前章摘要、卷摘要、关系、地点描述
  └→ Token 预算校验 + 三级截断（如需）

阶段 2.5：用户审阅（前端依赖，M1）
  └→ 展示 contextPreview JSON
  └→ 作者确认 / 修改 / 补充 / 移除 / 重新检索
  └→ 静默模式下跳过此阶段
```

> **不做的**：AI 生成中的动态按需检索（阶段 3），复杂度高，属于后续迭代。

#### 5.1.6 上下文用户审阅

> ⚠️ **前端依赖**：本功能依赖前端可视化界面，属于 M1 安全写作闭环。

**定位**：在 Prompt 组装完成后、发给 LLM 生成前，插入一个用户审阅环节，让作者确认上下文是否正确、是否需要调整。

**审阅接口返回的数据结构**：

```json
{
  "chapter": 20,
  "pov": "李四",
  "contextPreview": {
    "volumeOutline": "本卷：李四前往京城追查灭门案真相。本章需推进：潜入国师府获取证据。",
    "previousChapter": "第19章摘要：李四从神秘老者处得知国师府有暗室…",
    "characters": [
      {
        "name": "李四",
        "state": "筑基期·坚定·京城",
        "goal": "获取国师府暗室证据",
        "cheatList": ["万倍悟性"],
        "aliasList": []
      },
      {
        "name": "王五",
        "state": "筑基后期·愤怒·京城",
        "goal": "阻止李四调查",
        "relationToPov": "死敌 (HATES, intensity=5)"
      }
    ],
    "activeClues": [
      { "name": "灭门案真相", "type": "暗线", "progress": "李四已知国师可疑但无实证" },
      { "name": "宗门内斗", "type": "支线", "progress": "掌门开始怀疑李四身份" }
    ],
    "keyRelations": [
      { "from": "李四", "relation": "HATES", "to": "王五", "intensity": 5 }
    ],
    "locations": [
      { "name": "京城", "type": "CITY", "desc_Snippet": "帝国首都，国师府坐落于城东…" }
    ]
  },
  "tokenStats": {
    "total": 5200,
    "budget": 6000,
    "truncated": false,
    "truncatedItems": []
  }
}
```

**审阅操作**：

| 操作 | 说明 | 示例 |
|------|------|------|
| ✅ **确认** | 上下文无误，提交 LLM 开始生成 | — |
| ✏️ **修改** | 直接编辑某个字段 | 把「王五所在地：京城」改成「青云城」 |
| ➕ **补充** | 手动追加一段额外提示 | 「提醒 DeepSeek：国师府有禁制阵法，李四需要先破解」 |
| ❌ **移除** | 删除某条不需要的信息 | 移除「宗门内斗」线索，本章不想推进 |
| 🔄 **重新检索** | 全部重来 | 改检索参数后重新执行阶段 1+2 |

**三种审阅模式**（用户在对话中切换）：

| 模式 | 命令 | 行为 | 适用场景 |
|------|------|------|---------|
| **交互式** | `/mode review` | 每次生成前暂停，等用户确认 | 精准控制，关键章节 |
| **静默** | `/mode silent` | 不审阅，直接生成；上下文摘要输出到日志 | 快速写作、日常章节 |
| **仅截断时审阅** | `/mode auto` | 上下文超预算时才弹审阅，正常情况静默 | 大部分章节用（推荐） |

**与截断策略的关系**：

```
第一层：系统自动截断
  → 硬阈值 8000 token，Level 1→2→3 自动裁剪
  → 裁剪结果标记在 truncationInfo 中

第二层：用户手动审阅
  → 看到裁剪后的上下文
  → 如果对系统裁剪不满意，可以手动恢复被砍的内容
  → 「宗门内斗本章应该提一下，加回来」→ 从 5 条线索变 3 条
  → 如果加回导致超 8000 → 警告并阻止提交
```

**初次实施建议**：后端先实现接口返回 `contextPreview` JSON，前端未就绪时以「静默模式」运行，同时在对话窗口输出简化的上下文摘要供作者查看。

#### 5.1.7 伏笔的「写后校验」机制

伏笔不占 Prompt 的 Token 预算，改为**写作完成后校验**：

```
1. AI 生成第 N 章正文
2. 系统查询所有 status=DORMANT 的伏笔
3. 用一个小型 Prompt 问 AI：
   "以下伏笔是否适合在本章中被提及或推进？如有，请标注。"
   → 输入：伏笔列表 + 章节正文
4. 如果 AI 判断应该提及但未提 → 提示作者
   "⚠️ 第 3 章埋下的伏笔「神秘玉佩」已 45 章未提及，
    建议在本章或近期章节呼应"
```

#### 5.1.8 特殊场景处理

| 场景 | 处理方式 |
|------|---------|
| 写第 1 章 | 无前章摘要，用项目世界观 + 卷细纲替代 |
| 引入新角色 | 若 `Character` 节点尚未创建，返回提示「新角色，自由发挥」，不阻塞 |
| 角色久未出场 | 查该角色最后出场章节，将其摘要注入 Prompt |
| 多视角章节 | `pov` 含多角色 → 检索所有视角角色的状态卡 |
| Token 预算超限 | 按 P0→P1→P2 优先级截断 |

### 5.2 写作闭环（可恢复状态机，含两次审阅 + GraphPatch 确认）

```
[IDLE] ──用户输入 /write N──→ [RETRIEVING]
                                   │
                            构建 ChapterIntent
                            系统自动检索上下文（5.1.2）
                            三级截断（5.1.4）
                                   │
                                   ↓
                       ┌─ [CONTEXT_REVIEW] ← 第一次审阅
                       │         │
                       │   用户审阅上下文
                       │   （静默模式下可跳过）
                       │         │ 确认
                       ↓         ↓
                        [GENERATING]
                                   │
                            DeepSeek 流式生成
                                   │
                                   ↓
                       ┌─ [CONTENT_REVIEW] ← 第二次审阅 ←──── 不通过
                       │         │
                       │   🔒 不可跳过                    │
                       │   自动质检 + 用户必须人工确认      │
                        │   通过 / 重写 / 局部修改          │
                        │         │ 通过
                        ↓         ↓
                              [SAVE_DRAFT]
                                    │
                                    ↓
                             [EXTRACTING_PATCH]
                                    │
                                    ↓
                        ┌─ [PATCH_REVIEW] ← GraphPatch 确认
                        │         │
                        │   🔒 不可自动执行                 │
                        │   候选变更可恢复                  │
                        │   确认更新 / 返回审阅              │
                        │         │
                        ↓         ↓ 确认
                             [APPLYING_PATCH]
                                    │
                       白名单执行器写 Neo4j + 写 graph_change_log
                                    │
                                    ↓
                               [PUBLISHED]
                                    │
                                    ↓
                                [DONE] ──→ 回到 [IDLE]

  ⚠️ 两次审阅 + 一次确认的区别：
    CONTEXT_REVIEW = 审「喂给 AI 的信息对不对」（可跳过）
    CONTENT_REVIEW = 审「AI 写的行不行」（不可跳过，核心质量关）
    PATCH_REVIEW   = 审「图谱变更对不对」（不可自动，数据安全关）
```

#### 5.2.1 RETRIEVING — 上下文检索

就是 5.1 节的完整流程。产出：组装好的 Prompt + Token 统计 + 截断信息。

#### 5.2.2 CONTEXT_REVIEW — 第一次审阅：上下文

> ⏭️ **可跳过**：对应 5.1.6 用户审阅机制。在静默模式（`/mode silent`）或仅截断时审阅模式（`/mode auto`）下，如无截断则跳过。**前端依赖。**

| 操作 | 说明 |
|------|------|
| ✅ 确认 | 上下文无误，提交 LLM 开始生成 |
| ✏️ 修改 | 直接编辑某个字段（如改角色位置） |
| ➕ 补充 | 追加额外提示（如「国师府有禁制阵法」） |
| ❌ 移除 | 删掉某条不需要的信息 |
| 🔄 重新检索 | 改参数后重跑阶段 1 |

#### 5.2.3 GENERATING — DeepSeek 流式生成

| 机制 | 说明 |
|------|------|
| 传输方式 | WebSocket 推送，前端逐字显示 |
| 超时控制 | 单次生成不超过 120 秒 |
| 重试策略 | API 超时 → 等 3 秒重试，最多 3 次；Rate Limit → 等 10 秒重试，最多 2 次 |
| 断点续写 | 生成被截断时自动发「继续」指令 |

#### 5.2.4 CONTENT_REVIEW — 第二次审阅：生成结果

> 🔒 **不可跳过**：这是核心质量关口，无论哪种模式都必须等待用户人工确认。用户可以选择「通过」或「重写」，但不能由系统自动判定通过。

**自动质检（4 项）**：

| 检查项 | 规则 | 不通过行为 |
|--------|------|-----------|
| 字数达标 | 目标 ±20% | 提示偏多/偏少 |
| 视角一致 | 全文是否保持 POV 角色视角 | 警告视角跳跃 |
| 前章衔接 | 开头是否承接上章结尾 | 提示衔接断裂 |
| 新实体检测 | 出现不在图谱中的角色/地点名 | ⚠️ 标记，待用户确认 |

**用户审阅操作**：

| 操作 | 说明 |
|------|------|
| ✅ 通过并生成变更 | 正文保存为草稿，进入 `EXTRACTING_PATCH` |
| 🔄 重写 | 返回 GENERATING，可附带修改要求（如「不够燃，重写」） |
| ✏️ 局部修改 | 选中某段要求重写（进阶功能，迭代） |

#### 5.2.5 PATCH_REVIEW — 图谱变更确认（需人工确认）

> 🔒 **不可自动执行**：LLM 只抽取候选事实，不允许直接产 Cypher。后端将候选事实归一化为业务语义型 GraphPatch，展示给用户确认后，再由白名单执行器写入 Neo4j。这是数据安全关口，防止错误更新污染图谱。

```
CONTENT_REVIEW 用户点「通过并生成变更」
  ↓
SAVE_DRAFT：正文写入 MySQL `chapter_content`，status = DRAFT / PENDING_GRAPH_CONFIRM
  ↓
EXTRACTING_PATCH：LLM 抽取候选事实（自动）
  ↓
后端生成 GraphPatch + inversePatch
  ↓
PATCH_REVIEW：展示变更摘要给用户              ← 🔒 停在这里
  "角色变更：李四 情绪 坚定→恐惧
   位置变更：李四 青云城→国师府暗室
   新增角色：国师
   线索推进：灭门真相 30%→50%
   关系变更：+3 条"
  ↓
用户点击 [✅ 确认更新] 或 [↩ 返回审阅]
  ↓
APPLYING_PATCH：
  1. 重新 validatePatch，防止用户停留太久导致图谱状态变化
  2. 白名单执行器将 GraphPatch 转为受控 Cypher 并更新 Neo4j
  3. 写入 MySQL `graph_change_log`
  4. `chapter_content.status` 改为 PUBLISHED
  5. 写入 `writing_log`
  ↓
DONE
```

**按钮语义**：

| 按钮 | 行为 |
|------|------|
| `[✅ 确认更新]` | 执行用户勾选的 GraphPatch 操作，成功后发布章节 |
| `[↩ 返回审阅]` | 丢弃本次候选 GraphPatch，不改图谱，正文保留草稿，回到 CONTENT_REVIEW |
| `/undo` | 撤销最近一次已 `APPLIED` 的 GraphPatch，执行 `inversePatch`，默认只撤销图谱，不删除正文 |

**跨库一致性原则**：MySQL 和 Neo4j 不做跨库强事务。正文、候选 Patch、图谱执行和发布状态通过 `chapter_generation_session` 状态机与 `operation_batch_id` 幂等恢复。Neo4j 更新失败时，正文保留为 `PENDING_GRAPH_UPDATE`，允许用户重试。

#### 5.2.6 GraphPatch — 图谱变更单

GraphPatch 是位于「LLM 抽取候选事实」和「Neo4j 写入」之间的标准变更单。它不是给用户直接编辑 Cypher 的入口，而是后端可校验、可审阅、可撤销、可恢复的业务语义结构。

**原则**：
- LLM 不直接产 Cypher。
- LLM 不决定最终 `before` 值，`before` 必须由后端从当前图谱读取。
- 所有 Patch 必须带 `projectId`，且只能等于当前项目。
- 所有操作必须能生成 `inversePatch`。
- 用户确认的是 GraphPatch 摘要，不是 LLM 原始输出。

**业务语义型 GraphPatch 示例**：

```json
{
  "patchId": "uuid",
  "projectId": 10001,
  "chapterNumber": 20,
  "source": "CHAPTER_GENERATION",
  "status": "PENDING_CONFIRM",
  "operations": [
    {
      "opId": "uuid",
      "type": "MOVE_CHARACTER",
      "characterName": "李四",
      "fromLocation": "青云城",
      "toLocation": "国师府暗室",
      "confidence": 0.92,
      "evidence": "李四推开暗室的石门",
      "reason": "本章明确写到李四进入国师府暗室"
    },
    {
      "opId": "uuid",
      "type": "ADVANCE_CLUE",
      "clueName": "灭门案真相",
      "beforeRevealLevel": 0.3,
      "afterRevealLevel": 0.5,
      "summaryAfter": "李四发现国师与灭门案有关",
      "confidence": 0.88,
      "evidence": "密函中出现国师私印",
      "reason": "密函提供了新证据"
    }
  ]
}
```

**操作状态**：

| 状态 | 含义 | 默认行为 |
|------|------|---------|
| `READY` | 校验通过，可执行 | 默认勾选 |
| `LOW_CONFIDENCE` | 置信度低，但可由用户判断 | 默认不勾选，高亮提示 |
| `CONFLICT` | 与当前图谱事实冲突 | 默认不勾选，必须用户选择解决方案 |
| `BLOCKED` | 非法操作、跨项目、缺必填字段等 | 不可勾选，只展示原因 |

**冲突示例**：

```text
冲突：李四当前位置不一致
Patch 认为：青云城
图谱当前：皇宫
目标位置：国师府暗室

[以当前图谱为准继续执行] [返回审阅] [忽略此变更]
```

**常见冲突处理**：

| 冲突 | 处理 |
|------|------|
| `before` 不一致 | 用户选择「以当前图谱为准继续执行」或「忽略此项」 |
| 目标实体不存在 | 用户选择「创建新实体并执行」「绑定到已有实体」「忽略此项」 |
| 新实体重名 | 用户选择「使用已有实体」「改名创建」「忽略此项」 |
| 重复关系 | 自动降级为 no-op，摘要中提示 |
| 类型歧义 | 用户选择「作为物品」「作为线索」「忽略此项」 |

#### 5.2.7 GraphPatch 操作集（8 组业务操作）

GraphPatch 使用业务语义型操作，不采用底层 CRUD 型操作，不允许 `RAW_CYPHER`。

| 分组 | 操作 | 对应节点/关系 | 变更字段（加粗为本次新增） |
|------|------|-------------|------|
| 结构 | `CREATE_VOLUME`、`UPDATE_VOLUME`、`CREATE_CHAPTER`、`UPDATE_CHAPTER_SUMMARY`、`LINK_CHAPTER_PREVIOUS`、`ATTACH_RULE`、**`LINK_LOCATION_PARENT`** | `Volume`、`Chapter`、`CONTAINS`、`PREVIOUS`、`HAS_RULE`、**`CONTAINS(Location)`** | parentLocation |
| 角色 | `CREATE_CHARACTER`、`UPDATE_CHARACTER_STATE`、`MOVE_CHARACTER`、`UPDATE_CHARACTER_RELATION`、`MARK_CHARACTER_STATUS` | `Character`、`CURRENTLY_AT`、`KNOWS`、`LOVES`、`HATES`、`IS_FAMILY_OF` | emotion, **emotionIntensity**, **secondaryEmotion**, goal, **goalProgress**, **goalStatus**, **powerLevel** |
| 地点 | `CREATE_LOCATION`、`UPDATE_LOCATION`、`MARK_LOCATION_APPEARANCE`、**`LINK_LOCATION_CONTAINS`** | `Location`、`APPEARS_IN`、**`CONTAINS(Location)`** | parentLocationName, childLocationName |
| 物品 | `CREATE_ITEM`、`UPDATE_ITEM_STATUS`、`TRANSFER_ITEM`、`MARK_ITEM_APPEARANCE`、**`CONSUME_ITEM`**、**`LINK_ITEM_COMPOSITION`** | `Item`、`POSSESSES`、`APPEARS_IN`、**`ITEM_OF`** | status, **quantity**, **consumedQuantity**, **parentItemName** |
| 事件 | `CREATE_EVENT`、`UPDATE_EVENT`、`MARK_EVENT_OCCURRED`、`LINK_EVENT_PARTICIPANT` | `Event`、`APPEARS_IN`、`PARTICIPATES_IN` | — |
| 线索 | `CREATE_CLUE`、`UPDATE_CLUE`、`ADVANCE_CLUE`、`RESOLVE_CLUE`、`LINK_CLUE_CHARACTER`、`MARK_CHARACTER_KNOWS_CLUE`、`INTERSECT_CLUES`、`TRIGGER_CLUE`、`ASSIGN_CLUE_TO_VOLUME`、**`LINK_CLUE_CHEAT`** | `Clue`、`DRIVES`、`ADVANCES`、`INVOLVES`、`KNOWS_ABOUT`、`INTERSECTS`、`TRIGGERS`、`BELONGS_TO`、**`RELATES_TO`** | revealLevel, **currentStage**, summary, **isFalse** |
| 金手指 | `CREATE_CHEAT`、`UPDATE_CHEAT`、`ASSIGN_CHEAT_TO_CHARACTER`、`BIND_CHEAT_TO_ITEM` | `Cheat`、`HAS_CHEAT`、`BOUND_TO` | **currentStage** |
| 马甲 | `CREATE_ALIAS`、`UPDATE_ALIAS`、`ASSIGN_ALIAS_TO_CHARACTER`、`REVEAL_ALIAS_TO_CHARACTER`、`MARK_ALIAS_APPEARANCE` | `Alias`、`HAS_ALIAS`、`KNOWS_ALIAS`、`APPEARS_IN` | — |

> **新增操作说明**：
> - `LINK_LOCATION_CONTAINS`：建立地点包含关系（如「京城」CONTAINS「国师府」）。inverse：删除 CONTAINS 关系。
> - `CONSUME_ITEM`：消耗可消耗物品（如丹药数量-1）。inverse：恢复 quantity 原值。
> - `LINK_ITEM_COMPOSITION`：建立物品组成关系（如「断魂刀」ITEM_OF「上古神兵」）。inverse：删除 ITEM_OF 关系。
> - `LINK_CLUE_CHEAT`：关联线索与金手指（如「能力代价」RELATES_TO「万倍悟性」）。inverse：删除 RELATES_TO 关系。
> - `UPDATE_CHARACTER_STATE` 扩展参数：除原有 emotion/goal/location/status 外，新增 emotionIntensity、secondaryEmotion、goalProgress、goalStatus、powerLevel。inversePatch 自动回滚全部变更字段。
> - `ADVANCE_CLUE` 扩展参数：除原有 revealLevel/status/summary 外，新增 beforeStage/afterStage。
> - `MARK_CHARACTER_KNOWS_CLUE` 扩展参数：新增 isFalse（默认 false），追踪角色错误认知。
> - `UPDATE_CHEAT` 扩展参数：新增 currentStage，追踪金手指副作用升级。

**撤销策略**：
- 所有操作必须生成 `inversePatch`，否则不能进入 P0。
- 新增实体撤销时统一 `archived=true`，不物理删除。
- 关系不统一归档，撤销时删除或恢复关系；关系历史靠 `graph_change_log` 追溯。
- 所有查询默认过滤 `archived=true` 的节点。

**inversePatch 示例**：

```text
MOVE_CHARACTER
forward:
  删除旧 CURRENTLY_AT
  创建新 CURRENTLY_AT
inverse:
  删除新 CURRENTLY_AT
  恢复旧 CURRENTLY_AT

ADVANCE_CLUE
forward:
  更新 Clue.revealLevel/currentStage/status/summary
  创建 Chapter -ADVANCES-> Clue
inverse:
  恢复 Clue 原 revealLevel/currentStage/status/summary
  删除本次 Chapter -ADVANCES-> Clue

CONSUME_ITEM
forward:
  更新 Item.quantity: 3 → 2
inverse:
  恢复 Item.quantity: 2 → 3

CREATE_CHARACTER
forward:
  创建 Character
inverse:
  设置 Character.archived = true
```

#### 5.2.8 抽取规则分级

全量支持 11 类节点 + 29 种关系，但不是所有事实都允许 LLM 从章节正文中自由抽取。

| 分级 | 来源/权限 | 范围 | 处理 |
|------|----------|------|------|
| A 类 | 系统维护，LLM 不参与 | `Project`、`Volume`、`Chapter`、`CONTAINS`、`PREVIOUS`、`HAS_RULE` | 由创建流程、章节号、规则配置维护 |
| B 类 | LLM 可建议，用户确认后写入 | 普通实体、出场、位置、持有、参与、线索推进、金手指、马甲、地点层级、物品消耗、物品组成 | 默认按置信度展示，用户确认后执行 |
| C 类 | 高风险关系，LLM 只能建议 | 死亡/失踪、物品毁坏/丢失、马甲暴露、线索收束、`LOVES/HATES/IS_FAMILY_OF` 重大变化 | 默认不勾选，必须有证据并人工确认 |
| D 类 | 用户维护 | `NarrativeRule`、平台红线、默认文风、目标字数、项目类型、世界观大改 | 仅 Wizard、设置页、管理页或明确用户命令可改 |

同一个操作在不同来源下默认策略不同：

| Patch 来源 | 默认策略 |
|-----------|---------|
| `CHAPTER_EXTRACTION` | 谨慎；低置信/高风险默认不勾选 |
| `USER_COMMAND` | 用户意图明确；除冲突/阻塞外默认勾选，高风险仍提示 |
| `WIZARD` | 立项阶段数据，默认勾选，但做 schema 校验和重名合并 |
| `MANAGEMENT_EDIT` | 用户手动编辑，通常直接执行；批量/高风险变更二次确认 |
| `IMPORT` | 批量导入，全部进入待审阅队列 |

### 5.3 滚动生长模式

核心理念：**大纲不是在写之前画好的，是写的过程中与系统一起「发现」的。**

#### 5.3.1 写后复盘（每章自动，可跳过）

写完每章后，系统自动分析并给出 3 条可能走向。作者可以看一眼采纳，也可以直接跳过继续写下一章。

```
输入：Chapter.summary + 活跃 Clue + 角色状态
LLM 调用：DeepSeek，一次（~800 in + ~200 out）
输出：3 条下章方向，各关联对应线索
成本：约 0.08 分/章
```

> ⏭️ **可跳过**：复盘结果展示后，作者可选择「采纳建议」或「跳过，我自有方向」。不影响写作流程。

#### 5.3.2 方向建议（每章前，可跳过）

写第 N 章前，系统基于复盘结果 + 当前图谱给出方向建议。老手有细纲时自动跳过，不打扰。

```
系统：📋 第 6 章建议方向：
       ① 继续宗门大比，对决内门第一 → 推进主线
       ② 玉佩残魂揭示信息 → 推进暗线
       ③ 师妹受伤，感情升温 → 推进支线
      💡 建议：①+② 组合，对决中玉佩救场

      [采纳]  [我自己来：______]  [跳过]
```

> ⏭️ **可跳过**：老手已导入细纲则自动跳过此步骤，直接进入生成。

#### 5.3.3 卷自动识别（每 10 章）

```
系统：📊 前 10 章分析：
       第 1-3 章：杂役日常 + 报名大比
       第 4-7 章：宗门大比 + 初露锋芒
       第 8-10 章：进入内门 + 京城线索浮现

      💡 这是一个完整的「起点阶段」，建议命名「第一卷：少年游」
      [✅ 确认]  [改名]  [先不分卷]
```

#### 5.3.4 老手路径

老手在 Wizard 中进行精简版立项（5 步），跳过教学。在第 3 步可选择粘贴大纲文本，LLM 自动解析为卷章结构。建完后直接进入写作，使用沉浸模式（`/mode flow`）。上下文中自动注入叙事规则和世界观约束，不额外打扰。

> 大纲粘贴解析：一次 LLM 调用，耗时 ~3 秒，成本 ~0.04 分。支持自然格式（「第一卷 xxx 第 1 章 xxx」），LLM 自行容错。解析后展示结果供作者确认，确认后批量创建 Volume + Chapter 节点及 CONTAINS/PREVIOUS 关系。

> 计划追踪 + 偏离检测暂不做，依赖后续迭代。

### 5.4 用户交互体系

#### 5.4.1 两种作者，一个开关

第 0 步只问一个问题：

```
你是？
  🔰 我是新手，第一次写小说 → 走 7 步 Wizard，每步带教学
  ✍️ 我是老手，有自己的套路 → 走 5 步精简 Wizard，跳过教学
```

> **大纲导入方式**：支持直接在输入框粘贴大纲文本，LLM 自动解析。不做文件上传（.md/.docx）。

#### 5.4.2 新手 Wizard（7 步）

| 步骤 | 内容 | 教学要点 |
|------|------|---------|
| 0 | 摸底 | 了解用户背景，决定引导深度 |
| 1 | 世界观 | 世界观 = 规则书，决定角色能做什么 |
| 2 | 主角 | 好主角 = 想要 × 缺陷 × 成长 |
| 3 | 对立力量 | 反派的力量 = 故事的力量 |
| 4 | 故事线 | 长篇 = 明暗交织（主线/暗线/支线） |
| 5 | 世界地图 | 地点即剧情 |
| 6 | 启程 | 不需要知道第 50 章写什么，只需要第一章的第一个画面 |

**Wizard 智能特性**：自适应深度、实时反馈（「帮我分析」按钮）、自然语言输入（非强制填表）、每步可跳过/可回退、进度条可见。

#### 5.4.3 老手 Wizard（5 步，精简版）

| 步骤 | 内容 |
|------|------|
| 0 | 摸底（选「老手」） |
| 1 | 世界观 + 平台选择（自动加载平台红线） |
| 2 | 角色（自然语言批量录入） |
| 3 | **故事线 + 大纲粘贴**（主线/暗线/支线；可选粘贴大纲文本，LLM 自动解析卷章结构） |
| 4 | 叙事规则（字数/文风/禁止项） |
| 完成 | 立项报告 → 直接开写 |

#### 5.4.4 三种工作节奏

| 模式 | 命令 | CONTEXT_REVIEW | CONTENT_REVIEW | PATCH_REVIEW | 适用场景 |
|------|------|:---:|:---:|:---:|------|
| 沉浸写作 | `/mode flow` | ⏭️ 跳过 | 🔒 停，等确认 | 🔒 停，等确认 | 日常稳定输出 |
| 审慎写作 | `/mode review` | 🔒 停，等确认 | 🔒 停，等确认 | 🔒 停，等确认 | 关键章节、转折点 |
| 规划模式 | `/mode plan` | — 不触发 | — 不触发 | — 不触发 | 休息日、回顾日 |

> **核心规则**：CONTENT_REVIEW 和 PATCH_REVIEW 在任何写作模式下都不能跳过——AI 写的内容必须人看过，图谱变更必须人确认过。

#### 5.4.5 六个仪表盘（管理模式）

| 仪表盘 | 功能 |
|--------|------|
| 项目总览 | 节点统计、卷章进度、待关注事项、快速入口 |
| 角色管理 | 列表 + 详情 + 原地编辑 + 关系网 + 出场记录 + 情绪历史 |
| 线索管理 | 列表 + 进度可视化 + 推进历史 + 交汇图 + 知情者 |
| 大纲/细纲 | 卷章结构 + 完成状态 + 计划 vs 实际 + 偏离标记 |
| 物品/金手指/马甲 | 统一管理，含类型/状态/持有者 |
| 叙事规则 | 平台红线 + 字数 + 文风 + 禁止项，可增删改 |

#### 5.4.6 自然语言操作

| 示例 | 系统行为 |
|------|---------|
| `李四什么状态` | 返回角色状态卡 |
| `查灭门案线索进展` | 返回线索详情 + 推进历史 |
| `王五的断魂刀在第 18 章被毁` | 自动更新 Item + 创建 Event + 更新关系 |
| `新建角色：铁匠老李，在京城开兵器铺` | LLM 解析 → 创建 Character + Location + 关系 |

#### 5.4.7 命令速查

| 命令 | 功能 |
|------|------|
| `/write N --pov 角色` | 发起写作 |
| `/dashboard` | 项目总览 |
| `/chars` / `/char 李四` | 角色列表 / 详情 |
| `/clues` / `/clue 灭门` | 线索列表 / 详情 |
| `/outline` | 大纲/细纲 |
| `/items` / `/cheats` / `/aliases` | 物品/金手指/马甲 |
| `/rules` | 叙事规则 |
| `/mode flow \| review \| plan` | 切换工作节奏 |
| `/history` | 变更历史 |
| `/undo` | 撤销最近更新 |
| `/export` | 导出全文/图谱 |

### 5.5 用户典型一天

#### 新手的一天

```
早上，第一次打开「小说工坊」：
  1. Wizard 第 0 步：系统问「你之前写过小说吗？」→ 选「从没写过」
  2. 系统：那就从头开始，我会告诉你每一步为什么重要。
  3. 第 1 步：跟着提示写出世界观「一个灵气稀薄的修仙界」
  4. 第 2 步：创建主角李四，系统帮我分析了「想要/缺陷/成长」三点
  5. 第 3 步：快速模式输入 3 个角色，系统自动创建
  6. 第 4 步：系统解释主线/暗线/支线的区别，我设了 2 条
  7. 第 5 步：快速输入 3 个地点
  8. 第 6 步：系统说「只需要第一章的第一个画面」，我写下：
     "李四在宗门大比报名处前，攥着皱巴巴的报名表"
  9. 系统：🎉 准备就绪，开始写第一章！

10. 点了「开始写」，系统展示了上下文摘要
11. 确认 → DeepSeek 开始一行行输出
12. 2500 字出来了！质检三绿一黄（发现新角色「张师兄」）
13. 点了通过 → 图谱自动更新完成
14. 系统复盘：「看起来你埋了玉佩的伏笔，下章要不要展开？」
15. 采纳建议 → /write 第2章 ...

一下午写了 3 章，每章之间系统自动记住一切。
我不需要回头翻设定，不需要担心角色前后矛盾。
```

#### 老手的一天

```
早上，打开系统：
  1. /mode flow → 沉浸写作模式
  2. /write 第20章 --pov 李四
  3. 上下文自动检索完毕，静默通过
  4. DeepSeek 开始流式输出...
  5. 写完。质检全绿，一个新增实体「国师」需要确认
  6. 通过 → 图谱 5 秒更新完毕
  7. 系统复盘：「灭门真相推进到 50%，计划追踪一致 ✅」
     「下章建议：暗室密函 + 国师追击」

  8. 采纳 → /write 第21章
  9. 中间喝了口咖啡，问了一句「王五什么状态」
     系统：王五[筑基后期·坚定·国师府前门·目标：追捕李四]
  10. 继续写...

一上午轻松 5 章。
每章之间我只做了两件事：看一眼方向对不对、结果合不合格。
图谱状态、线索进度、角色情绪——全自动跟踪。
```

#### 三种节奏无缝切换

```
沉浸写作 (/mode flow)：
  写写写，不停，每章只停一次确认环节
  → 适合日常稳定输出

审慎写作 (/mode review)：
  每步可审：上下文 → 生成 → 图谱更新
  → 适合关键章节、转折点

规划模式 (/mode plan)：
  不写，纯管理。查角色、改线索、调大纲、埋伏笔
  系统随时提醒：「玉佩已 18 章未提及」「王五 10 章未出场」
  → 适合休息日、回顾日
```

---

## 六、前端方案

### 6.1 总体布局（SmartAdmin 壳）

```
┌──────────────────────────────────────────────┐
│  顶栏：logo + 项目选择 + 用户头像              │
├────────┬─────────────────────────────────────┤
│ 侧边栏  │         内容区                       │
│        │                                     │
│ 项目总览 │    ┌───────────────────────────┐    │
│ 写作中心 │    │                           │    │
│ 世界资料 │    │     写作工作台（默认首页）   │    │
│ 图谱审阅 │    │     或 其他管理页面         │    │
│ 统计日志 │    │                           │    │
│ 项目设置 │    └───────────────────────────┘    │
│        │                                     │
└────────┴─────────────────────────────────────┘

写作工作台和图谱页面是两个独立菜单，用户可开两个浏览器窗口分别查看。
```

**侧边栏菜单约束**：最多两级，不做三级菜单。一级菜单代表用户要做的事，二级菜单代表具体页面。金手指、马甲、叙事规则直接显示出来，不藏在人物详情里，降低用户找入口的负担。

| 一级菜单 | 二级菜单 |
|----------|----------|
| 项目总览 | 无 |
| 写作中心 | 写作工作台、章节管理、大纲细纲、生成记录 |
| 世界资料 | 人物、地点、线索、物品、事件、金手指、马甲、叙事规则 |
| 图谱审阅 | 关系图谱、图谱变更、一致性检查 |
| 统计日志 | 写作统计、操作日志、模型用量 |
| 项目设置 | 基础设置、模型 Key |

菜单命名尽量用作者能听懂的词：

- `GraphPatch` 页面叫“图谱变更”。
- `Alias` 页面叫“马甲”。
- `Cheat` 页面叫“金手指”。
- `NarrativeRule` 页面叫“叙事规则”。
- `Item` 页面叫“物品”。

**顶栏项目切换**：

```
┌──────────────────────────────────────────────┐
│  📖 剑道独尊 ▼  ·  👤 用户                    │
└──────────────────────────────────────────────┘
```

- 点击项目名弹出下拉：列出用户所有项目（含状态「连载中/草稿」）+ `[+ 新建项目]`
- 切换后：消息流清空，加载新项目的章节分隔线，输入区 ready
- 项目信息存前端 store，URL 不带项目 ID

### 6.2 对话写作面板

> **菜单入口**：侧边栏「写作中心 / 写作工作台」。用户登录选项目后的默认首页。

#### 6.2.1 三段式结构

```
┌─────────────────────────────────────────────┐
│  顶栏：当前章节信息                           │
│  「第 20 章 · 暗室密函」  POV: 李四           │
├─────────────────────────────────────────────┤
│  消息流（可滚动）                             │
│  ┌──────────────────────────────────────┐    │
│  │ 用户指令 / 系统卡片 / AI 生成 / 查询结果 │   │
│  └──────────────────────────────────────┘    │
├─────────────────────────────────────────────┤
│  输入区                                     │
│  [/write 20 --pov 李四           ] [发送]    │
└─────────────────────────────────────────────┘
```

#### 6.2.2 消息类型（8 种）— 字段级规格

每种系统卡片都有固定的字段布局，以下逐一定义。

---

**类型 1：用户指令**

```
┌──────────────────────────────────────┐
│ 蓝色气泡（右对齐）                    │
│ /write 20 --pov 李四                 │
│ 14:32                                │
└──────────────────────────────────────┘
```

| 字段 | 值来源 | 样式 |
|------|--------|------|
| 指令文本 | 用户输入原文 | 白色文字，14px，蓝色背景 (`#1677ff`) |
| 时间戳 | `new Date()` | 灰色，12px，气泡右下角 |

---

**类型 2：AI 生成**

```
┌──────────────────────────────────────┐
│ 🤖 AI · 第 20 章「暗室密函」          │
│                                      │
│ 李四推开暗室的石门，一股腐霉味扑面而   │
│ 来。他借着微弱的夜明珠光芒，看清了四   │
│ 周——这是一个堆放卷宗的密室...         │
│ （流式逐字渲染，正在生成时末尾有光标）  │
│                                      │
│ 生成中...  ████████░░░░  1200 字      │  ← 仅生成中显示
│                                      │
│ ✅ 生成完毕 | 2350 字 | 用时 28 秒    │  ← 生成完显示
│                                      │
│ [保留并继续] [丢弃重来] [✏️ 编辑开头]   │  ← 仅「已停止」状态
└──────────────────────────────────────┘
```

| 字段 | 值来源 | 样式 |
|------|--------|------|
| 标题行 | `「第 N 章 · 章节标题」` | 灰底圆角标签，14px，气泡顶部 |
| 正文 | DeepSeek 流式 SSE | 白色底，15px，行高 1.8，首行缩进 2em |
| 进度条 | `已接收字数 / 预估 3000` | 蓝色渐变条，仅生成中显示 |
| 字数统计 | 后端 `content.length` | `2350 字` |
| 耗时 | `生成完成时间 - 开始时间` | `用时 28 秒` |
| 停止按钮 | `[⏹️ 停止生成]` 气泡右上方 | 红色文字按钮，仅生成中显示 |
| 停止后操作 | 三个按钮 | 灰色描边按钮，仅「已停止」状态 |

**流式渲染规则**：
- 后端 WebSocket 逐 token 推送，前端逐字追加
- 每 100ms 最多渲染一次（节流，防止高频 DOM 操作）
- 自动滚动到底（仅当用户未手动上翻时）
- 生成中光标：文本末尾有一个闪烁的 `|`

---

**类型 3：上下文摘要卡片**

```
┌──────────────────────────────────────────┐
│ 📖 上下文已就绪              14:32:05      │
│                                           │
│ 📘 卷：第二卷「京城风云」                   │
│ 📄 上章：第 19 章「暗室」                   │
│    李四从神秘老者处得知国师府有暗室...       │
│                                           │
│ 👤 出场角色（2 人）                        │
│    · 李四 [筑基期 · 坚定 · 国师府地下]      │
│      目标：获取证据 | 持有：密函            │
│    · 王五 [筑基后期 · 愤怒 · 京城]          │
│      目标：追捕李四 | 与李四：死敌(5)        │
│                                           │
│ 🧵 活跃线索（2 条）                        │
│    · 暗线·灭门真相 [██████░░░░] 50%       │
│    · 支线·宗门内斗 [███░░░░░░░] 20%       │
│                                           │
│ 📊 Token: 4800 / 6000  未截断              │
│                                           │
│ [📋 展开详情]  [✏️ 修改]  [✅ 确认生成]      │
└──────────────────────────────────────────┘
```

| 字段 | 值来源 | 样式 |
|------|--------|------|
| 卷信息 | `Volume.summary`（截取 50 字） | 灰底标签，左对齐 |
| 上章摘要 | `Chapter{number:N-1}.summary`（截取 80 字） | 灰色文字，13px |
| 角色状态卡 | `Character` + `CURRENTLY_AT` 关系 | 按 `role` 排序（主角→反派→配角），每人一行 |
| 角色行格式 | `名字 [境界 · 情绪 · 所在地]` | 粗体名字 + 方括号标签 |
| 线索进度条 | `Clue.revealLevel`（0.0~1.0） | 蓝色进度条 + 百分比 |
| Token 统计 | `tokenTotal + " / " + tokenBudget` | 绿色（<5000）/ 橙色（5000~6000）/ 红色（>6000） |
| 截断标记 | `truncated ? "已截断" : "未截断"` | 红色小标签（仅截断时） |
| 展开详情 | 点 `[📋 展开详情]` → 卡片内下拉展开完整 JSON | 只读 |
| 修改上下文 | 点 `[✏️ 修改]` → 右侧滑出 Drawer | — |
| 确认生成 | 点 `[✅ 确认生成]` → 进入 GENERATING | 主按钮，蓝色 |

**截断时的特殊显示**：
```
📊 Token: 7600 / 6000  ⚠️ 已截断（省略 2 条低优线索、1 个角色关系）
```
Token 数字变红，截断说明黄色背景。

---

**类型 4：质检结果卡片**

```
┌──────────────────────────────────────────┐
│ 🔍 自动质检                    14:32:33    │
│                                           │
│ ✅ 字数达标：2350 / 目标 2500±20%         │
│ ✅ 视角一致：全程李四视角                   │
│ ✅ 前章衔接：开篇承接第 19 章结尾           │
│ ⚠️ 新实体：国师（不在图谱中）              │
│                                           │
│ [✅ 通过并更新图谱]  [🔄 重写]              │
└──────────────────────────────────────────┘
```

| 字段 | 值来源 | 样式 |
|------|--------|------|
| 字数检查 | 实际字数 vs 目标字数 ±20% | ✅ 绿 / ⚠️ 黄（超出范围）/ ❌ 红（严重偏差） |
| 视角检查 | LLM 判断 | ✅ 绿 / ⚠️ 黄 |
| 衔接检查 | LLM 判断 | ✅ 绿 / ⚠️ 黄 |
| 新实体检查 | Cypher 对比图谱角色名 | ⚠️ 黄色，列出陌生名字 |
| 通过并生成变更 | 正文保存草稿，进入 EXTRACTING_PATCH / PATCH_REVIEW | 主按钮，蓝色 |
| 重写 | 输入修改要求 → 回 GENERATING | 次按钮，灰色 |

---

**类型 5：变更摘要卡片**

```
┌──────────────────────────────────────────┐
│ 🔄 图谱变更摘要                 14:32:38    │
│                                           │
│ 🧑 角色变更：                              │
│    李四：情绪 坚定→恐惧                    │
│    王五：目标→「追捕李四」                  │
│                                           │
│ 🆕 新增：                                  │
│    角色「国师」反派                        │
│    物品「密函」信物 · 持有者 李四           │
│                                           │
│ 🧵 线索推进：                              │
│    灭门案真相：30%→50%                     │
│                                            │
│ 🔗 关系变更：+3 条                          │
│                                            │
│ [✅ 确认更新]  [↩ 返回审阅]                 │
└──────────────────────────────────────────┘
```

| 字段 | 值来源 | 样式 |
|------|--------|------|
| 角色变更 | GraphPatch 中的角色类 operations | 绿色箭头 `→` 连接旧值新值 |
| 新增实体 | GraphPatch 中的 `CREATE_*` operations | 蓝色 `🆕` 标签 |
| 线索推进 | GraphPatch 中的 `ADVANCE_CLUE` operations | 进度百分比 `30%→50%` |
| 关系变更 | GraphPatch 关系统计 | `+N 条` |
| 确认更新 | 执行用户勾选的 GraphPatch，写 Neo4j + `graph_change_log` + 发布正文 | 主按钮 |
| 返回审阅 | 丢弃本次候选 GraphPatch，不改图谱，正文保留草稿并回到 CONTENT_REVIEW | 次按钮，灰色文字 |

---

**类型 6：复盘/方向卡片**

```
┌──────────────────────────────────────────┐
│ 📊 第 20 章复盘                 14:32:42   │
│                                           │
│ 本章发生了：                               │
│   · 李四潜入国师府暗室                     │
│   · 发现密函，证实国师参与灭门              │
│   · 被王五发现，逃脱                       │
│                                           │
│ 🧵 可能走向：                              │
│   ① 国师下令全城搜捕 → 主线                │
│   ② 密函中提及第三位知情者 → 暗线           │
│   ③ 师妹来京城寻李四 → 支线                │
│                                           │
│ 💡 建议：①+② 组合，京城戒严 + 秘密接头      │
│                                           │
│ [采纳建议]  [跳过]                         │
└──────────────────────────────────────────┘
```

| 字段 | 值来源 | 样式 |
|------|--------|------|
| 本章事件 | LLM 从 Chapter.summary 提炼 3 点 | 无序列表，加粗关键动词 |
| 可能走向 | LLM 生成 3 条 | 带箭头标签（主线/暗线/支线） |
| 建议 | LLM 推荐组合 | 黄色背景高亮 |
| 采纳 | 填入输入框 | 蓝色文字按钮 |
| 跳过 | 不填入 | 灰色文字按钮 |

---

**类型 7：主动提醒**

```
┌──────────────────────────────────────────┐
│ ⚠️ 伏笔「神秘玉佩」已 18 章未提及           │
│    埋于第 3 章，建议在近期章节安排呼应。     │
│    [查看伏笔详情]  [我知道了]               │
└──────────────────────────────────────────┘
```

| 字段 | 值来源 | 样式 |
|------|--------|------|
| 提醒文案 | 后端定时任务扫描 | 黄色背景，橙色边框，消息流中插入 |
| 查看详情 | 点后跳转线索管理页 | 链接按钮 |
| 我知道了 | 消除此条提醒 | 关闭按钮 |

---

**类型 8：查询结果卡片**

```
┌──────────────────────────────────────────┐
│ 🔍 查询：角色 李四              14:35:00   │
│                                           │
│ 👤 李四                                    │
│    境界：筑基中期                          │
│    情绪：恐惧                              │
│    所在地：国师府暗室                      │
│    当前目标：从暗室逃脱                     │
│    持有：密函                              │
│    金手指：万倍悟性                        │
└──────────────────────────────────────────┘
```

| 字段 | 值来源 | 样式 |
|------|--------|------|
| 查询标题 | LLM 解析的意图 | 灰色 `查询：{意图}` |
| 结构化数据 | Cypher 返回结果 | 表格式布局，左标签右值 |

---

**章节分隔线**

```
──── 📘 第二卷 · 第 20 章「暗室密函」· 2026-05-14 14:32 ────
```

- 全宽，灰色背景，居中文字
- 可点击 → 跳转章节管理页查看正文（MySQL `chapter_content`）
- 分隔线之间可折叠（超过 3 条历史分隔线自动折叠为一行 `「展开前 20 章」`）

#### 6.2.3 交互细节（字段级）

**修改上下文 — 侧边抽屉 (Drawer)**

点上下文卡片的 `[✏️ 修改]` → 右侧滑出 400px 宽抽屉，半透明遮罩覆盖消息流（遮罩可点关闭）。

抽屉内布局：
```
┌───────────────────────────┐
│ ✏️ 修改上下文     [关闭 ✕] │
├───────────────────────────┤
│                           │
│ 📘 本卷细纲                │
│ ┌───────────────────────┐ │
│ │ 李四前往京城追查...    │ │
│ └───────────────────────┘ │
│                           │
│ 📄 前章摘要                │
│ ┌───────────────────────┐ │
│ │ 李四从神秘老者处...    │ │
│ └───────────────────────┘ │
│                           │
│ 👤 角色状态                │
│ 李四                      │
│   情绪：[恐惧 ▼]          │  ← 下拉选择枚举值
│   位置：[国师府暗室 ✏️]    │  ← 可编辑文本框
│   目标：[从暗室逃脱 ✏️]    │
│                           │
│ 王五                      │
│   情绪：[愤怒 ▼]          │
│   位置：[京城 ✏️]          │
│   [移除此角色 ☐]          │
│                           │
│ 🧵 活跃线索                │
│   灭门案真相  [本章不推进 ☐]│
│   宗门内斗    [本章不推进 ☐]│
│                           │
│ 🎁 金手指/马甲             │
│   万倍悟性   [移除 ☐]      │
│                           │
│ ───────────────────────   │
│ ➕ 补充提示：              │
│ ┌───────────────────────┐ │
│ │ 国师府有禁制阵法...     │ │  ← 自由文本区
│ └───────────────────────┘ │
│                           │
│ [🔄 重新检索] [✅ 确认修改] │
└───────────────────────────┘
```

**修改后行为**：
- 点 `[✅ 确认修改]` → 抽屉关闭 → 上下文卡片原地刷新为新数据 + 重新计算 Token
- 点 `[🔄 重新检索]` → 用修改后的参数重新跑 RETRIEVING → 卡片刷新
- 若修改导致 Token 超过 8000 → 触发截断 → 卡片显示截断标记 ⚠️

**修改片段 — 内联编辑**

点 AI 生成气泡中的段落（任意 200 字以上连续文本触发）→

选中的段落高亮为浅蓝底，段落下方出现浮动操作条：
```
┌─ 操作条 ─────────────────────────────┐
│ [💾 保存修改] [🤖 AI 重写] [✕ 取消]   │
└──────────────────────────────────────┘
```

- `[💾 保存修改]`：段落变为可编辑（contenteditable），用户直接打字，保存后原地替换
- `[🤖 AI 重写]`：弹出一个 1 行的输入框 `「重写要求（可选）」` + `[确认重写]`，DeepSeek 仅重写该段
- AI 重写的段落出现后，用户可再次修改或保存

**取消生成**

生成中的气泡右上方有 `[⏹️ 停止]` 红色文字按钮（仅生成中可见）。

点停止 → WebSocket 发 `cancel` → 后端中断 DeepSeek 流。已生成部分保留：

```
┌──────────────────────────────────────┐
│ 🤖 AI · 第 20 章「暗室密函」          │
│                                      │
│ 李四推开暗室的石门，一股腐霉味扑面而   │
│ 来。他借着微弱的夜明珠光芒，看清了四   │
│ 周——这竟然是一个祭坛...               │
│                                      │
│ ⏹️ 已停止 · 1200 字                  │  ← 灰色标签
│                                      │
│ [保留并继续] [丢弃重来] [✏️ 编辑开头]   │
└──────────────────────────────────────┘
```

| 按钮 | 前端行为 | 后端行为 |
|------|---------|---------|
| [保留并继续] | 按钮消失，重新触发生成 | `continue=true`，续写已缓存文本 |
| [丢弃重来] | 清空气泡内容，重新触发生成 | 已缓存内容删除，全新生成 |
| [✏️ 编辑开头] | 段落变可编辑（同内联编辑），改完出现 [继续生成] | — |

**复盘建议采纳**

点复盘卡片 `[采纳建议]` → 按钮短暂变绿 ✓ → 输入区自动更新：

```
输入框：/write 21 --pov 李四
提示行：💡 方向：京城戒严 + 秘密接头（主线+暗线）
```

提示行样式：灰色 12px 斜体，前缀 💡。用户可编辑输入框和提示行，也可直接回车。

#### 6.2.4 消息流行为规则

**自动滚动**：
- 新消息到达时 → 自动滚到底
- 若用户手动上翻超过窗口高度 → 不再自动滚动，底部出现浮动按钮 `↓ 回到最新`（蓝色圆形，位于消息流右下角）
- 点击 `↓ 回到最新` → 滚到底，按钮消失

**旧消息折叠**：
- 超过 3 个章节分隔线的历史区域 → 自动折叠为一行
- 折叠行样式：`「──── 展开前 20 章 ────」`（灰色背景，居中，可点击展开）
- 展开后显示所有历史分隔线，点击某条分隔线可展开该章的上下文卡片（骨架态）

**生成中的 UI 状态**：
- 输入区禁用（灰色），placeholder 变为 `「生成中...」`
- 侧边栏菜单可正常操作（翻看其他页面不影响生成）
- 消息流可上翻查看历史

**重试失败提示**：
- DeepSeek 调用连续失败 3 次后，在 AI 气泡位置显示错误卡片：
  ```
  ┌──────────────────────────────────────────┐
  │ ❌ DeepSeek 调用失败（已重试 3 次）        │
  │    请稍后重试，或检查 API Key 是否有效。    │
  │                                           │
  │ [🔄 重试]  [⏭️ 跳过本章]                   │
  └──────────────────────────────────────────┘
  ```
- 红色背景，白色文字
- `[🔄 重试]` 重置重试计数，重新发起请求
- `[⏭️ 跳过本章]` 放弃本章，回到 IDLE

#### 6.2.4 输入区

**定位**：用户控制系统的唯一入口。不是聊天框、不是终端，而是「自然语言 + 命令」二合一的输入区。

**基础交互**：

| 规则 | 说明 |
|------|------|
| 回车发送 | 回车提交，不支持 `Shift+回车` 换行（单行输入） |
| placeholder | `「输入 /write 开始写作，或直接问我问题...」` |
| 禁用状态 | 生成中时输入区灰色，placeholder 变为 `「生成中...」` |

**三类输入**：

| 类型 | 示例 | 行为 |
|------|------|------|
| 结构化命令 | `/write 20 --pov 李四` | 精确执行 |
| 自然语言查询 | `李四什么状态`、`查灭门案进展` | LLM 解析意图 → 后端白名单查询 → 返回结果卡片 |
| 自然语言操作 | `王五断魂刀在第18章被毁` | LLM → 生成 GraphPatch → 用户确认 → 白名单执行器执行 |

> 自然语言操作必须经过**确认闸门**：展示变更摘要（与 PATCH_REVIEW 同模式），用户点确认后才执行。

**智能辅助**：

| 辅助 | 触发 | 说明 |
|------|------|------|
| 命令补全 | 输入 `/` | 100ms 后弹出浮层，匹配命令高亮，回车选择 |
| 角色名补全 | 输入角色名首字 | 从图谱角色列表缓存中匹配 |

**输入区不做的**：不上传文件、不多行编辑、不画图。长文本（如大纲粘贴）只在 Wizard 和管理页「📖 章节」处理，写作面板输入区不接受大段粘贴。

**查询失败的反馈**：

LLM 无法解析用户自然语言意图时，返回：
```
🤔 我没理解你的意思。试试：
  · /char 李四 → 查角色
  · /clue 灭门 → 查线索
  · 或重新描述你的问题
```

#### 6.2.5 空状态

```
没立项：
  「你还没有项目，去创建一个吧  [开始立项]」

有项目没章节：
  「你的故事准备好了，开始写第一章吧」

输入框预填：/write 1 --pov （主角名）
```

#### 6.2.6 消息流行为补充

**上下文卡片在静默模式下**：

`/mode silent` 时，上下文卡片依然展示但自动折叠为一行：
```
📖 上下文已就绪 | Token: 4800/6000 | 已自动确认  [展开详情]
```
不阻塞流程，但让用户知道检索已完成。若发生截断则自动展开显示截断警告。

**AI 生成气泡正文可选中复制**：

生成完成后，气泡内正文可鼠标选中、Ctrl+C 复制，支持全选（Ctrl+A 仅选中当前气泡正文）。

#### 6.2.7 WebSocket 稳定性

| 层面 | 机制 |
|------|------|
| 心跳 | 应用层 heartbeat，每 10 秒；服务端同时可使用原生 ping/pong |
| 前端断线检测 | 3 秒没收到任何消息 → 标记连接异常并发起重连 |
| 自动重连 | 指数退避：1s → 2s → 4s → 8s，最多 5 次 |
| 后端断线处理 | 生成中断开后等 30 秒；30 秒内重连则用 `generationJobId` 续接，超过 30 秒则中断模型流 |
| 内容恢复 | Redis 缓存已生成文本（10 分钟）用于快速补流；MySQL `chapter_generation_session` + `chapter_content` 用于长期恢复 |
| 用户取消 | 前端发 `{"action":"cancel"}`，后端中断 DeepSeek 流 |

**关闭页面 / 刷新后的恢复规则**：

| 关闭时状态 | 下次进入处理 |
|-----------|-------------|
| `GENERATING` | 30 秒内重连则续接；超过 30 秒保存为 `INTERRUPTED_DRAFT` |
| `CONTENT_REVIEW` | 恢复待审阅草稿卡片 |
| `PATCH_REVIEW` | 恢复待确认 GraphPatch，不重新抽取覆盖 |
| `APPLYING_PATCH` | 用 `operationBatchId` 检查 `graph_change_log`，幂等恢复发布状态或提示重试 |

生成中断恢复卡片：

```text
上次第 20 章生成中断，已保留 1200 字
[继续生成] [丢弃重来] [进入审阅]
```

### 6.3 知识图谱面板（独立页面）

菜单「图谱审阅 / 关系图谱」打开，使用 @antv/g6 渲染。

| 交互 | 行为 |
|------|------|
| 点击节点 | 右侧弹出详情卡片（角色状态 / 线索进度） |
| 拖拽节点 | 自由布局 |
| 图谱实时更新 | 章节完成后 WebSocket 推送变更，前端增量渲染 |

> 图谱面板和写作面板是两个独立页面，用户可开两个窗口并排查看。不做拖拽生成查询指令（P2 迭代）。

### 6.4 管理仪表盘

| 页面 | 技术 | 核心功能 |
|------|------|---------|
| 项目总览 | AntDV Card + Statistic | 节点统计、卷章进度、待关注事项 |
| 写作中心 / 章节管理 | AntDV Table + Collapse | 按卷分组的章列表，P0 只看+改标题，正文从 MySQL `chapter_content` 加载，P1 可修改 |
| 世界资料 / 人物 | AntDV Table + Drawer | 列表 + 详情抽屉 + 原地编辑 + 关系网 |
| 世界资料 / 地点 | AntDV Table + Drawer | 列表 + 详情 |
| 世界资料 / 线索 | AntDV Table + Progress | 列表 + 进度条 + 推进历史 + 知情者 |
| 世界资料 / 物品 | AntDV Table + Drawer | 列表 + 持有人 + 出场记录 |
| 世界资料 / 事件 | AntDV Table + Drawer | 列表 + 参与者 + 影响线索 |
| 世界资料 / 金手指 | AntDV Table + Drawer | 能力来源、限制、代价、绑定角色/物品 |
| 世界资料 / 马甲 | AntDV Table + Drawer | 真实身份、知道该马甲的人、首次暴露章节 |
| 世界资料 / 叙事规则 | AntDV Table + Drawer | 平台红线/字数/文风/禁止项 |
| 图谱审阅 / 图谱变更 | AntDV Table + Drawer | AI 建议修改的人物、线索、地点、关系等设定 |

### 6.5 Wizard 立项引导

独立的全屏流程页面（非侧边栏菜单），Steps + Form 布局。

| 特性 | 说明 |
|------|------|
| 新老分流 | 第 0 步一个开关：新手 / 老手 |
| 教学文字 | 每步解释「为什么重要」+ 经典作品范例 |
| 自然语言输入 | 支持快速模式（一段话批量创建角色） |
| 大纲粘贴 | 老手第 3 步可选粘贴大纲文本，LLM 解析 |
| 平台预设 | 第 1 步选平台 → 自动加载叙事规则红线 |
| 可跳过/回退 | 每步非强制，可回头修改 |
| 完成后跳转 | 立项报告 → 「开始写第一章」→ 跳转写作面板 |

### 6.6 用户设置页

侧边栏「项目设置 / 模型 Key」打开，三个 Tab 页签。

| Tab | 字段 | 来源 |
|-----|------|------|
| 账号 | 邮箱（只读）、昵称（可改）、修改密码 | SmartAdmin 自带，微调前端 |
| **API Key** | DeepSeek Key、通义千问 Key、连接状态、月用量统计 | P0，我们写，存 `user_api_key` 表 |
| 写作偏好 | 默认字数、默认视角、默认审阅模式 | P1 |

**API Key 安全处理**：
- 存储：后端 AES 加密存 MySQL
- 展示：默认 `sk-••••••••`，点 `👁️ 显示` 解密
- 测试连接：后端发小请求验证 Key 有效性（写一个字即停）

### 6.7 异常与兼容

| 场景 | 处理 |
|------|------|
| 404 / 500 | SmartAdmin 自带错误页，直接复用 |
| Neo4j 断连 | 写作面板顶部红色横幅 `「⚠️ 知识库连接异常，请稍后刷新重试」` |
| 页面加载 | 消息流骨架屏（灰色占位条），1 秒内完成 |
| 管理页加载 | AntDV Table 自带 loading |

**浏览器兼容**：支持 Chrome 90+、Edge 90+。Safari / Firefox 不主动测试但理论兼容。

---
