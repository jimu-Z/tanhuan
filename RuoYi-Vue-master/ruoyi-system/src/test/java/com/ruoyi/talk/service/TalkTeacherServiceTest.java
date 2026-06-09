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
 * TDD T1: TalkTeacherService 教师导入测试
 *
 * 测试行为：管理员导入教师时，自动创建 sys_user + 分配角色
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

    /**
     * RED 1: 导入辅导员 → 自动创建 sys_user → 分配 talk_counselor 角色
     */
    @Test
    void insertCounselorShouldCreateSysUserAndAssignRole() {
        // Given: 角色查询返回 talk_counselor (roleId=5)
        SysRole counselorRole = new SysRole();
        counselorRole.setRoleId(5L);
        when(roleMapper.selectRoleList(any())).thenReturn(List.of(counselorRole));

        when(teacherMapper.insertTalkTeacher(any())).thenReturn(1);
        when(userMapper.selectUserByUserName("T001")).thenReturn(null);  // 用户不存在，需创建
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
        assertThat(createdUser.getUserName()).isEqualTo("T001");  // 账号=工号
        assertThat(createdUser.getNickName()).isEqualTo("张老师");

        // Then: 角色分配
        ArgumentCaptor<List<SysUserRole>> roleCaptor = ArgumentCaptor.forClass(List.class);
        verify(userRoleMapper).batchUserRole(roleCaptor.capture());
        SysUserRole userRole = roleCaptor.getValue().get(0);
        assertThat(userRole.getRoleId()).isEqualTo(5L);
    }

    /**
     * RED 2: 工号已存在 → 返回不唯一（false）
     */
    @Test
    void duplicateTeacherCodeShouldReturnNotUnique() {
        when(teacherMapper.checkTeacherCodeUnique("T001")).thenReturn(1);

        boolean isUnique = teacherService.checkTeacherCodeUnique(testTeacher);

        assertThat(isUnique).isFalse();
    }
}
