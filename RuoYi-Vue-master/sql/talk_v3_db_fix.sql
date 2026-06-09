-- 修复表注释和列注释编码 + 添加缺失索引
-- 使用 ALTER TABLE COMMENT 修复3张表的注释

ALTER TABLE talk_teacher COMMENT = '教师信息表';
ALTER TABLE talk_alert COMMENT = '心理健康预警表';
ALTER TABLE talk_appointment COMMENT = '学生预约谈话表';

-- 修复 talk_teacher 列注释
ALTER TABLE talk_teacher MODIFY COLUMN teacher_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '教师ID';
ALTER TABLE talk_teacher MODIFY COLUMN teacher_code VARCHAR(50) NOT NULL COMMENT '工号';
ALTER TABLE talk_teacher MODIFY COLUMN teacher_name VARCHAR(50) NOT NULL COMMENT '姓名';
ALTER TABLE talk_teacher MODIFY COLUMN dept_id BIGINT(20) NOT NULL COMMENT '所属学院ID';
ALTER TABLE talk_teacher MODIFY COLUMN position VARCHAR(20) NOT NULL COMMENT '岗位';
ALTER TABLE talk_teacher MODIFY COLUMN phone VARCHAR(20) DEFAULT NULL COMMENT '手机号码';
ALTER TABLE talk_teacher MODIFY COLUMN user_id BIGINT(20) DEFAULT NULL COMMENT '关联的用户ID';
ALTER TABLE talk_teacher MODIFY COLUMN status CHAR(1) DEFAULT '0' COMMENT '状态';
ALTER TABLE talk_teacher MODIFY COLUMN create_by VARCHAR(64) DEFAULT '' COMMENT '创建者';
ALTER TABLE talk_teacher MODIFY COLUMN create_time DATETIME COMMENT '创建时间';
ALTER TABLE talk_teacher MODIFY COLUMN update_by VARCHAR(64) DEFAULT '' COMMENT '更新者';
ALTER TABLE talk_teacher MODIFY COLUMN update_time DATETIME COMMENT '更新时间';

-- 添加 remark 备注列
ALTER TABLE talk_teacher ADD COLUMN remark VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER status;

-- 修复 talk_alert 列注释
ALTER TABLE talk_alert MODIFY COLUMN alert_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '预警ID';
ALTER TABLE talk_alert MODIFY COLUMN student_id BIGINT(20) NOT NULL COMMENT '学生ID';
ALTER TABLE talk_alert MODIFY COLUMN alert_type VARCHAR(30) NOT NULL COMMENT '预警类型';
ALTER TABLE talk_alert MODIFY COLUMN alert_level VARCHAR(10) NOT NULL COMMENT '预警等级';
ALTER TABLE talk_alert MODIFY COLUMN alert_reason VARCHAR(500) DEFAULT NULL COMMENT '预警原因';
ALTER TABLE talk_alert MODIFY COLUMN alert_status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '处理状态';
ALTER TABLE talk_alert MODIFY COLUMN handler VARCHAR(50) DEFAULT NULL COMMENT '处理人';
ALTER TABLE talk_alert MODIFY COLUMN handle_time DATETIME DEFAULT NULL COMMENT '处理时间';
ALTER TABLE talk_alert MODIFY COLUMN handle_remark VARCHAR(500) DEFAULT NULL COMMENT '处理备注';
ALTER TABLE talk_alert MODIFY COLUMN create_by VARCHAR(64) DEFAULT '' COMMENT '创建者';
ALTER TABLE talk_alert MODIFY COLUMN create_time DATETIME COMMENT '创建时间';
ALTER TABLE talk_alert MODIFY COLUMN update_by VARCHAR(64) DEFAULT '' COMMENT '更新者';
ALTER TABLE talk_alert MODIFY COLUMN update_time DATETIME COMMENT '更新时间';

-- 修复 talk_appointment 列注释
ALTER TABLE talk_appointment MODIFY COLUMN appointment_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '预约ID';
ALTER TABLE talk_appointment MODIFY COLUMN student_id BIGINT(20) NOT NULL COMMENT '学生ID';
ALTER TABLE talk_appointment MODIFY COLUMN teacher_id BIGINT(20) NOT NULL COMMENT '教师ID';
ALTER TABLE talk_appointment MODIFY COLUMN appointment_time DATETIME NOT NULL COMMENT '预约时间';
ALTER TABLE talk_appointment MODIFY COLUMN location VARCHAR(100) DEFAULT NULL COMMENT '预约地点';
ALTER TABLE talk_appointment MODIFY COLUMN reason VARCHAR(500) DEFAULT NULL COMMENT '预约原因';
ALTER TABLE talk_appointment MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态';
ALTER TABLE talk_appointment MODIFY COLUMN reject_reason VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因';
ALTER TABLE talk_appointment MODIFY COLUMN session_id BIGINT(20) DEFAULT NULL COMMENT '关联的谈话会话ID';
ALTER TABLE talk_appointment MODIFY COLUMN create_by VARCHAR(64) DEFAULT '' COMMENT '创建者';
ALTER TABLE talk_appointment MODIFY COLUMN create_time DATETIME COMMENT '创建时间';
ALTER TABLE talk_appointment MODIFY COLUMN update_by VARCHAR(64) DEFAULT '' COMMENT '更新者';
ALTER TABLE talk_appointment MODIFY COLUMN update_time DATETIME COMMENT '更新时间';

-- 添加缺失索引（用存储过程实现幂等）
SET @sql_idx1 = IF((SELECT COUNT(1) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA='xuexiaotanhua' AND TABLE_NAME='talk_student' AND INDEX_NAME='idx_talk_student_idcard') = 0, 'CREATE INDEX idx_talk_student_idcard ON talk_student(id_card)', 'SELECT 1');
PREPARE stmt1 FROM @sql_idx1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SET @sql_idx2 = IF((SELECT COUNT(1) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA='xuexiaotanhua' AND TABLE_NAME='talk_session' AND INDEX_NAME='idx_talk_session_person') = 0, 'CREATE INDEX idx_talk_session_person ON talk_session(talk_person)', 'SELECT 1');
PREPARE stmt2 FROM @sql_idx2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;
