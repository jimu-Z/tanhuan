package com.ruoyi.talk.service;

import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.mapper.TalkTeacherMapper;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.talk.service.impl.TalkTeacherServiceImpl;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TeacherClassDepartmentTest {
    TalkTeacherServiceImpl service;
    TalkTeacherMapper teachers;
    SysDeptMapper departments;
    MockedStatic<TalkPermissionUtils> permissions;
    @BeforeEach void setup() {
        service = new TalkTeacherServiceImpl(); teachers = mock(TalkTeacherMapper.class); departments = mock(SysDeptMapper.class);
        ReflectionTestUtils.setField(service, "teacherMapper", teachers); ReflectionTestUtils.setField(service, "deptMapper", departments);
        when(teachers.selectClassNamesByCollegeDept(100L)).thenReturn(List.of("本院班级"));
        permissions = mockStatic(TalkPermissionUtils.class);
    }
    @AfterEach void close() { permissions.close(); }
    SysDept dept(String type, String deleted) { SysDept d = new SysDept(); d.setDeptId(100L); d.setDeptType(type); d.setDelFlag(deleted); return d; }
    @Test void validCollegeQueriesItsOwnClasses() {
        when(departments.selectDeptById(100L)).thenReturn(dept("college", "0"));
        assertThat(service.selectClassNamesByCollegeDept(100L)).containsExactly("本院班级");
    }
    @ParameterizedTest @ValueSource(strings={"school","grade","class","dept","unknown"})
    void nonCollegeTeacherDepartmentCannotProvideClassChoices(String type) {
        when(departments.selectDeptById(100L)).thenReturn(dept(type,"0"));
        assertThat(service.selectClassNamesByCollegeDept(100L)).isEmpty();
        verifyNoInteractions(teachers);
    }
    @Test void deletedOrMissingCollegeCannotProvideClassChoices() {
        when(departments.selectDeptById(100L)).thenReturn(dept("college","2"));
        assertThat(service.selectClassNamesByCollegeDept(100L)).isEmpty();
        when(departments.selectDeptById(100L)).thenReturn(null);
        assertThat(service.selectClassNamesByCollegeDept(100L)).isEmpty();
        assertThat(service.selectClassNamesByCollegeDept(null)).isEmpty();
        verifyNoInteractions(teachers);
    }
}
