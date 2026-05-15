// M0 AI 小说知识图谱初始化脚本。
// 作用：为核心节点建立唯一约束，避免同一项目下重复写入同一个业务对象。
// 说明：图谱标签只表达实体类型，项目归属统一通过 projectId 属性表达。

CREATE CONSTRAINT novel_project_project_id IF NOT EXISTS
FOR (p:Project) REQUIRE p.projectId IS UNIQUE;

CREATE CONSTRAINT novel_character_project_name IF NOT EXISTS
FOR (c:Character) REQUIRE (c.projectId, c.name) IS UNIQUE;

CREATE CONSTRAINT novel_location_project_name IF NOT EXISTS
FOR (l:Location) REQUIRE (l.projectId, l.name) IS UNIQUE;

CREATE CONSTRAINT novel_clue_project_name IF NOT EXISTS
FOR (c:Clue) REQUIRE (c.projectId, c.name) IS UNIQUE;

CREATE CONSTRAINT novel_chapter_project_number IF NOT EXISTS
FOR (c:Chapter) REQUIRE (c.projectId, c.number) IS UNIQUE;

CREATE CONSTRAINT novel_volume_project_number IF NOT EXISTS
FOR (v:Volume) REQUIRE (v.projectId, v.number) IS UNIQUE;

CREATE CONSTRAINT novel_item_project_name IF NOT EXISTS
FOR (i:Item) REQUIRE (i.projectId, i.name) IS UNIQUE;

CREATE CONSTRAINT novel_event_project_name IF NOT EXISTS
FOR (e:Event) REQUIRE (e.projectId, e.name) IS UNIQUE;

CREATE CONSTRAINT novel_cheat_project_name IF NOT EXISTS
FOR (c:Cheat) REQUIRE (c.projectId, c.name) IS UNIQUE;

CREATE CONSTRAINT novel_alias_project_name IF NOT EXISTS
FOR (a:Alias) REQUIRE (a.projectId, a.name) IS UNIQUE;

CREATE CONSTRAINT novel_rule_project_name IF NOT EXISTS
FOR (r:NarrativeRule) REQUIRE (r.projectId, r.name) IS UNIQUE;
