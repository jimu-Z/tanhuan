-- 修复 dict_type 中剩余的破损中文
UPDATE sys_dict_type SET dict_name = '谈话内容标签', remark = '谈话内容标签（可多选）' WHERE dict_type = 'talk_content_tag';

UPDATE sys_dict_type SET remark = '教师岗位列表' WHERE dict_type = 'teacher_position';
UPDATE sys_dict_type SET remark = '预警等级列表' WHERE dict_type = 'alert_level';
UPDATE sys_dict_type SET remark = '预警类型列表' WHERE dict_type = 'alert_type';
UPDATE sys_dict_type SET remark = '预警处理状态列表' WHERE dict_type = 'alert_status';
UPDATE sys_dict_type SET remark = '预约状态列表' WHERE dict_type = 'appointment_status';
UPDATE sys_dict_type SET remark = '心理健康敏感关键词' WHERE dict_type = 'alert_keywords';
