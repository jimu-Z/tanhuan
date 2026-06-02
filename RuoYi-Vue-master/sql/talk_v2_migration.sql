-- ----------------------------
-- 学生谈心谈话系统 V2 升级脚本
-- 新增: 标签管理表、附件表、通知字段
-- 注意：仅在首次部署时执行，请勿在生产环境重复执行
-- ----------------------------

-- ----------------------------
-- 1、谈话标签定义表（系统级标签管理）
-- ----------------------------
CREATE TABLE IF NOT EXISTS talk_tag (
  tag_id           BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '标签ID',
  tag_key          VARCHAR(50)     NOT NULL                   COMMENT '标签键',
  tag_name         VARCHAR(100)    NOT NULL                   COMMENT '标签名',
  sort_order       INT             DEFAULT 0                  COMMENT '排序',
  status           CHAR(1)         DEFAULT '0'                COMMENT '状态（0启用 1停用）',
  del_flag         CHAR(1)         DEFAULT '0'                COMMENT '删除标志（0存在 2删除）',
  create_by        VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
  create_time      DATETIME                                   COMMENT '创建时间',
  update_by        VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
  update_time      DATETIME                                   COMMENT '更新时间',
  PRIMARY KEY (tag_id),
  UNIQUE KEY uk_tag_key (tag_key)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '谈话标签定义表';

-- 初始化默认标签数据（从 TalkConstants 迁移）
INSERT INTO talk_tag (tag_key, tag_name, sort_order, status, del_flag, create_by, create_time) VALUES
('thought_education', '思想理论教育和价值引领', 1, '0', '0', 'admin', NOW()),
('party_class',       '党团和班级建设',          2, '0', '0', 'admin', NOW()),
('study_style',       '学风建设',                3, '0', '0', 'admin', NOW()),
('daily_affairs',     '日常事务',                4, '0', '0', 'admin', NOW()),
('mental_health',     '心理健康教育与咨询',      5, '0', '0', 'admin', NOW()),
('crisis_response',   '危机事件应对',            6, '0', '0', 'admin', NOW()),
('career_guidance',   '职业规划与就业创业指导',  7, '0', '0', 'admin', NOW());

-- ----------------------------
-- 2、谈话附件表
-- ----------------------------
CREATE TABLE IF NOT EXISTS talk_attachment (
  attachment_id    BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '附件ID',
  session_id       BIGINT(20)      NOT NULL                   COMMENT '会话ID',
  file_name        VARCHAR(255)    NOT NULL                   COMMENT '原始文件名',
  file_path        VARCHAR(500)    NOT NULL                   COMMENT '服务器存储路径',
  file_size        BIGINT(20)      DEFAULT 0                  COMMENT '文件大小（字节）',
  file_type        VARCHAR(100)    DEFAULT NULL               COMMENT 'MIME类型',
  sort_order       INT             DEFAULT 0                  COMMENT '排序',
  create_by        VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
  create_time      DATETIME                                   COMMENT '创建时间',
  PRIMARY KEY (attachment_id),
  KEY idx_att_session (session_id)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '谈话附件表';

-- ----------------------------
-- 3、谈话记录表增加通知字段
-- ----------------------------
ALTER TABLE talk_student_record
  ADD COLUMN notified          TINYINT(1) DEFAULT 0  COMMENT '是否已通知学生（0未通知 1已通知）',
  ADD COLUMN teacher_notified  TINYINT(1) DEFAULT 0  COMMENT '教师是否已查看反馈（0未查看 1已查看）';

-- ----------------------------
-- 4、新增学生角色（复用若依sys_user体系）
-- ----------------------------
INSERT INTO sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, update_by, update_time, remark)
VALUES (6, '学生', 'talk_student', 6, 5, 1, 1, '0', '0', 'admin', NOW(), '', NULL, '学生角色，仅可查看自己的谈话记录');
