package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.service.impl.TalkStudentServiceImpl;

/** 学生不再绑定登录账号的回归测试。 */
@ExtendWith(MockitoExtension.class)
class TalkStudentServiceTest {

    @Test void blockedStudentDeletionRetainsBusinessReason() {
        when(talkStudentMapper.selectTalkStudentByStudentId(1L)).thenReturn(student);
        when(talkStudentMapper.countRecordsByStudentId(1L)).thenReturn(2);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> studentService.deleteTalkStudentByStudentIds(new Long[]{1L}))
                .isInstanceOf(com.ruoyi.common.exception.ServiceException.class).hasMessageContaining("2 条谈话记录");
        verify(talkStudentMapper,never()).deleteTalkStudentByStudentIds(any());
    }
    @Mock
    private TalkStudentMapper talkStudentMapper;
    @Mock
    private SysUserMapper sysUserMapper;
    @InjectMocks
    private TalkStudentServiceImpl studentService;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private TalkStudent student;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(true);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("admin");
        student = new TalkStudent();
        student.setStudentId(1L);
        student.setStudentCode("S001");
        student.setStudentName("张三");
        student.setDeptId(200L);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    void deletingStudentShouldNotDeleteLegacyLoginAccount() {
        when(talkStudentMapper.selectTalkStudentByStudentId(1L)).thenReturn(student);
        when(talkStudentMapper.countRecordsByStudentId(1L)).thenReturn(0);
        when(talkStudentMapper.deleteTalkStudentByStudentIds(any(Long[].class))).thenReturn(1);

        assertThat(studentService.deleteTalkStudentByStudentIds(new Long[] { 1L })).isEqualTo(1);

        verify(sysUserMapper, never()).deleteUserPhysically(any());
    }

    @Test
    void updatingStudentShouldNotSynchronizeLegacyLoginAccount() {
        TalkStudent update = new TalkStudent();
        update.setStudentId(1L);
        update.setStudentName("张三丰");
        when(talkStudentMapper.selectTalkStudentByStudentId(1L)).thenReturn(student);
        when(talkStudentMapper.updateTalkStudent(update)).thenReturn(1);

        assertThat(studentService.updateTalkStudent(update)).isEqualTo(1);

        verify(sysUserMapper, never()).updateUser(any(SysUser.class));
    }
}
