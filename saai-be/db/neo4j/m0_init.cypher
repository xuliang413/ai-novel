// ============================================================
// AI小说第一阶段 - Neo4j 初始化约束
// 覆盖技术方案第四章所有实体节点
// 11 个 CONSTRAINT: Project/Volume/Chapter/Character/Location/Clue/Item/Event/Cheat/Alias/NarrativeRule
// 可重复执行
// ============================================================

// 节点唯一性约束: 每个projectId下每种实体都有业务唯一ID

CREATE CONSTRAINT unique_project IF NOT EXISTS
FOR (n:Project) REQUIRE n.projectId IS UNIQUE;

CREATE CONSTRAINT unique_volume IF NOT EXISTS
FOR (n:Volume) REQUIRE (n.projectId, n.volumeId) IS NODE KEY;

CREATE CONSTRAINT unique_chapter IF NOT EXISTS
FOR (n:Chapter) REQUIRE (n.projectId, n.chapterNumber) IS NODE KEY;

CREATE CONSTRAINT unique_character IF NOT EXISTS
FOR (n:Character) REQUIRE (n.projectId, n.characterId) IS NODE KEY;

CREATE CONSTRAINT unique_location IF NOT EXISTS
FOR (n:Location) REQUIRE (n.projectId, n.locationId) IS NODE KEY;

CREATE CONSTRAINT unique_clue IF NOT EXISTS
FOR (n:Clue) REQUIRE (n.projectId, n.clueId) IS NODE KEY;

CREATE CONSTRAINT unique_item IF NOT EXISTS
FOR (n:Item) REQUIRE (n.projectId, n.itemId) IS NODE KEY;

CREATE CONSTRAINT unique_event IF NOT EXISTS
FOR (n:Event) REQUIRE (n.projectId, n.eventId) IS NODE KEY;

CREATE CONSTRAINT unique_cheat IF NOT EXISTS
FOR (n:Cheat) REQUIRE (n.projectId, n.cheatId) IS NODE KEY;

CREATE CONSTRAINT unique_alias IF NOT EXISTS
FOR (n:Alias) REQUIRE (n.projectId, n.aliasId) IS NODE KEY;

CREATE CONSTRAINT unique_narrative_rule IF NOT EXISTS
FOR (n:NarrativeRule) REQUIRE (n.projectId, n.ruleId) IS NODE KEY;

// 索引: 加速项目维度查询

CREATE INDEX idx_project_projectId IF NOT EXISTS FOR (n:Project) ON (n.projectId);
CREATE INDEX idx_volume_projectId IF NOT EXISTS FOR (n:Volume) ON (n.projectId);
CREATE INDEX idx_chapter_projectId IF NOT EXISTS FOR (n:Chapter) ON (n.projectId);
CREATE INDEX idx_character_projectId IF NOT EXISTS FOR (n:Character) ON (n.projectId);
CREATE INDEX idx_location_projectId IF NOT EXISTS FOR (n:Location) ON (n.projectId);
CREATE INDEX idx_clue_projectId IF NOT EXISTS FOR (n:Clue) ON (n.projectId);
CREATE INDEX idx_item_projectId IF NOT EXISTS FOR (n:Item) ON (n.projectId);
CREATE INDEX idx_event_projectId IF NOT EXISTS FOR (n:Event) ON (n.projectId);
CREATE INDEX idx_cheat_projectId IF NOT EXISTS FOR (n:Cheat) ON (n.projectId);
CREATE INDEX idx_alias_projectId IF NOT EXISTS FOR (n:Alias) ON (n.projectId);
CREATE INDEX idx_narrative_rule_projectId IF NOT EXISTS FOR (n:NarrativeRule) ON (n.projectId);

// 特殊索引: 加速写作检索
CREATE INDEX idx_character_status IF NOT EXISTS FOR (n:Character) ON (n.currentStatus);
CREATE INDEX idx_clue_status IF NOT EXISTS FOR (n:Clue) ON (n.clueStatus);
CREATE INDEX idx_chapter_number IF NOT EXISTS FOR (n:Chapter) ON (n.chapterNumber);
CREATE INDEX idx_archived IF NOT EXISTS FOR (n:Chapter) ON (n.archived);
