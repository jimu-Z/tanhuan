-- ----------------------------
-- 学生谈心谈话管理系统 - 数据库初始化脚本
-- ----------------------------

-- ----------------------------
-- 1、sys_dept 扩展 - 增加部门类型字段
-- ----------------------------
ALTER TABLE sys_dept ADD COLUMN dept_type VARCHAR(20) DEFAULT 'dept' COMMENT '部门类型: dept=默认 college=学院 grade=年级 class=班级';

-- ----------------------------
-- 2、学生基础信息表
-- ----------------------------
DROP TABLE IF EXISTS talk_student;
CREATE TABLE talk_student (
  student_id           BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '学生ID',
  student_code         VARCHAR(50)     NOT NULL                   COMMENT '学号',
  student_name         VARCHAR(50)     NOT NULL                   COMMENT '姓名',
  dept_id              BIGINT(20)      NOT NULL                   COMMENT '部门ID(班级)',
  gender               CHAR(1)         DEFAULT '0'                COMMENT '性别（0男 1女）',
  political_status     VARCHAR(20)     DEFAULT NULL               COMMENT '政治面貌',
  nation               VARCHAR(20)     DEFAULT NULL               COMMENT '民族',
  phone                VARCHAR(20)     DEFAULT NULL               COMMENT '本人联系电话',
  id_card              VARCHAR(18)     DEFAULT NULL               COMMENT '身份证号',
  address              VARCHAR(200)    DEFAULT NULL               COMMENT '家庭住址',
  father_name          VARCHAR(50)     DEFAULT NULL               COMMENT '父亲姓名',
  father_phone         VARCHAR(20)     DEFAULT NULL               COMMENT '父亲电话',
  mother_name          VARCHAR(50)     DEFAULT NULL               COMMENT '母亲姓名',
  mother_phone         VARCHAR(20)     DEFAULT NULL               COMMENT '母亲电话',
  class_monitor        VARCHAR(50)     DEFAULT NULL               COMMENT '班长',
  dorm_leader          VARCHAR(50)     DEFAULT NULL               COMMENT '舍长',
  dorm_building        VARCHAR(50)     DEFAULT NULL               COMMENT '宿舍楼',
  dorm_room            VARCHAR(20)     DEFAULT NULL               COMMENT '宿舍号',
  enrollment_status    VARCHAR(20)     DEFAULT NULL               COMMENT '学籍状态',
  mental_health_status VARCHAR(20)     DEFAULT NULL               COMMENT '心理健康状态',
  poverty_level        VARCHAR(20)     DEFAULT NULL               COMMENT '贫困等级认定',
  remark               VARCHAR(500)    DEFAULT NULL               COMMENT '备注',
  create_by            VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
  create_time          DATETIME                                   COMMENT '创建时间',
  update_by            VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
  update_time          DATETIME                                   COMMENT '更新时间',
  PRIMARY KEY (student_id),
  UNIQUE KEY uk_student_code (student_code),
  KEY idx_talk_student_dept (dept_id),
  KEY idx_talk_student_name (student_name)
) ENGINE=INNODB AUTO_INCREMENT=1 COMMENT = '学生基础信息表';

-- ----------------------------
-- 3、谈话会话表
-- ----------------------------
DROP TABLE IF EXISTS talk_session;
CREATE TABLE talk_session (
  session_id       BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '会话ID',
  talk_type        VARCHAR(20)     NOT NULL                   COMMENT '谈话类型（individual 个体谈话 / group 集体谈话）',
  talk_time        DATETIME        NOT NULL                   COMMENT '谈话时间',
  talk_location    VARCHAR(100)    DEFAULT NULL               COMMENT '谈话地点',
  talk_person      VARCHAR(50)     NOT NULL                   COMMENT '谈话人(默认当前班主任)',
  talk_content     TEXT                                       COMMENT '谈话内容（所有参与学生共享）',
  create_by        VARCHAR(64)     DEFAULT ''                 COMMENT '创建者',
  create_time      DATETIME                                   COMMENT '创建时间',
  update_by        VARCHAR(64)     DEFAULT ''                 COMMENT '更新者',
  update_time      DATETIME                                   COMMENT '更新时间',
  PRIMARY KEY (session_id),
  KEY idx_talk_session_type (talk_type),
  KEY idx_talk_session_time (talk_time),
  KEY idx_talk_session_create_by (create_by)
) ENGINE=INNODB AUTO_INCREMENT=1000 COMMENT = '谈话会话表';

-- ----------------------------
-- 4、学生谈话记录明细表（关联学生与会话）
-- ----------------------------
DROP TABLE IF EXISTS talk_student_record;
CREATE TABLE talk_student_record (
  record_id        BIGINT(20)      NOT NULL AUTO_INCREMENT    COMMENT '记录ID',
  session_id       BIGINT(20)      NOT NULL                   COMMENT '会话ID',
  student_id       BIGINT(20)      NOT NULL                   COMMENT '学生ID',
  student_feedback TEXT                                       COMMENT '学生反馈',
  followup_plan    VARCHAR(500)    NOT NULL DEFAULT ''        COMMENT '跟进计划',
  followup_status  VARCHAR(20)     DEFAULT 'pending'          COMMENT '跟进状态（none=无需跟进 pending=待跟进 in_progress=跟进中 completed=已完成）',
  create_time      DATETIME                                   COMMENT '创建时间',
  PRIMARY KEY (record_id),
  KEY idx_tsr_session (session_id),
  KEY idx_tsr_student (student_id),
  KEY idx_tsr_status (followup_status)
) ENGINE=INNODB AUTO_INCREMENT=1000 COMMENT = '学生谈话记录明细表';

-- ----------------------------
-- 5、会话-内容标签关联表
-- ----------------------------
DROP TABLE IF EXISTS talk_session_tag;
CREATE TABLE talk_session_tag (
  session_id       BIGINT(20)      NOT NULL                   COMMENT '会话ID',
  tag_value        VARCHAR(50)     NOT NULL                   COMMENT '标签值(对应 sys_dict_data.dict_value)',
  PRIMARY KEY (session_id, tag_value)
) ENGINE=INNODB COMMENT = '会话-内容标签关联表';

-- ----------------------------
-- 6、字典类型 - 谈话相关
-- ----------------------------
INSERT INTO sys_dict_type VALUES(11, '谈话内容标签',    'talk_content_tag',       '0', 'admin', sysdate(), '', null, '谈话内容标签（可多选）');
INSERT INTO sys_dict_type VALUES(12, '学籍状态',        'enrollment_status',      '0', 'admin', sysdate(), '', null, '学籍状态列表');
INSERT INTO sys_dict_type VALUES(13, '心理健康状态',    'mental_health_status',   '0', 'admin', sysdate(), '', null, '心理健康状态列表');
INSERT INTO sys_dict_type VALUES(14, '贫困等级认定',    'poverty_level',          '0', 'admin', sysdate(), '', null, '贫困等级认定列表');
INSERT INTO sys_dict_type VALUES(15, '跟进状态',        'followup_status',        '0', 'admin', sysdate(), '', null, '跟进状态列表');
INSERT INTO sys_dict_type VALUES(16, '谈话类型',        'talk_type',              '0', 'admin', sysdate(), '', null, '谈话类型列表');
INSERT INTO sys_dict_type VALUES(17, '政治面貌',        'political_status',       '0', 'admin', sysdate(), '', null, '政治面貌列表');
INSERT INTO sys_dict_type VALUES(18, '部门类型',        'dept_type',              '0', 'admin', sysdate(), '', null, '部门类型列表');

-- ----------------------------
-- 7、字典数据 - 谈话内容标签
-- ----------------------------
INSERT INTO sys_dict_data VALUES(30, 1, '思想理论教育和价值引领',   'thought_education',    'talk_content_tag', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(31, 2, '党团和班级建设',           'party_class',          'talk_content_tag', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(32, 3, '学风建设',                 'study_style',          'talk_content_tag', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(33, 4, '日常事务',                 'daily_affairs',        'talk_content_tag', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(34, 5, '心理健康教育与咨询',       'mental_health',        'talk_content_tag', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(35, 6, '危机事件应对',             'crisis_response',      'talk_content_tag', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(36, 7, '职业规划与就业创业指导',   'career_guidance',      'talk_content_tag', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 8、字典数据 - 学籍状态
-- ----------------------------
INSERT INTO sys_dict_data VALUES(40, 1, '在读',     'active',       'enrollment_status', '', '', 'Y', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(41, 2, '休学',     'suspended',    'enrollment_status', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(42, 3, '退学',     'withdrawn',    'enrollment_status', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(43, 4, '毕业',     'graduated',    'enrollment_status', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 9、字典数据 - 心理健康状态
-- ----------------------------
INSERT INTO sys_dict_data VALUES(50, 1, '正常',     'normal',           'mental_health_status', '', '', 'Y', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(51, 2, '周跟踪',   'weekly_track',     'mental_health_status', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(52, 3, '月跟踪',   'monthly_track',    'mental_health_status', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 10、字典数据 - 贫困等级认定
-- ----------------------------
INSERT INTO sys_dict_data VALUES(60, 1, '无',                     'none',       'poverty_level', '', '', 'Y', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(61, 2, '家庭经济一般困难',       'general',    'poverty_level', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(62, 3, '家庭经济困难',           'difficult',  'poverty_level', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(63, 4, '家庭经济特别困难',       'severe',     'poverty_level', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 11、字典数据 - 跟进状态
-- ----------------------------
INSERT INTO sys_dict_data VALUES(70, 1, '无需跟进',   'none',           'followup_status', '', '', 'Y', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(71, 2, '待跟进',     'pending',        'followup_status', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(72, 3, '跟进中',     'in_progress',    'followup_status', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(73, 4, '已完成',     'completed',      'followup_status', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 12、字典数据 - 谈话类型
-- ----------------------------
INSERT INTO sys_dict_data VALUES(80, 1, '个体谈话',   'individual',     'talk_type', '', '', 'Y', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(81, 2, '集体谈话',   'group',          'talk_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 13、字典数据 - 政治面貌
-- ----------------------------
INSERT INTO sys_dict_data VALUES(90, 1, '群众',       'people',             'political_status', '', '', 'Y', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(91, 2, '共青团员',   'league_member',      'political_status', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(92, 3, '中共预备党员', 'probationary_member','political_status', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(93, 4, '中共党员',   'party_member',       'political_status', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(94, 5, '其他',       'other',              'political_status', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 14、字典数据 - 部门类型
-- ----------------------------
INSERT INTO sys_dict_data VALUES(100, 1, '学院',     'college',      'dept_type', '', '', 'Y', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(101, 2, '年级',     'grade',        'dept_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
INSERT INTO sys_dict_data VALUES(102, 3, '班级',     'class',        'dept_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- ----------------------------
-- 15、系统角色 - 谈心谈话系统
-- ----------------------------
INSERT INTO sys_role VALUES(3, '系统管理员', 'talk_admin',      1, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '谈心谈话系统管理员，拥有所有权限');
INSERT INTO sys_role VALUES(4, '书记/副书记', 'talk_secretary',  2, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '查看本学院所有学生谈话记录');
INSERT INTO sys_role VALUES(5, '辅导员/班主任', 'talk_counselor', 3, 5, 1, 1, '0', '0', 'admin', sysdate(), '', null, '仅查看自己负责谈话的学生记录');

-- ========== ���ݱ��ݶ�ʱ���� ==========

-- ���ӱ������� Quartz ����
INSERT INTO sys_job VALUES(100, 'talkBackup', 'ryTask', 'talkBackup', '0 0 2 * * ?', '0', '0', '0', 'admin', NOW(), '', NULL, '̸��̸�����ݱ���');

