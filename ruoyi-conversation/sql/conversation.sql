-- ----------------------------
-- 学校谈话管理系统 DDL
-- ----------------------------

-- 专业表
DROP TABLE IF EXISTS `biz_major`;
CREATE TABLE `biz_major` (
  `major_id` bigint NOT NULL AUTO_INCREMENT COMMENT '专业ID',
  `major_name` varchar(100) NOT NULL COMMENT '专业名称',
  `major_code` varchar(50) DEFAULT NULL COMMENT '专业代码',
  `dept_id` bigint NOT NULL COMMENT '所属学院ID(sys_dept)',
  `status` char(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`major_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_major_name` (`major_name`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='专业表';


-- 班级表
DROP TABLE IF EXISTS `biz_class`;
CREATE TABLE `biz_class` (
  `class_id` bigint NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `class_name` varchar(100) NOT NULL COMMENT '班级名称',
  `class_code` varchar(50) DEFAULT NULL COMMENT '班级代码',
  `major_id` bigint NOT NULL COMMENT '所属专业ID',
  `grade` varchar(20) DEFAULT NULL COMMENT '年级(如2024级)',
  `status` char(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`class_id`),
  KEY `idx_major_id` (`major_id`),
  KEY `idx_class_name` (`class_name`),
  KEY `idx_grade` (`grade`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='班级表';


-- 学生信息表
DROP TABLE IF EXISTS `biz_student`;
CREATE TABLE `biz_student` (
  `student_id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生ID(主键)',
  `student_no` varchar(50) NOT NULL COMMENT '学号',
  `student_name` varchar(50) NOT NULL COMMENT '姓名',
  `gender` char(1) DEFAULT '0' COMMENT '性别(0男 1女 2未知)',
  `college_name` varchar(100) DEFAULT NULL COMMENT '学院名称(冗余)',
  `major_name` varchar(100) DEFAULT NULL COMMENT '专业名称(冗余)',
  `class_name` varchar(100) DEFAULT NULL COMMENT '班级名称(冗余)',
  `dept_id` bigint DEFAULT NULL COMMENT '学院ID',
  `major_id` bigint DEFAULT NULL COMMENT '专业ID',
  `class_id` bigint DEFAULT NULL COMMENT '班级ID',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
  `dormitory` varchar(100) DEFAULT NULL COMMENT '宿舍号',
  `status` char(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志(0正常 2删除)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  KEY `idx_student_name` (`student_name`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_major_id` (`major_id`),
  KEY `idx_class_id` (`class_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学生信息表';


-- 谈话记录表
DROP TABLE IF EXISTS `biz_conversation`;
CREATE TABLE `biz_conversation` (
  `conversation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '谈话记录ID',
  `student_no` varchar(50) NOT NULL COMMENT '学号',
  `student_name` varchar(50) NOT NULL COMMENT '学生姓名(快照)',
  `college_name` varchar(100) DEFAULT NULL COMMENT '学院名称(快照)',
  `major_name` varchar(100) DEFAULT NULL COMMENT '专业名称(快照)',
  `class_name` varchar(100) DEFAULT NULL COMMENT '班级名称(快照)',
  `dept_id` bigint DEFAULT NULL COMMENT '学院ID(快照,用于权限过滤)',
  `conversation_time` datetime NOT NULL COMMENT '谈话时间',
  `conversation_place` varchar(200) NOT NULL COMMENT '谈话地点',
  `speaker` varchar(50) NOT NULL COMMENT '谈话人',
  `topic` varchar(200) NOT NULL COMMENT '谈话主题',
  `content` text COMMENT '谈话内容',
  `follow_up_items` varchar(1000) DEFAULT NULL COMMENT '后续跟进事项',
  `status` char(1) DEFAULT '0' COMMENT '状态(0未跟进 1已跟进 2已完成)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志(0正常 2删除)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者(操作人)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`conversation_id`),
  KEY `idx_student_no` (`student_no`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_conversation_time` (`conversation_time`),
  KEY `idx_status` (`status`),
  KEY `idx_create_by` (`create_by`),
  KEY `idx_del_flag` (`del_flag`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='谈话记录表';


-- 谈话跟进记录表
DROP TABLE IF EXISTS `biz_conversation_follow_up`;
CREATE TABLE `biz_conversation_follow_up` (
  `follow_id` bigint NOT NULL AUTO_INCREMENT COMMENT '跟进记录ID',
  `conversation_id` bigint NOT NULL COMMENT '关联谈话记录ID',
  `follow_time` datetime NOT NULL COMMENT '跟进时间',
  `follow_content` text COMMENT '跟进内容',
  `follow_status` char(1) DEFAULT '1' COMMENT '跟进后状态(0未跟进 1已跟进 2已完成)',
  `follow_by` varchar(64) DEFAULT '' COMMENT '跟进人',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`follow_id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_follow_time` (`follow_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='谈话跟进记录表';


-- 辅导员-班级关联表
DROP TABLE IF EXISTS `sys_user_class`;
CREATE TABLE `sys_user_class` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  PRIMARY KEY (`user_id`,`class_id`),
  KEY `idx_class_id` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户与班级关联表';


-- 导出任务表
DROP TABLE IF EXISTS `biz_export_task`;
CREATE TABLE `biz_export_task` (
  `task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` varchar(200) NOT NULL COMMENT '任务名称',
  `export_type` varchar(20) NOT NULL COMMENT '导出类型(xls/pdf)',
  `export_params` text COMMENT '导出条件(JSON)',
  `file_name` varchar(500) DEFAULT NULL COMMENT '文件名',
  `file_path` varchar(1000) DEFAULT NULL COMMENT '文件存储路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `status` char(1) DEFAULT '0' COMMENT '状态(0待处理 1处理中 2已完成 3失败)',
  `error_msg` varchar(2000) DEFAULT NULL COMMENT '错误信息',
  `record_count` int DEFAULT 0 COMMENT '导出记录数',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`task_id`),
  KEY `idx_create_by` (`create_by`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='导出任务表';


-- 备份日志表
DROP TABLE IF EXISTS `biz_backup_log`;
CREATE TABLE `biz_backup_log` (
  `backup_id` bigint NOT NULL AUTO_INCREMENT COMMENT '备份ID',
  `backup_type` char(1) NOT NULL COMMENT '备份类型(0手动 1自动)',
  `file_name` varchar(500) NOT NULL COMMENT '备份文件名',
  `file_path` varchar(1000) NOT NULL COMMENT '备份文件路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `status` char(1) DEFAULT '1' COMMENT '状态(0失败 1成功)',
  `error_msg` varchar(2000) DEFAULT NULL COMMENT '错误信息',
  `duration` bigint DEFAULT NULL COMMENT '耗时(毫秒)',
  `create_by` varchar(64) DEFAULT '' COMMENT '操作人',
  `create_time` datetime DEFAULT NULL COMMENT '备份时间',
  PRIMARY KEY (`backup_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='备份日志表';


-- ----------------------------
-- 初始数据：角色
-- ----------------------------
-- 学院书记角色
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT 100, '学院书记', 'college_secretary', 3, '2', '0', '0', 'admin', sysdate(), '学院书记，可查看本学院所有数据'
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_id` = 100);

-- 学院副书记角色
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT 101, '学院副书记', 'college_vice_secretary', 4, '2', '0', '0', 'admin', sysdate(), '学院副书记，可查看本学院所有数据'
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_id` = 101);

-- 辅导员角色
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
SELECT 102, '辅导员', 'counselor', 5, '5', '0', '0', 'admin', sysdate(), '辅导员，仅查看自己负责的班级谈话记录'
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_id` = 102);


-- ----------------------------
-- 初始数据：菜单
-- ----------------------------
-- 根菜单：谈话管理
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2000, '谈话管理', 0, 1, 'conversation', NULL, NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'education', 'admin', sysdate(), '', NULL, '谈话管理根菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2000);

-- 学生台账管理
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2001, '学生台账', 2000, 1, 'student', NULL, NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'people', 'admin', sysdate(), '', NULL, '学生台账管理'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2001);

-- 学生信息管理
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2002, '学生信息', 2001, 1, 'info', 'conversation/student/index', NULL, NULL, 1, 0, 'C', '0', '0', 'conversation:student:list', '#', 'admin', sysdate(), '', NULL, '学生信息管理菜单'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2002);

-- 学生信息操作按钮权限
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2003, '学生查询', 2002, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'conversation:student:query', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2003);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2004, '学生新增', 2002, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'conversation:student:add', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2004);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2005, '学生修改', 2002, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'conversation:student:edit', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2005);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2006, '学生删除', 2002, 4, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'conversation:student:remove', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2006);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2007, '学生导入', 2002, 5, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'conversation:student:import', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2007);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2008, '学生导出', 2002, 6, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'conversation:student:export', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2008);

-- 专业管理
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2009, '专业管理', 2001, 2, 'major', 'conversation/major/index', NULL, NULL, 1, 0, 'C', '0', '0', 'conversation:major:list', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2009);

-- 班级管理
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2010, '班级管理', 2001, 3, 'class', 'conversation/class/index', NULL, NULL, 1, 0, 'C', '0', '0', 'conversation:class:list', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2010);

-- 谈话记录管理根菜单
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2020, '谈话记录', 2000, 2, 'record', NULL, NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'form', 'admin', sysdate(), '', NULL, '谈话记录管理'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2020);

-- 新建谈话
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2021, '新建谈话', 2020, 1, 'add', 'conversation/record/add', NULL, NULL, 1, 0, 'C', '0', '0', 'conversation:record:add', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2021);

-- 谈话记录查询
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2022, '记录查询', 2020, 2, 'list', 'conversation/record/list', NULL, NULL, 1, 0, 'C', '0', '0', 'conversation:record:list', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2022);

-- 我的谈话记录
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2023, '我的记录', 2020, 3, 'my', 'conversation/record/my', NULL, NULL, 1, 0, 'C', '0', '0', 'conversation:record:my', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2023);

-- 数据统计根菜单
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2030, '数据统计', 2000, 3, 'statistics', NULL, NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'chart', 'admin', sysdate(), '', NULL, '数据统计'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2030);

-- 学院谈话统计
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2031, '学院统计', 2030, 1, 'college', 'conversation/statistics/college', NULL, NULL, 1, 0, 'C', '0', '0', 'conversation:statistics:college', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2031);

-- 导出中心根菜单
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2040, '导出中心', 2000, 4, 'export', NULL, NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'download', 'admin', sysdate(), '', NULL, '导出中心'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2040);

-- 导出任务列表
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2041, '导出任务', 2040, 1, 'task', 'conversation/export/task', NULL, NULL, 1, 0, 'C', '0', '0', 'conversation:export:task', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2041);

-- 系统运维根菜单
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2050, '系统运维', 2000, 5, 'ops', NULL, NULL, NULL, 1, 0, 'M', '0', '0', NULL, 'server', 'admin', sysdate(), '', NULL, '系统运维'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2050);

-- 数据备份
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2051, '数据备份', 2050, 1, 'backup', 'conversation/ops/backup', NULL, NULL, 1, 0, 'C', '0', '0', 'conversation:ops:backup', '#', 'admin', sysdate(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2051);

-- Dashboard 工作台
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
SELECT 2060, '工作台', 2000, 0, 'dashboard', 'conversation/dashboard/index', NULL, NULL, 1, 0, 'C', '0', '0', 'conversation:dashboard', 'dashboard', 'admin', sysdate(), '', NULL, '谈话管理工作台'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2060);


-- ----------------------------
-- 初始数据：角色菜单权限关联
-- ----------------------------
-- 为学院书记角色分配菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 100, menu_id FROM `sys_menu` WHERE menu_id BETWEEN 2000 AND 2060 AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 100 AND `menu_id` = `sys_menu`.`menu_id`);

-- 为学院副书记角色分配菜单（与书记相同）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 101, menu_id FROM `sys_menu` WHERE menu_id BETWEEN 2000 AND 2060 AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 101 AND `menu_id` = `sys_menu`.`menu_id`);

-- 为辅导员角色分配菜单（无导出中心和系统运维权限）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 102, menu_id FROM `sys_menu`
WHERE menu_id IN (2001, 2002, 2003, 2004, 2005, 2006, 2007, 2020, 2021, 2022, 2023, 2030, 2031, 2060)
AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 102 AND `menu_id` = `sys_menu`.`menu_id`);