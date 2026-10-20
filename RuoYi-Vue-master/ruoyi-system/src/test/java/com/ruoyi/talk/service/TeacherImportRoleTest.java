package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.List;
import com.ruoyi.common.core.domain.entity.*;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.*;
import com.ruoyi.talk.domain.TalkTeacher;
import com.ruoyi.talk.mapper.TalkTeacherMapper;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.talk.service.impl.TalkTeacherServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

class TeacherImportRoleTest {
    private TalkTeacherServiceImpl service;
    private TalkTeacherMapper teachers;
    private SysUserMapper users;
    private SysRoleMapper roles;
    private SysUserRoleMapper userRoles;
    private PlatformTransactionManager transactions;
    private RedisCache sessions;
    private MockedStatic<TalkPermissionUtils> permissions;
    private MockedStatic<SecurityUtils> security;
    private TalkTeacher teacher;
    private SysUser existingUser;

    @BeforeEach
    void setup() {
        service=new TalkTeacherServiceImpl();
        teachers=mock(TalkTeacherMapper.class); users=mock(SysUserMapper.class);
        roles=mock(SysRoleMapper.class); userRoles=mock(SysUserRoleMapper.class);
        transactions=mock(PlatformTransactionManager.class);
        sessions=mock(RedisCache.class);
        ReflectionTestUtils.setField(service,"redisCache",sessions);
        ReflectionTestUtils.setField(service,"teacherMapper",teachers);
        ReflectionTestUtils.setField(service,"userMapper",users);
        ReflectionTestUtils.setField(service,"roleMapper",roles);
        ReflectionTestUtils.setField(service,"userRoleMapper",userRoles);
        ReflectionTestUtils.setField(service,"transactionManager",transactions);
        when(transactions.getTransaction(any())).thenAnswer(i->new SimpleTransactionStatus());
        SysDeptMapper departments=mock(SysDeptMapper.class);
        SysDept college=new SysDept();college.setDeptId(100L);college.setDeptType("college");
        when(departments.selectDeptById(100L)).thenReturn(college);
        ReflectionTestUtils.setField(service,"deptMapper",departments);
        permissions=mockStatic(TalkPermissionUtils.class);
        permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(true);
        security=mockStatic(SecurityUtils.class);
        security.when(SecurityUtils::getUsername).thenReturn("test-admin");
        security.when(SecurityUtils::getUserId).thenReturn(42L);
        security.when(()->SecurityUtils.isAdmin(anyLong())).thenAnswer(i->Long.valueOf(1L).equals(i.getArgument(0)));
        when(roles.selectRoleList(any())).thenAnswer(i->{
            SysRole query=i.getArgument(0);SysRole role=role(query.getRoleKey());return List.of(role);
        });
        teacher=new TalkTeacher();teacher.setTeacherCode("000101");teacher.setTeacherName("测试教师");
        teacher.setDeptId(100L);teacher.setPosition("辅导员");
        existingUser=new SysUser();existingUser.setUserId(10L);existingUser.setUserName("000101");
        existingUser.setDeptId(100L);
        when(users.selectUserByUserName("000101")).thenReturn(existingUser);
    }

    @AfterEach void close(){security.close();permissions.close();}

    @Test void quickRoleChangeRejectsMissingAccount() {
        prepareFormUpdate();when(users.selectUserById(10L)).thenReturn(null);
        org.assertj.core.api.Assertions.assertThatThrownBy(()->service.changeCollegeAdminRole(1L,true)).hasMessageContaining("登录账号不存在");
        verify(userRoles,never()).deleteUserRoleInfo(any());verify(userRoles,never()).batchUserRole(any());
    }
    @Test void quickRoleChangeProtectsBuiltInAccountEvenWithoutAdminRoleRow() {
        prepareFormUpdate();existingUser.setUserId(1L);
        TalkTeacher old=new TalkTeacher();old.setTeacherId(1L);old.setDeptId(100L);old.setUserId(1L);when(teachers.selectTalkTeacherById(1L)).thenReturn(old);when(users.selectUserById(1L)).thenReturn(existingUser);
        when(roles.selectRolePermissionByUserId(1L)).thenReturn(List.of(role("college_admin")));
        org.assertj.core.api.Assertions.assertThatThrownBy(()->service.changeCollegeAdminRole(1L,false)).hasMessageContaining("内置管理员");
        verify(userRoles,never()).deleteUserRoleInfo(any());
    }
    @Test void quickRoleChangeRejectsDisabledTargetBeforeDeletingSource() {
        prepareFormUpdate();doAnswer(i->{SysRole query=i.getArgument(0);SysRole r=role(query.getRoleKey());if("college_admin".equals(query.getRoleKey()))r.setStatus("1");return List.of(r);}).when(roles).selectRoleList(any());
        org.assertj.core.api.Assertions.assertThatThrownBy(()->service.changeCollegeAdminRole(1L,true)).hasMessageContaining("停用");verify(userRoles,never()).deleteUserRoleInfo(any());verify(userRoles,never()).batchUserRole(any());
    }
    @Test void quickRoleChangeValidatesTargetBeforeAnyWriteWhenRoleMissing() {
        prepareFormUpdate();doAnswer(i->{SysRole query=i.getArgument(0);return "college_admin".equals(query.getRoleKey())?List.of():List.of(role(query.getRoleKey()));}).when(roles).selectRoleList(any());
        org.assertj.core.api.Assertions.assertThatThrownBy(()->service.changeCollegeAdminRole(1L,true)).hasMessageContaining("目标角色");verify(userRoles,never()).deleteUserRoleInfo(any());
    }
    @Test void quickRolePromotionRevokesExistingLoginSessions() {
        prepareFormUpdate();when(sessions.keys(anyString())).thenReturn(List.of("login_tokens:qa"));LoginUser login=new LoginUser();login.setUserId(10L);when(sessions.getCacheObject("login_tokens:qa")).thenReturn(login);
        assertThat(service.changeCollegeAdminRole(1L,true)).isEqualTo(1);verify(userRoles).batchUserRole(any());verify(sessions).deleteObject("login_tokens:qa");
    }

    private void prepareFormUpdate() {
        teacher.setTeacherId(1L);
        TalkTeacher old = new TalkTeacher();
        old.setTeacherId(1L); old.setTeacherCode("000101"); old.setDeptId(100L); old.setUserId(10L);
        when(teachers.selectTalkTeacherById(1L)).thenReturn(old);
        when(users.selectUserById(10L)).thenReturn(existingUser);
        when(roles.selectRolePermissionByUserId(10L)).thenReturn(List.of(role("talk_teacher")));
        when(roles.selectRoleListByUserId(10L)).thenReturn(List.of(8L));
    }

    @Test void disabledNewTeacherCreatesDisabledAccount() {
        when(users.selectUserByUserName("000101")).thenReturn(null);
        when(users.insertUser(any())).thenAnswer(i -> { SysUser u=i.getArgument(0); u.setUserId(50L); return 1; });
        teacher.setStatus("1");
        service.insertTalkTeacher(teacher);
        assertThat(teacher.getStatus()).isEqualTo("1");
        verify(users).insertUser(argThat(u -> "1".equals(u.getStatus())));
    }

    @Test void importRejectsBlankTeacherNameBeforeWriting() {
        teacher.setTeacherName("   ");
        assertThat(service.importTeacher(List.of(teacher),false)).contains("失败 1 条").contains("姓名不能为空");
        verify(teachers,never()).insertTalkTeacher(any());verify(users,never()).updateUser(any());
    }

    @Test void importUpdateRejectsInvalidStatusBeforeWriting() {
        TalkTeacher old=new TalkTeacher();old.setTeacherId(1L);old.setDeptId(100L);old.setUserId(10L);
        when(teachers.selectByTeacherCode("000101")).thenReturn(old);teacher.setStatus("invalid");
        assertThat(service.importTeacher(List.of(teacher),true)).contains("失败 1 条").contains("状态只能");
        verify(teachers,never()).updateTalkTeacher(any());verify(users,never()).updateUser(any());
    }

    @Test void importRetainsExplicitDisabledState() {
        teacher.setStatus("1");
        assertThat(service.importTeacher(List.of(teacher),false)).contains("成功导入 1 条");
        assertThat(teacher.getStatus()).isEqualTo("1");
        verify(users).updateUser(argThat(u -> "1".equals(u.getStatus())));
    }

    @Test void collegeAdminCannotDisableSelfWithUnchangedRole() {
        prepareFormUpdate();
        permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(false);
        security.when(SecurityUtils::getUserId).thenReturn(10L);
        when(roles.selectRolePermissionByUserId(10L)).thenReturn(List.of(role("college_admin")));
        teacher.setRoleKey("college_admin"); teacher.setStatus("1");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.updateTalkTeacher(teacher))
                .hasMessageContaining("自己");
        verify(teachers, never()).updateTalkTeacher(any());
    }

    @Test void collegeAdminCannotDisableLastActiveAdministrator() {
        prepareFormUpdate();
        permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(false);
        existingUser.setStatus("0");
        when(roles.selectRolePermissionByUserId(10L)).thenReturn(List.of(role("college_admin")));
        when(teachers.countActiveCollegeAdminsByDeptId(100L)).thenReturn(1);
        teacher.setStatus("1");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.updateTalkTeacher(teacher))
                .hasMessageContaining("至少保留");
        verify(teachers, never()).updateTalkTeacher(any());
    }

    @Test void deleteBatchRejectsBuiltInAdminBeforeDeletingAnyAccount() {
        prepareFormUpdate();
        TalkTeacher protectedTeacher=new TalkTeacher(); protectedTeacher.setTeacherId(2L);
        protectedTeacher.setUserId(1L); protectedTeacher.setDeptId(100L);
        when(teachers.selectTalkTeacherById(2L)).thenReturn(protectedTeacher);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.deleteTalkTeacherByIds(new Long[]{1L,2L}))
                .hasMessageContaining("内置管理员");
        verify(users,never()).deleteUserPhysically(anyLong());
        verify(teachers,never()).deleteTalkTeacherById(anyLong());
    }

    @Test void collegeAdminCannotDeleteLinkedSchoolAdministrator() {
        prepareFormUpdate();
        permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(false);
        when(roles.selectRolePermissionByUserId(10L)).thenReturn(List.of(role("school_admin")));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.deleteTalkTeacherByIds(new Long[]{1L}))
                .hasMessageContaining("校管理员");
        verify(users,never()).deleteUserPhysically(anyLong());
    }

    @Test @SuppressWarnings("unchecked")
    void formUpdateSavesRemarkAndSelectedRole() {
        prepareFormUpdate();
        teacher.setRoleKey("college_admin"); teacher.setRemark("负责学院教师管理");
        service.updateTalkTeacher(teacher);
        verify(teachers).updateTalkTeacher(argThat(t -> "负责学院教师管理".equals(t.getRemark())));
        ArgumentCaptor<List<SysUserRole>> assigned = ArgumentCaptor.forClass(List.class);
        verify(userRoles).batchUserRole(assigned.capture());
        assertThat(assigned.getValue()).singleElement().satisfies(r -> assertThat(r.getRoleId()).isEqualTo(9L));
        verify(sessions).keys(any());
    }

    @Test void formUpdateRejectsSchoolRoleBeforeWriting() {
        prepareFormUpdate();
        permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(false);
        teacher.setRoleKey("school_admin");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.updateTalkTeacher(teacher))
                .hasMessageContaining("仅校级管理员");
        verify(teachers, never()).updateTalkTeacher(any());
        verifyNoInteractions(userRoles);
    }

    @Test void formUpdateUsesStoredCodeForAccountValidation() {
        prepareFormUpdate();
        permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(false);
        when(roles.selectRolePermissionByUserId(10L)).thenReturn(List.of(role("school_admin")));
        teacher.setTeacherCode("forged-code"); teacher.setRoleKey("talk_teacher");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.updateTalkTeacher(teacher))
                .hasMessageContaining("不能修改校管理员");
        verify(teachers, never()).updateTalkTeacher(any());
    }

    @ParameterizedTest
    @CsvSource({"老师,talk_teacher,8","院管理员,college_admin,9","校管理员,school_admin,10"})
    @SuppressWarnings("unchecked")
    void newImportAssignsSelectedRole(String label,String key,long id) {
        teacher.setRoleKey(label);
        String result=service.importTeacher(List.of(teacher),false);
        assertThat(result).contains("成功导入 1 条").doesNotContain("失败");
        ArgumentCaptor<List<SysUserRole>> capture=ArgumentCaptor.forClass(List.class);
        verify(userRoles).batchUserRole(capture.capture());
        assertThat(capture.getValue()).singleElement().satisfies(r->{
            assertThat(r.getUserId()).isEqualTo(10L);assertThat(r.getRoleId()).isEqualTo(id);
        });
        assertThat(teacher.getRoleKey()).isEqualTo(key);
        verify(transactions).commit(any());
    }

    @Test void updateReplacesBusinessRoleButPreservesUnrelatedRoles() {
        TalkTeacher old=new TalkTeacher();old.setTeacherId(1L);old.setDeptId(100L);old.setUserId(10L);
        when(teachers.selectByTeacherCode("000101")).thenReturn(old);
        when(roles.selectRolePermissionByUserId(10L)).thenReturn(List.of(role("talk_teacher"),role("custom")));
        teacher.setRoleKey("院管理员");
        assertThat(service.importTeacher(List.of(teacher),true)).contains("其中更新 1 条");
        ArgumentCaptor<SysUserRole> removed=ArgumentCaptor.forClass(SysUserRole.class);
        verify(userRoles).deleteUserRoleInfo(removed.capture());
        assertThat(removed.getValue().getRoleId()).isEqualTo(8L);
        verify(userRoles,never()).deleteUserRoleByUserId(anyLong());
    }

    @Test void collegeAdminCannotImportSchoolAdministrator() {
        permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(false);
        teacher.setRoleKey("校管理员");
        assertThat(service.importTeacher(List.of(teacher),false)).contains("失败 1 条");
        verify(teachers,never()).insertTalkTeacher(any());verify(users,never()).updateUser(any());
        verifyNoInteractions(userRoles);
    }

    @Test void invalidRoleFailsBeforeWriting() {
        teacher.setRoleKey("超级管理员");
        assertThat(service.importTeacher(List.of(teacher),false)).contains("失败 1 条");
        verify(teachers,never()).insertTalkTeacher(any());verify(users,never()).updateUser(any());
        verifyNoInteractions(userRoles);
    }

    @Test void roleFailureRollsBackOnlyItsRow() {
        teacher.setRoleKey("老师");
        doThrow(new IllegalStateException("role write failed")).when(userRoles).batchUserRole(any());
        assertThat(service.importTeacher(List.of(teacher),false)).contains("失败 1 条");
        verify(transactions).rollback(any());verify(transactions,never()).commit(any());
        verifyNoInteractions(sessions);
    }

    @Test void importCannotChangeBuiltInAdmin() {
        existingUser.setUserId(1L);
        teacher.setRoleKey("老师");
        assertThat(service.importTeacher(List.of(teacher),false)).contains("失败 1 条");
        verify(users,never()).updateUser(any());verify(teachers,never()).insertTalkTeacher(any());
    }

    @Test void collegeAdminCannotDemoteSelfOrLastAdministrator() {
        permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(false);
        permissions.when(TalkPermissionUtils::isCollegeAdmin).thenReturn(true);
        existingUser.setUserId(42L);
        when(roles.selectRolePermissionByUserId(42L)).thenReturn(List.of(role("college_admin")));
        teacher.setRoleKey("老师");
        assertThat(service.importTeacher(List.of(teacher),true)).contains("失败 1 条");
        verify(users,never()).updateUser(any());verifyNoInteractions(userRoles);
    }

    @Test void collegeAdminCannotDemoteLastAdministrator() {
        permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(false);
        when(roles.selectRolePermissionByUserId(10L)).thenReturn(List.of(role("college_admin")));
        when(teachers.countActiveCollegeAdminsByDeptId(100L)).thenReturn(1);
        teacher.setRoleKey("老师");
        assertThat(service.importTeacher(List.of(teacher),true)).contains("失败 1 条");
        verify(users,never()).updateUser(any());verifyNoInteractions(userRoles);
    }

    @Test void oldTemplatePreservesExistingAdministratorRole() {
        when(roles.selectRolePermissionByUserId(10L)).thenReturn(List.of(role("college_admin")));
        assertThat(service.importTeacher(List.of(teacher),false)).contains("成功导入 1 条");
        verifyNoInteractions(userRoles);
    }

    @ParameterizedTest
    @CsvSource({"老师,8","院管理员,9","校管理员,10"})
    @SuppressWarnings("unchecked")
    void createsNewAccountWithSelectedRole(String label,long id) {
        when(users.selectUserByUserName("000101")).thenReturn(null);
        when(users.insertUser(any())).thenAnswer(i->{SysUser u=i.getArgument(0);u.setUserId(50L);return 1;});
        teacher.setRoleKey(label);
        assertThat(service.importTeacher(List.of(teacher),false)).contains("成功导入 1 条");
        ArgumentCaptor<List<SysUserRole>> capture=ArgumentCaptor.forClass(List.class);
        verify(userRoles).batchUserRole(capture.capture());
        assertThat(capture.getValue()).singleElement().satisfies(r->{
            assertThat(r.getUserId()).isEqualTo(50L);assertThat(r.getRoleId()).isEqualTo(id);
        });
    }

    @Test void failedRowDoesNotPreventNextRowImport() {
        TalkTeacher invalid=new TalkTeacher();invalid.setTeacherCode("BAD");invalid.setRoleKey("超级管理员");
        teacher.setRoleKey("院管理员");
        assertThat(service.importTeacher(List.of(invalid,teacher),false)).contains("成功导入 1 条").contains("失败 1 条");
        verify(transactions).rollback(any());verify(transactions).commit(any());
    }

    @Test void collegeAdminCannotModifyExistingSchoolAdministrator() {
        permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(false);
        when(roles.selectRolePermissionByUserId(10L)).thenReturn(List.of(role("school_admin")));
        teacher.setRoleKey("老师");
        assertThat(service.importTeacher(List.of(teacher),true)).contains("失败 1 条");
        verify(users,never()).updateUser(any());verifyNoInteractions(userRoles);
    }

    @Test void disabledTargetRoleFailsBeforeWriting() {
        SysRole disabled=role("college_admin");disabled.setStatus("1");
        doReturn(List.of(disabled)).when(roles).selectRoleList(any());
        teacher.setRoleKey("院管理员");
        assertThat(service.importTeacher(List.of(teacher),false)).contains("失败 1 条");
        verify(teachers,never()).insertTalkTeacher(any());verifyNoInteractions(userRoles);
    }

    @Test void prefixedAccountStillCannotBeBuiltInAdministrator() {
        when(users.selectUserByUserName("000101")).thenReturn(null);
        when(teachers.countByDeptAndNameExcludePosition(100L,"测试教师","辅导员")).thenReturn(1);
        existingUser.setUserId(1L);existingUser.setUserName("f_000101");
        when(users.selectUserByUserName("f_000101")).thenReturn(existingUser);
        teacher.setRoleKey("老师");
        assertThat(service.importTeacher(List.of(teacher),false)).contains("失败 1 条");
        verify(users,never()).updateUser(any());verify(teachers,never()).insertTalkTeacher(any());
    }

    @Test void changedRoleInvalidatesAllTargetSessionsAndPreservesOtherUsers() {
        teacher.setRoleKey("老师");
        when(roles.selectRolePermissionByUserId(10L)).thenReturn(List.of(role("school_admin")));
        when(sessions.keys(anyString())).thenReturn(List.of("login_tokens:a","login_tokens:b","login_tokens:c"));
        LoginUser first=new LoginUser();first.setUserId(10L);
        LoginUser second=new LoginUser();second.setUserId(10L);
        LoginUser other=new LoginUser();other.setUserId(20L);
        when(sessions.getCacheObject("login_tokens:a")).thenReturn(first);
        when(sessions.getCacheObject("login_tokens:b")).thenReturn(second);
        when(sessions.getCacheObject("login_tokens:c")).thenReturn(other);
        assertThat(service.importTeacher(List.of(teacher),true)).contains("成功导入 1 条");
        verify(sessions).deleteObject("login_tokens:a");verify(sessions).deleteObject("login_tokens:b");
        verify(sessions,never()).deleteObject("login_tokens:c");
    }

    @Test void sessionInvalidationFailureRollsBackRoleRow() {
        teacher.setRoleKey("老师");
        when(roles.selectRolePermissionByUserId(10L)).thenReturn(List.of(role("school_admin")));
        when(sessions.keys(anyString())).thenThrow(new IllegalStateException("session cache unavailable"));
        assertThat(service.importTeacher(List.of(teacher),true)).contains("失败 1 条");
        verify(transactions).rollback(any());verify(transactions,never()).commit(any());
    }

    @Test void unchangedRolePreservesLoginSessions() {
        teacher.setRoleKey("院管理员");
        when(roles.selectRolePermissionByUserId(10L)).thenReturn(List.of(role("college_admin")));
        when(roles.selectRoleListByUserId(10L)).thenReturn(List.of(9L));
        assertThat(service.importTeacher(List.of(teacher),true)).contains("成功导入 1 条");
        verifyNoInteractions(sessions);
    }

    private SysRole role(String key) {
        SysRole r=new SysRole();r.setRoleKey(key);r.setStatus("0");
        r.setRoleId("talk_teacher".equals(key)?8L:"college_admin".equals(key)?9L:"school_admin".equals(key)?10L:20L);
        return r;
    }
}
