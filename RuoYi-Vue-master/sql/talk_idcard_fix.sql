-- 修复身份证号字段长度不足的问题
-- 原因：部分学生身份证号为19位（可能是15位旧号升级或特殊格式），原VARCHAR(18)不够
ALTER TABLE talk_student MODIFY COLUMN id_card VARCHAR(20) DEFAULT NULL COMMENT '身份证号';
