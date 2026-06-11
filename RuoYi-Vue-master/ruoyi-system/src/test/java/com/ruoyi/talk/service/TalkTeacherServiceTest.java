package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.talk.domain.TalkTeacher;
import com.ruoyi.talk.mapper.TalkTeacherMapper;
import com.ruoyi.talk.service.impl.TalkTeacherServiceImpl;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

/**
 * TDD: TalkTeacherService 教师管理测试
 */
@ExtendWith(MockitoExtension.class)
class TalkTeacherServiceTest {

    @Mock
    private TalkTeacherMapper teacherMapper;

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @InjectMocks
    private TalkTeacherServiceImpl teacherService;

    private TalkTeacher testTeacher;

    @BeforeEach
    void setUp() {
        testTeacher = new TalkTeacher();
        testTeacher.setTeacherCode("T001");
        testTeacher.setTeacherName("张老师");
        testTeacher.setDeptId(100L);
        testTeacher.setPosition("counselor");
        testTeacher.setPhone("13800138000");
        testTeacher.setCreateBy("admin");
    }

    // ==================== T1: 教师删除 → 物理删除 sys_user ====================

    /**
     * 删除教师时，应物理删除（DELETE FROM）关联的 sys_user，而非逻辑删除
     */
    @Test
    void deleteTeacherShouldPhysicallyDeleteUser() {
        // Given: 教师有 userId=10
        testTeacher.setTeacherId(1L);
        testTeacher.setUserId(10L);
        when(teacherMapper.selectTalkTeacherById(1L)).thenReturn(testTeacher);
        when(teacherMapper.deleteTalkTeacherById(1L)).thenReturn(1);

        // When: 删除教师
        int result = teacherService.deleteTalkTeacherByIds(new Long[] { 1L });

        // Then: 返回删除条数 > 0
        assertThat(result).isGreaterThan(0);

        // Then: 应调用物理删除 user，而不是逻辑删除
        verify(userMapper).deleteUserPhysically(10L);
    }

    /**
     * 删除没有 userId 的教师时，不应尝试删用户
     */
    @Test
    void deleteTeacherWithoutUserIdShouldNotDeleteUser() {
        // Given: 教师没有 userId
        testTeacher.setTeacherId(1L);
        testTeacher.setUserId(null);
        when(teacherMapper.selectTalkTeacherById(1L)).thenReturn(testTeacher);
        when(teacherMapper.deleteTalkTeacherById(1L)).thenReturn(1);

        // When: 删除教师
        teacherService.deleteTalkTeacherByIds(new Long[] { 1L });

        // Then: 不应该调用任何 userMapper 的删除方法
        verify(userMapper, org.mockito.Mockito.never()).deleteUserPhysically(org.mockito.ArgumentMatchers.anyLong());
    }

    // ==================== 原有测试：导入教师创建用户 ====================

    /**
     * 导入辅导员 → 自动创建 sys_user → 分配 talk_counselor 角色
     */
    @Test
    void insertCounselorShouldCreateSysUserAndAssignRole() {
        // Given: 角色查询返回 talk_counselor (roleId=5)
        SysRole counselorRole = new SysRole();
        counselorRole.setRoleId(5L);
        when(roleMapper.selectRoleList(any())).thenReturn(List.of(counselorRole));

        when(teacherMapper.insertTalkTeacher(any())).thenReturn(1);
        when(userMapper.selectUserByUserName("T001")).thenReturn(null); // 用户不存在，需创建
        when(userMapper.insertUser(any(SysUser.class))).thenReturn(1);

        // When
        int result = teacherService.insertTalkTeacher(testTeacher);

        // Then: 返回值 > 0
        assertThat(result).isGreaterThan(0);

        // Then: talk_teacher 已插入
        verify(teacherMapper).insertTalkTeacher(testTeacher);

        // Then: sys_user 已创建
        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(userMapper).insertUser(userCaptor.capture());
        SysUser createdUser = userCaptor.getValue();
        assertThat(createdUser.getUserName()).isEqualTo("T001"); // 账号=工号
        assertThat(createdUser.getNickName()).isEqualTo("张老师");

        // Then: 角色分配
        ArgumentCaptor<List<SysUserRole>> roleCaptor = ArgumentCaptor.forClass(List.class);
        verify(userRoleMapper).batchUserRole(roleCaptor.capture());
        SysUserRole userRole = roleCaptor.getValue().get(0);
        assertThat(userRole.getRoleId()).isEqualTo(5L);
    }

    /**
     * 工号已存在 → 返回不唯一（false）
     */
    @Test
    void duplicateTeacherCodeShouldReturnNotUnique() {
        when(teacherMapper.checkTeacherCodeUnique("T001")).thenReturn(1);

        boolean isUnique = teacherService.checkTeacherCodeUnique(testTeacher);

        assertThat(isUnique).isFalse();
    }
}
