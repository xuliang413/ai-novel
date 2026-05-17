-- ============================================================
-- AI小说第一阶段 - 业务表DDL
-- 覆盖技术方案第四章所有实体属性
-- 可重复执行: CREATE TABLE IF NOT EXISTS
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------------------
-- 1. 项目表 (t_novel_project)
-- 顶层容器, 一本小说对应一个项目
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_project` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(255) NOT NULL COMMENT '项目名',
  `genre` varchar(50) NOT NULL COMMENT '类型: XIANXIA/XUANHUAN/URBAN/HISTORY/SCIFI/MYSTERY/WUXIA/FANTASY',
  `world_building` text COMMENT '世界观概述, 自然语言, 注入每章Prompt',
  `protagonist_name` varchar(100) DEFAULT NULL COMMENT '主角名, 快捷引用',
  `style_description` text COMMENT '文风描述, 约束AI写作风格',
  `platform` varchar(50) DEFAULT NULL COMMENT '目标平台: QIDIAN/FANQIE/ZONGHENG',
  `target_total_words` int(0) DEFAULT NULL COMMENT '目标总字数, 统计参考',
  `target_chapter_words` int(0) DEFAULT 3000 COMMENT '每章目标字数, 质检基准',
  `token_budget` int(0) DEFAULT 6000 COMMENT '上下文Token目标预算',
  `token_hard_limit` int(0) DEFAULT 8000 COMMENT '上下文Token硬上限',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/PAUSED/ARCHIVED',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记: 0正常 1已归档',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID, 所有查询强制过滤',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '小说项目表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 2. 卷表 (t_novel_volume)
-- 小说分卷, 每卷有概要, AI写该卷章节时注入概要
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_volume` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `number` int(0) NOT NULL COMMENT '卷序号, 从1开始',
  `title` varchar(255) NOT NULL COMMENT '卷标题',
  `summary` text COMMENT '卷概要, 自然语言, 注入每章Prompt',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_project_number`(`project_id`, `number`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '小说卷表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 3. 章节表 (t_novel_chapter)
-- 写作的基本单元, 正文存MySQL, 摘要存Neo4j
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_chapter` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `volume_id` bigint(0) DEFAULT NULL COMMENT '所属卷ID, 可选',
  `chapter_number` int(0) NOT NULL COMMENT '章节序号, 全局递增',
  `title` varchar(255) DEFAULT NULL COMMENT '章节标题',
  `summary` varchar(500) DEFAULT NULL COMMENT '章节摘要, 300字以内, Neo4j同步',
  `content` longtext COMMENT '章节正文, 仅存MySQL',
  `pov` varchar(100) DEFAULT NULL COMMENT 'POV视角人物名',
  `word_count` int(0) DEFAULT 0 COMMENT '正文字数',
  `status` varchar(30) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PENDING_GRAPH_CONFIRM/PUBLISHED/PENDING_GRAPH_UPDATE/INTERRUPTED_DRAFT',
  `embedding` text COMMENT '章节摘要向量化, JSON数组, 未配向量Key时为空',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_chapter`(`project_id`, `chapter_number`) USING BTREE,
  INDEX `idx_volume_id`(`volume_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '小说章节表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 4. 章节细纲表 (t_chapter_outline)
-- 提前为某章号写的场景规划, 只存MySQL
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_chapter_outline` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `chapter_number` int(0) NOT NULL COMMENT '对应章节号, 可超过当前进度',
  `scene_beats` text COMMENT '场景节拍, JSON存场景列表或纯文本',
  `summary` varchar(500) DEFAULT NULL COMMENT '细纲摘要',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_chapter_no`(`project_id`, `chapter_number`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '章节细纲表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 5. 角色表 (t_novel_character)
-- 设定属性管理页随时可改, 状态属性仅通过写作流程GraphPatch审阅修改
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_character` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_type` varchar(30) NOT NULL DEFAULT 'MINOR' COMMENT '定位: PROTAGONIST/ANTAGONIST/SUPPORTING/MINOR',
  `description` text COMMENT '基础描述, 外貌/性格等常驻信息',
  -- 动态属性: 以下字段仅通过写作流程GraphPatch修改
  `current_goal` text COMMENT '当前目标, 自然语言',
  `goal_progress` decimal(3,2) DEFAULT NULL COMMENT '目标完成度, 0~1',
  `goal_status` varchar(20) DEFAULT NULL COMMENT '目标状态: IN_PROGRESS/ACHIEVED/ABANDONED/DIVERTED',
  `current_emotion` varchar(30) DEFAULT NULL COMMENT '当前主导情绪: ANGER/FEAR/DETERMINED/DESPAIR/JOY/SADNESS/CALM/SUSPICIOUS/SHAME/PRIDE/HOPE/GRIEF/ANXIETY',
  `emotion_intensity` tinyint(0) DEFAULT NULL COMMENT '情绪强度 1~5',
  `secondary_emotion` varchar(30) DEFAULT NULL COMMENT '次生情绪, 表层+深层并存',
  `power_level` varchar(200) DEFAULT NULL COMMENT '战力/境界, 自然语言不用枚举',
  `current_status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '存活状态: ACTIVE/INACTIVE/DEAD/MISSING/UNKNOWN',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_project_name`(`project_id`, `name`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '小说角色表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 6. 地点表 (t_novel_location)
-- 全部属性管理页随时可改, 支持层级CONTAINS关系(最多3层)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_location` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `name` varchar(200) NOT NULL COMMENT '地点名称',
  `type` varchar(30) NOT NULL COMMENT '类型: CITY/VILLAGE/BUILDING/SECT/WILDERNESS/REALM/BATTLEFIELD',
  `summary` text COMMENT '自然语言描述',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_project_name`(`project_id`, `name`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '小说地点表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 7. 线索表 (t_novel_clue)
-- 贯穿多章的叙事线, PLOT_THREAD占Token进Prompt, FORESHADOWING休眠期不进
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_clue` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `name` varchar(200) NOT NULL COMMENT '线索名称',
  `type` varchar(20) NOT NULL COMMENT '类型: MAIN/SUB/HIDDEN',
  `sub_type` varchar(20) NOT NULL COMMENT '子类型: PLOT_THREAD/FORESHADOWING',
  `description` text COMMENT '完整描述, 设定属性',
  `priority` tinyint(0) DEFAULT 3 COMMENT '优先级 1~5, 多条ACTIVE线索抢Prompt预算时使用',
  `target_chapter` int(0) DEFAULT NULL COMMENT '计划收束章节号, 规划参考',
  `tone` varchar(20) DEFAULT NULL COMMENT '情绪基调: TRAGIC/TENSE/ROMANTIC/HEROIC/MYSTERIOUS/DARK',
  -- 动态属性: 仅通过写作流程修改
  `summary` text COMMENT '当前进展摘要, 每章推进后更新',
  `reveal_level` decimal(3,2) DEFAULT 0.00 COMMENT '揭露程度 0~1',
  `current_stage` varchar(200) DEFAULT NULL COMMENT '当前阶段, 自然语言描述',
  `clue_status` varchar(20) NOT NULL DEFAULT 'DORMANT' COMMENT '状态: DORMANT/ACTIVE/RESOLVED',
  `last_alerted_chapter` int(0) DEFAULT NULL COMMENT '最后一次提醒的章节号, 防止重复提醒',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_project_status`(`project_id`, `clue_status`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '小说线索表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 8. 物品表 (t_novel_item)
-- 角色持有物, 数量扣减仅通过写作流程
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_item` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `name` varchar(200) NOT NULL COMMENT '物品名称',
  `type` varchar(30) NOT NULL COMMENT '类型: WEAPON/ARMOR/TOOL/CONSUMABLE/TREASURE/DOCUMENT/CURRENCY/OTHER',
  `summary` text COMMENT '描述',
  -- 动态属性: 仅通过写作流程修改
  `quantity` int(0) DEFAULT NULL COMMENT '数量, 可消耗物品才填, 唯一物品不填',
  `item_status` varchar(20) DEFAULT 'INTACT' COMMENT '状态: INTACT/DAMAGED/DESTROYED/LOST',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '小说物品表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 9. 事件表 (t_novel_event)
-- 标记故事关键节点
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_event` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `name` varchar(200) NOT NULL COMMENT '事件名称',
  `summary` text COMMENT '事件描述',
  `chapter_occurred` int(0) DEFAULT NULL COMMENT '发生章节号, 用户可手动补',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '小说事件表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 10. 金手指表 (t_novel_cheat)
-- 角色的特殊能力
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_cheat` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `name` varchar(200) NOT NULL COMMENT '金手指名称',
  `type` varchar(30) NOT NULL COMMENT '类型: ABILITY/ITEM_BOUND/SPACE/SYSTEM',
  `summary` text COMMENT '描述',
  `origin` text COMMENT '来源',
  `limitation` text COMMENT '限制/副作用',
  `evolution` text COMMENT '进化/升级路径',
  -- 动态属性: 仅通过写作流程修改
  `current_stage` varchar(200) DEFAULT NULL COMMENT '当前副作用阶段',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '小说金手指表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 11. 马甲表 (t_novel_alias)
-- 角色的隐藏身份
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_alias` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `name` varchar(100) NOT NULL COMMENT '马甲名称',
  `type` varchar(30) NOT NULL COMMENT '类型: ONLINE_IDENTITY/DISGUISE/ALTER_EGO/OTHER',
  `alias_context` text COMMENT '使用场景描述',
  `summary` text COMMENT '描述',
  -- 动态属性: 仅通过写作流程修改
  `revealed` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否已被识破',
  `revealed_to` varchar(500) DEFAULT NULL COMMENT '被谁识破, 逗号分隔角色名',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '小说马甲表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 12. 叙事规则表 (t_novel_narrative_rule)
-- 写作约束, 不进Neo4j, 纯MySQL存
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_narrative_rule` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `name` varchar(200) NOT NULL COMMENT '规则名称',
  `content` text NOT NULL COMMENT '自然语言规则内容',
  `priority` tinyint(0) NOT NULL DEFAULT 3 COMMENT '优先级 1~5, 拼System Prompt时按priority降序',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_id`(`project_id`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '小说叙事规则表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 13. 角色关系表 (t_novel_character_relation)
-- KNOWS/LOVES/HATES/IS_FAMILY_OF 四种关系
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_character_relation` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `character_id` bigint(0) NOT NULL COMMENT '源角色ID',
  `target_character_id` bigint(0) NOT NULL COMMENT '目标角色ID',
  `relation_type` varchar(30) NOT NULL COMMENT '关系大类: KNOWS/LOVES/HATES/IS_FAMILY_OF',
  -- KNOWS专属字段
  `knows_relation_type` varchar(30) DEFAULT NULL COMMENT 'KNOWS子类型: FRIEND/ALLY/RIVAL/ACQUAINTANCE/SUBORDINATE/ENEMY',
  -- LOVES专属字段
  `love_status` varchar(20) DEFAULT NULL COMMENT '爱慕状态: UNREQUITED/MUTUAL/PAST',
  -- HATES专属字段
  `hate_intensity` tinyint(0) DEFAULT NULL COMMENT '仇恨强度 1~5',
  -- IS_FAMILY_OF专属字段
  `family_type` varchar(30) DEFAULT NULL COMMENT '亲缘类型: FATHER/MOTHER/BROTHER/SISTER/SON/DAUGHTER/COUSIN/SPOUSE/MASTER/DISCIPLE',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_char`(`project_id`, `character_id`) USING BTREE,
  INDEX `idx_project_target`(`project_id`, `target_character_id`) USING BTREE,
  INDEX `idx_relation_type`(`relation_type`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '小说角色关系表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 14. 角色当前位置表 (t_novel_character_location)
-- 记录角色当前所在地以及移动发生章节, 仅通过写作流程GraphPatch写入
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_character_location` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `character_id` bigint(0) NOT NULL COMMENT '角色ID',
  `location_id` bigint(0) NOT NULL COMMENT '地点ID',
  `location_name` varchar(200) DEFAULT NULL COMMENT '地点名称冗余, 便于日志和审阅页展示',
  `entered_in_chapter` int(0) DEFAULT NULL COMMENT '角色移动到该地点的章节号',
  `current_flag` tinyint(0) NOT NULL DEFAULT 1 COMMENT '是否当前所在地: 1当前 0历史',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_char_current`(`project_id`, `character_id`, `current_flag`) USING BTREE,
  INDEX `idx_project_location`(`project_id`, `location_id`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色当前位置表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 15. 角色金手指关联表 (t_novel_character_cheat)
-- 角色持有金手指, 记录获得章节
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_character_cheat` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `character_id` bigint(0) NOT NULL COMMENT '角色ID',
  `cheat_id` bigint(0) NOT NULL COMMENT '金手指ID',
  `acquired_in_chapter` int(0) DEFAULT NULL COMMENT '在第几章获得',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_char`(`project_id`, `character_id`) USING BTREE,
  INDEX `idx_project_cheat`(`project_id`, `cheat_id`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色金手指关联表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 16. 章节出场记录表 (t_novel_chapter_appearance)
-- 记录每章出场了哪些实体
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_chapter_appearance` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `chapter_id` bigint(0) NOT NULL COMMENT '章节ID',
  `chapter_number` int(0) NOT NULL COMMENT '章节号, 冗余便于查询',
  `entity_type` varchar(30) NOT NULL COMMENT '实体类型: CHARACTER/LOCATION/ITEM/EVENT/CHEAT/ALIAS',
  `entity_id` bigint(0) NOT NULL COMMENT '实体ID',
  `entity_name` varchar(200) NOT NULL COMMENT '实体名称, 冗余便于展示',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_chapter_entity`(`chapter_id`, `entity_type`, `entity_id`) USING BTREE,
  INDEX `idx_project_chapter`(`project_id`, `chapter_number`) USING BTREE,
  INDEX `idx_entity`(`entity_type`, `entity_id`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '章节出场记录表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 17. 线索推进记录表 (t_novel_clue_advance)
-- 记录每章对线索的推进
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_novel_clue_advance` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `clue_id` bigint(0) NOT NULL COMMENT '线索ID',
  `chapter_id` bigint(0) NOT NULL COMMENT '推进章节ID',
  `chapter_number` int(0) NOT NULL COMMENT '章节号, 冗余便于查询',
  `progress_description` text COMMENT '推进到什么程度, 自然语言',
  `reveal_level` decimal(3,2) DEFAULT NULL COMMENT '推进后的揭露程度',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '归档标记',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_clue`(`project_id`, `clue_id`) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '线索推进记录表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 18. 用户API Key表 (t_user_api_key)
-- 按模型用途分类: CHAT(对话)/EMBEDDING(向量)/RERANK(重排)
-- 每种类型一条记录, 各自独立配置url+key+模型名, 完全解耦
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_user_api_key` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `model_type` varchar(20) NOT NULL COMMENT '模型用途: CHAT(对话)/EMBEDDING(向量)/RERANK(重排)',
  `url` varchar(500) DEFAULT NULL COMMENT 'API地址, 用户可配以切换兼容OpenAI接口的任意提供商',
  `api_key` varchar(500) DEFAULT NULL COMMENT 'API Key, AES加密存储',
  `model_name` varchar(100) DEFAULT NULL COMMENT '模型名称, 如deepseek-chat/qwen3-embedding/qwen3-rerank',
  `provider_name` varchar(50) DEFAULT NULL COMMENT '提供商描述(可选), 如DeepSeek/通义千问, 仅展示用',
  `temperature` decimal(3,2) DEFAULT 0.7 COMMENT '生成温度(仅CHAT类型使用)',
  `max_tokens` int(0) DEFAULT 4096 COMMENT '最大Token数(仅CHAT类型使用)',
  `timeout` int(0) DEFAULT 60000 COMMENT '超时毫秒数',
  `deleted_flag` tinyint(0) NOT NULL DEFAULT 0 COMMENT '删除标记',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_model_type`(`user_id`, `model_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户API Key表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 19. 写作会话表 (t_chapter_generation_session)
-- 每次写作的完整流程记录, 黑匣子
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_chapter_generation_session` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `chapter_id` bigint(0) DEFAULT NULL COMMENT '关联章节ID',
  `chapter_number` int(0) NOT NULL COMMENT '章节号',
  `status` varchar(30) NOT NULL DEFAULT 'IDLE' COMMENT '会话状态: GENERATING/CONTENT_REVIEW/PATCH_REVIEW/PENDING_GRAPH_UPDATE/SUCCESS/INTERRUPTED/FAILED',
  `chapter_intent_json` text COMMENT '写作意图快照, JSON格式',
  `context_snapshot_json` text COMMENT '上下文检索快照, JSON格式',
  `provider` varchar(30) DEFAULT NULL COMMENT '使用的LLM提供商',
  `prompt_summary` text COMMENT '提示词摘要',
  `graph_patch_json` text COMMENT '候选GraphPatch JSON',
  `inverse_patch_json` text COMMENT '逆向操作JSON, 用于撤销',
  `operation_batch_id` varchar(64) DEFAULT NULL COMMENT '操作批次ID, 幂等保护',
  `result_summary` text COMMENT '结果摘要或失败原因',
  `retry_count` tinyint(0) DEFAULT 0 COMMENT '重试次数',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_chapter_active`(`project_id`, `chapter_number`, `status`) USING BTREE,
  INDEX `idx_chapter_id`(`chapter_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '章节生成会话表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 20. 图谱变更日志表 (t_graph_change_log)
-- 记录每次GraphPatch执行结果, 支持撤销
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_graph_change_log` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `session_id` bigint(0) NOT NULL COMMENT '关联会话ID',
  `chapter_id` bigint(0) DEFAULT NULL COMMENT '关联章节ID',
  `chapter_number` int(0) NOT NULL COMMENT '章节号',
  `operation_batch_id` varchar(64) NOT NULL COMMENT '操作批次ID, 幂等去重',
  `patch_json` text NOT NULL COMMENT '执行的GraphPatch JSON',
  `inverse_patch_json` text COMMENT '逆向操作JSON',
  `status` varchar(20) NOT NULL DEFAULT 'APPLIED' COMMENT '状态: APPLIED/UNDONE/FAILED',
  `error_message` text COMMENT '失败原因',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_batch_id`(`operation_batch_id`) USING BTREE,
  INDEX `idx_project_session`(`project_id`, `session_id`) USING BTREE,
  INDEX `idx_chapter_number`(`project_id`, `chapter_number`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '图谱变更日志表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 21. 写作日志表 (t_writing_log)
-- 每章生成后自动记录字数/Token/耗时
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_writing_log` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `session_id` bigint(0) DEFAULT NULL COMMENT '关联会话ID',
  `chapter_id` bigint(0) NOT NULL COMMENT '章节ID',
  `chapter_number` int(0) NOT NULL COMMENT '章节号',
  `word_count` int(0) DEFAULT 0 COMMENT '生成字数',
  `prompt_tokens` int(0) DEFAULT 0 COMMENT 'Prompt Token消耗',
  `completion_tokens` int(0) DEFAULT 0 COMMENT '生成Token消耗',
  `duration_ms` bigint(0) DEFAULT 0 COMMENT '生成耗时毫秒',
  `provider` varchar(30) DEFAULT NULL COMMENT '使用的LLM提供商',
  `model_name` varchar(100) DEFAULT NULL COMMENT '使用的模型名称',
  `write_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '写作时间',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_chapter`(`project_id`, `chapter_number`) USING BTREE,
  INDEX `idx_session_id`(`session_id`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '写作日志表' ROW_FORMAT = Dynamic;

-- --------------------------------------------------------
-- 22. 写作日历表 (t_writing_calendar)
-- 记录日期和章节/字数的对应关系
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_writing_calendar` (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(0) NOT NULL COMMENT '所属项目ID',
  `write_date` date NOT NULL COMMENT '写作日期',
  `chapters_written` text COMMENT '当天生成的章节号列表, JSON数组',
  `total_words` int(0) DEFAULT 0 COMMENT '当天总字数',
  `create_user_id` bigint(0) NOT NULL COMMENT '创建用户ID',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_date`(`project_id`, `write_date`) USING BTREE,
  INDEX `idx_create_user`(`create_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '写作日历表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
