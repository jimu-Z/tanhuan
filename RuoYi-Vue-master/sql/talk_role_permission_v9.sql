-- 谈心谈话 v9：四类角色、学院数据边界、系统/学院标签模板
-- MySQL 8.x；脚本可重复执行。

-- 1. 业务数据补充学院快照/作用域字段
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'talk_session' AND column_name = 'college_dept_id') = 0,
    'ALTER TABLE talk_session ADD COLUMN college_dept_id BIGINT NULL COMMENT ''谈话所属学院（历史快照）'' AFTER session_id', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'talk_tag' AND column_name = 'scope_type') = 0,
    'ALTER TABLE talk_tag ADD COLUMN scope_type VARCHAR(20) NOT NULL DEFAULT ''system'' COMMENT ''system/college'' AFTER tag_id', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'talk_tag' AND column_name = 'college_dept_id') = 0,
    'ALTER TABLE talk_tag ADD COLUMN college_dept_id BIGINT NULL COMMENT ''学院标签所属学院'' AFTER scope_type', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'talk_template' AND column_name = 'college_dept_id') = 0,
    'ALTER TABLE talk_template ADD COLUMN college_dept_id BIGINT NULL COMMENT ''学院模板所属学院'' AFTER template_id', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl = IF((SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'talk_session' AND index_name = 'idx_talk_session_college') = 0,
    'CREATE INDEX idx_talk_session_college ON talk_session(college_dept_id)', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 优先按参与学生所属学院回填历史会话，再按创建人所属学院兜底。
UPDATE talk_session ts
JOIN (
    SELECT tsr.session_id, MIN(college.dept_id) AS college_dept_id
    FROM talk_student_record tsr
    JOIN talk_student student ON student.student_id = tsr.student_id
    JOIN sys_dept student_dept ON student_dept.dept_id = student.dept_id
    JOIN sys_dept college ON college.dept_type = 'college'
        AND (college.dept_id = student_dept.dept_id OR FIND_IN_SET(college.dept_id, student_dept.ancestors))
    GROUP BY tsr.session_id
) source ON source.session_id = ts.session_id
SET ts.college_dept_id = source.college_dept_id
WHERE ts.college_dept_id IS NULL;

UPDATE talk_session ts
JOIN sys_user creator ON creator.user_name = ts.create_by
JOIN sys_dept creator_dept ON creator_dept.dept_id = creator.dept_id
JOIN sys_dept college ON college.dept_type = 'college'
    AND (college.dept_id = creator_dept.dept_id OR FIND_IN_SET(college.dept_id, creator_dept.ancestors))
SET ts.college_dept_id = college.dept_id
WHERE ts.college_dept_id IS NULL;

-- 已有定义全部转为系统内置；历史会话标签文本仍保存在 talk_session_tag.tag_value 中。
UPDATE talk_tag SET scope_type = 'system', college_dept_id = NULL;
UPDATE talk_template SET template_type = 'system', college_dept_id = NULL;

-- 2. 角色迁移：admin(内置)、school_admin、college_admin、talk_teacher
UPDATE sys_role
SET role_name = '校管理员', role_key = 'school_admin', role_sort = 2, data_scope = '1', status = '0',
    remark = '与内置admin拥有相同业务和系统权限，但不可修改内置admin账号'
WHERE role_id = 3 OR role_key = 'talk_admin';

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly,
                      status, del_flag, create_by, create_time, remark)
SELECT '院管理员', 'college_admin', 3, '4', 1, 1, '0', '0', 'admin', NOW(), '管理本学院业务、教师与班级'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'college_admin');

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly,
                      status, del_flag, create_by, create_time, remark)
SELECT '老师', 'talk_teacher', 4, '4', 1, 1, '0', '0', 'admin', NOW(), '查看本学院全部业务数据；岗位不参与授权'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'talk_teacher');

SET @schoolAdminRoleId = (SELECT role_id FROM sys_role WHERE role_key = 'school_admin' LIMIT 1);
SET @collegeAdminRoleId = (SELECT role_id FROM sys_role WHERE role_key = 'college_admin' LIMIT 1);
SET @teacherRoleId = (SELECT role_id FROM sys_role WHERE role_key = 'talk_teacher' LIMIT 1);

UPDATE sys_role SET data_scope = '4', status = '0' WHERE role_key IN ('college_admin', 'talk_teacher');

-- 书记/辅导员等旧角色账号统一迁移为老师。
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT DISTINCT ur.user_id, @teacherRoleId
FROM sys_user_role ur
JOIN sys_role role_old ON role_old.role_id = ur.role_id
WHERE role_old.role_key IN ('talk_secretary', 'talk_counselor');

DELETE ur FROM sys_user_role ur
JOIN sys_role role_old ON role_old.role_id = ur.role_id
WHERE role_old.role_key IN ('talk_secretary', 'talk_counselor');

-- 教师账号的部门必须直接指向学院；以教师资料为准同步一次。
UPDATE sys_user user_account
JOIN talk_teacher teacher ON teacher.user_id = user_account.user_id
JOIN sys_dept college ON college.dept_id = teacher.dept_id AND college.dept_type = 'college'
SET user_account.dept_id = college.dept_id;

UPDATE sys_role SET status = '1', remark = CONCAT(IFNULL(remark, ''), '；v9已停用并迁移至talk_teacher')
WHERE role_key IN ('talk_secretary', 'talk_counselor');

-- 学生端不上线：保留账号和数据，但禁用账号及角色。
UPDATE sys_user user_account
JOIN sys_user_role ur ON ur.user_id = user_account.user_id
JOIN sys_role student_role ON student_role.role_id = ur.role_id AND student_role.role_key = 'talk_student'
SET user_account.status = '1';
UPDATE sys_role SET status = '1', remark = CONCAT(IFNULL(remark, ''), '；v9学生端停用') WHERE role_key = 'talk_student';

-- 3. 菜单显隐与按钮权限
INSERT IGNORE INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
 menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
(2007, '学生信息导入', 2001, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'talk:student:import', '#', 'admin', NOW(), '', NULL, '');

-- 统一查询、统计分析上线；其余暂不上线模块只隐藏，不删除代码或数据。
UPDATE sys_menu SET visible = '1'
WHERE menu_id IN (2040, 2055, 2065, 2075, 2100)
   OR parent_id IN (2040, 2055, 2065, 2075, 2100);
UPDATE sys_menu SET visible = '0', status = '0'
WHERE menu_id IN (2050, 2070);

-- 校管理员拥有所有菜单；接口层同时授予 *:*:*。
DELETE FROM sys_role_menu WHERE role_id = @schoolAdminRoleId;
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT @schoolAdminRoleId, menu_id FROM sys_menu;

-- 院管理员：核心业务全部操作 + 本学院教师和组织（班级）管理。
DELETE FROM sys_role_menu WHERE role_id = @collegeAdminRoleId;
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(@collegeAdminRoleId, 1), (@collegeAdminRoleId, 103),
(@collegeAdminRoleId, 1016), (@collegeAdminRoleId, 1017), (@collegeAdminRoleId, 1018), (@collegeAdminRoleId, 1019),
(@collegeAdminRoleId, 2000),
(@collegeAdminRoleId, 2001), (@collegeAdminRoleId, 2002), (@collegeAdminRoleId, 2003), (@collegeAdminRoleId, 2004), (@collegeAdminRoleId, 2005), (@collegeAdminRoleId, 2006), (@collegeAdminRoleId, 2007),
(@collegeAdminRoleId, 2010), (@collegeAdminRoleId, 2011), (@collegeAdminRoleId, 2012), (@collegeAdminRoleId, 2013), (@collegeAdminRoleId, 2014), (@collegeAdminRoleId, 2015),
(@collegeAdminRoleId, 2020), (@collegeAdminRoleId, 2021), (@collegeAdminRoleId, 2022), (@collegeAdminRoleId, 2023), (@collegeAdminRoleId, 2024), (@collegeAdminRoleId, 2025),
(@collegeAdminRoleId, 2030), (@collegeAdminRoleId, 2050), (@collegeAdminRoleId, 2070),
(@collegeAdminRoleId, 2060), (@collegeAdminRoleId, 2061), (@collegeAdminRoleId, 2062), (@collegeAdminRoleId, 2063), (@collegeAdminRoleId, 2064),
(@collegeAdminRoleId, 2080), (@collegeAdminRoleId, 2081), (@collegeAdminRoleId, 2082), (@collegeAdminRoleId, 2083), (@collegeAdminRoleId, 2084),
(@collegeAdminRoleId, 2090), (@collegeAdminRoleId, 2091), (@collegeAdminRoleId, 2092), (@collegeAdminRoleId, 2093), (@collegeAdminRoleId, 2094), (@collegeAdminRoleId, 2095);

-- 老师：本学院学生可增改导入导出（不可删除）；谈话仅对本人创建的数据可写；学院标签模板可共同维护；教师名单只读。
DELETE FROM sys_role_menu WHERE role_id = @teacherRoleId;
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(@teacherRoleId, 2000),
(@teacherRoleId, 2001), (@teacherRoleId, 2002), (@teacherRoleId, 2003), (@teacherRoleId, 2004), (@teacherRoleId, 2006), (@teacherRoleId, 2007),
(@teacherRoleId, 2010), (@teacherRoleId, 2011), (@teacherRoleId, 2012), (@teacherRoleId, 2013), (@teacherRoleId, 2014), (@teacherRoleId, 2015),
(@teacherRoleId, 2020), (@teacherRoleId, 2021), (@teacherRoleId, 2022), (@teacherRoleId, 2023), (@teacherRoleId, 2024), (@teacherRoleId, 2025),
(@teacherRoleId, 2030), (@teacherRoleId, 2050), (@teacherRoleId, 2070),
(@teacherRoleId, 2060), (@teacherRoleId, 2061), (@teacherRoleId, 2062), (@teacherRoleId, 2063), (@teacherRoleId, 2064),
(@teacherRoleId, 2080), (@teacherRoleId, 2081), (@teacherRoleId, 2082), (@teacherRoleId, 2083), (@teacherRoleId, 2084),
(@teacherRoleId, 2090), (@teacherRoleId, 2091);

-- 检查仍未能归属学院的历史会话（结果应由管理员人工处理）。
SELECT session_id, talk_time, create_by FROM talk_session WHERE college_dept_id IS NULL;
