package com.ruoyi.talk.security;

import java.util.List;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 谈话业务统一角色与学院数据范围判断。
 */
public final class TalkPermissionUtils
{
    public static final String ROLE_SCHOOL_ADMIN = "school_admin";
    public static final String ROLE_COLLEGE_ADMIN = "college_admin";
    public static final String ROLE_TEACHER = "talk_teacher";

    private static final Long DENY_COLLEGE_ID = -1L;

    private TalkPermissionUtils()
    {
    }

    public static boolean hasExactRole(String roleKey)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null)
        {
            return false;
        }
        List<SysRole> roles = loginUser.getUser().getRoles();
        return roles != null && roles.stream().anyMatch(role -> roleKey.equals(role.getRoleKey())
                && !"1".equals(role.getStatus()));
    }

    public static boolean isGlobalAdmin()
    {
        return SecurityUtils.isAdmin() || hasExactRole(ROLE_SCHOOL_ADMIN);
    }

    public static boolean isCollegeAdmin()
    {
        return hasExactRole(ROLE_COLLEGE_ADMIN);
    }

    public static boolean isTeacher()
    {
        return hasExactRole(ROLE_TEACHER);
    }

    public static boolean isCollegeScoped()
    {
        return isCollegeAdmin() || isTeacher();
    }

    public static Long currentCollegeId()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser == null ? null : loginUser.getUser();
        Long deptId = SecurityUtils.getDeptId();
        if (deptId == null || user == null || user.getDept() == null
                || !"college".equals(user.getDept().getDeptType()))
        {
            return DENY_COLLEGE_ID;
        }
        return deptId;
    }

    public static void applyCollegeScope(BaseEntity entity)
    {
        if (isGlobalAdmin())
        {
            return;
        }
        entity.getParams().put("collegeDeptId", currentCollegeId());
    }

    public static void requireCollegeAccess(Long collegeDeptId)
    {
        if (isGlobalAdmin())
        {
            return;
        }
        if (!isCollegeScoped() || collegeDeptId == null || !collegeDeptId.equals(currentCollegeId()))
        {
            throw new ServiceException("无权访问该学院数据");
        }
    }

    public static void requireCollegeManager()
    {
        if (!isGlobalAdmin() && !isCollegeAdmin())
        {
            throw new ServiceException("仅管理员可执行该操作");
        }
    }

    public static void requireOwnedWrite(Long collegeDeptId, String createBy)
    {
        requireCollegeAccess(collegeDeptId);
        if (isGlobalAdmin() || isCollegeAdmin())
        {
            return;
        }
        if (!isTeacher() || StringUtils.isEmpty(createBy) || !createBy.equals(SecurityUtils.getUsername()))
        {
            throw new ServiceException("只能修改或删除自己创建的谈话");
        }
    }

    public static boolean canManageSystemDefinitions()
    {
        return isGlobalAdmin();
    }
}
