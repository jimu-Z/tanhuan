package com.ruoyi.framework.aspectj;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Set;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class SchoolAdminDataScopeTest {
    @DataScope(deptAlias="d", permission="system:dept:list")
    void departmentQuery() {}

    String scope(Long userId, String key, String status, String dataScope, Set<String> permissions) throws Throwable {
        SysRole role = new SysRole(); role.setRoleKey(key); role.setStatus(status);
        role.setDataScope(dataScope); role.setPermissions(permissions);
        SysUser user = new SysUser(); user.setUserId(userId); user.setDeptId(100L); user.setRoles(List.of(role));
        BaseEntity query = new BaseEntity(); query.getParams().put("dataScope", " OR 1=1");
        JoinPoint point = mock(JoinPoint.class); when(point.getArgs()).thenReturn(new Object[]{query});
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(() -> SecurityUtils.isAdmin(userId)).thenReturn(userId == 1L);
            security.when(SecurityUtils::getLoginUser).thenReturn(new LoginUser(userId, 100L, user, Set.of("*:*:*")));
            new DataScopeAspect().doBefore(point, getClass().getDeclaredMethod("departmentQuery").getAnnotation(DataScope.class));
        } finally { RequestContextHolder.resetRequestAttributes(); }
        return (String) query.getParams().get("dataScope");
    }

    @Test void schoolAdministratorHasGlobalScopeWithoutPerRoleMenuPermissions() throws Throwable {
        assertThat(scope(42L, "school_admin", "0", "1", null)).isEmpty();
    }
    @Test void disabledSchoolAdministratorDoesNotGainGlobalScope() throws Throwable {
        assertThat(scope(42L, "school_admin", "1", "1", null)).contains("d.dept_id = 0");
    }
    @ParameterizedTest @ValueSource(strings={"college_admin", "talk_teacher"})
    void collegeRolesRetainTheirDepartmentBoundary(String key) throws Throwable {
        assertThat(scope(42L, key, "0", "4", Set.of("system:dept:list")))
            .contains("dept_id = 100", "find_in_set( 100 , ancestors )").doesNotContain("1=1");
    }
    @Test void unrelatedAllDataRoleWithoutMatchingPermissionStillHasNoData() throws Throwable {
        assertThat(scope(42L, "other", "0", "1", null)).contains("d.dept_id = 0");
    }
    @Test void builtinAdministratorStillHasGlobalScopeAndClearsInjectedScope() throws Throwable {
        assertThat(scope(1L, "admin", "0", "1", null)).isEmpty();
    }
}
