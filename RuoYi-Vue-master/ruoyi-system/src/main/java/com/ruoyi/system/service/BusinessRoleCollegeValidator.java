package com.ruoyi.system.service;

import java.util.List;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;

/** Shared write guard for business accounts that must belong directly to a college. */
@Component
public class BusinessRoleCollegeValidator
{
    private final SysRoleMapper roleMapper;
    private final SysDeptMapper deptMapper;
    private final SysUserMapper userMapper;

    public BusinessRoleCollegeValidator(SysRoleMapper roleMapper, SysDeptMapper deptMapper, SysUserMapper userMapper)
    {
        this.roleMapper = roleMapper;
        this.deptMapper = deptMapper;
        this.userMapper = userMapper;
    }

    public void checkRoleDepartment(Long deptId, Long[] roleIds)
    {
        if (requiresCollege(roleIds))
        {
            checkCollege(deptId);
        }
    }

    public void checkUserForRoles(Long userId, Long[] roleIds)
    {
        if (requiresCollege(roleIds))
        {
            checkUserCollege(userId);
        }
    }

    public void checkUsersForRole(Long roleId, Long[] userIds)
    {
        if (requiresCollege(new Long[] { roleId }))
        {
            for (Long userId : userIds)
            {
                checkUserCollege(userId);
            }
        }
    }

    // Generic user imports retain existing roles and the existing department.
    public void checkExistingUserDepartment(SysUser user)
    {
        List<SysRole> roles = roleMapper.selectRolePermissionByUserId(user.getUserId());
        if (roles != null && roles.stream().anyMatch(this::requiresCollege))
        {
            checkCollege(user.getDeptId());
        }
    }

    private boolean requiresCollege(Long[] roleIds)
    {
        if (roleIds != null)
        {
            for (Long roleId : roleIds)
            {
                if (requiresCollege(roleMapper.selectRoleById(roleId)))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean requiresCollege(SysRole role)
    {
        return role != null && ("talk_teacher".equals(role.getRoleKey()) || "college_admin".equals(role.getRoleKey()));
    }

    private void checkUserCollege(Long userId)
    {
        SysUser user = userMapper.selectUserById(userId);
        checkCollege(user == null ? null : user.getDeptId());
    }

    private void checkCollege(Long deptId)
    {
        SysDept dept = deptId == null ? null : deptMapper.selectDeptById(deptId);
        if (dept == null || !"college".equals(dept.getDeptType()) || "2".equals(dept.getDelFlag()))
        {
            throw new ServiceException("老师和院管理员必须选择所属学院，不能选择学校、年级、班级或其他部门");
        }
    }
}
