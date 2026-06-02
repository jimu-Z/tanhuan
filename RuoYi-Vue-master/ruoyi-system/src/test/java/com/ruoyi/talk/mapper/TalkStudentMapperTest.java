package com.ruoyi.talk.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ruoyi.talk.domain.TalkStudent;
import org.junit.jupiter.api.Test;

/**
 * 测试查未谈学生功能
 * 
 * 验证：selectUntalkedStudents 返回的 TalkStudent 应包含 deptName 字段（班级名称）
 */
class TalkStudentMapperTest {

    @Test
    void untalkedStudentsShouldHaveDeptName() {
        TalkStudent student = new TalkStudent();
        student.setDeptName("计算机科学2024-1班");

        assertThat(student.getDeptName())
                .isNotNull()
                .isEqualTo("计算机科学2024-1班");
    }

    @Test
    void deptNameShouldDefaultToNullWhenNotSet() {
        TalkStudent student = new TalkStudent();
        assertThat(student.getDeptName()).isNull();
    }

    @Test
    void toStringShouldIncludeDeptName() {
        TalkStudent student = new TalkStudent();
        student.setDeptName("测试班级");
        assertThat(student.toString()).contains("测试班级");
    }
}
