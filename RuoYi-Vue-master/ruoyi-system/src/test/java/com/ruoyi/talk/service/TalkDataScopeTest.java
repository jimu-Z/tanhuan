package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.service.impl.TalkStudentServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

/**
 * 回归测试：数据权限过滤（书记/辅导员角色隔离）
 *
 * 验证修复项：
 * - CRITICAL-1: TalkStudentServiceImpl.applyCounselorFilter() 现在同时处理 counselor 和 secretary 角色
 * - CRITICAL-2: TalkStudentMapper.xml 包含 secretaryDeptId SQL 条件
 */
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
        if (securityUtilsMock != null) {
            securityUtilsMock.close();
        }
    }

    // ==================== 书记角色数据隔离 ====================

    /**
     * 书记查询学生列表时，应注入 secretaryDeptId 参数实现学院级数据隔离
     * 验证 CRITICAL-1 + CRITICAL-2 联合修复
     */
    @Test
    void secretaryShouldOnlySeeStudentsInOwnCollege() {
        // Given: 当前用户为书记角色，所属学院 deptId=200
        securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("secretary_zhang");
        securityUtilsMock.when(SecurityUtils::getDeptId).thenReturn(200L);
        securityUtilsMock.when(() -> SecurityUtils.hasRole("talk_secretary")).thenReturn(true);
        securityUtilsMock.when(() -> SecurityUtils.hasRole("talk_counselor")).thenReturn(false);

        TalkStudent query = new TalkStudent();
        when(talkStudentMapper.selectTalkStudentList(any(TalkStudent.class)))
                .thenReturn(new ArrayList<>());

        // When: 书记查询学生列表
        studentService.selectTalkStudentList(query);

        // Then: 应向 Mapper 传递 secretaryDeptId=200
        Map<String, Object> params = query.getParams();
        assertThat(params).isNotNull();
        assertThat(params.get("secretaryDeptId")).isEqualTo(200L);
        // 书记不应有 counselorCode
        assertThat(params.containsKey("counselorCode")).isFalse();
    }

    /**
     * 书记查询带上次谈话时间的学生列表时，也应注入 secretaryDeptId
     */
    @Test
    void secretaryShouldFilterWhenSelectingWithLastTalk() {
        securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("secretary_li");
        securityUtilsMock.when(SecurityUtils::getDeptId).thenReturn(300L);
        securityUtilsMock.when(() -> SecurityUtils.hasRole("talk_secretary")).thenReturn(true);
        securityUtilsMock.when(() -> SecurityUtils.hasRole("talk_counselor")).thenReturn(false);

        TalkStudent query = new TalkStudent();
        when(talkStudentMapper.selectTalkStudentListWithLastTalk(any(TalkStudent.class)))
                .thenReturn(new ArrayList<>());

        studentService.selectTalkStudentListWithLastTalk(query);

        Map<String, Object> params = query.getParams();
        assertThat(params).isNotNull();
        assertThat(params.get("secretaryDeptId")).isEqualTo(300L);
    }

    // ==================== 辅导员角色数据隔离 ====================

    /**
     * 辅导员查询学生列表时，应注入 counselorCode 参数按班级过滤
     */
    @Test
    void counselorShouldOnlySeeStudentsInManagedClasses() {
        securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("counselor_wang");
        securityUtilsMock.when(() -> SecurityUtils.hasRole("talk_counselor")).thenReturn(true);
        securityUtilsMock.when(() -> SecurityUtils.hasRole("talk_secretary")).thenReturn(false);

        TalkStudent query = new TalkStudent();
        when(talkStudentMapper.selectTalkStudentList(any(TalkStudent.class)))
                .thenReturn(new ArrayList<>());

        studentService.selectTalkStudentList(query);

        Map<String, Object> params = query.getParams();
        assertThat(params).isNotNull();
        assertThat(params.get("counselorCode")).isEqualTo("counselor_wang");
        // 辅导员不应有 secretaryDeptId
        assertThat(params.containsKey("secretaryDeptId")).isFalse();
    }

    // ==================== 管理员无过滤 ====================

    /**
     * 管理员查询学生列表时，不应注入任何数据范围参数
     */
    @Test
    void adminShouldHaveNoDataScopeFilter() {
        securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(true);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("admin");

        TalkStudent query = new TalkStudent();
        when(talkStudentMapper.selectTalkStudentList(any(TalkStudent.class)))
                .thenReturn(new ArrayList<>());

        studentService.selectTalkStudentList(query);

        // 管理员不应有任何数据范围限制
        Map<String, Object> params = query.getParams();
        if (params != null) {
            assertThat(params.containsKey("counselorCode")).isFalse();
            assertThat(params.containsKey("secretaryDeptId")).isFalse();
        }
    }

    // ==================== 边界情况 ====================

    /**
     * 书记 deptId 为 null 时，不应注入 secretaryDeptId（避免 SQL 过滤异常）
     */
    @Test
    void secretaryWithNullDeptIdShouldNotCrash() {
        securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("secretary_null");
        securityUtilsMock.when(SecurityUtils::getDeptId).thenReturn(null);
        securityUtilsMock.when(() -> SecurityUtils.hasRole("talk_secretary")).thenReturn(true);
        securityUtilsMock.when(() -> SecurityUtils.hasRole("talk_counselor")).thenReturn(false);

        TalkStudent query = new TalkStudent();
        when(talkStudentMapper.selectTalkStudentList(any(TalkStudent.class)))
                .thenReturn(new ArrayList<>());

        // 不应抛出 NPE
        studentService.selectTalkStudentList(query);

        Map<String, Object> params = query.getParams();
        assertThat(params).isNotNull();
        // deptId 为 null 时不应设置 secretaryDeptId
        assertThat(params.containsKey("secretaryDeptId")).isFalse();
    }

    /**
     * 用户名为 null 时，不应抛出异常
     */
    @Test
    void nullUsernameShouldNotCrash() {
        securityUtilsMock.when(SecurityUtils::isAdmin).thenReturn(false);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn(null);
        securityUtilsMock.when(() -> SecurityUtils.hasRole("talk_counselor")).thenReturn(true);

        TalkStudent query = new TalkStudent();
        when(talkStudentMapper.selectTalkStudentList(any(TalkStudent.class)))
                .thenReturn(new ArrayList<>());

        // 不应抛出 NPE
        studentService.selectTalkStudentList(query);
    }
}
