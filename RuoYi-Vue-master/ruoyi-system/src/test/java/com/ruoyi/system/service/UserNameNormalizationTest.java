package com.ruoyi.system.service;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserPostMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.system.service.impl.SysUserServiceImpl;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserNameNormalizationTest {
    SysUserServiceImpl service;
    SysUserMapper mapper;

    @BeforeEach void setup() {
        service = spy(new SysUserServiceImpl());
        mapper = mock(SysUserMapper.class);
        ReflectionTestUtils.setField(service, "userMapper", mapper);
        ReflectionTestUtils.setField(service, "userRoleMapper", mock(SysUserRoleMapper.class));
        ReflectionTestUtils.setField(service, "userPostMapper", mock(SysUserPostMapper.class));
        ReflectionTestUtils.setField(service, "collegeValidator", mock(BusinessRoleCollegeValidator.class));
        ReflectionTestUtils.setField(service, "deptService", mock(ISysDeptService.class));
        Validator validator = mock(Validator.class);
        when(validator.validate(any(SysUser.class))).thenReturn(Set.of());
        ReflectionTestUtils.setField(service, "validator", validator);
        ISysConfigService config = mock(ISysConfigService.class);
        when(config.selectConfigByKey("sys.user.initPassword")).thenReturn("unit-test-only");
        ReflectionTestUtils.setField(service, "configService", config);
        when(mapper.insertUser(any())).thenReturn(1);
        when(mapper.updateUser(any())).thenReturn(1);
        doNothing().when(service).checkUserDataScope(anyLong());
    }

    SysUser user(String name) {
        SysUser user = new SysUser();
        user.setUserName(name);
        user.setNickName("QA");
        user.setRoleIds(new Long[0]);
        user.setPostIds(new Long[0]);
        return user;
    }

    @Test void insertWritesTheSameNameTheLoginActionUses() {
        service.insertUser(user(" \tqa_school_new \t"));
        verify(mapper).insertUser(argThat(u -> "qa_school_new".equals(u.getUserName())));
    }

    @Test void registrationAlsoWritesCanonicalName() {
        service.registerUser(user(" qa_registered "));
        verify(mapper).insertUser(argThat(u -> "qa_registered".equals(u.getUserName())));
    }

    @Test void importNewRowUsesCanonicalLookupAndWrite() {
        service.importUser(List.of(user(" qa_imported ")), false, "qa_admin");
        verify(mapper).selectUserByUserName("qa_imported");
        verify(mapper).insertUser(argThat(u -> "qa_imported".equals(u.getUserName())));
    }

    @Test void paddedImportCannotCreateAnotherExistingLoginName() {
        SysUser existing = user("existing");
        existing.setUserId(42L);
        when(mapper.selectUserByUserName("existing")).thenReturn(existing);
        assertThatThrownBy(() -> service.importUser(List.of(user(" existing ")), false, "qa_admin"))
            .isInstanceOf(ServiceException.class).hasMessageContaining("已存在");
        verify(mapper, never()).insertUser(any());
    }

    @Test void existingImportFindsCanonicalUserAndKeepsItsDepartment() {
        SysUser existing = user("existing");
        existing.setUserId(42L);
        existing.setDeptId(910001L);
        when(mapper.selectUserByUserName("existing")).thenReturn(existing);
        service.importUser(List.of(user(" existing ")), true, "qa_admin");
        verify(mapper).updateUser(argThat(u -> u.getUserId().equals(42L)
            && "existing".equals(u.getUserName()) && u.getDeptId().equals(910001L)));
        verify(mapper, never()).insertUser(any());
    }

    @Test void unicodeWhitespaceMatchesBrowserLoginTrim() {
        for (String padding : new String[] {"\u00a0", "\u3000", "\ufeff", "\u2028"}) {
            service.insertUser(user(padding + "qa_unicode" + padding));
        }
        verify(mapper, times(4)).insertUser(argThat(u -> "qa_unicode".equals(u.getUserName())));
    }

    @Test void ordinaryProfileUpdateDoesNotRenameLegacyAccounts() {
        SysUser existing = user(" legacy ");
        existing.setUserId(42L);
        service.updateUser(existing);
        verify(mapper).updateUser(argThat(u -> " legacy ".equals(u.getUserName())));
    }
}
