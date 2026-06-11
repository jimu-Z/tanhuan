package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.ArgumentMatchers.anyLong;

import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;
import com.ruoyi.talk.service.impl.TalkStudentServiceImpl;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TDD: TalkStudentService 学生管理测试
 */
@ExtendWith(MockitoExtension.class)
class TalkStudentServiceTest {

    @Mock
    private TalkStudentMapper talkStudentMapper;

    @Mock
    private SysDeptMapper sysDeptMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    @Mock
    private TalkStudentRecordMapper talkStudentRecordMapper;

    @Mock
    private TalkSessionMapper talkSessionMapper;

    @Mock
    private ITalkTeacherService talkTeacherService;

    @InjectMocks
    private TalkStudentServiceImpl studentService;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    private TalkStudent testStudent;
    private SysUser testUser;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("admin");

        testStudent = new TalkStudent();
        testStudent.setStudentId(1L);
        testStudent.setStudentCode("S001");
        testStudent.setStudentName("张三");
        testStudent.setDeptId(100L);

        testUser = new SysUser();
        testUser.setUserId(20L);
        testUser.setUserName("S001");
        testUser.setNickName("张三");
    }

    @AfterEach
    void tearDown() {
        if (securityUtilsMock != null) {
            securityUtilsMock.close();
        }
    }

    // ==================== T2: 学生删除 → 物理删除 sys_user ====================

    /**
     * 删除学生时，应通过 studentCode 找到 sys_user 并物理删除
     */
    @Test
    void deleteStudentShouldPhysicallyDeleteUserByStudentCode() {
        // Given: 学生没有谈话记录
        when(talkStudentMapper.countRecordsByStudentId(1L)).thenReturn(0);
        when(talkStudentMapper.selectTalkStudentByStudentId(1L)).thenReturn(testStudent);
        // sys_user 存在，userName=studentCode
        when(sysUserMapper.selectUserByUserName("S001")).thenReturn(testUser);
        when(talkStudentMapper.deleteTalkStudentByStudentIds(any(Long[].class))).thenReturn(1);

        // When: 批量删除学生
        int result = studentService.deleteTalkStudentByStudentIds(new Long[] { 1L });

        // Then: 删除成功
        assertThat(result).isGreaterThan(0);

        // Then: 应物理删除 sys_user
        verify(sysUserMapper).deleteUserPhysically(20L);
    }

    /**
     * 删除学生时，若 sys_user 不存在则只删学生记录，不报错
     */
    @Test
    void deleteStudentWithoutMatchingUserShouldNotFail() {
        // Given: studentCode 对应的 sys_user 不存在
        when(talkStudentMapper.countRecordsByStudentId(1L)).thenReturn(0);
        when(talkStudentMapper.selectTalkStudentByStudentId(1L)).thenReturn(testStudent);
        when(sysUserMapper.selectUserByUserName("S001")).thenReturn(null);
        when(talkStudentMapper.deleteTalkStudentByStudentIds(any(Long[].class))).thenReturn(1);

        // When: 删除学生
        int result = studentService.deleteTalkStudentByStudentIds(new Long[] { 1L });

        // Then: 删除成功且不报错
        assertThat(result).isGreaterThan(0);
        verify(sysUserMapper, org.mockito.Mockito.never()).deleteUserPhysically(org.mockito.ArgumentMatchers.anyLong());
    }

    /**
     * 单个删除学生时也物理删除 sys_user
     */
    @Test
    void deleteSingleStudentShouldPhysicallyDeleteUser() {
        when(talkStudentMapper.countRecordsByStudentId(1L)).thenReturn(0);
        when(talkStudentMapper.selectTalkStudentByStudentId(1L)).thenReturn(testStudent);
        when(sysUserMapper.selectUserByUserName("S001")).thenReturn(testUser);
        when(talkStudentMapper.deleteTalkStudentByStudentId(1L)).thenReturn(1);

        int result = studentService.deleteTalkStudentByStudentId(1L);

        assertThat(result).isGreaterThan(0);
        verify(sysUserMapper).deleteUserPhysically(20L);
    }

    // ==================== T3: 修改学生 → 同步 nickname ====================

    /**
     * 修改学生姓名时，应同步更新 sys_user 的 nick_name
     */
    @Test
    void updateStudentShouldSyncNickNameToSysUser() {
        // Given: 学生改了姓名
        testStudent.setStudentName("张三丰");
        when(talkStudentMapper.updateTalkStudent(testStudent)).thenReturn(1);
        when(sysUserMapper.selectUserByUserName("S001")).thenReturn(testUser);

        // When: 修改学生
        int result = studentService.updateTalkStudent(testStudent);

        // Then: 修改成功
        assertThat(result).isGreaterThan(0);

        // Then: sys_user.nick_name 同步为新的学生姓名
        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).updateUser(userCaptor.capture());
        SysUser updatedUser = userCaptor.getValue();
        assertThat(updatedUser.getNickName()).isEqualTo("张三丰");
    }

    /**
     * 修改非姓名的学生字段时，不应更新 sys_user
     */
    @Test
    void updateStudentWithoutNameChangeShouldNotTouchSysUser() {
        // Given: 姓名未变，只改其他字段
        testStudent.setStudentName("张三"); // 没变
        testStudent.setPhone("13900001111");
        when(talkStudentMapper.updateTalkStudent(testStudent)).thenReturn(1);
        // 查到的 user 的 nickName 已经是 "张三"
        when(sysUserMapper.selectUserByUserName("S001")).thenReturn(testUser);

        // When: 修改学生
        studentService.updateTalkStudent(testStudent);

        // Then: 姓名没变，nickName 也没变，不应调用 updateUser
        verify(sysUserMapper, org.mockito.Mockito.never()).updateUser(org.mockito.ArgumentMatchers.any(SysUser.class));
    }

    /**
     * 修改学生时若 sys_user 不存在，不报错
     */
    @Test
    void updateStudentWithoutMatchingUserShouldNotFail() {
        testStudent.setStudentName("李四");
        when(talkStudentMapper.updateTalkStudent(testStudent)).thenReturn(1);
        when(sysUserMapper.selectUserByUserName("S001")).thenReturn(null);

        int result = studentService.updateTalkStudent(testStudent);

        assertThat(result).isGreaterThan(0);
        verify(sysUserMapper, org.mockito.Mockito.never()).updateUser(org.mockito.ArgumentMatchers.any(SysUser.class));
    }
}
