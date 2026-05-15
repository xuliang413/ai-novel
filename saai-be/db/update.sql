-- M0 AI 小说后端骨架表。
CREATE TABLE IF NOT EXISTS `t_novel_project` (
  `project_id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `project_name` varchar(100) NOT NULL COMMENT '项目名称',
  `genre` varchar(50) DEFAULT NULL COMMENT '小说类型枚举：XIANXIA-仙侠，XUANHUAN-玄幻，URBAN-都市，HISTORY-历史，SCIFI-科幻，MYSTERY-悬疑，WUXIA-武侠，FANTASY-奇幻',
  `summary` text COMMENT '项目简介',
  `protagonist` varchar(100) DEFAULT NULL COMMENT '主角名称',
  `target_words` int DEFAULT NULL COMMENT '目标字数',
  `status` varchar(30) NOT NULL DEFAULT 'ACTIVE' COMMENT '项目状态枚举：ACTIVE-写作中，PAUSED-暂停，ARCHIVED-归档',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`project_id`),
  KEY `idx_create_user_id` (`create_user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说项目';

CREATE TABLE IF NOT EXISTS `t_novel_character` (
  `character_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `character_name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_type` varchar(50) DEFAULT NULL COMMENT '角色定位枚举：PROTAGONIST-主角，ANTAGONIST-反派，SUPPORTING-配角，MINOR-路人或临时角色',
  `summary` text COMMENT '角色简介',
  `current_status` varchar(100) DEFAULT NULL COMMENT '角色状态枚举：ACTIVE-活跃，INACTIVE-暂离，DEAD-死亡，MISSING-失踪，UNKNOWN-未知',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`character_id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说角色';

CREATE TABLE IF NOT EXISTS `t_novel_location` (
  `location_id` bigint NOT NULL AUTO_INCREMENT COMMENT '地点ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `location_name` varchar(100) NOT NULL COMMENT '地点名称',
  `location_type` varchar(50) DEFAULT NULL COMMENT '地点类型枚举：CITY-城市，VILLAGE-村镇，BUILDING-建筑，SECT-宗门或组织驻地，WILDERNESS-荒野，REALM-秘境或世界，BATTLEFIELD-战场',
  `summary` text COMMENT '地点简介',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`location_id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说地点';

CREATE TABLE IF NOT EXISTS `t_novel_clue` (
  `clue_id` bigint NOT NULL AUTO_INCREMENT COMMENT '线索ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `clue_name` varchar(100) NOT NULL COMMENT '线索名称',
  `clue_type` varchar(50) DEFAULT NULL COMMENT '线索类型枚举：MAIN-主线，SUB-支线，HIDDEN-暗线',
  `clue_status` varchar(30) NOT NULL DEFAULT 'DORMANT' COMMENT '线索状态枚举：DORMANT-未激活，ACTIVE-推进中，RESOLVED-已解决',
  `summary` text COMMENT '线索简介',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`clue_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_clue_status` (`clue_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说线索';

CREATE TABLE IF NOT EXISTS `t_novel_volume` (
  `volume_id` bigint NOT NULL AUTO_INCREMENT COMMENT '卷ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `volume_no` int NOT NULL COMMENT '卷序号',
  `volume_title` varchar(200) NOT NULL COMMENT '卷标题',
  `summary` text COMMENT '卷概要',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`volume_id`),
  UNIQUE KEY `uk_project_volume_no` (`project_id`, `volume_no`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说卷';

CREATE TABLE IF NOT EXISTS `t_novel_item` (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '物品ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `item_name` varchar(100) NOT NULL COMMENT '物品名称',
  `item_type` varchar(50) DEFAULT NULL COMMENT '物品类型',
  `item_status` varchar(50) NOT NULL DEFAULT 'INTACT' COMMENT '物品状态',
  `summary` text COMMENT '物品简介',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`item_id`),
  UNIQUE KEY `uk_project_item_name` (`project_id`, `item_name`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说物品';

CREATE TABLE IF NOT EXISTS `t_novel_event` (
  `event_id` bigint NOT NULL AUTO_INCREMENT COMMENT '事件ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `event_name` varchar(100) NOT NULL COMMENT '事件名称',
  `summary` text COMMENT '事件简介',
  `chapter_occurred` int DEFAULT NULL COMMENT '事件发生章节',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`event_id`),
  UNIQUE KEY `uk_project_event_name` (`project_id`, `event_name`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说事件';

CREATE TABLE IF NOT EXISTS `t_novel_cheat` (
  `cheat_id` bigint NOT NULL AUTO_INCREMENT COMMENT '金手指ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `cheat_name` varchar(100) NOT NULL COMMENT '金手指名称',
  `cheat_type` varchar(50) DEFAULT NULL COMMENT '金手指类型',
  `summary` text COMMENT '金手指简介',
  `origin` varchar(500) DEFAULT NULL COMMENT '来源',
  `limitation` varchar(500) DEFAULT NULL COMMENT '限制或副作用',
  `evolution` varchar(500) DEFAULT NULL COMMENT '升级路径',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`cheat_id`),
  UNIQUE KEY `uk_project_cheat_name` (`project_id`, `cheat_name`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说金手指';

CREATE TABLE IF NOT EXISTS `t_novel_alias` (
  `alias_id` bigint NOT NULL AUTO_INCREMENT COMMENT '马甲ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `alias_name` varchar(100) NOT NULL COMMENT '马甲名称',
  `alias_type` varchar(50) DEFAULT NULL COMMENT '马甲类型',
  `alias_context` varchar(200) NOT NULL COMMENT '使用场景',
  `summary` text COMMENT '马甲简介',
  `revealed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已识破',
  `revealed_to` varchar(500) DEFAULT NULL COMMENT '识破角色列表',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`alias_id`),
  UNIQUE KEY `uk_project_alias_name` (`project_id`, `alias_name`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说马甲';

CREATE TABLE IF NOT EXISTS `t_novel_narrative_rule` (
  `rule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '叙事规则ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_type` varchar(50) NOT NULL COMMENT '规则类型',
  `rule_value` varchar(2000) NOT NULL COMMENT '规则内容',
  `priority` int NOT NULL DEFAULT 3 COMMENT '优先级，1-5，5最高',
  `deleted_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`rule_id`),
  UNIQUE KEY `uk_project_rule_name` (`project_id`, `rule_name`),
  KEY `idx_project_type` (`project_id`, `rule_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说叙事规则';

CREATE TABLE IF NOT EXISTS `t_novel_chapter` (
  `chapter_id` bigint NOT NULL AUTO_INCREMENT COMMENT '章节ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `chapter_no` int NOT NULL COMMENT '章节序号',
  `title` varchar(200) NOT NULL COMMENT '章节标题',
  `summary` text COMMENT '章节摘要',
  `content` mediumtext COMMENT '章节正文',
  `status` varchar(30) NOT NULL DEFAULT 'DRAFT' COMMENT '章节状态枚举：DRAFT-草稿，PENDING_GRAPH_CONFIRM-待确认图谱变更，PENDING_GRAPH_UPDATE-待同步图谱，PUBLISHED-已发布，INTERRUPTED_DRAFT-中断草稿',
  `generation_session_id` bigint DEFAULT NULL COMMENT '最近一次生成会话ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`chapter_id`),
  UNIQUE KEY `uk_project_chapter_no` (`project_id`, `chapter_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说章节';

CREATE TABLE IF NOT EXISTS `t_chapter_outline` (
  `outline_id` bigint NOT NULL AUTO_INCREMENT COMMENT '章节细纲ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `chapter_no` int NOT NULL COMMENT '章节序号',
  `scenes_json` mediumtext COMMENT '场景节拍 JSON',
  `summary` text COMMENT '细纲摘要',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`outline_id`),
  UNIQUE KEY `uk_project_chapter_no` (`project_id`, `chapter_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说章节细纲';

CREATE TABLE IF NOT EXISTS `t_writing_log` (
  `writing_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '写作日志ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `chapter_id` bigint DEFAULT NULL COMMENT '章节ID',
  `chapter_no` int DEFAULT NULL COMMENT '章节序号',
  `word_count` int DEFAULT NULL COMMENT '字数',
  `token_used` int DEFAULT NULL COMMENT 'Token消耗估算',
  `success` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否成功',
  `provider` varchar(30) DEFAULT NULL COMMENT '生成供应商',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`writing_log_id`),
  KEY `idx_project_chapter` (`project_id`, `chapter_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说写作日志';

CREATE TABLE IF NOT EXISTS `t_user_api_key` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `deepseek_key` varchar(1000) DEFAULT NULL COMMENT '加密后的DeepSeek API Key',
  `qwen_key` varchar(1000) DEFAULT NULL COMMENT '加密后的通义千问API Key',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户大模型API Key';

CREATE TABLE IF NOT EXISTS `t_chapter_generation_session` (
  `session_id` bigint NOT NULL AUTO_INCREMENT COMMENT '生成会话ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `chapter_id` bigint DEFAULT NULL COMMENT '章节ID',
  `chapter_no` int NOT NULL COMMENT '章节序号',
  `provider` varchar(30) NOT NULL COMMENT '生成供应商枚举：MOCK-本地模拟生成，DEEPSEEK-DeepSeek，TONGYI-通义千问',
  `status` varchar(30) NOT NULL COMMENT '生成状态枚举：GENERATING-生成中，CONTENT_REVIEW-正文审阅，EXTRACTING_PATCH-抽取Patch，PATCH_REVIEW-Patch确认，APPLYING_PATCH-执行Patch，SUCCESS-完成，FAILED-失败，INTERRUPTED-中断',
  `prompt_snapshot` text COMMENT '提示词快照',
  `intent_json` mediumtext COMMENT 'ChapterIntent JSON',
  `context_snapshot` mediumtext COMMENT '上下文快照',
  `content_review_json` mediumtext COMMENT '正文审阅质检 JSON',
  `graph_patch_json` mediumtext COMMENT '待确认 GraphPatch JSON',
  `inverse_patch_json` mediumtext COMMENT '待确认 inversePatch JSON',
  `operation_batch_id` varchar(64) DEFAULT NULL COMMENT '本次图谱操作批次ID',
  `result_excerpt` text COMMENT '生成结果摘要',
  `error_message` text COMMENT '失败原因',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`session_id`),
  KEY `idx_project_chapter` (`project_id`, `chapter_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI章节生成会话';

CREATE TABLE IF NOT EXISTS `t_graph_change_log` (
  `change_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '图谱变更日志ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `chapter_id` bigint DEFAULT NULL COMMENT '章节ID',
  `chapter_no` int DEFAULT NULL COMMENT '章节序号',
  `session_id` bigint DEFAULT NULL COMMENT '生成会话ID',
  `patch_id` varchar(64) NOT NULL COMMENT 'GraphPatch ID',
  `operation_batch_id` varchar(64) NOT NULL COMMENT '操作批次ID',
  `patch_json` mediumtext NOT NULL COMMENT '实际执行 GraphPatch JSON',
  `inverse_patch_json` mediumtext NOT NULL COMMENT '反向 GraphPatch JSON',
  `status` varchar(30) NOT NULL DEFAULT 'APPLIED' COMMENT '状态枚举：APPLIED-已应用，UNDONE-已撤销，FAILED-失败',
  `error_message` text COMMENT '失败原因',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人用户ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`change_log_id`),
  UNIQUE KEY `uk_operation_batch_id` (`operation_batch_id`),
  KEY `idx_project_status` (`project_id`, `status`),
  KEY `idx_chapter_id` (`chapter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI小说图谱变更日志';

ALTER TABLE `t_novel_chapter`
  MODIFY COLUMN `status` varchar(30) NOT NULL DEFAULT 'DRAFT' COMMENT '章节状态枚举：DRAFT-草稿，PENDING_GRAPH_CONFIRM-待确认图谱变更，PENDING_GRAPH_UPDATE-待同步图谱，PUBLISHED-已发布，INTERRUPTED_DRAFT-中断草稿';

SET @sql = (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE `t_chapter_generation_session` ADD COLUMN `intent_json` mediumtext COMMENT ''ChapterIntent JSON'' AFTER `prompt_snapshot`',
            'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 't_chapter_generation_session'
    AND column_name = 'intent_json'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE `t_chapter_generation_session` ADD COLUMN `content_review_json` mediumtext COMMENT ''正文审阅质检 JSON'' AFTER `context_snapshot`',
            'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 't_chapter_generation_session'
    AND column_name = 'content_review_json'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE `t_chapter_generation_session` ADD COLUMN `graph_patch_json` mediumtext COMMENT ''待确认 GraphPatch JSON'' AFTER `content_review_json`',
            'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 't_chapter_generation_session'
    AND column_name = 'graph_patch_json'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE `t_chapter_generation_session` ADD COLUMN `inverse_patch_json` mediumtext COMMENT ''待确认 inversePatch JSON'' AFTER `graph_patch_json`',
            'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 't_chapter_generation_session'
    AND column_name = 'inverse_patch_json'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
  SELECT IF(COUNT(*) = 0,
            'ALTER TABLE `t_chapter_generation_session` ADD COLUMN `operation_batch_id` varchar(64) DEFAULT NULL COMMENT ''本次图谱操作批次ID'' AFTER `inverse_patch_json`',
            'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 't_chapter_generation_session'
    AND column_name = 'operation_batch_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
