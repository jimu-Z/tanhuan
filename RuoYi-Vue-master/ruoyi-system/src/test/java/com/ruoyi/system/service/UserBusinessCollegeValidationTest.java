package com.ruoyi.system.service;

import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.*;
import com.ruoyi.system.service.impl.SysRoleServiceImpl;
import com.ruoyi.system.service.impl.SysUserServiceImpl;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserBusinessCollegeValidationTest {
    SysUserServiceImpl users;
    SysRoleServiceImpl roles;
    SysUserMapper userMapper;
    SysRoleMapper roleMapper;
    SysDeptMapper deptMapper;
    SysUserRoleMapper links;
    SysUserPostMapper posts;
    ISysDeptService depts;
    @BeforeEach void setup() {
        users=spy(new SysUserServiceImpl()); roles=spy(new SysRoleServiceImpl());
        userMapper=mock(SysUserMapper.class); roleMapper=mock(SysRoleMapper.class);
        deptMapper=mock(SysDeptMapper.class); links=mock(SysUserRoleMapper.class);
        posts=mock(SysUserPostMapper.class); depts=mock(ISysDeptService.class);
        ReflectionTestUtils.setField(users,"userMapper",userMapper);
        ReflectionTestUtils.setField(users,"roleMapper",roleMapper);
        ReflectionTestUtils.setField(users,"userRoleMapper",links);
        ReflectionTestUtils.setField(users,"userPostMapper",posts);
        ReflectionTestUtils.setField(users,"deptService",depts);
        Validator validator=mock(Validator.class);
        when(validator.validate(any(SysUser.class))).thenReturn(Set.of());
        ReflectionTestUtils.setField(users,"validator",validator);
        ReflectionTestUtils.setField(roles,"roleMapper",roleMapper);
        ReflectionTestUtils.setField(roles,"userRoleMapper",links);
        doNothing().when(users).checkUserDataScope(anyLong());
        doNothing().when(roles).checkRoleDataScope(any(Long[].class));
        when(roleMapper.selectRoleById(8L)).thenReturn(role(8L,"talk_teacher"));
        when(roleMapper.selectRoleById(7L)).thenReturn(role(7L,"college_admin"));
        when(roleMapper.selectRoleById(3L)).thenReturn(role(3L,"school_admin"));
        when(roleMapper.selectRoleById(2L)).thenReturn(role(2L,"common"));
        when(roleMapper.selectRolePermissionByUserId(910014L)).thenReturn(List.of(role(8L,"talk_teacher")));
        when(deptMapper.selectDeptById(910001L)).thenReturn(dept(910001L,"college"));
        when(deptMapper.selectDeptById(910002L)).thenReturn(dept(910002L,"grade"));
        when(deptMapper.selectDeptById(910003L)).thenReturn(dept(910003L,"class"));
        when(userMapper.insertUser(any())).thenReturn(1);
        when(userMapper.updateUser(any())).thenReturn(1);
        BusinessRoleCollegeValidator guard=new BusinessRoleCollegeValidator(roleMapper,deptMapper,userMapper);
        ReflectionTestUtils.setField(users,"collegeValidator",guard);
        ReflectionTestUtils.setField(roles,"collegeValidator",guard);
    }
    @Test void teacherInsertRejectsClassBeforeAnyWrite() {
        assertThatThrownBy(()->users.insertUser(user(910014L,910003L,8L))).isInstanceOf(ServiceException.class).hasMessageContaining("学院");
        verify(userMapper,never()).insertUser(any());verifyNoInteractions(links,posts);
    }
    @Test void teacherUpdateRejectsClassBeforeDeletingAssociations() {
        assertThatThrownBy(()->users.updateUser(user(910014L,910003L,8L))).isInstanceOf(ServiceException.class).hasMessageContaining("学院");
        verify(userMapper,never()).updateUser(any());verifyNoInteractions(links,posts);
    }
    @Test void collegeAdminRejectsGrade() {
        assertThatThrownBy(()->users.updateUser(user(910014L,910002L,7L))).isInstanceOf(ServiceException.class).hasMessageContaining("学院");
        verifyNoInteractions(links,posts);
    }
    @Test void restrictedRoleRequiresDepartment() {
        assertThatThrownBy(()->users.insertUser(user(910014L,null,8L))).isInstanceOf(ServiceException.class).hasMessageContaining("学院");
        verify(userMapper,never()).insertUser(any());
    }
    @Test void userRoleAuthorizationChecksExistingDepartmentBeforeDeletingRoles() {
        when(userMapper.selectUserById(910014L)).thenReturn(user(910014L,910003L,2L));
        assertThatThrownBy(()->users.insertUserAuth(910014L,new Long[]{8L})).isInstanceOf(ServiceException.class).hasMessageContaining("学院");
        verifyNoInteractions(links);
    }
    @Test void roleBatchChecksAllTargetsBeforeWriting() {
        when(userMapper.selectUserById(910013L)).thenReturn(user(910013L,910001L,7L));
        when(userMapper.selectUserById(910014L)).thenReturn(user(910014L,910003L,2L));
        assertThatThrownBy(()->roles.insertAuthUsers(8L,new Long[]{910013L,910014L})).isInstanceOf(ServiceException.class).hasMessageContaining("学院");
        verifyNoInteractions(links);
    }
    @Test void existingUserImportDoesNotPersistInvalidBusinessBinding() {
        SysUser existing=user(910014L,910003L,8L);
        when(userMapper.selectUserByUserName("qa_teacher")).thenReturn(existing);
        assertThatThrownBy(()->users.importUser(List.of(user(910014L,910001L)),true,"qa_admin")).isInstanceOf(ServiceException.class).hasMessageContaining("学院");
        verify(userMapper,never()).updateUser(any());
    }
    @Test void properCollegeKeepsNormalWrites() {
        assertThat(users.updateUser(user(910014L,910001L,8L,7L))).isEqualTo(1);
        verify(userMapper).updateUser(any());verify(links).deleteUserRoleByUserId(910014L);
    }
    @Test void schoolAndGenericRolesKeepTheirDepartmentSemantics() {
        assertThat(users.insertUser(user(910014L,910003L,3L,2L))).isEqualTo(1);
        verify(userMapper).insertUser(any());
    }
    @Test void invalidDepartmentKindsAndMissingRecordsFailForBothBusinessRoles() {
        for (String type : new String[]{"school","grade","class","dept","unknown",null}) {
            when(deptMapper.selectDeptById(920001L)).thenReturn(dept(920001L,type));
            for (Long roleId : new Long[]{7L,8L}) {
                assertThatThrownBy(()->users.updateUser(user(910014L,920001L,roleId))).isInstanceOf(ServiceException.class).hasMessageContaining("学院");
            }
        }
        assertThatThrownBy(()->users.updateUser(user(910014L,999999L,8L))).isInstanceOf(ServiceException.class);
        verifyNoInteractions(links,posts);verify(userMapper,never()).updateUser(any());
    }
    @Test void deletedCollegeCannotSatisfyBusinessRoleBinding() {
        SysDept deleted=dept(920001L,"college");deleted.setDelFlag("2");
        when(deptMapper.selectDeptById(920001L)).thenReturn(deleted);
        assertThatThrownBy(()->users.updateUser(user(910014L,920001L,8L))).isInstanceOf(ServiceException.class).hasMessageContaining("学院");
        verifyNoInteractions(links,posts);
    }
    @Test void businessRoleMixedWithGlobalRoleStillNeedsCollege() {
        assertThatThrownBy(()->users.updateUser(user(910014L,910003L,3L,8L))).isInstanceOf(ServiceException.class).hasMessageContaining("学院");
        verifyNoInteractions(links,posts);
    }
    @Test void validRoleBatchKeepsEveryRequestedPair() {
        when(userMapper.selectUserById(910013L)).thenReturn(user(910013L,910001L,7L));
        when(userMapper.selectUserById(910014L)).thenReturn(user(910014L,910001L,8L));
        roles.insertAuthUsers(8L,new Long[]{910013L,910014L});
        verify(links).batchUserRole(argThat(x->x.size()==2 && x.get(0).getUserId()==910013L && x.get(1).getUserId()==910014L && x.stream().allMatch(v->v.getRoleId()==8L)));
    }
    @Test void validCollegeUserAuthorizationStillWrites() {
        when(userMapper.selectUserById(910014L)).thenReturn(user(910014L,910001L,2L));
        users.insertUserAuth(910014L,new Long[]{8L});
        verify(links).deleteUserRoleByUserId(910014L);verify(links).batchUserRole(anyList());
    }
    @Test void validExistingImportPreservesOriginalDepartmentAndAssociations() {
        when(userMapper.selectUserByUserName("qa_teacher")).thenReturn(user(910014L,910001L,8L));
        assertThat(users.importUser(List.of(user(910014L,910003L)),true,"qa_admin")).contains("更新成功");
        verify(userMapper).updateUser(argThat(u->u.getDeptId()==910001L && u.getPassword()==null));
        verifyNoInteractions(links,posts);
    }
    @Test void removingBusinessAccessCanRepairAnInvalidStoredDepartment() {
        when(userMapper.selectUserById(910014L)).thenReturn(user(910014L,910003L,8L));
        users.insertUserAuth(910014L,new Long[]{2L});
        verify(links).deleteUserRoleByUserId(910014L);verify(links).batchUserRole(anyList());
    }
    @Test void revokingBusinessRoleDoesNotRequireValidDepartment() {
        com.ruoyi.system.domain.SysUserRole link=new com.ruoyi.system.domain.SysUserRole();link.setUserId(910014L);link.setRoleId(8L);
        roles.deleteAuthUser(link);verify(links).deleteUserRoleInfo(link);
    }
    static SysUser user(Long id,Long dept,Long... ids) {SysUser u=new SysUser(id);u.setDeptId(dept);u.setUserName("qa_teacher");u.setNickName("QA老师");u.setRoleIds(ids);return u;}
    static SysRole role(Long id,String key) {SysRole r=new SysRole(id);r.setRoleKey(key);return r;}
    static SysDept dept(Long id,String type) {SysDept d=new SysDept();d.setDeptId(id);d.setDeptType(type);d.setDelFlag("0");return d;}
}
