-- 修复 V3 中文编码问题
-- 菜单名称修复
UPDATE sys_menu SET menu_name = '教师管理' WHERE menu_id = 2090;
UPDATE sys_menu SET menu_name = '教师查询' WHERE menu_id = 2091;
UPDATE sys_menu SET menu_name = '教师新增' WHERE menu_id = 2092;
UPDATE sys_menu SET menu_name = '教师修改' WHERE menu_id = 2093;
UPDATE sys_menu SET menu_name = '教师删除' WHERE menu_id = 2094;
UPDATE sys_menu SET menu_name = '教师导入' WHERE menu_id = 2095;
UPDATE sys_menu SET menu_name = '预约管理' WHERE menu_id = 2100;
UPDATE sys_menu SET menu_name = '预约查询' WHERE menu_id = 2101;
UPDATE sys_menu SET menu_name = '预约处理' WHERE menu_id = 2102;

-- 字典类型名修复
UPDATE sys_dict_type SET dict_name = '教师岗位' WHERE dict_type = 'teacher_position';
UPDATE sys_dict_type SET dict_name = '预警等级' WHERE dict_type = 'alert_level';
UPDATE sys_dict_type SET dict_name = '预警类型' WHERE dict_type = 'alert_type';
UPDATE sys_dict_type SET dict_name = '预警处理状态' WHERE dict_type = 'alert_status';
UPDATE sys_dict_type SET dict_name = '预约状态' WHERE dict_type = 'appointment_status';
UPDATE sys_dict_type SET dict_name = '敏感关键词' WHERE dict_type = 'alert_keywords';

-- 心理健康状态修复
DELETE FROM sys_dict_data WHERE dict_type = 'mental_health_status';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(50, 1, '健康', 'healthy', 'mental_health_status', '', '', 'Y', '0', 'admin', NOW(), '', NULL, ''),
(51, 2, '轻度预警', 'mild_warning', 'mental_health_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(52, 3, '中度预警', 'moderate_warning', 'mental_health_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(53, 4, '重度预警', 'severe_warning', 'mental_health_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 教师岗位修复
DELETE FROM sys_dict_data WHERE dict_type = 'teacher_position';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(110, 1, '辅导员', 'counselor', 'teacher_position', '', '', 'Y', '0', 'admin', NOW(), '', NULL, ''),
(111, 2, '班主任', 'head_teacher', 'teacher_position', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(112, 3, '副书记', 'deputy_secretary', 'teacher_position', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(113, 4, '书记', 'secretary', 'teacher_position', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 预警等级修复
DELETE FROM sys_dict_data WHERE dict_type = 'alert_level';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(120, 1, '红色预警', 'red', 'alert_level', 'danger', '', 'N', '0', 'admin', NOW(), '', NULL, '严重'),
(121, 2, '橙色预警', 'orange', 'alert_level', 'warning', '', 'N', '0', 'admin', NOW(), '', NULL, '较重'),
(122, 3, '黄色预警', 'yellow', 'alert_level', 'primary', '', 'N', '0', 'admin', NOW(), '', NULL, '一般');

-- 预警类型修复
DELETE FROM sys_dict_data WHERE dict_type = 'alert_type';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(130, 1, '自动预警', 'auto', 'alert_type', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '根据心理健康状态自动生成'),
(131, 2, '手动预警', 'manual', 'alert_type', '', '', 'N', '0', 'admin', NOW(), '', NULL, '教师手动标记'),
(132, 3, '关键词预警', 'keyword', 'alert_type', '', '', 'N', '0', 'admin', NOW(), '', NULL, '学生反馈检测到敏感词');

-- 预警处理状态修复
DELETE FROM sys_dict_data WHERE dict_type = 'alert_status';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(140, 1, '待处理', 'pending', 'alert_status', '', '', 'Y', '0', 'admin', NOW(), '', NULL, ''),
(141, 2, '处理中', 'in_progress', 'alert_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(142, 3, '已解除', 'resolved', 'alert_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 预约状态修复
DELETE FROM sys_dict_data WHERE dict_type = 'appointment_status';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(150, 1, '待确认', 'pending', 'appointment_status', 'info', '', 'Y', '0', 'admin', NOW(), '', NULL, ''),
(151, 2, '已确认', 'confirmed', 'appointment_status', 'success', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(152, 3, '已拒绝', 'rejected', 'appointment_status', 'danger', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(153, 4, '已完成', 'completed', 'appointment_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(154, 5, '已取消', 'cancelled', 'appointment_status', 'warning', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 敏感关键词修复
DELETE FROM sys_dict_data WHERE dict_type = 'alert_keywords';
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(160, 1, '自杀', 'suicide', 'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(161, 2, '自残', 'self_harm', 'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(162, 3, '抑郁', 'depression', 'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(163, 4, '焦虑', 'anxiety', 'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(164, 5, '绝望', 'despair', 'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(165, 6, '不想活了', 'dont_want_to_live', 'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(166, 7, '想死', 'want_to_die', 'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(167, 8, '崩溃', 'breakdown', 'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(168, 9, '失眠', 'insomnia', 'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, ''),
(169, 10, '厌世', 'misanthropy', 'alert_keywords', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
