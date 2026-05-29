-- ============================================
-- 谈心谈话系统 - 增量菜单更新 (2026-05-29)
-- 仅包含新增菜单 2050-2070 + 模板按钮权限 + 角色赋权
-- 安全执行：使用 INSERT IGNORE 避免重复插入错误
-- ============================================

-- 查找顶层菜单 "学生谈心谈话" 的 menu_id
SET @talkMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '学生谈心谈话' AND parent_id = 0 LIMIT 1);

-- 统计分析仪表盘（二级菜单）
INSERT IGNORE INTO sys_menu VALUES(2050, '统计分析', @talkMenuId, 7, 'dashboard', 'talk/dashboardV2/index', '', '', 1, 0, 'C', '0', '0', 'talk:dashboard:view', 'chart', 'admin', NOW(), '', NULL, '统计分析仪表盘');

-- 预警提醒（二级菜单）
INSERT IGNORE INTO sys_menu VALUES(2055, '预警提醒', @talkMenuId, 8, 'alert', 'talk/alertsV2/index', '', '', 1, 0, 'C', '0', '0', 'talk:alert:view', 'warning', 'admin', NOW(), '', NULL, '谈话预警提醒');

-- 谈话模板库（二级菜单 + 按钮权限）
INSERT IGNORE INTO sys_menu VALUES(2060, '谈话模板库', @talkMenuId, 9, 'template', 'talk/templatesV2/index', '', '', 1, 0, 'C', '0', '0', 'talk:template:list', 'documentation', 'admin', NOW(), '', NULL, '谈话内容模板管理');
INSERT IGNORE INTO sys_menu VALUES(2061, '模板查询', 2060, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:template:query', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2062, '模板新增', 2060, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:template:add', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2063, '模板修改', 2060, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:template:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2064, '模板删除', 2060, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:template:remove', '#', 'admin', NOW(), '', NULL, '');

-- 数据大屏（二级菜单）
INSERT IGNORE INTO sys_menu VALUES(2065, '数据大屏', @talkMenuId, 10, 'bigscreen', 'talk/bigscreenV2/index', '', '', 1, 0, 'C', '0', '0', 'talk:bigscreen:view', 'monitor', 'admin', NOW(), '', NULL, '数据可视化大屏');

-- 统一查询（二级菜单）
INSERT IGNORE INTO sys_menu VALUES(2070, '统一查询', @talkMenuId, 11, 'query', 'talk/unifiedQuery/index', '', '', 1, 0, 'C', '0', '0', 'talk:unified:view', 'search', 'admin', NOW(), '', NULL, '统一查询（合并谈话会话/记录/高级查询）');

-- 为角色分配新增菜单权限（使用 INSERT IGNORE 防重复）
-- 管理员（role_id=3）全部可见
INSERT IGNORE INTO sys_role_menu VALUES(3, 2050),(3, 2055),(3, 2060),(3, 2061),(3, 2062),(3, 2063),(3, 2064),(3, 2065),(3, 2070);
-- 书记/副书记（role_id=4）查看权限
INSERT IGNORE INTO sys_role_menu VALUES(4, 2050),(4, 2055),(4, 2060),(4, 2061),(4, 2065),(4, 2070);
-- 辅导员/班主任（role_id=5）查看+新增模板
INSERT IGNORE INTO sys_role_menu VALUES(5, 2050),(5, 2055),(5, 2060),(5, 2061),(5, 2062),(5, 2065),(5, 2070);
