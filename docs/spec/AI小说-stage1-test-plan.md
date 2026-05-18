# AI小说第一阶段 — 集成验收测试计划

> **测试方法论**: E2E/集成验证（真实后端 + HTTP + MySQL/Neo4j 对账）
> **基础环境**: Spring Boot `sa-admin` (port 11024) | MySQL `saai` | Neo4j `bolt://localhost:7687` | DeepSeek `deepseek-v4-pro`
> **测试分层**: 9 个 Phase，按依赖图自底向上执行
> **当前状态**: MySQL 全部表为空，Neo4j 清空（0 节点 0 关系）

---

## 执行前清理步骤（幂等保护）

> 每条 Phase 都可以安全重跑——开始前自动清理该 Phase 可能产生的测试数据

```sql
-- Phase 3 重跑前: 删除 admin 创建的测试项目（同时清理 Neo4j）
DELETE FROM saai.t_novel_project WHERE name LIKE '验收测试小说%' AND create_user_id = 1;

-- Phase 4 重跑前: 删除测试项目下全部资产
DELETE FROM saai.t_novel_character WHERE project_id IN (SELECT id FROM saai.t_novel_project WHERE name LIKE '验收测试小说%');
DELETE FROM saai.t_novel_location WHERE project_id IN (SELECT id FROM saai.t_novel_project WHERE name LIKE '验收测试小说%');
-- ... 同理清理所有 t_novel_* 表

-- Phase 2 重跑: API Key save 幂等（uk_user_model_type 保证）
-- Phase 6 重跑: 删除测试项目下全部章节和会话
DELETE FROM saai.t_novel_chapter WHERE project_id IN (SELECT id FROM saai.t_novel_project WHERE name LIKE '验收测试小说%');
DELETE FROM saai.t_chapter_generation_session WHERE project_id IN (SELECT id FROM saai.t_novel_project WHERE name LIKE '验收测试小说%');
DELETE FROM saai.t_writing_log WHERE project_id IN (SELECT id FROM saai.t_novel_project WHERE name LIKE '验收测试小说%');
DELETE FROM saai.t_graph_change_log WHERE project_id IN (SELECT id FROM saai.t_novel_project WHERE name LIKE '验收测试小说%');
```

```cypher
// Neo4j 重跑清理: 删除测试项目所有节点和关系
MATCH (n {projectId: 999999}) DETACH DELETE n;  // 999999 替换为实际 PROJECT_ID
```

---

## 架构决策

| 决策 | 理由 |
|------|------|
| **先登录拿 Token，后续所有请求带 Token** | 模拟真实用户操作链路，验证 Sa-Token + JWT |
| **每步都做 MySQL + Neo4j 双写对账** | 方案核心诉求——双存储一致性是最不可妥协的质量红线 |
| **Phase 间严格顺序依赖** | Phase N 的输出是 Phase N+1 的输入 |
| **测试数据使用固定 login_name** | 用 `admin`（employee_id=1）作为主用户，用 `huke`（employee_id=2）做租户隔离验证 |
| **每个验收用例 = HTTP 调用 → MySQL 对账 SQL → Neo4j 对账 Cypher** | 三位一体，任一不通过即为失败 |
| **执行前自动清理测试数据** | 每条 Phase 可安全重跑，不产生重复数据 |

---

## 环境依赖图

```
Sa-Token 登录 (POST /login)
    │
    ├─→ UserApiKey 配置 (POST /novel/userApiKey/save)
    │       │
    │       └─→ AI 写作流程（Phase 6）
    │
    ├─→ Project 创建 (POST /novel/project/create)
    │       │
    │       ├─→ Asset CRUD（角色/地点/线索/物品/事件/金手指/马甲/叙事规则/卷/关系）
    │       ├─→ Volume 创建
    │       ├─→ Chapter + Outline 管理
    │       ├─→ Dashboard 统计
    │       ├─→ Graph 查询（概览/关系网/线索历史）
    │       └─→ AI 写作闭环（依赖 Project + Character + Location + Clue + Volume + NarrativeRule + API Key）
    │
    └─→ 多租户隔离（用第二个用户 huke 验证）
```

---

## Phase 1: 环境就绪验证 ✅（已完成）

### 1.1 MySQL 连接
- [x] MySQL MCP 可查询 `saai` 库
- [x] 22 张表结构完整（16 核心 + 6 辅助），全部 0 行

### 1.2 Neo4j 连接
- [x] Cypher 可执行，5 种 Label（Project/Character/Location/Clue/Chapter）
- [x] 已清理为 0 节点 0 关系

### 1.3 DeepSeek API Key
- [x] `sk-3d2ee505e35a4409b136fc53d4a94f59` → `deepseek-v4-pro` 可用
- [x] 返回 `reasoning_tokens` 正常

### 1.4 用户账号
| employee_id | login_name | administrator_flag | 测试角色 |
|-------------|-----------|-------------------|---------|
| 1 | admin | 1 | 主用户 |
| 2 | huke | 0 | 租户隔离验证 |

---

## Phase 2: 认证与 API Key 配置

> **目标**: 获取 Token，配置 DeepSeek Key，验证 Key 连接

### Task 2.1: admin 登录获取 Token

**Description**: 调用 `POST /login` 使用 admin 账号登录，获取 Sa-Token。

**HTTP**: `POST http://localhost:11024/login`
```json
{
  "loginName": "admin",
  "password": "admin123",
  "loginDevice": 1
}
```

**期望**:
- `ResponseDTO.ok == true`
- `LoginResultVO.token` 非空（后续所有请求 Header 携带 `Authorization: Bearer {token}`）

> ⚠️ admin 的密码是 argon2 哈希，如果 `admin123` 不对，需要查 SmartAdmin 默认密码或走重置流程。

---

### Task 2.2: 保存 DeepSeek API Key

**Description**: 调 `POST /novel/userApiKey/save` 为当前用户配置 DeepSeek CHAT 模型。

**HTTP**: `POST http://localhost:11024/novel/userApiKey/save`
```json
{
  "modelType": "CHAT",
  "url": "https://api.deepseek.com",
  "apiKey": "sk-3d2ee505e35a4409b136fc53d4a94f59",
  "modelName": "deepseek-v4-pro",
  "providerName": "DeepSeek",
  "temperature": 0.7,
  "maxTokens": 4096,
  "timeout": 60000
}
```

**MySQL 对账**:
```sql
SELECT model_type, model_name, provider_name, deleted_flag
FROM saai.t_user_api_key
WHERE user_id = 1;
```
- [ ] `model_type = 'CHAT'`, `model_name = 'deepseek-v4-pro'`, `deleted_flag = 0`
- [ ] `api_key` 字段存储的是 AES 加密后的密文（非明文 `sk-3d...`）

**接口返回对账**:
- [ ] `POST /novel/userApiKey/list` 返回的 `apiKey` 字段为脱敏格式 `sk-••••••••`

---

### Task 2.3: 测试 Key 连接性（强验证）

**Description**: 调 `POST /novel/userApiKey/test` 验证 Key 可用。Spec §3.1 要求"后端发短请求，写一个字即停"——不能只验 HTTP 200，必须确认后端确实发了 LLM 请求。

**HTTP**: `POST http://localhost:11024/novel/userApiKey/test?id={keyId}`
- [ ] `ResponseDTO.ok == true`
- [ ] `ResponseDTO.message` 包含"测试通过"或类似语义

**MySQL 对账（验证后端确实调了 LLM）**:
```sql
-- 测试请求可能写入 writing_log 或生成会话记录
SELECT COUNT(*) FROM saai.t_writing_log WHERE project_id = 0;
-- 如果测试不走 writing_log，则验证返回的 message 中包含耗时信息
```
- [ ] 有记录，或 message 包含耗时/Token 统计

**异常验证——无效 Key**:
使用无效 Key（如 `sk-00000000000000000000000000000000`）调用 `save` 后 `test`：
- [ ] 返回失败，message 明确提示认证错误

---

### Checkpoint: Phase 2 ✅
- [ ] admin Token 有效
- [ ] DeepSeek Key 已配置可用
- [ ] Key 脱敏正确

---

## Phase 3: 项目管理 CRUD

> **目标**: 验证项目创建/查询/编辑/归档全生命周期，确认双存储（MySQL + Neo4j）同步

### Task 3.1: 创建项目

**HTTP**: `POST http://localhost:11024/novel/project/create`
```json
{
  "name": "验收测试小说",
  "genre": "XIANXIA",
  "worldBuilding": "末法时代灵气稀薄，修仙者需依赖丹药修炼。天下分为三大洲：东胜神洲、西牛贺洲、南赡部洲。",
  "protagonistName": "林澈",
  "styleDescription": "白描克制冷峻，少用形容词，对话自然。",
  "platform": "QIDIAN",
  "targetTotalWords": 1000000,
  "targetChapterWords": 3000,
  "tokenBudget": 6000,
  "tokenHardLimit": 8000
}
```

**期望**: 返回 `projectId`（记为 `${PROJECT_ID}`）

**MySQL 对账**:
```sql
SELECT id, name, genre, status, create_user_id, deleted_flag, token_budget, token_hard_limit
FROM saai.t_novel_project WHERE id = ${PROJECT_ID};
```
- [ ] `name = '验收测试小说'`, `status = 'ACTIVE'`, `deleted_flag = 0`
- [ ] `create_user_id = 1`
- [ ] `token_budget = 6000`, `token_hard_limit = 8000`

**Neo4j 对账**:
```cypher
MATCH (p:Project {projectId: ${PROJECT_ID}})
RETURN p.name, p.genre, p.protagonist, p.status, p.archived
```
- [ ] `p.name = '验收测试小说'`, `p.genre = 'XIANXIA'`, `p.archived = false`

---

### Task 3.2: 分页查询项目

**HTTP**: `POST http://localhost:11024/novel/project/page/query`
```json
{ "pageNum": 1, "pageSize": 10 }
```
- [ ] `PageResult.data` 包含刚创建的项目
- [ ] `PageResult.totalItems >= 1`

---

### Task 3.3: 项目详情

**HTTP**: `GET http://localhost:11024/novel/project/get/${PROJECT_ID}`
- [ ] 返回完整 VO，所有字段正确

---

### Task 3.4: 编辑项目

**HTTP**: `POST http://localhost:11024/novel/project/update`
```json
{
  "id": ${PROJECT_ID},
  "name": "验收测试小说v2",
  "targetChapterWords": 4000
}
```

**MySQL 对账**:
- [ ] `name = '验收测试小说v2'`, `target_chapter_words = 4000`

**Neo4j 对账**:
- [ ] `p.name = '验收测试小说v2'`

---

### Task 3.5: 归档项目

**HTTP**: `GET http://localhost:11024/novel/project/archive/${PROJECT_ID}`

**MySQL 对账**:
- [ ] `deleted_flag = 1`

**Neo4j 对账**:
- [ ] `p.archived = true`

**再次调用归档** → 取消归档：
- [ ] `deleted_flag = 0`, `p.archived = false`

---

### Checkpoint: Phase 3 ✅
- [ ] 项目创建/查询/编辑/归档/取消归档全部通过
- [ ] MySQL + Neo4j 同步一致

---

### Task 3.6: Mini 隔离冒烟（提前验证多租户基础逻辑）

> Phase 3 建完项目后立即做一次快速隔离验证——不等到 Phase 8 才发现基础隔离 bug。

**流程**:
1. huke 登录获取 `tokenB`
2. `GET /novel/project/get/${PROJECT_ID}` with tokenB → **必须失败（403 或 404）**
3. `POST /novel/project/page/query` with tokenB → **data 中不含 admin 的项目**

**期望**: 隔离逻辑已生效，后续 Phase 无需担心数据污染。
- [ ] huke 看不到 admin 的项目

---

## Phase 4: 资产管理 CRUD（11 种实体）

> **策略**: 先创建 2 个角色作为基础依赖，再逐个创建其他实体。每个实体走 create → page/query → get → update → archive → unarchive 全流程。

### Task 4.1: 创建角色（主角 林澈）

**HTTP**: `POST http://localhost:11024/novel/asset/character/create`
```json
{
  "projectId": ${PROJECT_ID},
  "name": "林澈",
  "roleType": "PROTAGONIST",
  "description": "二十五岁，剑眉星目，性格坚韧寡言。出身寒微，幼年因灵根被废而遭受家族欺凌。后在废弃矿洞中获得神秘青铜碎片，开启逆天之路。",
  "currentStatus": "ACTIVE"
}
```

**MySQL 对账**:
```sql
SELECT id, name, role_type, project_id, deleted_flag FROM saai.t_novel_character WHERE project_id = ${PROJECT_ID};
```
- [ ] 记 `characterId_lc = id`

**Neo4j 对账**:
```cypher
MATCH (c:Character {projectId: ${PROJECT_ID}, characterId: ${characterId_lc}})
RETURN c.name, c.roleType, c.currentStatus, c.archived
```

---

### Task 4.2: 创建角色（反派 国师 萧寒渊）

**HTTP**: `POST http://localhost:11024/novel/asset/character/create`
```json
{
  "projectId": ${PROJECT_ID},
  "name": "萧寒渊",
  "roleType": "ANTAGONIST",
  "description": "国师，外表儒雅实则心狠手辣。掌控朝堂三十年，暗中搜集天下灵脉，实则是在寻找长生之秘。",
  "currentStatus": "ACTIVE"
}
```
- [ ] 记 `characterId_xhy = id`

---

### Task 4.3: 创建地点

**HTTP**: `POST http://localhost:11024/novel/asset/location/create`
```json
{
  "projectId": ${PROJECT_ID},
  "name": "国师府",
  "type": "BUILDING",
  "summary": "位于京城中心，占地百亩，地下有三层密室。"
}
```
- [ ] 记 `locationId_gf = id`，验证 MySQL + Neo4j 同步

```json
{
  "projectId": ${PROJECT_ID},
  "name": "东胜神洲",
  "type": "CITY",
  "summary": "天下第一大洲，灵气最盛之地。"
}
```
- [ ] 记 `locationId_ds = id`

---

### Task 4.3b: 地点层级关系验证（CONTAINS）

**Description**: 方案 §3.1 要求地点支持层级（CONTAINS 关系，最多 3 层）。创建子地点并通过 update 将父地点关联。

**HTTP**: `POST http://localhost:11024/novel/asset/location/create`
```json
{
  "projectId": ${PROJECT_ID},
  "name": "国师府地下密室",
  "type": "BUILDING",
  "summary": "国师府地下的三层密室，隐藏着长生之秘。"
}
```
- [ ] 记 `locationId_ms = id`

**HTTP**: `POST http://localhost:11024/novel/asset/location/update`
```json
{
  "id": ${locationId_ms},
  "projectId": ${PROJECT_ID},
  "parentId": ${locationId_gf}
}
```

**Neo4j 对账**:
```cypher
MATCH (child:Location {locationId: ${locationId_ms}})-[:CONTAINS]-(parent:Location {locationId: ${locationId_gf}})
RETURN child.name, parent.name
```
- [ ] 关系存在且方向正确：`(国师府)-[:CONTAINS]->(国师府地下密室)`

**三层层级验证**:
再创建一个第三级地点（如密室中的密室），验证深度可达 3 层：
```
东胜神洲 -[:CONTAINS]-> 国师府 -[:CONTAINS]-> 地下密室 -[:CONTAINS]-> 最深密室
```
- [ ] 三层 CONTAINS 链完整

---

### Task 4.4: 创建线索

**HTTP**: `POST http://localhost:11024/novel/asset/clue/create`
```json
{
  "projectId": ${PROJECT_ID},
  "name": "灭门真相",
  "type": "MAIN",
  "subType": "PLOT_THREAD",
  "description": "林澈家族十三年前被灭门的真相。表面是山贼所为，实则背后牵扯到朝堂势力。",
  "priority": 5,
  "targetChapter": 200,
  "tone": "MYSTERIOUS"
}
```
- [ ] 记 `clueId_mm = id`

```json
{
  "projectId": ${PROJECT_ID},
  "name": "青铜碎片的秘密",
  "type": "SUB",
  "subType": "FORESHADOWING",
  "description": "林澈获得的青铜碎片到底是什么？似乎和远古大能有某种联系。",
  "priority": 4,
  "tone": "MYSTERIOUS"
}
```
- [ ] 记 `clueId_qt = id`

---

### Task 4.5: 创建物品

**HTTP**: `POST http://localhost:11024/novel/asset/item/create`
```json
{
  "projectId": ${PROJECT_ID},
  "name": "神秘青铜碎片",
  "type": "TREASURE",
  "summary": "在废弃矿洞深处发现的半块青铜碎片，上面刻有远古符文，触之温热。"
}
```
- [ ] 记 `itemId = id`

---

### Task 4.6: 创建事件

**HTTP**: `POST http://localhost:11024/novel/asset/event/create`
```json
{
  "projectId": ${PROJECT_ID},
  "name": "林氏灭门",
  "summary": "十三年前，林澈满门在一夜之间被屠尽，仅林澈一人幸存。",
  "chapterOccurred": 0
}
```
- [ ] 记 `eventId = id`

---

### Task 4.7: 创建金手指

**HTTP**: `POST http://localhost:11024/novel/asset/cheat/create`
```json
{
  "projectId": ${PROJECT_ID},
  "name": "青铜碎片",
  "type": "ITEM_BOUND",
  "summary": "蕴含远古大能的残魂，可传授功法、炼丹术",
  "origin": "废弃矿洞深处",
  "limitation": "每次使用消耗宿主寿命，初期每日只能使用一次",
  "evolution": "碎片完整度提升后解锁更强能力"
}
```
- [ ] 记 `cheatId = id`

---

### Task 4.8: 创建马甲

**HTTP**: `POST http://localhost:11024/novel/asset/alias/create`
```json
{
  "projectId": ${PROJECT_ID},
  "name": "无名剑客",
  "type": "DISGUISE",
  "aliasContext": "林澈在京城活动时用的化名，戴青铜面具，不以真面目示人。",
  "summary": "京城神秘高手，来历不明，剑法诡异。"
}
```

---

### Task 4.9: 创建叙事规则

**HTTP**: `POST http://localhost:11024/novel/asset/narrative-rule/create`
```json
{
  "projectId": ${PROJECT_ID},
  "name": "字数约束",
  "content": "每章严格控制在 3000 字左右，允许 ±20% 浮动。",
  "priority": 5
}
```
```json
{
  "projectId": ${PROJECT_ID},
  "name": "文风统一",
  "content": "保持白描克制风格，不使用过于华丽的形容词，对话自然符合角色身份。",
  "priority": 4
}
```

---

### Task 4.10: 创建卷

**HTTP**: `POST http://localhost:11024/novel/asset/volume/create`
```json
{
  "projectId": ${PROJECT_ID},
  "number": 1,
  "title": "第一卷：废脉崛起",
  "summary": "林澈从废弃矿洞中获得神秘青铜碎片，开启逆天修行之路。初入江湖，结识伙伴，暗中调查林家灭门真相。"
}
```
- [ ] 记 `volumeId = id`

---

### Task 4.11: 创建角色关系（KNOWS 白名单 + HATES）

**Description**: 方案 §3.1 要求 KNOWS 的 relationType 走严格枚举校验，"不在白名单的类型拒绝写入"。需要创建第二个普通角色来做 KNOWS 验证。

**HTTP — HATES 关系**: `POST http://localhost:11024/novel/asset/character-relation/create`
```json
{
  "projectId": ${PROJECT_ID},
  "characterId": ${characterId_lc},
  "targetCharacterId": ${characterId_xhy},
  "relationType": "HATES",
  "hateIntensity": 5
}
```
- [ ] MySQL 写入成功
- [ ] Neo4j: `(林澈)-[:HATES {hateIntensity: 5}]->(萧寒渊)` 存在

**准备 KNOWS 关系目标角色**:
```json
{
  "projectId": ${PROJECT_ID},
  "name": "陈伯",
  "roleType": "SUPPORTING",
  "description": "林家老仆，十三年前灭门唯一的目击者，对林澈忠心耿耿。"
}
```
- [ ] 记 `characterId_cb = id`

**HTTP — KNOWS + 合法 relationType（FRIEND）**: 
```json
{
  "projectId": ${PROJECT_ID},
  "characterId": ${characterId_lc},
  "targetCharacterId": ${characterId_cb},
  "relationType": "KNOWS",
  "knowsRelationType": "FRIEND"
}
```
- [ ] ✅ 成功，MySQL 写入 `knows_relation_type = 'FRIEND'`

**HTTP — KNOWS + 合法 relationType（ALLY）**:
```json
{
  "projectId": ${PROJECT_ID},
  "characterId": ${characterId_lc},
  "targetCharacterId": ${characterId_cb},
  "relationType": "KNOWS",
  "knowsRelationType": "ALLY"
}
```
- [ ] ✅ 成功（或幂等覆盖前一条）

**HTTP — KNOWS + 非法 relationType（BROTHER）**:
```json
{
  "projectId": ${PROJECT_ID},
  "characterId": ${characterId_lc},
  "targetCharacterId": ${characterId_cb},
  "relationType": "KNOWS",
  "knowsRelationType": "BROTHER"
}
```
- [ ] ❌ 必须被拒绝，message 提示枚举值非法

**Neo4j 最终对账**:
```cypher
MATCH (a:Character {characterId: ${characterId_lc}})-[r:KNOWS]->(b:Character {characterId: ${characterId_cb}})
RETURN a.name, b.name, r.relationType
```
- [ ] 只有 FRIEND 或 ALLY，没有 BROTHER

---

### Task 4.12: 批量归档与取消归档

**Description**: 对每种实体各取一条，走 archive → unarchive 完整闭环。

| 实体 | 归档端点 | MySQL | Neo4j |
|------|---------|-------|-------|
| 角色 | `GET /novel/asset/character/archive/{id}` | `deleted_flag=1` | `archived=true` |
| 地点 | `GET /novel/asset/location/archive/{id}` | `deleted_flag=1` | `archived=true` |
| 线索 | `GET /novel/asset/clue/archive/{id}` | `deleted_flag=1` | `archived=true` |
| 物品 | `GET /novel/asset/item/archive/{id}` | `deleted_flag=1` | `archived=true` |
| 事件 | `GET /novel/asset/event/archive/{id}` | `deleted_flag=1` | `archived=true` |
| 金手指 | `GET /novel/asset/cheat/archive/{id}` | `deleted_flag=1` | `archived=true` |
| 马甲 | `GET /novel/asset/alias/archive/{id}` | `deleted_flag=1` | `archived=true` |
| 叙事规则 | `GET /novel/asset/narrative-rule/archive/{id}` | `deleted_flag=1` | `archived=true` |
| 卷 | `GET /novel/asset/volume/archive/{id}` | `deleted_flag=1` | `archived=true` |
| 关系 | `GET /novel/asset/character-relation/archive/{id}` | `deleted_flag=1` | Neo4j 关系 `active=false` 或删除 |

> 每条归档后立即调一次取消归档，确认所有状态恢复。

---

### Task 4.13: 可修改性边界验证

**Description**: 验证方案 §3.3——管理页只能改设定属性，不能改动态属性。覆盖角色、线索、物品、马甲全部四种有动态属性的实体。

| 操作 | 端点 | 期望 |
|------|------|------|
| 改角色 `name` | `POST /novel/asset/character/update` | ✅ 成功 |
| 改角色 `currentEmotion` | `POST /novel/asset/character/update` | ❌ 被忽略或拒绝 |
| 改线索 `name` | `POST /novel/asset/clue/update` | ✅ 成功 |
| 改线索 `revealLevel` | `POST /novel/asset/clue/update` | ❌ 被忽略或拒绝 |
| 改物品 `name` | `POST /novel/asset/item/update` | ✅ 成功 |
| 改物品 `quantity` | `POST /novel/asset/item/update` | ❌ 被忽略或拒绝 |
| 改马甲 `name` | `POST /novel/asset/alias/update` | ✅ 成功 |
| 改马甲 `revealed` | `POST /novel/asset/alias/update` | ❌ 被忽略或拒绝 |

---

### Task 4.14: 归档实体检索排除验证

**Description**: 验证方案 §3.4 核心语义——"归档后不出现在写作检索和候选中"。归档一个角色后启动写作，确认上下文不包含该角色；归档一条线索后，确认检索结果排除该线索。

**流程**:
1. 归档"陈伯"（刚创建的 SUPPORTING 角色）
2. 尝试启动第 5 章写作（无需成功，只需获取检索上下文）
3. 检查上下文快照中是否包含"陈伯"

**Neo4j 对账**:
```cypher
// 归档后角色仍存在但 archived=true
MATCH (c:Character {characterId: ${characterId_cb}})
RETURN c.name, c.archived
```
- [ ] `c.archived = true`

**上下文检索验证**（通过 `POST /novel/write/start` 或 `buildWritePrompt`）:
- [ ] 写作检索上下文不包含 `characterId_cb` 对应的"陈伯"
- [ ] 正常角色（林澈、萧寒渊）仍在上下文中

**取消归档后恢复**:
`GET /novel/asset/character/archive/{characterId_cb}` again → 取消归档
- [ ] 再次启动写作 → 上下文中恢复包含"陈伯"

---

### Checkpoint: Phase 4 ✅
- [ ] 11 种实体全部走通 create → get → update
- [ ] 每种走通 archive → unarchive
- [ ] MySQL + Neo4j 同步一致
- [ ] 地点三层 CONTAINS 层级链完整
- [ ] KNOWS relationType 白名单过滤生效
- [ ] 物品/马甲可修改性边界生效
- [ ] 归档实体不出现在检索候选中

---

## Phase 5: 章节管理与细纲

> **目标**: 验证章节分页查询、详情、编辑，以及细纲 CRUD

### Task 5.1: 创建细纲

**HTTP**: `POST http://localhost:11024/novel/chapter/outline/create`
```json
{
  "projectId": ${PROJECT_ID},
  "chapterNumber": 1,
  "sceneBeats": "1. 林澈在废弃矿洞深处发现青铜碎片\n2. 碎片入体，灵根重塑\n3. 矿洞坍塌，死里逃生",
  "summary": "林澈矿洞奇遇"
}
```
- [ ] 记 `outlineId_1 = id`

```json
{
  "projectId": ${PROJECT_ID},
  "chapterNumber": 2,
  "sceneBeats": "1. 林澈回到村庄发现灵力恢复\n2. 遭遇恶霸挑衅，初试身手\n3. 引来修真者注意",
  "summary": "初露锋芒"
}
```

---

### Task 5.2: 查询细纲

- `POST /novel/chapter/outline/page/query` → 按 chapterNumber 排序
- `GET /novel/chapter/outline/get/{outlineId}` → 详情

---

### Task 5.3: 编辑细纲

**HTTP**: `POST /novel/chapter/outline/update`
```json
{
  "id": ${outlineId_1},
  "sceneBeats": "1. 林澈在废弃矿洞深处发现青铜碎片\n2. 碎片入体，灵根重塑，突发异象\n3. 矿洞坍塌，神秘声音指引逃生",
  "summary": "林澈矿洞奇遇（修订）"
}
```

---

### Task 5.4: 细纲归档

`GET /novel/chapter/outline/archive/{outlineId}` → `deleted_flag=1`

---

### Task 5.5: 章节查询（写作前为空）

**HTTP**: `POST /novel/chapter/page/query`
```json
{ "projectId": ${PROJECT_ID}, "pageNum": 1, "pageSize": 10 }
```
- [ ] 当前结果为空（Phase 6 写作后才有）

---

### Checkpoint: Phase 5 ✅
- [ ] 细纲 CRUD + 归档全部通过
- [ ] 章节列表为空（等待 Phase 6 写入）

---

### Task 5.6: 章节关联卷验证（Phase 6 写完之后）

> ⚠️ 此 Task 在 Phase 6.3 之后执行——章节写入了再验证 volume_id 关联。

**HTTP**: `POST /novel/chapter/update`
```json
{
  "id": ${chapterId_1},
  "projectId": ${PROJECT_ID},
  "volumeId": ${volumeId}
}
```
- [ ] ✅ 写入成功

**MySQL 对账**:
```sql
SELECT volume_id FROM saai.t_novel_chapter WHERE id = ${chapterId_1};
```
- [ ] `volume_id = ${volumeId}`

**Neo4j 对账**:
```cypher
MATCH (v:Volume {volumeId: ${volumeId}})-[:CONTAINS]->(c:Chapter {chapterNumber: 1})
RETURN v.title, c.title
```
- [ ] 关系存在

**已发布章节编辑提示验证**:
编辑已发布的第 1 章标题：
```json
{
  "id": ${chapterId_1},
  "projectId": ${PROJECT_ID},
  "title": "第1章：矿洞奇遇（修订）"
}
```
- [ ] 返回成功，但 message 中包含"可能影响上下文"等提示信息

---

## Phase 6: AI 写作闭环（🔴 核心路径）

> **目标**: 验证方案 §3.2 完整写作流程——上下文检索 → AI 生成 → 正文审阅 → GraphPatch 抽取 → Patch 审阅 → 发布
> **前置**: 所有资产已创建、API Key 已配置、细纲已准备

### Task 6.1: 发起 AI 写作（第 1 章，有细纲）

**HTTP**: `POST http://localhost:11024/novel/write/start`
```json
{
  "projectId": ${PROJECT_ID},
  "chapterNumber": 1,
  "chapterGoal": "",
  "pov": "林澈"
}
```

**期望返回**:
- [ ] `NovelWriteSessionVO.id` 非空（记 `sessionId_1`）
- [ ] `NovelWriteSessionVO.status == 'CONTENT_REVIEW'`
- [ ] `NovelWriteSessionVO.content` 非空（AI 生成正文）
- [ ] `NovelWriteSessionVO.title` 非空
- [ ] `NovelWriteSessionVO.wordCount > 0`

**第一章节特殊逻辑验证（§3.2）**:
> 方案明确："第 1 章候选池为空时：拉项目所有角色和地点作为初始上下文"

- [ ] 通过 `POST /novel/write/start-stream` 获取 Prompt，检查上下文是否包含两个角色（林澈、萧寒渊）和两个地点（国师府、东胜神洲）
- [ ] `contextSnapshotJson`（session 记录中）或 Prompt 中可识别到项目级世界观概述（`worldBuilding` 内容）

**MySQL 对账**:
```sql
-- 章节草稿已写入
SELECT id, chapter_number, title, status, word_count
FROM saai.t_novel_chapter
WHERE project_id = ${PROJECT_ID} AND chapter_number = 1;
```
- [ ] `status = 'DRAFT'` 或 `'PENDING_GRAPH_CONFIRM'`
- [ ] `word_count > 0`

```sql
-- 会话记录已创建
SELECT id, status, chapter_number
FROM saai.t_chapter_generation_session
WHERE project_id = ${PROJECT_ID} AND chapter_number = 1;
```
- [ ] `status = 'CONTENT_REVIEW'`

---

### Task 6.2: 正文审阅通过 → 触发 GraphPatch 抽取

**HTTP**: `POST http://localhost:11024/novel/write/content-review-pass/${sessionId_1}`
```json
{
  "projectId": ${PROJECT_ID},
  "chapterNumber": 1
}
```

**期望返回**:
- [ ] `NovelWriteSessionVO.status == 'PATCH_REVIEW'`
- [ ] `NovelWriteSessionVO.graphPatches` 列表非空
- [ ] 每个 Patch 包含 `operationType`、`nodeLabel`、`entityId`、`oldValue`、`newValue`
- [ ] 高风险操作（角色关系/马甲暴露/物品消耗）的 `defaultChecked == false`

---

### Task 6.2b: Cypher 注入安全性验证（🔴 安全红线）

**Description**: 验证 GraphPatch 执行器对 `newValue`/`oldValue` 作参数化处理——含 Cypher 关键词的字符串值不能被当作可执行语句。这是 security-and-hardening 的"Never Do"级别：用户输入绝不能拼接到查询语言中。

**流程**：GraphPatch 审阅时，手动构造一个含恶意字符的 Patch 并确认：
```json
{
  "operationType": "UPDATE_PROPERTY",
  "nodeLabel": "Character",
  "entityId": ${characterId_lc},
  "propertyKey": "currentEmotion",
  "newValue": "'; MATCH (n) DETACH DELETE n; //"
}
```

**期望**:
- [ ] 该字符串作为 `currentEmotion` 的**字面值**写入 Neo4j（不做 Cypher 解析执行）
- [ ] Neo4j 中 `林澈.currentEmotion = "'; MATCH (n) DETACH DELETE n; //"`（原样存储）
- [ ] 其他节点和关系未被删除（证明注入未被执行）

**Neo4j 对账**:
```cypher
MATCH (c:Character {characterId: ${characterId_lc}})
RETURN c.currentEmotion
```
- [ ] 值为完整字符串，后续写回正常值清理

---

### Task 6.3: 审阅并确认 GraphPatch 发布章节

**HTTP**: `POST http://localhost:11024/novel/write/patch-confirm/${sessionId_1}`
```json
[ /* 用户勾选的 patches，使用上一步返回的列表，全部勾选 */ ]
```

**期望返回**:
- [ ] `NovelWriteSessionVO.status == 'COMPLETED'`

**MySQL 对账**:
```sql
-- 章节状态变为 PUBLISHED
SELECT status, title, summary, word_count
FROM saai.t_novel_chapter
WHERE project_id = ${PROJECT_ID} AND chapter_number = 1;
```
- [ ] `status = 'PUBLISHED'`

```sql
-- 写作日志已记录
SELECT word_count, prompt_tokens, completion_tokens, duration_ms, model_name
FROM saai.t_writing_log
WHERE project_id = ${PROJECT_ID} AND chapter_number = 1;
```
- [ ] 有记录，`word_count > 0`

```sql
-- 图谱变更日志已记录
SELECT status, chapter_number
FROM saai.t_graph_change_log
WHERE project_id = ${PROJECT_ID} AND chapter_number = 1;
```
- [ ] `status = 'APPLIED'`

**Neo4j 对账**:
```cypher
// Chapter 节点已创建
MATCH (c:Chapter {projectId: ${PROJECT_ID}, chapterNumber: 1})
RETURN c.title, c.summary, c.status, c.archived
```
- [ ] 有记录，对应 MySQL 章节

```cypher
// 角色状态随 Patch 更新（如情绪/目标可能有变化）
MATCH (c:Character {projectId: ${PROJECT_ID}})
RETURN c.name, c.currentEmotion, c.currentGoal, c.currentStatus
```
- [ ] 不全是空值（取决于 AI 抽取的 Patch）

```cypher
// 出场记录关系（如果 AI 抽取到了角色出场）
MATCH (c:Character)-[r:APPEARS_IN]->(ch:Chapter {projectId: ${PROJECT_ID}})
RETURN c.name, ch.chapterNumber
```
- [ ] 至少主角 `林澈` 出现在第 1 章

---

### Task 6.4: 发起 AI 写作（第 2 章，验证上下文带入）

**HTTP**: `POST http://localhost:11024/novel/write/start`
```json
{
  "projectId": ${PROJECT_ID},
  "chapterNumber": 2,
  "chapterGoal": "林澈回到村庄，发现身体发生了奇异变化",
  "pov": "林澈"
}
```

**期望**:
- [ ] 成功生成第 2 章
- [ ] 上下文检索中包含了第 1 章摘要、角色当前状态
- [ ] `sessionId_2` 可用

**走完审阅 + 发布流程**（同 6.2-6.3）

---

### Task 6.5: 撤销最近一次图谱变更

**HTTP**: `POST http://localhost:11024/novel/write/undo/${PROJECT_ID}`

**MySQL 对账**:
- [ ] `t_novel_chapter` 中第 2 章仍存在（不删正文）
- [ ] `t_writing_log` 中第 2 章日志仍存在（不删日志）
- [ ] `t_graph_change_log` 中对应记录 `status = 'UNDONE'`

**Neo4j 对账**:
- [ ] 第 2 章写入的关系被回退（`APPEARS_IN` 等关系减少）
- [ ] 第 1 章写入的关系保留（只撤最近一次）

---

### Task 6.6: 正文审阅退回

**流程**:
1. 再启动第 2 章写作 → `sessionId_2b`
2. `POST /novel/write/content-review-pass/${sessionId_2b}` → 进入 PATCH_REVIEW
3. `POST /novel/write/patch-back/${sessionId_2b}` → 回到 CONTENT_REVIEW
4. 再次 `POST /novel/write/content-review-pass` → 再次 PATCH_REVIEW
5. `POST /novel/write/patch-confirm` → COMPLETED

**期望**:
- [ ] `patch-back` 后状态正确回退
- [ ] 回退后再次确认能正常流转到 COMPLETED

---

### Task 6.7: 无细纲 + 用户方向写作（第 3 章）

**HTTP**: `POST http://localhost:11024/novel/write/start`
```json
{
  "projectId": ${PROJECT_ID},
  "chapterNumber": 3,
  "chapterGoal": "林澈在修炼中意外触发青铜碎片的第一个能力",
  "pov": "林澈"
}
```

**期望**:
- [ ] 无细纲但有 chapterGoal，AI 以 chapterGoal 为方向
- [ ] 正常生成 + 审阅 + 发布

---

### Task 6.7b: Token 预算截断触发上下文审阅（§3.2）

**Description**: 方案 §3.2 明确："默认跳过上下文审阅，仅当 Token 截断发生时才弹出给用户确认"。通过将项目 `tokenBudget` 调至极低值强制触发截断。

**流程**:
1. `POST /novel/project/update` 将 `tokenBudget` 设为 100、`tokenHardLimit` 设为 200
2. 发起第 4 章写作
3. 检查返回的 session 中 `NovelWriteSessionVO.status`

**期望**:
- [ ] 上下文确实被截断（session 的 `contextSnapshotJson` 中可见截断标记）
- [ ] 如果有上下文审阅状态，则 session 状态应在 `GENERATING` 前先到 `CONTEXT_REVIEW`
- [ ] 如果没有独立状态，至少 `promptSummary` 或日志中可识别截断信息

**恢复**:
1. `POST /novel/project/update` 将 `tokenBudget` 恢复为 6000、`tokenHardLimit` 恢复为 8000

---

### Task 6.8: WebSocket 流式写作（第 4 章）

**流程**:
1. `POST /novel/write/start-stream` → 返回 Prompt（System + User）
2. 连接 `ws://localhost:11024/ws/novel/write?token={token}`
3. 发送 `{"action":"start","projectId":${PROJECT_ID},"chapterNo":4,"chapterGoal":"林澈决定前往京城","pov":"林澈"}`
4. 接收逐 token 推送 `{"type":"token","data":"文"}`
5. 接收完成事件 `{"type":"complete","title":"...","sessionId":...}`
6. 通过 HTTP `POST /novel/write/session/{sessionId}` 查询会话状态

**期望**:
- [ ] WebSocket 连接成功
- [ ] 流式 token 逐个到达
- [ ] `complete` 事件携带正确的 sessionId
- [ ] 后续可通过 REST 接口继续审阅流程

---

### Task 6.9: 写后 DORMANT 伏笔提醒验证

**Description**: 方案 §3.2 流程图末尾："写后校验——Cypher 回溯 DORMANT 伏笔，提醒用户是否启动"。验证系统能否检测到长时间未推进的线索。

**流程**:
1. 调线索 update 将"青铜碎片的秘密"的 `clueStatus` 设为 DORMANT
2. 发起第 5 章写作（无细纲、无 chapterGoal）
3. 正常走完审阅 + 发布流程

**Neo4j 对账**:
```cypher
// 第 5 章发布后，检查是否有 DORMANT 线索被重新标记为 ACTIVE
MATCH (c:Clue {projectId: ${PROJECT_ID}})
RETURN c.name, c.clueStatus, c.type
```
- [ ] 如果系统实现了 DORMANT 回溯，则应有日志或提醒

**MySQL 对账**:
```sql
-- writer 可能更新线索的 last_alerted_chapter
SELECT name, clue_status, last_alerted_chapter
FROM saai.t_novel_clue WHERE project_id = ${PROJECT_ID};
```
- [ ] 有合理的值（不做强判定——取决于实现是否已包含此功能）

---

### Checkpoint: Phase 6 ✅
- [ ] 写作状态机 6 状态全部正确流转
- [ ] 正文生成落 MySQL、GraphPatch 落 Neo4j
- [ ] 撤销只撤图谱不删正文日志
- [ ] 退回正确回退状态
- [ ] WebSocket 流式推送正常
- [ ] Cypher 注入防护有效（Task 6.2b）
- [ ] 第一章节上下文正确注入全部资产（Task 6.1）
- [ ] Token 截断可触发上下文审阅（Task 6.7b）

---

## Phase 7: 图谱查询与仪表盘

> **目标**: 验证图谱概览/角色关系网/线索推进历史/仪表盘统计

### Task 7.1: 图谱概览

**HTTP**: `GET http://localhost:11024/novel/graph/overview/${PROJECT_ID}`

**期望**:
- [ ] `NovelGraphOverviewVO` 包含各节点类型计数
- [ ] `characterCount >= 2`, `chapterCount >= 4`, `locationCount >= 2`, `clueCount >= 2`
- [ ] 关系统计非空

---

### Task 7.2: 角色关系网

**HTTP**: `GET http://localhost:11024/novel/graph/character-network/${PROJECT_ID}`

**期望**:
- [ ] `nodes` 包含所有角色
- [ ] `edges` 包含 `HATES` 关系（林澈 → 萧寒渊）

---

### Task 7.3: 线索推进历史

**HTTP**: `GET http://localhost:11024/novel/graph/clue-history/${PROJECT_ID}/${clueId_mm}`

**期望**:
- [ ] `NovelClueHistoryVO` 返回该线索被哪些章节推进的时间线

---

### Task 7.4: 仪表盘

**HTTP**: `GET http://localhost:11024/novel/dashboard/${PROJECT_ID}`

**MySQL 对账**:
```sql
-- 手动 COUNT 验证仪表盘数字
SELECT COUNT(*) FROM saai.t_novel_chapter WHERE project_id = ${PROJECT_ID};
SELECT COUNT(*) FROM saai.t_novel_character WHERE project_id = ${PROJECT_ID} AND deleted_flag = 0;
SELECT COUNT(*) FROM saai.t_novel_clue WHERE project_id = ${PROJECT_ID} AND deleted_flag = 0;
```

**期望**:
- [ ] `NovelDashboardVO.chapterCount = 4`, `characterCount = 2`
- [ ] `totalWords = 前 4 章字数之和`

---

### Checkpoint: Phase 7 ✅
- [ ] 图谱三种查询正常
- [ ] 仪表盘数字与 MySQL 手动 COUNT 一致

---

## Phase 8: 多租户安全隔离（🔴 不可妥协红线）

> **目标**: 验证用户 A 无法访问用户 B 的任何数据

### Task 8.1: huke 登录

**HTTP**: `POST /login` with `loginName="huke"`, `password="admin123"`, `loginDevice=1`
- [ ] 获取 Token（记为 `tokenB`）

### Task 8.2: huke 看不到 admin 的项目

| 测试 | HTTP | 期望 |
|------|------|------|
| 列表查询 | `POST /novel/project/page/query` with tokenB | data 为空或不包含 admin 的项目 |
| 详情查询 | `GET /novel/project/get/${PROJECT_ID}` with tokenB | 403 或 404 |
| 角色查询 | `POST /novel/asset/character/page/query` {projectId: ${PROJECT_ID}} with tokenB | 空结果或错误 |
| 图谱概览 | `GET /novel/graph/overview/${PROJECT_ID}` with tokenB | 返回错误 |
| 图谱角色网 | `GET /novel/graph/character-network/${PROJECT_ID}` with tokenB | 返回错误 |
| 线索历史 | `GET /novel/graph/clue-history/${PROJECT_ID}/${clueId}` with tokenB | 返回错误 |
| 仪表盘 | `GET /novel/dashboard/${PROJECT_ID}` with tokenB | 返回错误 |
| 章节查询 | `POST /novel/chapter/page/query` {projectId: ${PROJECT_ID}} with tokenB | 空结果或错误 |
| 写作启动 | `POST /novel/write/start` {projectId: ${PROJECT_ID}} with tokenB | 返回错误 |

### Task 8.3: 无 Token 跨界

| 测试 | HTTP | 期望 |
|------|------|------|
| 不带 Token 访问 | `GET /novel/project/get/${PROJECT_ID}` | 401 |

### Task 8.4: 水平越权——同用户不同项目

**Description**: 验证 "admin 用项目 A 的 sessionId/assetId 操作项目 B" 被拒绝。这是 security-and-hardening 的核心模式——资源归属校验不仅跨用户，也要跨项目。

**准备——admin 创建第二个项目**:
`POST /novel/project/create` { name: "第二个测试项目", genre: "URBAN", ... }
- [ ] 记 `${PROJECT_ID_2}`

**测试矩阵**:
| 测试 | HTTP | 期望 |
|------|------|------|
| 用项目 A 的 chapterId 在项目 B 的查询中 | `POST /novel/chapter/page/query` {projectId: ${PROJECT_ID_2}} — data 中不应包含项目 A 的章节 | 空或不含项目 A 章节 |
| 用项目 A 的 sessionId 操作项目 B | `POST /novel/write/content-review-pass/{sessionId_1}` but with projectId=${PROJECT_ID_2} | 返回错误 |
| 用项目 A 的 characterId 在项目 B 上下文中 | `GET /novel/asset/character/get/{characterId_lc}` — 如果后端按 characterId 查询时不做 projectId 校验 | 应返回错误或按 projectId 过滤 |

**Neo4j 对账**:
```cypher
// 两个项目在 Neo4j 中完全隔离
MATCH (n:Project) RETURN n.projectId, n.name
```
- [ ] 两个 Project 节点各含各自的 projectId，无交叉引用

### Checkpoint: Phase 8 ✅
- [ ] 所有跨界访问全部被拦截
- [ ] 这是不可妥协的红线——任一失败 → 阻塞发布

---

## Phase 9: 降级与异常路径

> **目标**: 验证无 Key 降级、错误状态恢复

### Task 9.1: 无 Key 时 Mock 降级

**流程**:
1. huke 登录
2. huke 创建自己的项目（不配 API Key）
3. `POST /novel/write/start` → 正文为占位文本，状态机正常流转

**期望**:
- [ ] 不抛异常
- [ ] 正文内容为明显的 Mock 标记（如 `[Mock] 未配置 LLM API Key...`）
- [ ] 状态机仍能走到 CONTENT_REVIEW → PATCH_REVIEW → COMPLETED

---

### Task 9.2: 同一章节并发写作互斥

**流程**:
1. 启动第 5 章写作 → 获取 sessionId
2. 在 CONTENT_REVIEW 期间，再次启动第 5 章写作

**期望**:
- [ ] 第二次启动被拒绝（提示该章节有活跃会话）

---

### Task 9.3: 错误信息安全——不泄露内部细节

**Description**: 验证 401/403/500 响应不暴露 stack trace、内部类名、SQL 语句。这是 security-and-hardening 的 "Never Do" 级别。

**测试矩阵**:
| 场景 | HTTP | 期望 |
|------|------|------|
| 401 无 Token | `GET /novel/project/get/99999` 不带 Header | `message` 不含异常类名、不含 SQL |
| 403 无权限 | `GET /novel/graph/overview/${PROJECT_ID}` 用 tokenB | `message` 不含异常类名、不含内部路径 |
| 500 触发（不存在的资源） | `GET /novel/asset/character/get/99999999` | `message` 可读但不含 stack trace |
| API Key 测试无效 Key | Task 2.3 已覆盖 | `message` 不含原始 API Key 明文 |

- [ ] 所有错误响应的 `message` 字段非空且可读
- [ ] 所有错误响应的 `message` 不包含 `Exception`、`StackTrace`、`at com.`、SQL 关键字
- [ ] ResponseDTO 信封格式一致

---

### Task 9.4: 健康检查

**Description**: 验证后端基本可用性——Spring Boot Actuator 或等价端点。

**HTTP**: `GET http://localhost:11024/actuator/health`
- [ ] 返回 200 或 `{"status":"UP"}`
- [ ] 如果 actuator 未启用，尝试 `GET http://localhost:11024/` 或 `GET /login/getLoginInfo`（不依赖认证）

---

### Checkpoint: Phase 9 ✅
- [ ] 无 Key 降级正常
- [ ] 单章节互斥生效
- [ ] 错误信息不泄露内部细节
- [ ] 健康检查通过

---

## 整体交付判定（CI/CD 门禁）

### 红线（任一失败 → 阻塞发布）
1. **跨租户数据泄露** — Phase 8 全部用例通过
2. **Neo4j 写入绕过归属校验** — GraphPatch 确认时需验证 projectId
3. **状态机卡死** — Phase 6 全部状态转换通过
4. **API Key 明文泄露** — 返回给前端的 Key 必须脱敏（Task 2.2）
5. **归档实体出现在检索中** — Phase 4 归档后不出现在候选（Task 4.14）
6. **Cypher 注入** — 恶意字符串被当作字面值存储而非可执行语句（Task 6.2b）
7. **错误信息泄露内部细节** — 401/403/500 不含 stack trace / 类名 / SQL（Task 9.3）
8. **水平越权** — 同用户不同项目间资源不可互访（Task 8.4）

### 可观测性门禁

| 检查项 | 验证方式 | 对应 Task |
|--------|---------|----------|
| 健康检查 | `GET /actuator/health` → 200 | Task 9.4 |
| 写作日志完整性 | `t_writing_log` 含 wordCount/promptTokens/completionTokens/durationMs/modelName | Task 6.3 |
| 图谱变更可追溯 | `t_graph_change_log` 含 patchJson/inversePatchJson/status | Task 6.3 |
| 错误信封一致性 | 所有 `ResponseDTO` 的 `message` 可读且格式统一 | Task 9.3 |

### 覆盖率目标

| 覆盖维度 | 目标 | 对应 Phase |
|----------|------|-----------|
| 实体 CRUD | 11 种 × 5 操作 = 55 条 | Phase 4 |
| 写作状态机 | 6 状态全路径 | Phase 6 |
| 安全—垂直隔离 | 10+ 条跨界用例 + 3 次隔离验证 | Phase 3.6 + Phase 8 |
| 安全—水平越权 | 同用户跨项目访问拦截 | Phase 8.4 |
| 安全—Cypher 注入 | 恶意字符串参数化 | Phase 6.2b |
| 安全—错误信息安全 | 不泄露 stack trace/类名 | Phase 9.3 |
| 降级路径 | 无 Key 走通 1 次 | Phase 9 |
| 可修改性边界 | 4 种实体 × 设定/动态 | Phase 4.13 |
| KNOWS 白名单 | 合法 2 条 + 非法 1 条 | Phase 4.11 |
| 地点层级 | CONTAINS 3 层 | Phase 4.3b |
| 归档检索排除 | 1 角色归档后验证上下文 | Phase 4.14 |
| 写后伏笔提醒 | DORMANT 回溯 | Phase 6.9 |
| API Key 测试 | 有效 Key + 无效 Key | Phase 2.3 |
| 章节卷关联 | volume_id + 已发布编辑提示 | Phase 5.6 |
| Token 截断 | 极限低预算触发截断审阅 | Phase 6.7b |
| 第一章节上下文 | 空候选池→拉全部资产 | Phase 6.1 |
| 健康检查 | /actuator/health → 200 | Phase 9.4 |

### P1 未做项（不加入验收）
| 功能 | 原因 |
|------|------|
| Wizard 立项引导 | P1 |
| 写后复盘/方向建议 | P1 |
| `/recover` 恢复中断会话 | P1 |
| 断线重连 | P1 |
| 六个仪表盘完整面板 | P1 |
| 向量/Rerank 检索 | 用户未配置 Embedding/Rerank Key |

---

## 执行约定

1. **每完成一个 Task 报进度**：Task N 通过 ✅ / 失败 ❌（附错误详情）
2. **失败即停**：Phase 8（安全红线）任何一条失败立即停止，先修代码
3. **双存储对账为强制项**：不做对账的 Task 不算通过
4. **Cleanup 不删数据**：测试数据用于后续回归验证，不主动清理
