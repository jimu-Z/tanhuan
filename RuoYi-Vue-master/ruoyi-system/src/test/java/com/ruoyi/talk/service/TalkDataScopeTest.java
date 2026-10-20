package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.service.impl.TalkStudentServiceImpl;

/** 回归测试：四类角色统一按学院过滤学生数据。 */
@ExtendWith(MockitoExtension.class)
class TalkDataScopeTest {
    @Mock
    private TalkStudentMapper talkStudentMapper;

    @InjectMocks
    private TalkStudentServiceImpl studentService;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    void teacherShouldSeeWholeOwnCollege() {
        loginAs("talk_teacher", 200L, false);
        TalkStudent query = new TalkStudent();
        when(talkStudentMapper.selectTalkStudentList(any())).thenReturn(new ArrayList<>());
        studentService.selectTalkStudentList(query);
        assertThat(query.getParams().get("collegeDeptId")).isEqualTo(200L);
        assertThat(query.getParams()).doesNotContainKeys("counselorCode", "secretaryDeptId");
    }

    @Test
    void collegeAdminShouldUseSameCollegeBoundary() {
        loginAs("college_admin", 300L, false);
        TalkStudent query = new TalkStudent();
        when(talkStudentMapper.selectTalkStudentListWithLastTalk(any())).thenReturn(new ArrayList<>());
        studentService.selectTalkStudentListWithLastTalk(query);
        assertThat(query.getParams().get("collegeDeptId")).isEqualTo(300L);
    }

    @Test
    void schoolAdminShouldHaveGlobalScope() {
        loginAs("school_admin", 100L, false);
        TalkStudent query = new TalkStudent();
        when(talkStudentMapper.selectTalkStudentList(any())).thenReturn(new ArrayList<>());
        studentService.selectTalkStudentList(query);
        assertThat(query.getParams()).doesNotContainKey("collegeDeptId");
    }

    @Test
    void builtInAdminShouldHaveGlobalScope() {
        loginAs("admin", 100L, true);
        TalkStudent query = new TalkStudent();
        when(talkStudentMapper.selectTalkStudentList(any())).thenReturn(new ArrayList<>());
        studentService.selectTalkStudentList(query);
        assertThat(query.getParams()).doesNotContainKey("collegeDeptId");
    }

    @Test
    void teacherWithoutCollegeShouldFailClosed() {
        loginAs("talk_teacher", null, false);
        TalkStudent query = new TalkStudent();
        when(talkStudentMapper.selectTalkStudentList(any())).thenReturn(new ArrayList<>());
        studentService.selectTalkStudentList(query);
        assertThat(query.getParams().get("collegeDeptId")).isEqualTo(-1L);
    }

    private void loginAs(String roleKey, Long deptId, boolean admin) {
        SysRole role = new SysRole();
        role.setRoleKey(roleKey);
        SysUser user = new SysUser();
        user.setRoles(List.of(role));
        if (deptId != null) {
            SysDept dept = new SysDept();
            dept.setDeptId(deptId);
            dept.setDeptType("college");
            user.setDept(dept);
        }
        LoginUser loginUser = new LoginUser(10L, deptId, user, java.util.Set.of());
        securityUtilsMock.when(SecurityUtils::getLoginUser).thenReturn(loginUser);
        securityUtilsMock.when(SecurityUtils::getDeptId).thenReturn(deptId);
        securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(admin);
    }
}
