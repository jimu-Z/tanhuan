package com.ruoyi.system.service;

import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysMenuMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.service.impl.SysMenuServiceImpl;
import java.util.List;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MenuManagementScopeTest {
    SysMenuServiceImpl service;
    SysMenuMapper menus;
    SysRoleMapper roles;
    MockedStatic<SecurityUtils> security;
    SysMenu filter;
    @BeforeEach void setup() {
        service = new SysMenuServiceImpl(); menus = mock(SysMenuMapper.class); roles = mock(SysRoleMapper.class);
        ReflectionTestUtils.setField(service, "menuMapper", menus);
        ReflectionTestUtils.setField(service, "roleMapper", roles);
        filter = new SysMenu(); security = mockStatic(SecurityUtils.class);
    }
    @AfterEach void close() { security.close(); }
    SysRole role(String key, String status) { SysRole role = new SysRole(); role.setRoleKey(key); role.setStatus(status); return role; }
    @Test void builtinAdminStillGetsAllManagementMenusWithoutRoleLookup() {
        security.when(() -> SecurityUtils.isAdmin(1L)).thenReturn(true);
        service.selectMenuList(filter, 1L);
        verify(menus).selectMenuList(filter); verifyNoInteractions(roles);
    }
    @Test void activeSchoolAdminCanFindUnassignedNewManagementMenu() {
        when(roles.selectRolePermissionByUserId(20L)).thenReturn(List.of(role("school_admin", "0")));
        SysMenu created = new SysMenu(); created.setMenuName("new hidden disabled QA menu");
        when(menus.selectMenuList(filter)).thenReturn(List.of(created));
        assertThat(service.selectMenuList(filter, 20L)).containsExactly(created);
        verify(menus, never()).selectMenuListByUserId(any());
    }
    @Test void disabledSchoolAdminStaysWithinAssignedMenus() {
        when(roles.selectRolePermissionByUserId(20L)).thenReturn(List.of(role("school_admin", "1")));
        service.selectMenuList(filter, 20L);
        verify(menus).selectMenuListByUserId(filter); verify(menus, never()).selectMenuList(any());
        assertThat(filter.getParams()).containsEntry("userId", 20L);
    }
    @Test void collegeAndTeacherRolesDoNotReceiveGlobalManagementMenus() {
        when(roles.selectRolePermissionByUserId(20L)).thenReturn(List.of(role("college_admin", "0"),role("talk_teacher", "0")));
        service.selectMenuList(filter, 20L);
        verify(menus).selectMenuListByUserId(filter); verify(menus, never()).selectMenuList(any());
    }
    @Test void missingOrSimilarRoleKeyIsNotSchoolAdmin() {
        service.selectMenuList(filter, 20L);
        when(roles.selectRolePermissionByUserId(20L)).thenReturn(List.of(role("school_admin_backup", "0")));
        service.selectMenuList(filter, 20L);
        verify(menus, times(2)).selectMenuListByUserId(filter); verify(menus, never()).selectMenuList(any());
    }
    @Test void NavigationStillUsesAssignedMenusForSchoolAdmin() {
        when(menus.selectMenuTreeByUserId(20L)).thenReturn(List.of());
        service.selectMenuTreeByUserId(20L);
        verify(menus).selectMenuTreeByUserId(20L); verify(menus, never()).selectMenuTreeAll();
    }
}
