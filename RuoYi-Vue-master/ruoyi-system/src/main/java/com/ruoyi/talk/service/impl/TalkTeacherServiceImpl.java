package com.ruoyi.talk.service.impl;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.talk.domain.TalkTeacher;
import com.ruoyi.talk.mapper.TalkTeacherMapper;
import com.ruoyi.talk.service.ITalkTeacherService;

/**
 * 教师信息Service业务层处理
 *
 * @author admin
 * @date 2026-06-06
 */
@Service
public class TalkTeacherServiceImpl implements ITalkTeacherService {
    private static final Logger log = LoggerFactory.getLogger(TalkTeacherServiceImpl.class);

    private static final String DEFAULT_PASSWORD = "123456";
    private static final String ROLE_COUNSELOR = "talk_counselor";
    private static final String ROLE_SECRETARY = "talk_secretary";

    @Autowired
    private TalkTeacherMapper teacherMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    @Override
    public TalkTeacher selectTalkTeacherById(Long teacherId) {
        return teacherMapper.selectTalkTeacherById(teacherId);
    }

    @Override
    public List<TalkTeacher> selectTalkTeacherList(TalkTeacher teacher) {
        applyDataScopeFilter(teacher);
        return teacherMapper.selectTalkTeacherList(teacher);
    }

    /**
     * 数据权限过滤：非管理员只能看到自己学院的教师
     */
    private void applyDataScopeFilter(TalkTeacher teacher) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        Long deptId = SecurityUtils.getDeptId();
        if (deptId != null) {
            teacher.setDeptId(deptId);
        }
    }

    @Override
    @Transactional
    public int insertTalkTeacher(TalkTeacher teacher) {
        // 兼任自动判定：同学院同名但不同岗位 → 加工号前缀
        teacher.setTeacherCode(resolveTeacherCode(teacher));

        teacher.setCreateTime(new Date());
        teacher.setStatus("0");
        int rows = teacherMapper.insertTalkTeacher(teacher);

        // 自动创建 sys_user
        createSysUserForTeacher(teacher);

        return rows;
    }

    @Override
    public int updateTalkTeacher(TalkTeacher teacher) {
        teacher.setUpdateBy(SecurityUtils.getUsername());
        teacher.setUpdateTime(new Date());
        return teacherMapper.updateTalkTeacher(teacher);
    }

    @Override
    public int deleteTalkTeacherByIds(Long[] teacherIds) {
        int count = 0;
        for (Long id : teacherIds) {
            TalkTeacher t = teacherMapper.selectTalkTeacherById(id);
            if (t != null && t.getUserId() != null) {
                userMapper.deleteUserById(t.getUserId());
            }
            count += teacherMapper.deleteTalkTeacherById(id);
        }
        return count;
    }

    @Override
    public boolean checkTeacherCodeUnique(TalkTeacher teacher) {
        int count = teacherMapper.checkTeacherCodeUnique(teacher.getTeacherCode());
        return count == 0;
    }

    @Override
    public TalkTeacher findCounselorByStudentDeptAndName(Long studentDeptId, String teacherName) {
        if (studentDeptId == null || teacherName == null) {
            return null;
        }
        // 从学生班级上溯dept链找到学院ID
        Long collegeId = findCollegeDeptId(studentDeptId);
        if (collegeId == null) {
            return null;
        }
        return teacherMapper.selectByDeptIdAndName(collegeId, teacherName);
    }

    @Override
    public List<TalkTeacher> selectCounselorsByDeptId(Long deptId) {
        return teacherMapper.selectCounselorsByDeptId(deptId);
    }

    @Override
    @Transactional
    public String importTeacher(List<TalkTeacher> teacherList, boolean updateSupport) {
        if (teacherList == null || teacherList.isEmpty()) {
            return "导入数据为空";
        }
        int successCount = 0;
        int failCount = 0;
        StringBuilder failMsg = new StringBuilder();

        for (TalkTeacher teacher : teacherList) {
            try {
                // 如果学院不存在则自动创建
                if (teacher.getDeptId() == null && teacher.getDeptName() != null) {
                    Long deptId = getOrCreateCollegeDept(teacher.getDeptName());
                    teacher.setDeptId(deptId);
                }

                teacher.setCreateBy(SecurityUtils.getUsername());
                teacher.setStatus("0");
                insertTalkTeacher(teacher);
                successCount++;
            } catch (Exception e) {
                failCount++;
                failMsg.append(teacher.getName()).append("(").append(e.getMessage()).append("); ");
                log.warn("导入教师失败: {} - {}", teacher.getName(), e.getMessage());
            }
        }

        String message = "成功导入 " + successCount + " 条";
        if (failCount > 0) {
            message += "，失败 " + failCount + " 条: " + failMsg;
        }
        return message;
    }

    /**
     * 上溯dept链找到学院级别的deptId
     */
    private Long findCollegeDeptId(Long deptId) {
        return findCollegeDeptId(deptId, 0);
    }

    private Long findCollegeDeptId(Long deptId, int depth) {
        if (depth > 10) {
            return null;
        }
        SysDept dept = deptMapper.selectDeptById(deptId);
        if (dept == null) {
            return null;
        }
        if ("college".equals(dept.getDeptType())) {
            return dept.getDeptId();
        }
        if (dept.getParentId() != null && dept.getParentId() != 0) {
            return findCollegeDeptId(dept.getParentId(), depth + 1);
        }
        return null;
    }

    /**
     * 获取或创建学院部门
     */
    private Long getOrCreateCollegeDept(String deptName) {
        // 查找是否已存在
        SysDept query = new SysDept();
        query.setDeptName(deptName);
        List<SysDept> depts = deptMapper.selectDeptList(query);
        if (!depts.isEmpty()) {
            return depts.get(0).getDeptId();
        }
        // 创建学院
        SysDept newDept = new SysDept();
        newDept.setDeptName(deptName);
        newDept.setParentId(100L);  // 顶级部门
        newDept.setAncestors("0,100");
        newDept.setDeptType("college");
        newDept.setStatus("0");
        newDept.setDelFlag("0");
        newDept.setCreateBy(SecurityUtils.getUsername());
        newDept.setCreateTime(new Date());
        deptMapper.insertDept(newDept);
        return newDept.getDeptId();
    }

    /**
     * 为教师自动创建 sys_user 账号并分配角色
     */
    private void createSysUserForTeacher(TalkTeacher teacher) {
        try {
            String userName = teacher.getTeacherCode();
            SysUser existingUser = userMapper.selectUserByUserName(userName);
            if (existingUser != null) {
                log.info("用户已存在，跳过创建: {}", userName);
                return;
            }

            SysUser newUser = new SysUser();
            newUser.setUserName(userName);
            newUser.setNickName(teacher.getName());
            newUser.setPassword(SecurityUtils.encryptPassword(DEFAULT_PASSWORD));
            newUser.setDeptId(teacher.getDeptId());
            newUser.setPhonenumber(teacher.getPhone());
            newUser.setStatus("0");
            newUser.setDelFlag("0");
            newUser.setCreateBy(teacher.getCreateBy() != null ? teacher.getCreateBy() : "system");
            newUser.setCreateTime(new Date());
            userMapper.insertUser(newUser);

            // 分配角色
            assignRole(newUser.getUserId(), teacher.getPosition());

            log.info("为教师 {} 创建用户成功: {}", teacher.getName(), userName);
        } catch (Exception e) {
            log.error("为教师创建用户失败: {}", teacher.getTeacherCode(), e);
        }
    }

    /**
     * 根据岗位分配角色
     */
    private void assignRole(Long userId, String position) {
        String roleKey;
        if ("secretary".equals(position) || "deputy_secretary".equals(position)) {
            roleKey = ROLE_SECRETARY;
        } else {
            roleKey = ROLE_COUNSELOR;
        }

        SysRole query = new SysRole();
        query.setRoleKey(roleKey);
        List<SysRole> roles = roleMapper.selectRoleList(query);
        if (!roles.isEmpty()) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roles.get(0).getRoleId());
            userRoleMapper.batchUserRole(List.of(userRole));
        }
    }

    /**
     * 兼任自动判定：同学院同名但不同岗位 → 加工号前缀
     * 辅导员/班主任岗位 → f_ 前缀；副书记/书记 → s_ 前缀
     */
    private String resolveTeacherCode(TalkTeacher teacher) {
        String code = teacher.getTeacherCode();
        if (code == null) {
            throw new IllegalArgumentException("教师工号不能为空");
        }
        if (teacher.getDeptId() == null || teacher.getName() == null) {
            return code;
        }

        String position = teacher.getPosition();
        // 检查同学院下是否有同名但不同岗位的教师
        int count = teacherMapper.countByDeptAndNameExcludePosition(
                teacher.getDeptId(), teacher.getName(), position);
        if (count == 0) {
            return code; // 无兼任，直接用原工号
        }

        // 有兼任，根据岗位加前缀
        if ("counselor".equals(position) || "head_teacher".equals(position)) {
            return "f_" + code;
        } else if ("secretary".equals(position) || "deputy_secretary".equals(position)) {
            return "s_" + code;
        }
        return code;
    }
}
