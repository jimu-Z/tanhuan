package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.util.List;
import java.util.Set;
import com.ruoyi.common.core.domain.entity.*;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.service.impl.TalkStudentServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class TalkStudentClassBindingTest {
    TalkStudentServiceImpl service;
    TalkStudentMapper students;
    SysDeptMapper departments;
    MockedStatic<SecurityUtils> security;

    @BeforeEach void setup() {
        service = new TalkStudentServiceImpl();
        students = mock(TalkStudentMapper.class);
        departments = mock(SysDeptMapper.class);
        ReflectionTestUtils.setField(service, "talkStudentMapper", students);
        ReflectionTestUtils.setField(service, "sysDeptMapper", departments);
        ReflectionTestUtils.setField(service, "importRelations", mock(com.ruoyi.talk.service.impl.StudentImportRelationsService.class));
        when(departments.selectDeptById(100L)).thenReturn(dept(100L, 0L, "college"));
        when(departments.selectDeptById(150L)).thenReturn(dept(150L, 100L, "grade"));
        when(departments.selectDeptById(200L)).thenReturn(dept(200L, 150L, "class"));
        when(students.selectTalkStudentByStudentId(1L)).thenReturn(student(200L));
        when(students.insertTalkStudent(any())).thenReturn(1);
        when(students.updateTalkStudent(any())).thenReturn(1);
        security = mockStatic(SecurityUtils.class);
    }
    @AfterEach void close() { security.close(); }
    SysDept dept(Long id, Long parent, String type) {
        SysDept result = new SysDept(); result.setDeptId(id); result.setParentId(parent); result.setDeptType(type);
        return result;
    }
    TalkStudent student(Long department) {
        TalkStudent result = new TalkStudent(); result.setStudentId(1L); result.setStudentCode("CLASS-TEST");
        result.setStudentName("测试学生"); result.setDeptId(department); return result;
    }
    void login(String key) {
        SysRole role = new SysRole(); role.setRoleKey(key);
        SysUser user = new SysUser(); user.setUserId(key.equals("admin") ? 1L : 42L);
        user.setDeptId(100L); user.setDept(dept(100L, 0L, "college")); user.setRoles(List.of(role));
        security.when(SecurityUtils::getLoginUser).thenReturn(new LoginUser(user.getUserId(), 100L, user, Set.of()));
        security.when(SecurityUtils::getDeptId).thenReturn(100L);
        security.when(SecurityUtils::getUsername).thenReturn("qa-owner");
        security.when(SecurityUtils::isAdmin).thenReturn(key.equals("admin"));
    }
    @ParameterizedTest @ValueSource(strings={"admin", "school_admin", "college_admin", "talk_teacher"})
    void rejectsCollegeAndGradeBeforeStudentInsert(String role) {
        login(role);
        for (Long department : List.of(100L, 150L)) {
            assertThatThrownBy(() -> service.insertTalkStudent(student(department)))
                .isInstanceOf(ServiceException.class).hasMessageContaining("班级");
        }
        verify(students, never()).insertTalkStudent(any());
    }
    @ParameterizedTest @ValueSource(strings={"admin", "school_admin", "college_admin", "talk_teacher"})
    void rejectsChangingClassToCollegeOrGrade(String role) {
        login(role);
        for (Long department : List.of(100L, 150L)) {
            assertThatThrownBy(() -> service.updateTalkStudent(student(department)))
                .isInstanceOf(ServiceException.class).hasMessageContaining("班级");
        }
        verify(students, never()).updateTalkStudent(any());
    }
    @ParameterizedTest @ValueSource(strings={"admin", "school_admin", "college_admin", "talk_teacher"})
    void allowsValidClassOnCreateAndUpdate(String role) {
        login(role);
        assertThat(service.insertTalkStudent(student(200L))).isEqualTo(1);
        assertThat(service.updateTalkStudent(student(200L))).isEqualTo(1);
    }
    @ParameterizedTest @ValueSource(strings={"admin", "school_admin", "college_admin", "talk_teacher"})
    void missingClassIsRejectedBeforeInsert(String role) {
        login(role);
        assertThatThrownBy(() -> service.insertTalkStudent(student(999L)))
            .isInstanceOf(ServiceException.class).hasMessageContaining("班级");
        verify(students, never()).insertTalkStudent(any());
    }
    @Test void normalizesStudentCodeBeforeUpdatingBothStudentAndAccount() {
        login("admin");TalkStudent edited=student(200L);edited.setStudentCode(" CLASS-NEW ");
        service.updateTalkStudent(edited);
        var saved=org.mockito.ArgumentCaptor.forClass(TalkStudent.class);verify(students).updateTalkStudent(saved.capture());
        assertThat(saved.getValue().getStudentCode()).isEqualTo("CLASS-NEW");
    }
}
