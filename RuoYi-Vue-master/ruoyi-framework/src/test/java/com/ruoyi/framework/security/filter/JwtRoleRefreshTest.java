package com.ruoyi.framework.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Set;
import jakarta.servlet.FilterChain;
import com.ruoyi.common.core.domain.entity.*;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.framework.web.service.SysPermissionService;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.talk.security.TalkPermissionUtils;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class JwtRoleRefreshTest {
    private JwtAuthenticationTokenFilter filter;
    private TokenService tokens;
    private ISysUserService users;
    private SysPermissionService permissions;
    private MockHttpServletRequest request;
    private LoginUser cached;

    @BeforeEach void setup() {
        SecurityContextHolder.clearContext();filter=new JwtAuthenticationTokenFilter();
        tokens=mock(TokenService.class);users=mock(ISysUserService.class);permissions=mock(SysPermissionService.class);
        ReflectionTestUtils.setField(filter,"tokenService",tokens);
        ReflectionTestUtils.setField(filter,"userService",users);
        ReflectionTestUtils.setField(filter,"permissionService",permissions);
        request=new MockHttpServletRequest();
        cached=new LoginUser(10L,100L,user("school_admin",100L),Set.of("*:*:*"));
        when(tokens.getLoginUser(request)).thenReturn(cached);
    }
    @AfterEach void clear(){SecurityContextHolder.clearContext();}

    @Test void oldAdministratorTokenUsesCurrentRoleAndCollege() throws Exception {
        SysUser current=user("talk_teacher",200L);
        when(users.selectUserById(10L)).thenReturn(current);
        when(permissions.getMenuPermission(current)).thenReturn(Set.of("talk:student:list"));
        filter.doFilterInternal(request,new MockHttpServletResponse(),mock(FilterChain.class));
        assertThat(TalkPermissionUtils.isGlobalAdmin()).isFalse();
        assertThat(TalkPermissionUtils.isTeacher()).isTrue();
        assertThat(TalkPermissionUtils.currentCollegeId()).isEqualTo(200L);
        LoginUser authenticated=(LoginUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertThat(authenticated.getPermissions()).containsExactly("talk:student:list");
        verify(tokens).verifyToken(authenticated);
    }

    @Test void restoredStaleTokenCannotRegainAdministratorRole() throws Exception {
        SysUser current=user("talk_teacher",100L);
        when(users.selectUserById(10L)).thenReturn(current);
        when(permissions.getMenuPermission(current)).thenReturn(Set.of("talk:student:list"));
        filter.doFilterInternal(request,new MockHttpServletResponse(),mock(FilterChain.class));
        assertThat(TalkPermissionUtils.isGlobalAdmin()).isFalse();
        SecurityContextHolder.clearContext();
        // Simulate a parallel old request re-writing a stale cached LoginUser.
        cached.setUser(user("school_admin",100L));cached.setPermissions(Set.of("*:*:*"));
        filter.doFilterInternal(request,new MockHttpServletResponse(),mock(FilterChain.class));
        assertThat(TalkPermissionUtils.isGlobalAdmin()).isFalse();
        assertThat(cached.getPermissions()).doesNotContain("*:*:*");
    }

    @Test void disabledAccountDoesNotAuthenticateEvenWithValidCachedToken() throws Exception {
        SysUser current=user("school_admin",100L);current.setStatus("1");
        when(users.selectUserById(10L)).thenReturn(current);
        filter.doFilterInternal(request,new MockHttpServletResponse(),mock(FilterChain.class));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokens,never()).verifyToken(any());
    }

    @Test void deletedAccountDoesNotAuthenticateEvenWithValidCachedToken() throws Exception {
        SysUser current=user("school_admin",100L);current.setDelFlag("2");
        when(users.selectUserById(10L)).thenReturn(current);
        filter.doFilterInternal(request,new MockHttpServletResponse(),mock(FilterChain.class));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test void missingAccountDoesNotAuthenticate() throws Exception {
        when(users.selectUserById(10L)).thenReturn(null);
        filter.doFilterInternal(request,new MockHttpServletResponse(),mock(FilterChain.class));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(tokens,never()).verifyToken(any());
    }

    @Test void requestWithoutTokenDoesNotQueryUsers() throws Exception {
        when(tokens.getLoginUser(request)).thenReturn(null);
        FilterChain chain=mock(FilterChain.class);
        filter.doFilterInternal(request,new MockHttpServletResponse(),chain);
        verifyNoInteractions(users,permissions);
        verify(chain).doFilter(eq(request),any());
    }

    @Test void disabledSchoolRoleDoesNotGrantGlobalAccess() throws Exception {
        SysUser current=user("school_admin",100L);current.getRoles().get(0).setStatus("1");
        when(users.selectUserById(10L)).thenReturn(current);
        when(permissions.getMenuPermission(current)).thenReturn(Set.of());
        filter.doFilterInternal(request,new MockHttpServletResponse(),mock(FilterChain.class));
        assertThat(TalkPermissionUtils.isGlobalAdmin()).isFalse();
    }

    private SysUser user(String key,long deptId) {
        SysUser u=new SysUser();u.setUserId(10L);u.setUserName("teacher-test");u.setDeptId(deptId);
        u.setStatus("0");u.setDelFlag("0");
        SysDept dept=new SysDept();dept.setDeptId(deptId);dept.setDeptType("college");u.setDept(dept);
        SysRole role=new SysRole();role.setRoleId(8L);role.setRoleKey(key);role.setStatus("0");
        u.setRoles(List.of(role));return u;
    }
}
