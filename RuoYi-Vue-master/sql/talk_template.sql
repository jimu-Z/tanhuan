-- ----------------------------
-- 谈话模板表
-- ----------------------------
DROP TABLE IF EXISTS talk_template;
CREATE TABLE talk_template (
    template_id    BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_name  VARCHAR(100) NOT NULL,
    template_content TEXT,
    template_type  VARCHAR(20) DEFAULT 'personal' COMMENT 'system=系统预置 personal=个人模板',
    template_tags  VARCHAR(200) COMMENT '逗号分隔的标签值',
    create_by      VARCHAR(64),
    create_time    DATETIME,
    update_by      VARCHAR(64),
    update_time    DATETIME,
    del_flag       CHAR(1) DEFAULT '0'
) ENGINE=INNODB COMMENT = '谈话模板表';

-- ----------------------------
-- 种子数据 - 5个系统模板
-- ----------------------------
INSERT INTO talk_template (template_name, template_content, template_type, template_tags, create_time) VALUES
('学业指导谈话', '【谈话目的】了解学生学习困难，分析原因并制定帮扶计划。\n\n【谈话要点】\n1. 近期学习情况回顾，是否存在挂科、缺课现象\n2. 学习困难的主要原因分析\n3. 制定具体的学习改进计划\n4. 明确后续跟进时间和方式\n\n【谈话记录】', 'system', 'study_style', NOW()),
('心理关怀谈话', '【谈话目的】关注学生心理健康，提供情绪支持和资源引导。\n\n【谈话要点】\n1. 近期生活、学习状态了解\n2. 情绪状态评估，是否存在焦虑、抑郁等情绪\n3. 了解社会支持网络（家庭、朋友）\n4. 介绍学校心理咨询资源和预约方式\n5. 约定下次跟进时间\n\n【谈话记录】', 'system', 'mental_health', NOW()),
('纪律约谈', '【谈话目的】针对违纪行为进行教育，引导学生认识错误并改正。\n\n【谈话要点】\n1. 明确违纪事实，听取学生陈述\n2. 解读相关校规校纪\n3. 引导学生认识错误的性质和后果\n4. 共同制定改正计划\n5. 告知后续处理流程\n\n【谈话记录】', 'system', 'daily_affairs,crisis_response', NOW()),
('就业指导谈话', '【谈话目的】了解毕业生就业意向，提供就业指导和资源支持。\n\n【谈话要点】\n1. 就业意向和职业规划了解\n2. 简历和面试准备情况\n3. 当前就业市场形势分析\n4. 学校就业资源和服务介绍\n5. 制定求职行动计划\n\n【谈话记录】', 'system', 'career_guidance', NOW()),
('新生适应谈话', '【谈话目的】了解新生入学适应情况，帮助尽快融入大学生活。\n\n【谈话要点】\n1. 入学以来学习和生活适应情况\n2. 人际关系建立情况（室友、同学、老师）\n3. 对大学学习方式的适应程度\n4. 是否存在困难或困惑\n5. 介绍校园资源和学生组织\n\n【谈话记录】', 'system', 'daily_affairs,study_style', NOW());
