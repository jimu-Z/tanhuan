-- 谈心谈话系统 - 补全脚本（修复缺失的字典和角色）

-- 字典类型
INSERT INTO sys_dict_type VALUES(11, '谈话内容标签',    'talk_content_tag',       '0', 'admin', NOW(), '', NULL, '谈话内容标签（可多选）');
INSERT INTO sys_dict_type VALUES(12, '学籍状态',        'enrollment_status',      '0', 'admin', NOW(), '', NULL, '学籍状态列表');
INSERT INTO sys_dict_type VALUES(13, '心理健康状态',    'mental_health_status',   '0', 'admin', NOW(), '', NULL, '心理健康状态列表');
INSERT INTO sys_dict_type VALUES(14, '贫困等级认定',    'poverty_level',          '0', 'admin', NOW(), '', NULL, '贫困等级认定列表');
INSERT INTO sys_dict_type VALUES(15, '跟进状态',        'followup_status',        '0', 'admin', NOW(), '', NULL, '跟进状态列表');
INSERT INTO sys_dict_type VALUES(16, '谈话类型',        'talk_type',              '0', 'admin', NOW(), '', NULL, '谈话类型列表');
INSERT INTO sys_dict_type VALUES(17, '政治面貌',        'political_status',       '0', 'admin', NOW(), '', NULL, '政治面貌列表');
INSERT INTO sys_dict_type VALUES(18, '部门类型',        'dept_type',              '0', 'admin', NOW(), '', NULL, '部门类型列表');

-- 谈话内容标签
INSERT INTO sys_dict_data VALUES(30, 1, '思想理论教育和价值引领',   'thought_education',    'talk_content_tag', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(31, 2, '党团和班级建设',           'party_class',          'talk_content_tag', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(32, 3, '学风建设',                 'study_style',          'talk_content_tag', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(33, 4, '日常事务',                 'daily_affairs',        'talk_content_tag', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(34, 5, '心理健康教育与咨询',       'mental_health',        'talk_content_tag', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(35, 6, '危机事件应对',             'crisis_response',      'talk_content_tag', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(36, 7, '职业规划与就业创业指导',   'career_guidance',      'talk_content_tag', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 学籍状态
INSERT INTO sys_dict_data VALUES(40, 1, '在读',     'active',       'enrollment_status', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(41, 2, '休学',     'suspended',    'enrollment_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(42, 3, '退学',     'withdrawn',    'enrollment_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(43, 4, '毕业',     'graduated',    'enrollment_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 心理健康状态
INSERT INTO sys_dict_data VALUES(50, 1, '正常',     'normal',           'mental_health_status', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(51, 2, '周跟踪',   'weekly_track',     'mental_health_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(52, 3, '月跟踪',   'monthly_track',    'mental_health_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 贫困等级认定
INSERT INTO sys_dict_data VALUES(60, 1, '无',                     'none',       'poverty_level', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(61, 2, '家庭经济一般困难',       'general',    'poverty_level', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(62, 3, '家庭经济困难',           'difficult',  'poverty_level', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(63, 4, '家庭经济特别困难',       'severe',     'poverty_level', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 跟进状态
INSERT INTO sys_dict_data VALUES(70, 1, '无需跟进',   'none',           'followup_status', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(71, 2, '待跟进',     'pending',        'followup_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(72, 3, '跟进中',     'in_progress',    'followup_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(73, 4, '已完成',     'completed',      'followup_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 谈话类型
INSERT INTO sys_dict_data VALUES(80, 1, '个体谈话',   'individual',     'talk_type', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(81, 2, '集体谈话',   'group',          'talk_type', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 政治面貌
INSERT INTO sys_dict_data VALUES(90, 1, '群众',       'people',             'political_status', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(91, 2, '共青团员',   'league_member',      'political_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(92, 3, '中共预备党员', 'probationary_member','political_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(93, 4, '中共党员',   'party_member',       'political_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(94, 5, '其他',       'other',              'political_status', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');

-- 部门类型
INSERT INTO sys_dict_data VALUES(100, 1, '学院',     'college',      'dept_type', '', '', 'Y', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(101, 2, '年级',     'grade',        'dept_type', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
INSERT INTO sys_dict_data VALUES(102, 3, '班级',     'class',        'dept_type', '', '', 'N', '0', 'admin', NOW(), '', NULL, '');
