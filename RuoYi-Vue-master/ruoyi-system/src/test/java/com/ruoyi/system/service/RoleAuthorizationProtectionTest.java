package com.ruoyi.system.service;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.system.service.impl.SysRoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

class RoleAuthorizationProtectionTest {
    private SysRoleServiceImpl service;
    private SysUserRoleMapper links;

    @BeforeEach void setup() {
        service = spy(new SysRoleServiceImpl());
        links = mock(SysUserRoleMapper.class);
        ReflectionTestUtils.setField(service, "userRoleMapper", links);
        ReflectionTestUtils.setField(service, "collegeValidator", mock(BusinessRoleCollegeValidator.class));
        doNothing().when(service).checkRoleDataScope(any(Long[].class));
    }

    @Test void builtinUserAtEitherEndRejectsWholeInsertBeforeWriting() {
        for (Long[] ids : new Long[][]{{1L, 910014L}, {910014L, 1L}}) {
            assertThatThrownBy(() -> service.insertAuthUsers(12L, ids))
                .isInstanceOf(ServiceException.class).hasMessageContaining("超级管理员用户");
        }
        verifyNoInteractions(links);
    }

    @Test void builtinUserCannotBeRevokedIndividually() {
        assertThatThrownBy(() -> service.deleteAuthUser(link(1L, 12L)))
            .isInstanceOf(ServiceException.class).hasMessageContaining("超级管理员用户");
        verifyNoInteractions(links);
    }

    @Test void builtinUserAtEitherEndRejectsWholeRevokeBatch() {
        for (Long[] ids : new Long[][]{{1L, 910014L}, {910014L, 1L}}) {
            assertThatThrownBy(() -> service.deleteAuthUsers(12L, ids))
                .isInstanceOf(ServiceException.class).hasMessageContaining("超级管理员用户");
        }
        verifyNoInteractions(links);
    }

    @Test void builtinRoleCannotBeAssignedThroughHiddenEndpoint() {
        assertThatThrownBy(() -> service.insertAuthUsers(1L, new Long[]{910014L}))
            .isInstanceOf(ServiceException.class).hasMessageContaining("超级管理员角色");
        verifyNoInteractions(links);
    }

    @Test void builtinRoleCannotBeRevokedIndividually() {
        assertThatThrownBy(() -> service.deleteAuthUser(link(910014L, 1L)))
            .isInstanceOf(ServiceException.class).hasMessageContaining("超级管理员角色");
        verifyNoInteractions(links);
    }

    @Test void builtinRoleCannotBeRevokedInBatch() {
        assertThatThrownBy(() -> service.deleteAuthUsers(1L, new Long[]{910014L}))
            .isInstanceOf(ServiceException.class).hasMessageContaining("超级管理员角色");
        verifyNoInteractions(links);
    }

    @Test void regularInsertKeepsAllRequestedUserRolePairs() {
        service.insertAuthUsers(12L, new Long[]{910013L, 910014L});
        ArgumentCaptor<List<SysUserRole>> captor = ArgumentCaptor.forClass(List.class);
        verify(links).batchUserRole(captor.capture());
        assertThat(captor.getValue()).extracting(SysUserRole::getUserId).containsExactly(910013L, 910014L);
        assertThat(captor.getValue()).extracting(SysUserRole::getRoleId).containsOnly(12L);
        verify(service).checkRoleDataScope(12L);
    }

    @Test void regularRevokesKeepTheExactTargetAndCheckScope() {
        SysUserRole target = link(910014L, 12L);
        service.deleteAuthUser(target);
        service.deleteAuthUsers(12L, new Long[]{910013L, 910014L});
        verify(links).deleteUserRoleInfo(target);
        verify(links).deleteUserRoleInfos(eq(12L), aryEq(new Long[]{910013L, 910014L}));
        verify(service, times(2)).checkRoleDataScope(12L);
    }

    @Test void outOfScopeRoleRejectsEveryMutationBeforeWriting() {
        doThrow(new ServiceException("没有权限访问角色数据")).when(service).checkRoleDataScope(any(Long[].class));
        assertThatThrownBy(() -> service.insertAuthUsers(12L, new Long[]{910014L})).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> service.deleteAuthUser(link(910014L, 12L))).isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> service.deleteAuthUsers(12L, new Long[]{910014L})).isInstanceOf(ServiceException.class);
        verifyNoInteractions(links);
    }

    private SysUserRole link(Long userId, Long roleId) {
        SysUserRole pair = new SysUserRole();
        pair.setUserId(userId);
        pair.setRoleId(roleId);
        return pair;
    }
}