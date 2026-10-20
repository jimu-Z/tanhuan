package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.util.List;
import com.ruoyi.common.core.domain.entity.*;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.domain.*;
import com.ruoyi.talk.mapper.*;
import com.ruoyi.talk.service.impl.TalkStudentRecordServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class TalkRecordIntegrityTest {
    TalkStudentRecordServiceImpl service;
    TalkStudentRecordMapper records;
    TalkSessionMapper sessions;
    TalkStudentMapper students;
    SysDeptMapper departments;
    MockedStatic<SecurityUtils> security;

    @BeforeEach void setup() {
        service=new TalkStudentRecordServiceImpl(); records=mock(TalkStudentRecordMapper.class);
        sessions=mock(TalkSessionMapper.class); students=mock(TalkStudentMapper.class);
        departments=mock(SysDeptMapper.class);
        ReflectionTestUtils.setField(service,"talkStudentRecordMapper",records);
        ReflectionTestUtils.setField(service,"talkSessionMapper",sessions);
        ReflectionTestUtils.setField(service,"talkStudentMapper",students);
        ReflectionTestUtils.setField(service,"sysDeptMapper",departments);
        security=mockStatic(SecurityUtils.class);
        TalkSession session=new TalkSession();session.setSessionId(101L);session.setCollegeDeptId(200L);session.setCreateBy("qa-owner");
        when(sessions.selectTalkSessionBySessionId(101L)).thenReturn(session);
        TalkStudentRecord existing=new TalkStudentRecord();existing.setRecordId(1L);existing.setSessionId(101L);existing.setStudentId(10L);
        when(records.selectTalkStudentRecordByRecordId(1L)).thenReturn(existing);
        when(records.updateTalkStudentRecord(any())).thenReturn(1);
    }
    @AfterEach void close(){security.close();}
    void login(String key) {
        SysRole role=new SysRole();role.setRoleKey(key);
        SysDept dept=new SysDept();dept.setDeptId(200L);dept.setDeptType("college");
        SysUser user=new SysUser();user.setUserId("admin".equals(key)?1L:42L);user.setDeptId(200L);user.setDept(dept);user.setRoles(List.of(role));
        LoginUser login=new LoginUser(user.getUserId(),200L,user,java.util.Set.of());
        security.when(SecurityUtils::getLoginUser).thenReturn(login);
        security.when(SecurityUtils::isAdmin).thenReturn("admin".equals(key));
        security.when(SecurityUtils::getDeptId).thenReturn(200L);
        security.when(SecurityUtils::getUsername).thenReturn("qa-owner");
    }
    TalkStudentRecord update(){TalkStudentRecord r=new TalkStudentRecord();r.setRecordId(1L);r.setStudentFeedback("正常更新");return r;}
    @ParameterizedTest @ValueSource(strings={"admin","school_admin","college_admin","talk_teacher"})
    void allRolesRejectChangingAuthorizedSession(String role) {
        login(role);TalkStudentRecord r=update();r.setSessionId(999L);
        assertThatThrownBy(()->service.updateTalkStudentRecord(r)).hasMessageContaining("不能变更");
        verify(records,never()).updateTalkStudentRecord(any());
    }
    @ParameterizedTest @ValueSource(strings={"admin","school_admin","college_admin","talk_teacher"})
    void allRolesRejectChangingRecordedStudent(String role) {
        login(role);TalkStudentRecord r=update();r.setStudentId(999L);
        assertThatThrownBy(()->service.updateTalkStudentRecord(r)).hasMessageContaining("不能变更");
        verify(records,never()).updateTalkStudentRecord(any());
    }
    @ParameterizedTest @ValueSource(strings={"admin","school_admin","college_admin","talk_teacher"})
    void allRolesCanEditOwnRecordWithoutResendingRelationships(String role) {
        login(role);assertThat(service.updateTalkStudentRecord(update())).isEqualTo(1);
    }
    @ParameterizedTest @ValueSource(strings={"admin","school_admin","college_admin","talk_teacher"})
    void allRolesRejectCrossCollegeStudentOnRecordInsert(String role) {
        login(role);TalkStudent student=new TalkStudent();student.setStudentId(999L);student.setDeptId(300L);
        when(students.selectTalkStudentByStudentId(999L)).thenReturn(student);
        SysDept dept=new SysDept();dept.setDeptId(300L);dept.setDeptType("college");when(departments.selectDeptById(300L)).thenReturn(dept);
        TalkStudentRecord r=new TalkStudentRecord();r.setSessionId(101L);r.setStudentId(999L);
        assertThatThrownBy(()->service.insertTalkStudentRecord(r)).hasMessageContaining("同一学院");
        verify(records,never()).insertTalkStudentRecord(any());
    }
}
