-- ============================================
-- 谈心谈话菜单修复脚本（可重复执行，幂等）
-- 修复问题：
--   1. 谈话管理菜单(2010)可能不存在
--   2. 角色-菜单关联可能缺失
--   3. 新菜单(2050-2084)可能未注册
-- ============================================

-- 顶层菜单
INSERT IGNORE INTO sys_menu VALUES(2000, '学生谈心谈话', 0, 1, 'talk', NULL, '', '', 1, 0, 'M', '0', '0', '', 'education', 'admin', NOW(), '', NULL, '学生谈心谈话管理目录');

-- 学生信息管理
INSERT IGNORE INTO sys_menu VALUES(2001, '学生信息管理', 2000, 1, 'student', 'talk/talkStudent/index', '', '', 1, 0, 'C', '0', '0', 'talk:student:list', 'list', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2002, '学生信息查询', 2001, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:student:query', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2003, '学生信息新增', 2001, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:student:add', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2004, '学生信息修改', 2001, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:student:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2005, '学生信息删除', 2001, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:student:remove', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2006, '学生信息导出', 2001, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:student:export', '#', 'admin', NOW(), '', NULL, '');

-- 谈话管理（关键菜单，之前可能缺失）
INSERT IGNORE INTO sys_menu VALUES(2010, '谈话管理', 2000, 2, 'management', 'talk/talkManagement/index', '', '', 1, 0, 'C', '0', '0', 'talk:session:list', 'form', 'admin', NOW(), '', NULL, '谈话管理（合并会话+记录CRUD）');
INSERT IGNORE INTO sys_menu VALUES(2011, '谈话会话查询', 2010, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:session:query', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2012, '谈话会话新增', 2010, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:session:add', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2013, '谈话会话修改', 2010, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:session:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2014, '谈话会话删除', 2010, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:session:remove', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2015, '谈话会话导出', 2010, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:session:export', '#', 'admin', NOW(), '', NULL, '');

-- 记录子菜单权限（2020为虚拟父节点，子菜单2021-2025用于权限控制）
INSERT IGNORE INTO sys_menu VALUES(2020, '谈话记录权限', 2010, 6, '#', '', '', '', 1, 0, 'M', '1', '1', '', '', 'admin', NOW(), '', NULL, '虚拟父节点，用于挂载记录操作权限');
INSERT IGNORE INTO sys_menu VALUES(2021, '谈话记录查询', 2020, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:record:query', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2022, '谈话记录新增', 2020, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:record:add', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2023, '谈话记录修改', 2020, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:record:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2024, '谈话记录删除', 2020, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:record:remove', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2025, '谈话记录导出', 2020, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:record:export', '#', 'admin', NOW(), '', NULL, '');

-- 发起谈话
INSERT IGNORE INTO sys_menu VALUES(2030, '发起谈话', 2000, 4, 'initiate', 'talk/talkInitiate/index', '', '', 1, 0, 'C', '0', '0', 'talk:session:add', 'edit', 'admin', NOW(), '', NULL, '');

-- 我的谈话记录
INSERT IGNORE INTO sys_menu VALUES(2040, '我的谈话记录', 2000, 5, 'myrecords', 'talk/myRecords/index', '', '', 1, 0, 'C', '0', '0', 'talk:session:list', 'user', 'admin', NOW(), '', NULL, '');

-- 统计分析仪表盘
INSERT IGNORE INTO sys_menu VALUES(2050, '统计分析', 2000, 7, 'dashboard', 'talk/dashboardV2/index', '', '', 1, 0, 'C', '0', '0', 'talk:dashboard:view', 'chart', 'admin', NOW(), '', NULL, '统计分析仪表盘');

-- 预警提醒
INSERT IGNORE INTO sys_menu VALUES(2055, '预警提醒', 2000, 7, 'alerts', 'talk/alertsV2/index', '', '', 1, 0, 'C', '0', '0', 'talk:alert:view', 'message', 'admin', NOW(), '', NULL, '心理健康预警提醒');
INSERT IGNORE INTO sys_menu VALUES(2056, '预警列表', 2055, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'talk:alert:list', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2057, '预警查询', 2055, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'talk:alert:query', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2058, '预警处理', 2055, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'talk:alert:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2059, '预警新增', 2055, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'talk:alert:add', '#', 'admin', NOW(), '', NULL, '');

-- 谈话模板库
INSERT IGNORE INTO sys_menu VALUES(2060, '谈话模板库', 2000, 9, 'template', 'talk/templatesV2/index', '', '', 1, 0, 'C', '0', '0', 'talk:template:list', 'documentation', 'admin', NOW(), '', NULL, '谈话内容模板管理');
INSERT IGNORE INTO sys_menu VALUES(2061, '模板查询', 2060, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:template:query', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2062, '模板新增', 2060, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:template:add', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2063, '模板修改', 2060, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:template:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2064, '模板删除', 2060, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:template:remove', '#', 'admin', NOW(), '', NULL, '');

-- 数据大屏
INSERT IGNORE INTO sys_menu VALUES(2065, '数据大屏', 2000, 10, 'bigscreen', 'talk/bigscreenV2/index', '', '', 1, 0, 'C', '0', '0', 'talk:bigscreen:view', 'monitor', 'admin', NOW(), '', NULL, '数据可视化大屏');

-- 统一查询
INSERT IGNORE INTO sys_menu VALUES(2070, '统一查询', 2000, 11, 'query', 'talk/unifiedQuery/index', '', '', 1, 0, 'C', '0', '0', 'talk:unified:view', 'search', 'admin', NOW(), '', NULL, '统一查询（合并谈话会话/记录/高级查询）');

-- 谈话跟进（菜单隐藏，功能正常：visible='1'表示不在侧边栏显示，但权限和路由仍可用）
INSERT IGNORE INTO sys_menu VALUES(2075, '谈话跟进', 2000, 12, 'followup', 'talk/followup/index', '', '', 1, 0, 'C', '1', '0', 'talk:followup:list', 'list', 'admin', NOW(), '', NULL, '谈话跟进管理（隐藏菜单）');
INSERT IGNORE INTO sys_menu VALUES(2076, '谈话跟进查询', 2075, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:followup:query', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2077, '谈话跟进修改', 2075, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:followup:edit', '#', 'admin', NOW(), '', NULL, '');

-- 谈话标签管理（仅管理员可见）
INSERT IGNORE INTO sys_menu VALUES(2080, '谈话标签管理', 2000, 13, 'talktag', 'talk/talkTag/index', '', '', 1, 0, 'C', '0', '0', 'talk:tag:list', 'tag', 'admin', NOW(), '', NULL, '谈话标签管理（管理员专用）');
INSERT IGNORE INTO sys_menu VALUES(2081, '标签查询', 2080, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:tag:query', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2082, '标签新增', 2080, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:tag:add', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2083, '标签修改', 2080, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:tag:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT IGNORE INTO sys_menu VALUES(2084, '标签删除', 2080, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:tag:remove', '#', 'admin', NOW(), '', NULL, '');

-- ============================================
-- 修复角色-菜单关联（先删后插，确保数据最新）
-- ============================================

-- 删除所有talk相关角色-菜单关联（menu_id >= 2000 AND menu_id < 2100）
DELETE FROM sys_role_menu WHERE menu_id >= 2000 AND menu_id < 2100;

-- 管理员角色（id=3）拥有全部菜单
INSERT INTO sys_role_menu VALUES
(3, 2000),(3, 2001),(3, 2002),(3, 2003),(3, 2004),(3, 2005),(3, 2006),
(3, 2010),(3, 2011),(3, 2012),(3, 2013),(3, 2014),(3, 2015),
(3, 2021),(3, 2022),(3, 2023),(3, 2024),(3, 2025),
(3, 2030),(3, 2040),
(3, 2050),(3, 2055),
(3, 2060),(3, 2061),(3, 2062),(3, 2063),(3, 2064),
(3, 2065),(3, 2070),
(3, 2075),(3, 2076),(3, 2077),
(3, 2080),(3, 2081),(3, 2082),(3, 2083),(3, 2084);

-- 书记/副书记角色（id=4）拥有查看权限
INSERT INTO sys_role_menu VALUES
(4, 2000),(4, 2001),(4, 2002),(4, 2006),
(4, 2010),(4, 2011),(4, 2015),
(4, 2020),(4, 2021),(4, 2025),
(4, 2030),(4, 2040),
(4, 2050),(4, 2055),(4, 2056),(4, 2057),(4, 2058),
(4, 2060),(4, 2061),
(4, 2065),(4, 2070),
(4, 2075),(4, 2076),
(4, 2080);

-- 辅导员/班主任角色（id=5）拥有查看和新增权限
INSERT INTO sys_role_menu VALUES
(5, 2000),(5, 2001),(5, 2002),(5, 2006),
(5, 2010),(5, 2011),(5, 2012),(5, 2015),
(5, 2020),(5, 2021),(5, 2022),(5, 2025),
(5, 2030),(5, 2040),
(5, 2050),(5, 2055),(5, 2056),(5, 2057),(5, 2058),
(5, 2060),(5, 2061),(5, 2062),
(5, 2065),(5, 2070),
(5, 2075),(5, 2076),(5, 2077),
(5, 2080),
(5, 2090),(5, 2091);

-- 学生角色（id=6）基本权限 + 预约管理
INSERT IGNORE INTO sys_role_menu VALUES
(6, 2000),
(6, 2100), (6, 2101);

-- V3 新增菜单：教师管理（管理员+书记可见）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(2090, '教师管理', 2000, 10, 'teacher', 'talk/teacher/index', NULL, NULL, 1, 0, 'C', '0', '0', 'talk:teacher:list', 'user', 'admin', NOW(), '', NULL, '教师信息管理菜单');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(2091, '教师查询', 2090, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:teacher:query',  '#', 'admin', NOW(), '', NULL, ''),
(2092, '教师新增', 2090, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:teacher:add',    '#', 'admin', NOW(), '', NULL, ''),
(2093, '教师修改', 2090, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:teacher:edit',   '#', 'admin', NOW(), '', NULL, ''),
(2094, '教师删除', 2090, 4, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:teacher:remove', '#', 'admin', NOW(), '', NULL, ''),
(2095, '教师导入', 2090, 5, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:teacher:import', '#', 'admin', NOW(), '', NULL, '');

-- V3 新增菜单：预约管理（管理员+学生可见）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(2100, '预约管理', 2000, 11, 'appointment', 'talk/appointment/index', NULL, NULL, 1, 0, 'C', '0', '0', 'talk:appointment:list', 'date', 'admin', NOW(), '', NULL, '预约管理菜单');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(2101, '预约查询', 2100, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:appointment:query',  '#', 'admin', NOW(), '', NULL, ''),
(2102, '预约处理', 2100, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:appointment:handle', '#', 'admin', NOW(), '', NULL, '');

-- V3 新增菜单：角色菜单授权
-- 管理员(id=3)拥有全部V3菜单
INSERT IGNORE INTO sys_role_menu VALUES
(3, 2090),(3, 2091),(3, 2092),(3, 2093),(3, 2094),(3, 2095),
(3, 2100),(3, 2101),(3, 2102);

-- 书记(id=4)拥有教师管理查看+编辑权限（按设计文档0.1节：书记只能管理本学院教师）
INSERT IGNORE INTO sys_role_menu VALUES
(4, 2090),(4, 2091),(4, 2092),(4, 2093),(4, 2094),
(4, 2020);

-- 辅导员(id=5)补上谈话记录列表权限
INSERT IGNORE INTO sys_role_menu VALUES
(5, 2020);

-- 辅导员不拥有教师管理和预约管理菜单

-- 验证：查询所有talk菜单
SELECT menu_id, menu_name, component, perms, visible FROM sys_menu WHERE menu_id >= 2000 AND menu_id < 2200 ORDER BY menu_id;

-- 确保跟进菜单2075为隐藏状态（幂等安全）
UPDATE sys_menu SET visible = '1', remark = '谈话跟进管理（隐藏菜单）' WHERE menu_id = 2075;
