-- 学生导入多人管理增量升级。先备份，再于部署新后端前执行；可重复执行。
-- 不开放学生端，不启用学生角色，不改变已有账号密码或角色菜单。
CREATE TABLE IF NOT EXISTS talk_student_manager (
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    PRIMARY KEY (student_id, teacher_id),
    KEY idx_student_manager_teacher (teacher_id),
    CONSTRAINT fk_student_manager_student FOREIGN KEY (student_id) REFERENCES talk_student(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_student_manager_teacher FOREIGN KEY (teacher_id) REFERENCES talk_teacher(teacher_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生与管理教师多对多绑定';

CREATE TABLE IF NOT EXISTS talk_class_manager (
    class_dept_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL,
    PRIMARY KEY (class_dept_id, teacher_id),
    KEY idx_class_manager_teacher (teacher_id),
    CONSTRAINT fk_class_manager_dept FOREIGN KEY (class_dept_id) REFERENCES sys_dept(dept_id) ON DELETE CASCADE,
    CONSTRAINT fk_class_manager_teacher FOREIGN KEY (teacher_id) REFERENCES talk_teacher(teacher_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级与管理教师多对多绑定，使用班级ID避免重名混淆';

CREATE TABLE IF NOT EXISTS talk_student_account (
    student_code VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    student_id BIGINT NULL,
    college_dept_id BIGINT NOT NULL,
    PRIMARY KEY (student_code),
    UNIQUE KEY uk_student_account_user (user_id),
    UNIQUE KEY uk_student_account_student (student_id),
    CONSTRAINT fk_student_account_student FOREIGN KEY (student_id) REFERENCES talk_student(student_id) ON DELETE SET NULL,
    CONSTRAINT fk_student_account_user FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生账号归属，删除资料后保留以支持安全重导';

-- 旧班级名关联只在首次没有精确关联时展开，保留现有管理范围；重跑不扩张新关联。
INSERT IGNORE INTO talk_class_manager (class_dept_id, teacher_id, create_time)
SELECT d.dept_id, t.teacher_id, NOW()
FROM talk_teacher t JOIN talk_teacher_class tc ON tc.teacher_code = t.teacher_code
JOIN sys_dept d ON d.dept_name = tc.class_name AND d.dept_type = 'class' AND d.del_flag = '0'
WHERE FIND_IN_SET(t.dept_id, d.ancestors)
  AND NOT EXISTS (SELECT 1 FROM talk_class_manager cm JOIN sys_dept precise ON precise.dept_id = cm.class_dept_id
                  WHERE cm.teacher_id = t.teacher_id AND precise.dept_name = tc.class_name);

INSERT IGNORE INTO sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT '学生', 'talk_student', 99, '5', 1, 1, '1', '0', 'system', NOW(), '仅用于学生导入账号归属，学生端暂不开通'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'talk_student' AND del_flag = '0');
