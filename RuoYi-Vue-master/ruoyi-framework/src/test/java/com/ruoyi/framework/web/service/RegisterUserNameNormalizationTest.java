package com.ruoyi.framework.web.service;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegisterUserNameNormalizationTest {
    @Test void registrationChecksCanonicalNameBeforeWriting() {
        SysRegisterService service = new SysRegisterService();
        ISysUserService users = mock(ISysUserService.class);
        ReflectionTestUtils.setField(service, "userService", users);
        ReflectionTestUtils.setField(service, "configService", mock(ISysConfigService.class));
        when(users.checkUserNameUnique(any())).thenReturn(false);
        RegisterBody body = new RegisterBody();
        body.setUsername("\u00a0existing\u3000");
        body.setPassword("unit-test-only");
        assertThat(service.register(body)).contains("已存在");
        verify(users).checkUserNameUnique(argThat(u -> "existing".equals(u.getUserName())));
        verify(users, never()).registerUser(any());
    }

    @Test void whitespaceCannotMakeTooShortNamePassValidation() {
        SysRegisterService service = new SysRegisterService();
        ISysUserService users = mock(ISysUserService.class);
        ReflectionTestUtils.setField(service, "userService", users);
        ReflectionTestUtils.setField(service, "configService", mock(ISysConfigService.class));
        RegisterBody body = new RegisterBody();
        body.setUsername(" x ");
        body.setPassword("unit-test-only");
        assertThat(service.register(body)).contains("2到20");
        verify(users, never()).checkUserNameUnique(any());
        verify(users, never()).registerUser(any());
    }
}
