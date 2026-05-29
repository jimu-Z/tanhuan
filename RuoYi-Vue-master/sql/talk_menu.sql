-- 学生谈心谈话管理系统 - 菜单SQL

-- 顶层菜单：学生谈心谈话管理
INSERT INTO sys_menu VALUES(2000, '学生谈心谈话', 0, 1, 'talk', NULL, '', '', 1, 0, 'M', '0', '0', '', 'education', 'admin', NOW(), '', NULL, '学生谈心谈话管理目录');

SET @talkMenuId = 2000;

-- 学生信息管理（二级菜单）
INSERT INTO sys_menu VALUES(2001, '学生信息管理', @talkMenuId, 1, 'student', 'talk/talkStudent/index', '', '', 1, 0, 'C', '0', '0', 'talk:student:list', 'list', 'admin', NOW(), '', NULL, '');
SET @studentMenuId = 2001;
INSERT INTO sys_menu VALUES(2002, '学生信息查询', @studentMenuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:student:query', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(2003, '学生信息新增', @studentMenuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:student:add', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(2004, '学生信息修改', @studentMenuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:student:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(2005, '学生信息删除', @studentMenuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:student:remove', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(2006, '学生信息导出', @studentMenuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:student:export', '#', 'admin', NOW(), '', NULL, '');

-- 谈话会话管理（二级菜单）
INSERT INTO sys_menu VALUES(2010, '谈话会话管理', @talkMenuId, 2, 'talksession', 'talk/talkSession/index', '', '', 1, 0, 'C', '0', '0', 'talk:session:list', 'form', 'admin', NOW(), '', NULL, '');
SET @sessionMenuId = 2010;
INSERT INTO sys_menu VALUES(2011, '谈话会话查询', @sessionMenuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:session:query', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(2012, '谈话会话新增', @sessionMenuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:session:add', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(2013, '谈话会话修改', @sessionMenuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:session:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(2014, '谈话会话删除', @sessionMenuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:session:remove', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(2015, '谈话会话导出', @sessionMenuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:session:export', '#', 'admin', NOW(), '', NULL, '');

-- 谈话记录明细管理（二级菜单）
INSERT INTO sys_menu VALUES(2020, '谈话记录管理', @talkMenuId, 3, 'talkrecord', 'talk/talkStudentRecord/index', '', '', 1, 0, 'C', '0', '0', 'talk:record:list', 'table', 'admin', NOW(), '', NULL, '');
SET @recordMenuId = 2020;
INSERT INTO sys_menu VALUES(2021, '谈话记录查询', @recordMenuId, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:record:query', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(2022, '谈话记录新增', @recordMenuId, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:record:add', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(2023, '谈话记录修改', @recordMenuId, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:record:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(2024, '谈话记录删除', @recordMenuId, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:record:remove', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_menu VALUES(2025, '谈话记录导出', @recordMenuId, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:record:export', '#', 'admin', NOW(), '', NULL, '');

-- 发起谈话（二级菜单）
INSERT INTO sys_menu VALUES(2030, '发起谈话', @talkMenuId, 4, 'initiate', 'talk/talkInitiate/index', '', '', 1, 0, 'C', '0', '0', 'talk:session:add', 'edit', 'admin', NOW(), '', NULL, '');

-- 为角色分配菜单权限
-- 管理员角色（id=3）拥有全部菜单
INSERT INTO sys_role_menu VALUES(3, 2000),(3, 2001),(3, 2002),(3, 2003),(3, 2004),(3, 2005),(3, 2006),(3, 2010),(3, 2011),(3, 2012),(3, 2013),(3, 2014),(3, 2015),(3, 2020),(3, 2021),(3, 2022),(3, 2023),(3, 2024),(3, 2025),(3, 2030);
-- 书记/副书记角色（id=4）拥有查看权限
INSERT INTO sys_role_menu VALUES(4, 2000),(4, 2001),(4, 2002),(4, 2006),(4, 2010),(4, 2011),(4, 2015),(4, 2020),(4, 2021),(4, 2025),(4, 2030);
-- 辅导员/班主任角色（id=5）拥有查看和新增权限
INSERT INTO sys_role_menu VALUES(5, 2000),(5, 2001),(5, 2002),(5, 2006),(5, 2010),(5, 2011),(5, 2012),(5, 2015),(5, 2020),(5, 2021),(5, 2022),(5, 2025),(5, 2030);
