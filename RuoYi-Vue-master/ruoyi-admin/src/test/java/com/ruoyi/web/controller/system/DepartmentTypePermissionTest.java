package com.ruoyi.web.controller.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.talk.service.ITalkStudentService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class DepartmentTypePermissionTest {
    SysDeptController controller;
    ISysDeptService service;
    MockedStatic<TalkPermissionUtils> permission;
    MockedStatic<SecurityUtils> security;
    @BeforeEach void setup() {
        controller = new SysDeptController(); service = mock(ISysDeptService.class);
        ReflectionTestUtils.setField(controller, "deptService", service);
        SysDept existing = dept("grade"); when(service.selectDeptById(10L)).thenReturn(existing);
        when(service.checkDeptNameUnique(any())).thenReturn(true);
        when(service.updateDept(any())).thenReturn(1);
        permission = mockStatic(TalkPermissionUtils.class);
        permission.when(TalkPermissionUtils::isCollegeAdmin).thenReturn(true);
        security = mockStatic(SecurityUtils.class);
        SysUser user = new SysUser(); user.setUserName("qa_college");
        security.when(SecurityUtils::getLoginUser).thenReturn(new LoginUser(42L,100L,user,java.util.Set.of()));
        security.when(SecurityUtils::getUsername).thenReturn("qa_college");
    }
    @AfterEach void close() { permission.close(); security.close(); }
    SysDept dept(String type) {
        SysDept dept = new SysDept(); dept.setDeptId(10L); dept.setParentId(100L);
        dept.setDeptName("测试年级"); dept.setDeptType(type); dept.setStatus("0"); return dept;
    }
    @ParameterizedTest @ValueSource(strings={"school", "college", "dept", "unknown"})
    void collegeAdministratorCannotChangeNodeToAnUnauthorizedType(String type) {
        assertThat(controller.edit(dept(type)).get("code")).isEqualTo(500);
        verify(service, never()).updateDept(any());
    }
    @ParameterizedTest @ValueSource(strings={"grade", "class"})
    void collegeAdministratorCanMaintainAcademicNodeTypes(String type) {
        assertThat(controller.edit(dept(type)).get("code")).isEqualTo(200);
        verify(service).updateDept(any());
    }
    @Test void partialUpdatePreservesExistingType() {
        assertThat(controller.edit(dept(null)).get("code")).isEqualTo(200);
    }
    @Test void occupiedClassCannotBeDeletedEvenWithoutStudentLoginAccounts() {
        when(service.deleteDeptById(10L)).thenReturn(1);
        ITalkStudentService students = mock(ITalkStudentService.class);
        when(students.countStudentsByDeptId(10L)).thenReturn(1);
        ReflectionTestUtils.setField(controller,"studentService",students);
        assertThat(controller.remove(10L).get("code")).isEqualTo(601);
        verify(service, never()).deleteDeptById(any());
    }
    @Test void emptyClassCanStillBeDeleted() {
        ReflectionTestUtils.setField(controller,"studentService",mock(ITalkStudentService.class));
        when(service.deleteDeptById(10L)).thenReturn(1);
        assertThat(controller.remove(10L).get("code")).isEqualTo(200);
        verify(service).deleteDeptById(10L);
    }
}
