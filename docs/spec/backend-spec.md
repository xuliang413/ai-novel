# Spec: AI 小说写作外脑 — 后端数据模型 & 核心 API

## Objective

为 AI 小说写作系统构建完整的后端数据模型（MySQL 16 张表 + Neo4j 11 个节点类型 + 29 种关系）和核心 API（项目/角色/线索/地点/物品/金手指/马甲/事件/叙事规则/卷/章节 CRUD + AI 写作编排 + 图谱检索 + GraphPatch 审阅），全部通过自动化测试。

## Tech Stack

| 层级 | 技术 | 版本 |
|------|------|------|
| Java | JDK | 17 |
| 框架 | Spring Boot | 3.5.4 |
| ORM | MyBatis-Plus | 3.5.12 |
| 关系数据库 | MySQL | 8.x |
| 图数据库 | Neo4j (Java Driver) | 5.x |
| AI 编排 | LangChain4j (OpenAI module) | 1.0.0-beta1 |
| 鉴权 | Sa-Token + JWT | 1.44.0 |
| 加密 | AES (SmartAdmin ApiEncryptService) | — |
| WebSocket | spring-boot-starter-websocket | — |
| 测试 | JUnit 5 + Spring Boot Test + Mockito | — |
| 构建 | Maven | — |

## Commands

```
Build:    mvn clean compile -pl sa-admin -am
Test:     mvn test -pl sa-admin -am
Dev:      (IDE 启动 AdminApplication, profile=dev)
Lint:     (无集成 lint，依赖 IDE + 编译检查)
```

## Project Structure

```
saai-be/
├── pom.xml                          # 父 POM
├── sa-base/                         # SmartAdmin 基础模块（不改）
├── sa-admin/                        # 应用模块
│   ├── pom.xml
│   ├── src/main/java/net/lab1024/sa/admin/
│   │   ├── AdminApplication.java
│   │   └── module/business/novel/
│   │       ├── controller/          # REST 控制器
│   │       │   ├── NovelProjectController.java
│   │       │   ├── NovelAssetController.java
│   │       │   ├── NovelChapterController.java
│   │       │   ├── NovelWriteController.java
│   │       │   ├── NovelGraphController.java
│   │       │   ├── NovelDashboardController.java
│   │       │   └── UserApiKeyController.java
│   │       ├── service/             # 业务服务
│   │       │   ├── NovelProjectService.java
│   │       │   ├── NovelAssetService.java
│   │       │   ├── NovelChapterService.java
│   │       │   ├── NovelWriteService.java
│   │       │   ├── NovelGraphService.java
│   │       │   ├── NovelRetrieveService.java
│   │       │   ├── NovelLLMService.java
│   │       │   ├── NovelDashboardService.java
│   │       │   └── UserApiKeyService.java
│   │       ├── dao/                 # MyBatis-Plus Mapper
│   │       ├── domain/
│   │       │   ├── entity/          # MySQL 实体
│   │       │   ├── form/            # 请求 DTO
│   │       │   ├── vo/              # 响应 DTO
│   │       │   └── model/           # 内部领域模型
│   │       ├── constant/            # 枚举
│   │       ├── config/              # 配置 (Neo4j, LLM, WebSocket)
│   │       └── ws/                  # WebSocket Handler
│   ├── src/main/resources/
│   │   ├── dev/application.yaml
│   │   └── mapper/
│   └── src/test/java/net/lab1024/sa/admin/
│       └── module/business/novel/
│           ├── service/             # Service 层测试
│           ├── controller/          # Controller 层测试
│           └── dao/                 # DAO 层测试（可选）
├── db/
│   ├── update.sql                   # 完整 DDL
│   ├── neo4j/m0_init.cypher         # Neo4j 约束
│   └── novel_menu.sql               # 菜单 SQL
└── docs/
    ├── ideas/ai-novel-writing-brain.md  # 精炼一页纸
    └── spec/                        # 规范文档（本文件）
```

## Code Style

沿用现有 SmartAdmin 风格：

```java
// Entity —— 不加 @Data，用 Lombok @Data（已有惯例）
@Data
@TableName("t_novel_character")
public class NovelCharacterEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long characterId;
    private Long projectId;
    private String characterName;
    // ...
}

// Service —— 方法级注释，非 Javadoc
/**
 * 新建角色，写入 MySQL 并同步 Neo4j。
 */
@Transactional(rollbackFor = Exception.class)
public ResponseDTO<Long> add(NovelCharacterAddForm form) { ... }

// Controller —— 统一返回 ResponseDTO
@PostMapping("/add")
public ResponseDTO<Long> add(@RequestBody @Valid NovelCharacterAddForm form) { ... }

// 测试 —— AAA 模式
@Test
void shouldCreateCharacterAndSyncToNeo4j() {
    // Arrange
    // Act
    // Assert
}
```

**命名约定：**
- Entity: `NovelXxxEntity` → 表 `t_novel_xxx`
- Form（请求）: `NovelXxxAddForm`, `NovelXxxUpdateForm`, `NovelXxxQueryForm`
- VO（响应）: `NovelXxxVO`, `NovelXxxDetailVO`
- Model（内部）: `XxxModel`
- Enum: `NovelXxxEnum`
- Mapper: `NovelXxxDao`（SmartAdmin 惯例）
- Service: `NovelXxxService`
- Controller: `NovelXxxController`

## Testing Strategy

**框架：** JUnit 5 + Spring Boot Test + Mockito

**测试分层：**

| 层 | 范围 | 工具 | 覆盖率目标 |
|---|------|------|----------|
| Service 单元测试 | 单个 Service 方法，DAO mock | Mockito + @ExtendWith | 核心流程 100% |
| Service 集成测试 | Service + MySQL (H2/真实库) | @SpringBootTest | 关键路径 |
| Neo4j 测试 | GraphService + Mock Driver | Mockito mock `org.neo4j.driver.Driver` | GraphPatch 白名单 100% |
| Controller 测试 | API 端点 | @WebMvcTest | 核心 API |

**Neo4j 测试策略：**
- Registry/Service 层测试：mock `org.neo4j.driver.Driver`，验证 Cypher 模板和参数绑定
- GraphPatch 白名单测试：验证 6 种操作类型→正确 Cypher 拼接，非法操作→异常

**测试文件位置：**
```
src/test/java/net/lab1024/sa/admin/module/business/novel/
├── service/NovelProjectServiceTest.java
├── service/NovelAssetServiceTest.java
├── service/NovelGraphServiceTest.java          # 核心，白名单执行器测试
├── service/NovelRetrieveServiceTest.java
├── service/NovelWriteServiceTest.java
├── service/NovelLLMServiceTest.java
```

**关键测试用例清单：**

1. `NovelProjectServiceTest` — 创建项目、查询、归档、项目不存在异常
2. `NovelAssetServiceTest` — 角色/线索/地点/物品 CRUD，项目隔离
3. `NovelGraphServiceTest` — 6 种 GraphPatch 操作→Cypher 校验，非法操作→异常
4. `NovelRetrieveServiceTest` — 前章摘要查询、角色状态卡查询、线索进展查询
5. `NovelWriteServiceTest` — 无 Key 降级流程、Mock 生成、状态机流转
6. `NovelLLMServiceTest` — API Key 为空返回 null、正常生成、异常降级

## Boundaries

**Always do:**
- 所有 API 返回 `ResponseDTO<T>`
- MySQL 写入带 `projectId` + `createUserId`（SaaS 隔离）
- Neo4j 全部 Cypher 带 `projectId` 参数
- GraphPatch 仅通过白名单执行器写入 Neo4j
- 业务异常用 `ResponseDTO.userErrorParam()` 返回

**Ask first:**
- 新增 Maven 依赖
- 修改现有 SmartAdmin 框架代码
- 新增 Neo4j 关系类型（需更新 `NovelGraphRelationEnum`）
- 修改状态机流转逻辑

**Never do:**
- 直接在 Service 里拼接原始 Cypher（必须走 GraphService 白名单）
- 在 Neo4j 中用动态标签做项目隔离
- API Key 明文存储或明文传输
- 在 Controller 里放复杂业务逻辑

## MySQL 数据模型（完整 DDL）

### 保留现有表（增加字段）

#### t_novel_project（扩展）
```sql
-- 现有字段保留，新增以下字段：
ALTER TABLE t_novel_project ADD COLUMN world_setting text COMMENT '世界观概述';
ALTER TABLE t_novel_project ADD COLUMN platform varchar(50) DEFAULT NULL COMMENT '首发平台';
ALTER TABLE t_novel_project ADD COLUMN style varchar(500) DEFAULT NULL COMMENT '文风描述';
ALTER TABLE t_novel_project ADD COLUMN chapter_target_words int DEFAULT 3000 COMMENT '每章目标字数';
```

#### t_novel_character（扩展）
```sql
-- 现有字段保留，新增以下字段：
ALTER TABLE t_novel_character ADD COLUMN current_goal varchar(500) DEFAULT NULL COMMENT '当前目标';
ALTER TABLE t_novel_character ADD COLUMN goal_progress decimal(3,2) DEFAULT NULL COMMENT '目标完成度 0.00~1.00';
ALTER TABLE t_novel_character ADD COLUMN goal_status varchar(30) DEFAULT NULL COMMENT '目标状态: IN_PROGRESS/ACHIEVED/ABANDONED/DIVERTED';
ALTER TABLE t_novel_character ADD COLUMN current_emotion varchar(50) DEFAULT NULL COMMENT '当前主导情绪';
ALTER TABLE t_novel_character ADD COLUMN emotion_intensity int DEFAULT NULL COMMENT '情绪强度 1~5';
ALTER TABLE t_novel_character ADD COLUMN secondary_emotion varchar(50) DEFAULT NULL COMMENT '次生情绪';
ALTER TABLE t_novel_character ADD COLUMN power_level varchar(100) DEFAULT NULL COMMENT '战力/境界';
ALTER TABLE t_novel_character ADD COLUMN description text COMMENT '角色基础描述';
```

#### t_novel_clue（扩展）
```sql
-- 现有字段保留，新增以下字段：
ALTER TABLE t_novel_clue ADD COLUMN sub_type varchar(50) DEFAULT NULL COMMENT '子类型: PLOT_THREAD/FORESHADOWING';
ALTER TABLE t_novel_clue ADD COLUMN description text COMMENT '完整描述';
ALTER TABLE t_novel_clue ADD COLUMN reveal_level decimal(3,2) DEFAULT 0.00 COMMENT '揭露程度 0.00~1.00';
ALTER TABLE t_novel_clue ADD COLUMN current_stage varchar(200) DEFAULT NULL COMMENT '当前阶段';
ALTER TABLE t_novel_clue ADD COLUMN target_chapter int DEFAULT NULL COMMENT '计划收束章节';
ALTER TABLE t_novel_clue ADD COLUMN priority int DEFAULT 3 COMMENT '优先级 1~5';
ALTER TABLE t_novel_clue ADD COLUMN tone varchar(30) DEFAULT NULL COMMENT '情绪基调: TRAGIC/TENSE/ROMANTIC/HEROIC/MYSTERIOUS/DARK';
```

#### t_novel_chapter（扩展）
```sql
ALTER TABLE t_novel_chapter ADD COLUMN pov varchar(200) DEFAULT NULL COMMENT 'POV视角人物，多视角逗号分隔';
ALTER TABLE t_novel_chapter ADD COLUMN volume_id bigint DEFAULT NULL COMMENT '所属卷ID';
ALTER TABLE t_novel_chapter ADD COLUMN deleted_flag tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识';
```

### 新增表

#### t_novel_character_relation
```sql
CREATE TABLE t_novel_character_relation (
  relation_id bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  project_id bigint NOT NULL COMMENT '项目ID',
  character_id_from bigint NOT NULL COMMENT '起点角色ID',
  character_id_to bigint NOT NULL COMMENT '终点角色ID',
  relation_type varchar(30) NOT NULL COMMENT '关系类型: KNOWS/LOVES/HATES/IS_FAMILY_OF',
  relation_sub_type varchar(30) DEFAULT NULL COMMENT '子类型: FRIEND/ALLY/RIVAL 等(KNOWS); UNREQUITED/MUTUAL/PAST(LOVES); FATHER/MOTHER等(IS_FAMILY_OF)',
  intensity int DEFAULT NULL COMMENT '强度: HATES用1~5',
  deleted_flag tinyint(1) NOT NULL DEFAULT 0,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (relation_id),
  KEY idx_project_from (project_id, character_id_from),
  KEY idx_project_to (project_id, character_id_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI小说角色关系';
```

#### t_novel_character_location
```sql
CREATE TABLE t_novel_character_location (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint NOT NULL,
  character_id bigint NOT NULL,
  location_id bigint NOT NULL,
  chapter_no int DEFAULT NULL COMMENT '位置确立章节',
  deleted_flag tinyint(1) NOT NULL DEFAULT 0,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_character (project_id, character_id),
  KEY idx_location (location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI小说角色当前位置';
```

#### t_novel_character_cheat
```sql
CREATE TABLE t_novel_character_cheat (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint NOT NULL,
  character_id bigint NOT NULL,
  cheat_id bigint NOT NULL,
  acquired_in_chapter int DEFAULT NULL COMMENT '获得章节',
  deleted_flag tinyint(1) NOT NULL DEFAULT 0,
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_character_cheat (project_id, character_id, cheat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI小说角色金手指';
```

#### t_novel_chapter_appearance
```sql
CREATE TABLE t_novel_chapter_appearance (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint NOT NULL,
  chapter_id bigint NOT NULL,
  entity_type varchar(30) NOT NULL COMMENT '实体类型: CHARACTER/LOCATION/ITEM/EVENT',
  entity_id bigint NOT NULL COMMENT '实体ID',
  deleted_flag tinyint(1) NOT NULL DEFAULT 0,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_chapter (chapter_id),
  KEY idx_entity (entity_type, entity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI小说章节出场记录';
```

#### t_novel_clue_advance
```sql
CREATE TABLE t_novel_clue_advance (
  id bigint NOT NULL AUTO_INCREMENT,
  project_id bigint NOT NULL,
  clue_id bigint NOT NULL,
  chapter_id bigint NOT NULL,
  chapter_no int NOT NULL,
  advance_description text COMMENT '推进描述',
  reveal_level_after decimal(3,2) DEFAULT NULL COMMENT '推进后揭露程度',
  stage_after varchar(200) DEFAULT NULL COMMENT '推进后阶段',
  deleted_flag tinyint(1) NOT NULL DEFAULT 0,
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_clue (clue_id),
  KEY idx_chapter (chapter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI小说线索推进记录';
```

## Neo4j 数据模型

### 节点类型（11 类）

复用现有 [m0_init.cypher](file:///d:/novel/ai-novel/saai-be/db/neo4j/m0_init.cypher) 的 11 个 CONSTRAINT。节点属性详见 [AI小说技术方案.md §4.2](file:///d:/novel/ai-novel/AI小说技术方案.md)。

### 关系类型（29 类）

见 [AI小说技术方案.md §4.3](file:///d:/novel/ai-novel/AI小说技术方案.md)。

所有关系统一带 `projectId`、`createdAt`、`updatedAt` 属性。

### Cypher 模板（GraphPatch 白名单 6 种操作）

| 操作 | Cypher 模板 | 说明 |
|------|-----------|------|
| MERGE_NODE | `MERGE (n:Label {projectId: $pid, name: $name}) SET n += $props` | 创建/更新节点 |
| DELETE_NODE | `MATCH (n:Label {projectId: $pid, name: $name}) DETACH DELETE n` | 删除节点及关系 |
| MERGE_REL | `MATCH (a:A {projectId: $pid, name: $aName}) MATCH (b:B {projectId: $pid, name: $bName}) MERGE (a)-[r:REL {projectId: $pid}]->(b) SET r += $props` | 创建/更新关系 |
| DELETE_REL | `MATCH (a:A {projectId: $pid, name: $aName})-[r:REL {projectId: $pid}]->(b:B {projectId: $pid, name: $bName}) DELETE r` | 删除关系 |
| UPDATE_NODE_PROPS | `MATCH (n:Label {projectId: $pid, name: $name}) SET n += $props` | 部分更新节点属性 |
| ARCHIVE_NODE | `MATCH (n:Label {projectId: $pid, name: $name}) SET n.archived = true` | 归档节点 |

## 核心 API

### 前缀：`/novel`

### 1. 项目 (Project)

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| POST | `/novel/project/add` | 创建项目 | 现有需扩展 |
| POST | `/novel/project/query` | 分页查询 | 现有 |
| GET | `/novel/project/detail/{projectId}` | 项目详情 | 现有 |
| POST | `/novel/project/update` | 更新项目 | 现有需扩展 |
| POST | `/novel/project/archive/{projectId}` | 归档项目 | 现有 |

### 2. 资产管理 (Asset) — 统一入口

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| POST | `/novel/asset/character/add` | 添加角色 | 现有 |
| POST | `/novel/asset/character/query` | 查询角色列表 | 现有 |
| POST | `/novel/asset/character/update` | 更新角色 | 需扩展属性 |
| POST | `/novel/asset/location/add` | 添加地点 | 现有 |
| POST | `/novel/asset/location/query` | 查询地点列表 | 现有 |
| POST | `/novel/asset/clue/add` | 添加线索 | 现有 |
| POST | `/novel/asset/clue/query` | 查询线索列表 | 现有 |
| POST | `/novel/asset/clue/update` | 更新线索 | 需扩展属性 |
| POST | `/novel/asset/item/add` | 添加物品 | 现有 |
| POST | `/novel/asset/event/add` | 添加事件 | 现有 |
| POST | `/novel/asset/cheat/add` | 添加金手指 | 现有 |
| POST | `/novel/asset/alias/add` | 添加马甲 | 现有 |
| POST | `/novel/asset/narrative-rule/add` | 添加叙事规则 | 现有 |
| POST | `/novel/asset/volume/add` | 添加卷 | 现有 |
| POST | `/novel/asset/character-relation/add` | 添加角色关系 | **新增** |
| POST | `/novel/asset/character-relation/query` | 查询角色关系 | **新增** |

### 3. 章节 (Chapter)

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| POST | `/novel/chapter/query` | 分页查询章节 | 现有 |
| GET | `/novel/chapter/detail/{chapterId}` | 章节详情+正文 | **新增** |
| POST | `/novel/chapter/update` | 更新章节 | 现有 |
| GET | `/novel/chapter/next-no/{projectId}` | 获取下一章序号 | 现有 |

### 4. 写作 (Write)

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| POST | `/novel/write/start` | 开始写作（阻塞式） | 现有需重写 |
| WS | `/novel/write/stream` | 流式写作（WebSocket） | 现有需重写 |
| POST | `/novel/write/recover` | 恢复中断会话 | 现有 |
| POST | `/novel/write/content-review-pass` | 正文审阅通过 | 现有需重写 |
| POST | `/novel/write/patch-confirm` | 确认图谱变更 | **新增** |
| POST | `/novel/write/patch-back` | 图谱变更回退 | 现有 |
| POST | `/novel/write/undo` | 撤销最近变更 | 现有 |

### 5. 图谱查询 (Graph)

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/novel/graph/panel/{projectId}` | 图谱概览面板 | 现有 |
| GET | `/novel/graph/character-network/{projectId}/{characterName}` | 角色关系网 | **新增** |
| GET | `/novel/graph/clue-progress/{projectId}/{clueName}` | 线索推进历史 | **新增** |

### 6. 仪表盘 (Dashboard)

| 方法 | 路径 | 说明 | 状态 |
|------|------|------|------|
| GET | `/novel/dashboard/summary/{projectId}` | 项目统计摘要 | 现有 |

## 写作状态机

```
IDLE → GENERATING → CONTENT_REVIEW → EXTRACTING_PATCH → PATCH_REVIEW → DONE
  ↑         ↓              ↓                                  ↓
  └─── INTERRUPTED ←────── 用户取消/超时 ──────────────────── 用户返回
```

| 状态 | 含义 | 操作 |
|------|------|------|
| GENERATING | AI 正在生成正文 | 用户可取消 |
| CONTENT_REVIEW | 正文已保存，等待用户审阅 | 用户可通过/重写/返回 |
| EXTRACTING_PATCH | AI 正在抽取图谱变更 | 自动执行，不可取消 |
| PATCH_REVIEW | 图谱变更等待用户确认 | 用户可勾选确认/全部返回 |
| DONE | 章节发布，图谱更新完成 | 终端状态 |
| INTERRUPTED | 生成中断 | 用户可恢复 |

## Success Criteria

- [ ] 所有 16 张 MySQL 表通过 `update.sql` 一键创建
- [ ] 所有 11 个 Neo4j 约束通过 `m0_init.cypher` 一键创建
- [ ] 项目/角色/线索/地点/物品/金手指/马甲/事件/叙事规则/卷：每种实体至少通过 Create + Query + Update 三个 API 测试
- [ ] `NovelWriteService.start()` 无 AI Key 降级流程通过测试（Mock 生成→保存→状态机正确流转）
- [ ] `NovelGraphService` 6 种 GraphPatch 操作全部通过 Cypher 模板校验测试
- [ ] `NovelRetrieveService` 核心检索 Cypher 模板通过参数绑定测试
- [ ] `NovelLLMService` AI Key 为空/异常场景正确降级
- [ ] 角色关系 CRUD 通过测试
- [ ] 所有测试 `mvn test` 绿色

---

**确认 / 调整？**
