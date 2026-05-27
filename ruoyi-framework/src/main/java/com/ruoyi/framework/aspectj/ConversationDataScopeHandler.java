package com.ruoyi.framework.aspectj;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.SysUserClassMapper;

@Component
public class ConversationDataScopeHandler {
    private static final String COUNSELOR_ROLE_KEY = "counselor";

    @Autowired
    private SysUserClassMapper sysUserClassMapper;

    public String getConversationDataScope(String conversationAlias) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return "";
        }
        SysUser user = loginUser.getUser();
        if (user.isAdmin()) {
            return "";
        }
        if (!hasCounselorRole(user)) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        sql.append(" AND (");
        sql.append(StringUtils.format(" {}.dept_id = {} AND {}.create_by = '{}' ",
                conversationAlias, user.getDeptId(), conversationAlias, user.getUserName()));
        sql.append(") ");
        return sql.toString();
    }

    public String getStudentDataScope(String studentAlias) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return "";
        }
        SysUser user = loginUser.getUser();
        if (user.isAdmin()) {
            return "";
        }
        if (!hasCounselorRole(user)) {
            return "";
        }
        List<Long> classIds = sysUserClassMapper.selectClassIdsByUserId(user.getUserId());
        if (classIds == null || classIds.isEmpty()) {
            return StringUtils.format(" AND {}.class_id = -1 ", studentAlias);
        }
        StringBuilder sql = new StringBuilder();
        sql.append(" AND (");
        sql.append(StringUtils.format(" {}.dept_id = {} ", studentAlias, user.getDeptId()));
        if (!classIds.isEmpty()) {
            sql.append(StringUtils.format(" OR {}.class_id IN ({}) ",
                    studentAlias, String.join(",", classIds.stream().map(String::valueOf).toArray(String[]::new))));
        }
        sql.append(") ");
        return sql.toString();
    }

    private boolean hasCounselorRole(SysUser user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return false;
        }
        for (SysRole role : user.getRoles()) {
            if (COUNSELOR_ROLE_KEY.equals(role.getRoleKey())) {
                return true;
            }
        }
        return false;
    }
}