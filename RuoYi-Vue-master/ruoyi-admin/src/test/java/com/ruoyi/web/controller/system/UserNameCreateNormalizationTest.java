package com.ruoyi.web.controller.system;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserNameCreateNormalizationTest {
    SysUserController controller;
    ISysUserService users;
    MockedStatic<SecurityUtils> security;

    @BeforeEach void setup() {
        controller = new SysUserController();
        users = mock(ISysUserService.class);
        ReflectionTestUtils.setField(controller, "userService", users);
        ReflectionTestUtils.setField(controller, "deptService", mock(ISysDeptService.class));
        ReflectionTestUtils.setField(controller, "roleService", mock(ISysRoleService.class));
        security = mockStatic(SecurityUtils.class);
        LoginUser loginUser = mock(LoginUser.class);
        when(loginUser.getUsername()).thenReturn("qa_school");
        security.when(SecurityUtils::getLoginUser).thenReturn(loginUser);
        security.when(() -> SecurityUtils.encryptPassword(anyString())).thenReturn("unit-encoded");
        when(users.checkUserNameUnique(any())).thenReturn(true);
        when(users.insertUser(any())).thenReturn(1);
    }

    @AfterEach void close() { security.close(); }

    SysUser user(String name) {
        SysUser user = new SysUser();
        user.setUserName(name);
        user.setPassword("unit-test-only");
        user.setRoleIds(new Long[0]);
        return user;
    }

    @Test void duplicateCheckSeesCanonicalNameBeforeAnyInsert() {
        when(users.checkUserNameUnique(any())).thenAnswer(call ->
            !"existing".equals(((SysUser) call.getArgument(0)).getUserName()));
        assertThat(controller.add(user("\u00a0existing\u3000")).get("code")).isEqualTo(500);
        verify(users, never()).insertUser(any());
    }

    @Test void normalizedEmptyNamesAreRejectedBeforePersistence() {
        for (String name : new String[] {"\u00a0", "\ufeff", "\u3000", " \t "}) {
            assertThat(controller.add(user(name)).get("code")).isEqualTo(500);
        }
        verify(users, never()).checkUserNameUnique(any());
        verify(users, never()).insertUser(any());
    }

    @Test void successfulCreatePassesCanonicalNameToTheWriteService() {
        assertThat(controller.add(user(" qa_new ")).get("code")).isEqualTo(200);
        verify(users).insertUser(argThat(u -> "qa_new".equals(u.getUserName())));
    }
}
