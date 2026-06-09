-- ============================================================
-- PROTOTYPE: V3 数据库变更脚本（验证数据模型用，完成后删除）
-- 问题：talk_teacher / talk_alert / talk_appointment 表结构是否合理？
-- ============================================================

-- ----------------------------
-- 1. 教师信息表 talk_teacher
-- ----------------------------
CREATE TABLE IF NOT EXISTS talk_teacher (
  teacher_id      BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '教师ID',
  teacher_code    VARCHAR(50)     NOT NULL                   COMMENT '工号',
  teacher_name    VARCHAR(50)     NOT NULL                   COMMENT '姓名',
  dept_id         BIGINT(20)      NOT NULL                   COMMENT '所属学院ID(sys_dept.dept_id)',
  position        VARCHAR(20)     NOT NULL                   COMMENT '岗位：counselor/head_teacher/deputy_secretary/secretary',
  phone           VARCHAR(20)     DEFAULT NULL               COMMENT '手机号码',
  user_id         BIGINT(20)      DEFAULT NULL               COMMENT '关联的sys_user.userId',
  status          CHAR(1)         DEFAULT '0'                COMMENT '状态（0正常 1停用）',
  remark          VARCHAR(500)    DEFAULT NULL               COMMENT '备注',
  create_by       VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
  create_time     DATETIME                                   COMMENT '创建时间',
  update_by       VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
  update_time     DATETIME                                   COMMENT '更新时间',
  PRIMARY KEY (teacher_id),
  UNIQUE KEY uk_teacher_code (teacher_code),
  KEY idx_teacher_dept (dept_id),
  KEY idx_teacher_name (name),
  KEY idx_teacher_user (user_id)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '教师信息表';

-- ----------------------------
-- 2. 心理健康预警表 talk_alert
-- ----------------------------
CREATE TABLE IF NOT EXISTS talk_alert (
  alert_id         BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '预警ID',
  student_id       BIGINT(20)      NOT NULL                   COMMENT '学生ID',
  alert_type       VARCHAR(30)     NOT NULL                   COMMENT '预警类型：auto(自动) / manual(手动) / keyword(关键词)',
  alert_level      VARCHAR(10)     NOT NULL                   COMMENT '预警等级：red(红) / orange(橙) / yellow(黄)',
  alert_reason     VARCHAR(500)    DEFAULT NULL               COMMENT '预警原因',
  alert_status     VARCHAR(20)     NOT NULL DEFAULT 'pending' COMMENT '处理状态：pending(待处理) / in_progress(处理中) / resolved(已解除)',
  handler          VARCHAR(50)     DEFAULT NULL               COMMENT '处理人',
  handle_time      DATETIME        DEFAULT NULL               COMMENT '处理时间',
  handle_remark    VARCHAR(500)    DEFAULT NULL               COMMENT '处理备注',
  create_by        VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
  create_time      DATETIME                                   COMMENT '创建时间',
  update_by        VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
  update_time      DATETIME                                   COMMENT '更新时间',
  PRIMARY KEY (alert_id),
  KEY idx_alert_student (student_id),
  KEY idx_alert_level (alert_level),
  KEY idx_alert_status (alert_status),
  KEY idx_alert_type (alert_type)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '心理健康预警表';

-- ----------------------------
-- 3. 学生预约谈话表 talk_appointment
-- ----------------------------
CREATE TABLE IF NOT EXISTS talk_appointment (
  appointment_id    BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '预约ID',
  student_id        BIGINT(20)      NOT NULL                   COMMENT '学生ID',
  teacher_id        BIGINT(20)      NOT NULL                   COMMENT '教师ID(被预约的辅导员/班主任)',
  appointment_time  DATETIME        NOT NULL                   COMMENT '预约时间',
  location          VARCHAR(100)    DEFAULT NULL               COMMENT '预约地点',
  reason            VARCHAR(500)    DEFAULT NULL               COMMENT '预约原因/简述',
  status            VARCHAR(20)     NOT NULL DEFAULT 'pending' COMMENT '状态：pending(待确认) / confirmed(已确认) / rejected(已拒绝) / completed(已完成) / cancelled(已取消)',
  reject_reason     VARCHAR(500)    DEFAULT NULL               COMMENT '拒绝原因',
  session_id        BIGINT(20)      DEFAULT NULL               COMMENT '确认后关联的谈话会话ID',
  create_by         VARCHAR(64)     DEFAULT ''                 COMMENT '创建者（学生用户名）',
  create_time       DATETIME                                   COMMENT '创建时间',
  update_by         VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
  update_time       DATETIME                                   COMMENT '更新时间',
  PRIMARY KEY (appointment_id),
  KEY idx_appt_student (student_id),
  KEY idx_appt_teacher (teacher_id),
  KEY idx_appt_status (status),
  KEY idx_appt_session (session_id)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '学生预约谈话表';

-- ----------------------------
-- 4. 新增字段：talk_student_record.original_student_feedback
-- ----------------------------
ALTER TABLE talk_student_record
  ADD COLUMN original_student_feedback TEXT COMMENT '学生原始反馈（教师修改后仍保留）';

-- ----------------------------
-- 5. 修改字典：mental_health_status 替换
-- ----------------------------
-- 删除旧值
DELETE FROM sys_dict_data WHERE dict_type = 'mental_health_status';
-- 插入新值
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(50, 1, '健康',     'healthy',      'mental_health_status', '', '', 'Y', '0', 'admin', NOW(), '', NULL, ''),
(51, 2, '轻度预警', 'mild_warning',   'mental_health_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(52, 3, '中度预警', 'moderate_warning','mental_health_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(53, 4, '重度预警', 'severe_warning',  'mental_health_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 6. 新建字典类型
-- ----------------------------
INSERT IGNORE INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) VALUES
(19, '教师岗位',     'teacher_position',   '0', 'admin', NOW(), '', NULL, '教师岗位列表'),
(20, '预警等级',     'alert_level',        '0', 'admin', NOW(), '', NULL, '预警等级列表'),
(21, '预警类型',     'alert_type',         '0', 'admin', NOW(), '', NULL, '预警类型列表'),
(22, '预警处理状态', 'alert_status',       '0', 'admin', NOW(), '', NULL, '预警处理状态列表'),
(23, '预约状态',     'appointment_status',  '0', 'admin', NOW(), '', NULL, '预约状态列表'),
(24, '敏感关键词',   'alert_keywords',     '0', 'admin', NOW(), '', NULL, '心理健康敏感关键词');

-- ----------------------------
-- 7. 新建字典数据
-- ----------------------------
-- 教师岗位
INSERT IGNORE INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(110, 1, '辅导员',  'counselor',         'teacher_position', '', '', 'Y', '0', 'admin', NOW(), '', NULL, ''),
(111, 2, '班主任',  'head_teacher',      'teacher_position', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(112, 3, '副书记',  'deputy_secretary',  'teacher_position', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(113, 4, '书记',    'secretary',         'teacher_position', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 预警等级
INSERT IGNORE INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(120, 1, '红色预警', 'red',    'alert_level', 'danger',  '', 'N', '0', 'admin', NOW(), '', NULL, '严重'),
(121, 2, '橙色预警', 'orange', 'alert_level', 'warning', '', 'N', '0', 'admin', NOW(), '', NULL, '较重'),
(122, 3, '黄色预警', 'yellow', 'alert_level', 'primary', '', 'N', '0', 'admin', NOW(), '', NULL, '一般');

-- 预警类型
INSERT IGNORE INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(130, 1, '自动预警',   'auto',      'alert_type', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '根据心理健康状态自动生成'),
(131, 2, '手动预警',   'manual',    'alert_type', '', '', 'N', '0', 'admin', NOW(), '', NULL, '教师手动标记'),
(132, 3, '关键词预警', 'keyword',   'alert_type', '', '', 'N', '0', 'admin', NOW(), '', NULL, '学生反馈检测到敏感词');

-- 预警处理状态
INSERT IGNORE INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(140, 1, '待处理', 'pending',     'alert_status', '', '', 'Y', '0', 'admin', NOW(), '', NULL, ''),
(141, 2, '处理中', 'in_progress', 'alert_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(142, 3, '已解除', 'resolved',    'alert_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 预约状态
INSERT IGNORE INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(150, 1, '待确认',  'pending',    'appointment_status', 'info',    '', 'Y', '0', 'admin', NOW(), '', NULL, ''),
(151, 2, '已确认',  'confirmed',  'appointment_status', 'success', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(152, 3, '已拒绝',  'rejected',   'appointment_status', 'danger',  '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(153, 4, '已完成',  'completed',  'appointment_status', '',        '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(154, 5, '已取消',  'cancelled',  'appointment_status', 'warning', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 敏感关键词（初始数据）
INSERT IGNORE INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(160, 1, '自杀',     'suicide',         'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(161, 2, '自残',     'self_harm',       'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(162, 3, '抑郁',     'depression',      'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(163, 4, '焦虑',     'anxiety',         'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(164, 5, '绝望',     'despair',         'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(165, 6, '不想活了', 'dont_want_to_live','alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(166, 7, '想死',     'want_to_die',     'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(167, 8, '崩溃',     'breakdown',       'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(168, 9, '失眠',     'insomnia',        'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(169, 10,'厌世',     'misanthropy',     'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 8. 修复：talk_content_tag 字典类型名乱码
-- ----------------------------
UPDATE sys_dict_type SET dict_name = '谈话内容标签' WHERE dict_type = 'talk_content_tag';

-- ----------------------------
-- 9. 新增菜单：教师管理（管理员+书记可见）
-- ----------------------------
-- 教师管理主菜单
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(2090, '教师管理', 2000, 10, 'teacher', 'talk/teacher/index', NULL, NULL, 1, 0, 'C', '0', '0', 'talk:teacher:list', 'user', 'admin', NOW(), '', NULL, '教师信息管理菜单');

-- 教师管理子权限
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(2091, '教师查询', 2090, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:teacher:query',  '#', 'admin', NOW(), '', NULL, ''),
(2092, '教师新增', 2090, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:teacher:add',    '#', 'admin', NOW(), '', NULL, ''),
(2093, '教师修改', 2090, 3, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:teacher:edit',   '#', 'admin', NOW(), '', NULL, ''),
(2094, '教师删除', 2090, 4, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:teacher:remove', '#', 'admin', NOW(), '', NULL, ''),
(2095, '教师导入', 2090, 5, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:teacher:import', '#', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 10. 新增菜单：预约管理（管理员+学生可见）
-- ----------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(2100, '预约管理', 2000, 11, 'appointment', 'talk/appointment/index', NULL, NULL, 1, 0, 'C', '0', '0', 'talk:appointment:list', 'date', 'admin', NOW(), '', NULL, '预约管理菜单');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
(2101, '预约查询', 2100, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:appointment:query',  '#', 'admin', NOW(), '', NULL, ''),
(2102, '预约处理', 2100, 2, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'talk:appointment:handle', '#', 'admin', NOW(), '', NULL, '');

-- ----------------------------
-- 11. 角色菜单授权
-- ----------------------------
-- 管理员拥有全部权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(3, 2090), (3, 2091), (3, 2092), (3, 2093), (3, 2094), (3, 2095),
(3, 2100), (3, 2101), (3, 2102);

-- 书记拥有教师管理权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(4, 2090), (4, 2091), (4, 2092), (4, 2093), (4, 2094);

-- 学生拥有预约管理权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(6, 2100), (6, 2101);

-- ============================================================
-- PROTOTYPE END
-- ============================================================
