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
