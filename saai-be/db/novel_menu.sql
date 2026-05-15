-- AI 小说侧边栏菜单脚本。
-- 约定：侧边栏最多两级，一级代表工作区，二级代表具体页面。
-- 说明：当前前端页面还没全部创建，component 先统一指向现有写作页占位，后续页面做好后只改 component 即可。

START TRANSACTION;

-- 清理旧小说菜单授权，避免侧边栏出现新旧入口并存。
DELETE rm
FROM `t_role_menu` rm
JOIN `t_menu` m ON rm.`menu_id` = m.`menu_id`
WHERE m.`path` LIKE '/novel/%'
   OR m.`menu_id` IN (316, 317)
   OR m.`menu_id` BETWEEN 5000 AND 5025
   OR m.`menu_name` IN (
      '小说写作',
      '写作审阅',
      '项目总览',
      '写作中心',
      '世界资料',
      '图谱审阅',
      '统计日志',
      '项目设置'
   );

-- 旧菜单软删除：保留历史 ID，避免误删线上已有引用。
UPDATE `t_menu`
SET `visible_flag` = 0,
    `disabled_flag` = 1,
    `deleted_flag` = 1,
    `update_user_id` = 1,
    `update_time` = NOW()
WHERE `path` LIKE '/novel/%'
   OR `menu_id` IN (316, 317)
   OR `menu_id` BETWEEN 5000 AND 5025
   OR `menu_name` IN (
      '小说写作',
      '写作审阅',
      '项目总览',
      '写作中心',
      '世界资料',
      '图谱审阅',
      '统计日志',
      '项目设置'
   );

REPLACE INTO `t_menu` (
  `menu_id`,
  `menu_name`,
  `menu_type`,
  `parent_id`,
  `sort`,
  `path`,
  `component`,
  `perms_type`,
  `api_perms`,
  `web_perms`,
  `icon`,
  `context_menu_id`,
  `frame_flag`,
  `frame_url`,
  `cache_flag`,
  `visible_flag`,
  `disabled_flag`,
  `deleted_flag`,
  `create_user_id`,
  `create_time`,
  `update_user_id`,
  `update_time`
) VALUES
  (5000, '项目总览', 2, 0, 40, '/novel/dashboard', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'DashboardOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),

  (5001, '写作中心', 1, 0, 41, NULL, NULL, NULL, NULL, NULL, 'EditOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5002, '写作工作台', 2, 5001, 10, '/novel/write', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'FileTextOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5003, '章节管理', 2, 5001, 20, '/novel/chapter', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'OrderedListOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5004, '大纲细纲', 2, 5001, 30, '/novel/outline', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'ProfileOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5005, '生成记录', 2, 5001, 40, '/novel/generation-record', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'HistoryOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),

  (5006, '世界资料', 1, 0, 42, NULL, NULL, NULL, NULL, NULL, 'BookOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5007, '人物', 2, 5006, 10, '/novel/character', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'UserOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5008, '地点', 2, 5006, 20, '/novel/location', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'EnvironmentOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5009, '线索', 2, 5006, 30, '/novel/clue', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'BulbOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5010, '物品', 2, 5006, 40, '/novel/item', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'InboxOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5011, '事件', 2, 5006, 50, '/novel/event', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'CalendarOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5012, '金手指', 2, 5006, 60, '/novel/cheat', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'ThunderboltOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5013, '马甲', 2, 5006, 70, '/novel/alias', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'IdcardOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5014, '叙事规则', 2, 5006, 80, '/novel/narrative-rule', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'SafetyCertificateOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),

  (5015, '图谱审阅', 1, 0, 43, NULL, NULL, NULL, NULL, NULL, 'ApartmentOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5016, '关系图谱', 2, 5015, 10, '/novel/graph/relation', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'ShareAltOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5017, '图谱变更', 2, 5015, 20, '/novel/graph/patch', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'AuditOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5018, '一致性检查', 2, 5015, 30, '/novel/graph/check', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'CheckCircleOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),

  (5019, '统计日志', 1, 0, 44, NULL, NULL, NULL, NULL, NULL, 'BarChartOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5020, '写作统计', 2, 5019, 10, '/novel/statistics/write', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'LineChartOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5021, '操作日志', 2, 5019, 20, '/novel/log/operation', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'UnorderedListOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5022, '模型用量', 2, 5019, 30, '/novel/statistics/model-usage', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'ApiOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),

  (5023, '项目设置', 1, 0, 45, NULL, NULL, NULL, NULL, NULL, 'SettingOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5024, '基础设置', 2, 5023, 10, '/novel/settings/basic', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'ToolOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW()),
  (5025, '模型 Key', 2, 5023, 20, '/novel/settings/api-key', '/business/novel/novel-write.vue', NULL, NULL, NULL, 'KeyOutlined', NULL, 0, NULL, 0, 1, 0, 0, 1, NOW(), 1, NOW());

-- 当前库已有小说菜单授权给 role_id=1，这里继续沿用。
INSERT INTO `t_role_menu` (`role_id`, `menu_id`, `create_time`, `update_time`)
SELECT 1, `menu_id`, NOW(), NOW()
FROM `t_menu`
WHERE `menu_id` BETWEEN 5000 AND 5025
  AND `deleted_flag` = 0;

COMMIT;
