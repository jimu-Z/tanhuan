package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.talk.domain.*;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.talk.service.impl.TalkStudentServiceImpl;
import com.ruoyi.talk.service.impl.StudentImportRelationsService;
import com.ruoyi.system.mapper.*;
import com.ruoyi.common.core.domain.entity.*;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.talk.mapper.StudentImportBindingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

class StudentImportManagersTest {
    TalkStudentServiceImpl service = new TalkStudentServiceImpl();
    TalkStudentMapper students = mock(TalkStudentMapper.class);
    SysDeptMapper departments = mock(SysDeptMapper.class);
    ITalkTeacherService teachers = mock(ITalkTeacherService.class);
    SysUserMapper users=mock(SysUserMapper.class);
    SysRoleMapper roles=mock(SysRoleMapper.class);
    SysUserRoleMapper userRoles=mock(SysUserRoleMapper.class);
    StudentImportBindingMapper bindings=mock(StudentImportBindingMapper.class);
    MockedStatic<SecurityUtils> security;
    org.springframework.transaction.PlatformTransactionManager transactions;
    @BeforeEach void security() {
        security=mockStatic(SecurityUtils.class,CALLS_REAL_METHODS);
        security.when(SecurityUtils::getUsername).thenReturn("qa-importer");
    }
    @AfterEach void close() { security.close(); }

    void setup() {
        ReflectionTestUtils.setField(service,"talkStudentMapper",students);
        ReflectionTestUtils.setField(service,"sysDeptMapper",departments);
        ReflectionTestUtils.setField(service,"talkTeacherService",teachers);
        transactions=StudentImportTestSupport.transactionManager(service);
        StudentImportRelationsService relations=new StudentImportRelationsService();
        ReflectionTestUtils.setField(relations,"teacherService",teachers);
        ReflectionTestUtils.setField(relations,"userMapper",users);
        ReflectionTestUtils.setField(relations,"roleMapper",roles);
        ReflectionTestUtils.setField(relations,"userRoleMapper",userRoles);
        ReflectionTestUtils.setField(relations,"deptMapper",departments);
        ReflectionTestUtils.setField(relations,"bindings",bindings);
        ReflectionTestUtils.setField(service,"importRelations",relations);
        SysRole role=new SysRole();role.setRoleId(9L);role.setRoleKey("talk_student");role.setDelFlag("0");role.setStatus("1");
        when(roles.selectRoleList(any())).thenReturn(List.of(role));
        when(users.insertUser(any())).thenAnswer(call->{((SysUser)call.getArgument(0)).setUserId(500L);return 1;});
        when(userRoles.batchUserRole(any())).thenReturn(1);
        Map<Long,Map<String,Object>> owners=new HashMap<>();
        when(bindings.selectAccountOwner(anyLong())).thenAnswer(call->owners.get(call.getArgument(0)));
        when(bindings.bindAccount(anyString(),anyLong(),anyLong(),anyLong())).thenAnswer(call->{
            owners.put(call.getArgument(1),Map.of("studentCode",call.getArgument(0),"studentId",call.getArgument(2),"collegeId",call.getArgument(3)));return 1;
        });
        when(bindings.bindingExists(anyLong(),anyLong(),anyLong())).thenReturn(1);
        SysDept clazz=new SysDept();clazz.setDeptId(30L);clazz.setDeptType("class");clazz.setParentId(20L);
        when(departments.selectDeptById(30L)).thenReturn(clazz);
        SysDept grade=new SysDept();grade.setDeptId(20L);grade.setDeptType("grade");grade.setParentId(10L);when(departments.selectDeptById(20L)).thenReturn(grade);
        SysDept college=new SysDept();college.setDeptId(10L);college.setDeptType("college");when(departments.selectDeptById(10L)).thenReturn(college);
        when(departments.selectDeptList(any())).thenAnswer(call -> {
            SysDept q=call.getArgument(0),d=new SysDept();d.setDeptName(q.getDeptName());d.setParentId(q.getParentId());
            d.setDeptId("学院".equals(q.getDeptName())?10L:"年级".equals(q.getDeptName())?20L:30L);d.setDeptType("college");return List.of(d);
        });
        when(students.insertTalkStudent(any())).thenAnswer(call->{((TalkStudent)call.getArgument(0)).setStudentId(100L);return 1;});
    }
    Map<String,Object> row() { return new HashMap<>(Map.of("student_code","20260001","student_name","学生甲","college","学院","grade","年级","class","一班")); }

    @Test void teacherInSameCollegeIsAcceptedRegardlessOfPosition() {
        setup();TalkTeacher t=new TalkTeacher();t.setTeacherId(1L);t.setTeacherCode("T001");t.setTeacherName("张老师");t.setPosition("讲师");t.setDeptId(10L);
        when(teachers.selectCounselorsByDeptId(10L)).thenReturn(List.of(t));
        TalkTeacher query=new TalkTeacher();query.setDeptId(10L);
        when(teachers.selectTalkTeacherList(any())).thenReturn(List.of(t));
        Map<String,Object> data=row();data.put("counselor","张老师");
        try(MockedStatic<TalkPermissionUtils> permissions=mockStatic(TalkPermissionUtils.class)) {
            permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(true);
            assertThat(service.importExecute(List.of(data),"skip").get("successCount")).isEqualTo(1);
            verify(bindings).bindStudent(100L,1L);verify(bindings).bindClass(30L,1L);
        }
    }
    @Test void importingNewStudentCreatesStudentAccount() {
        setup();
        try(MockedStatic<TalkPermissionUtils> permissions=mockStatic(TalkPermissionUtils.class)) {
            permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(true);
            assertThat(service.importExecute(List.of(row()),"skip").get("userCreatedCount")).isEqualTo(1);
            var saved=org.mockito.ArgumentCaptor.forClass(SysUser.class);verify(users).insertUser(saved.capture());
            assertThat(saved.getValue().getUserName()).isEqualTo("20260001");
            assertThat(SecurityUtils.matchesPassword("123456",saved.getValue().getPassword())).isTrue();
            assertThat(saved.getValue().getStatus()).isEqualTo("1");
        }
    }

    TalkTeacher teacher(long id,String code,String name,Long college) {
        TalkTeacher t=new TalkTeacher();t.setTeacherId(id);t.setTeacherCode(code);t.setTeacherName(name);t.setDeptId(college);t.setPosition("讲师");t.setStatus("1");return t;
    }
    Map<String,Object> execute(Map<String,Object> row,String mode) {
        try(MockedStatic<TalkPermissionUtils> permissions=mockStatic(TalkPermissionUtils.class)) {
            permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(true);
            return service.importExecute(List.of(row),mode);
        }
    }
    @Test void severalTeachersIncludingAdministratorCanManageSameStudentAndClassWithoutPositionOrStatusRestriction() {
        setup();TalkTeacher a=teacher(1,"T1","教师甲",10L),b=teacher(2,"T2","管理员乙",10L);b.setRoleKey("college_admin");
        when(teachers.selectTalkTeacherList(any())).thenReturn(List.of(a,b));
        Map<String,Object> row=row();row.put("counselor","教师甲、管理员乙");row.put("secretary","T1");
        assertThat(execute(row,"skip").get("successCount")).isEqualTo(1);
        for(long id:List.of(1L,2L)) { verify(bindings).bindStudent(100L,id);verify(bindings).bindClass(30L,id); }
        verify(bindings,times(2)).bindLegacyClass(anyString(),eq("一班"));
    }
    @Test void staffUsernameCollisionFailsBeforeAnyStudentOrAccountWriteAndRollsBack() {
        setup();SysUser account=new SysUser();account.setUserId(1L);when(users.selectUserByUserName("20260001")).thenReturn(account);
        assertThat(execute(row(),"skip").get("errorCount")).isEqualTo(1);
        verify(students,never()).insertTalkStudent(any());verify(users,never()).insertUser(any());verify(userRoles,never()).batchUserRole(any());
        verify(transactions).rollback(any());verify(transactions,never()).commit(any());
    }
    @Test void repeatedStudentImportKeepsExistingPasswordAndDoesNotAssignAdditionalRoles() {
        setup();TalkStudent old=new TalkStudent();old.setStudentId(100L);old.setDeptId(30L);
        when(students.selectTalkStudentByCode("20260001")).thenReturn(old);when(students.updateTalkStudent(any())).thenReturn(1);
        SysUser account=new SysUser();account.setUserId(500L);account.setDeptId(30L);account.setPassword("already-changed");when(users.selectUserByUserName("20260001")).thenReturn(account);
        SysRole role=new SysRole();role.setRoleKey("talk_student");when(roles.selectRolePermissionByUserId(500L)).thenReturn(List.of(role));
        when(users.updateUser(any())).thenReturn(1);
        assertThat(execute(row(),"update").get("userCreatedCount")).isEqualTo(0);
        var change=org.mockito.ArgumentCaptor.forClass(SysUser.class);verify(users).updateUser(change.capture());
        assertThat(change.getValue().getPassword()).isNull();assertThat(change.getValue().getStatus()).isNull();
        verify(userRoles,never()).batchUserRole(any());verify(users,never()).insertUser(any());
    }
    @Test void sameNameRequiresTeacherCodeAndForeignCollegeTeacherIsRejected() {
        setup();when(teachers.selectTalkTeacherList(any())).thenReturn(List.of(teacher(1,"T1","同名",10L),teacher(2,"T2","同名",10L),teacher(3,"T3","外院",11L)));
        Map<String,Object> row=row();row.put("head_teacher","同名");assertThat(execute(row,"skip").get("errorCount")).isEqualTo(1);
        row.put("head_teacher","外院");assertThat(execute(row,"skip").get("errorCount")).isEqualTo(1);
        verify(students,never()).insertTalkStudent(any());
        row.put("head_teacher","T2");assertThat(execute(row,"skip").get("successCount")).isEqualTo(1);verify(bindings).bindStudent(100L,2L);
    }
    @Test void roleSaveFailureRollsBackRowAndIsNotReportedAsSuccess() {
        setup();when(userRoles.batchUserRole(any())).thenThrow(new IllegalStateException("role storage unavailable"));
        Map<String,Object> result=execute(row(),"skip");assertThat(result.get("successCount")).isEqualTo(0);assertThat(result.get("userCreatedCount")).isEqualTo(0);
        verify(transactions).rollback(any());verify(transactions,never()).commit(any());verifyNoInteractions(bindings);
    }
    @Test void legacyStudentAccountFromAnotherCollegeCannotBeAdopted() {
        setup();TalkStudent old=new TalkStudent();old.setStudentId(100L);old.setDeptId(30L);when(students.selectTalkStudentByCode("20260001")).thenReturn(old);
        when(students.updateTalkStudent(any())).thenReturn(1);
        SysUser account=new SysUser();account.setUserId(500L);account.setDeptId(40L);when(users.selectUserByUserName("20260001")).thenReturn(account);
        SysRole role=new SysRole();role.setRoleKey("talk_student");when(roles.selectRolePermissionByUserId(500L)).thenReturn(List.of(role));when(users.updateUser(any())).thenReturn(1);
        SysDept foreign=new SysDept();foreign.setDeptId(40L);foreign.setDeptType("college");when(departments.selectDeptById(40L)).thenReturn(foreign);
        assertThat(execute(row(),"update").get("errorCount")).isEqualTo(1);
        verify(users,never()).updateUser(any());
    }
}
