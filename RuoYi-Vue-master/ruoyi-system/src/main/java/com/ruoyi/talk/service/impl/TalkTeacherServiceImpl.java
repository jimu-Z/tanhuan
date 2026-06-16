package com.ruoyi.talk.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
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
    @DataScope(deptAlias = "d")
    public List<TalkTeacher> selectTalkTeacherList(TalkTeacher teacher) {
        return teacherMapper.selectTalkTeacherList(teacher);
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
    @Transactional
    public int deleteTalkTeacherByIds(Long[] teacherIds) {
        int count = 0;
        for (Long id : teacherIds) {
            TalkTeacher t = teacherMapper.selectTalkTeacherById(id);
            if (t != null) {
                Long userId = t.getUserId();
                if (userId != null) {
                    userMapper.deleteUserPhysically(userId);
                } else {
                    // fallback: 通过 teacherCode 查找并物理删除 sys_user
                    SysUser user = userMapper.selectUserByUserName(t.getTeacherCode());
                    if (user != null) {
                        userMapper.deleteUserPhysically(user.getUserId());
                    }
                }
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
    public List<TalkTeacher> selectManagersByStudentDeptId(Long studentDeptId) {
        if (studentDeptId == null) {
            return new java.util.ArrayList<>();
        }
        Long collegeId = findCollegeDeptId(studentDeptId);
        if (collegeId == null) {
            return new java.util.ArrayList<>();
        }
        return teacherMapper.selectCounselorsByDeptId(collegeId);
    }

    @Override
    public String importTeacher(List<TalkTeacher> teacherList, boolean updateSupport) {
        if (teacherList == null || teacherList.isEmpty()) {
            return "导入数据为空";
        }
        int successCount = 0;
        int failCount = 0;
        int updateCount = 0;
        StringBuilder failMsg = new StringBuilder();

        for (TalkTeacher teacher : teacherList) {
            if (teacher == null) {
                failCount++;
                failMsg.append("空行; ");
                continue;
            }
            try {
                // 如果学院不存在则自动创建
                if (teacher.getDeptId() == null && teacher.getDeptName() != null) {
                    Long deptId = getOrCreateCollegeDept(teacher.getDeptName());
                    teacher.setDeptId(deptId);
                }

                // 检查工号是否已存在
                String teacherCode = teacher.getTeacherCode();
                if (StringUtils.isNotEmpty(teacherCode)) {
                    TalkTeacher existing = teacherMapper.selectByTeacherCode(teacherCode);
                    if (existing != null) {
                        if (updateSupport) {
                            // 更新模式：更新已有教师信息
                            teacher.setTeacherId(existing.getTeacherId());
                            teacher.setCreateBy(existing.getCreateBy());
                            teacher.setCreateTime(existing.getCreateTime());
                            teacher.setUpdateBy(SecurityUtils.getUsername());
                            teacher.setUpdateTime(new Date());
                            teacherMapper.updateTalkTeacher(teacher);
                            updateCount++;
                            successCount++;
                            continue;
                        } else {
                            failCount++;
                            failMsg.append(teacher.getTeacherName() != null ? teacher.getTeacherName() : teacherCode)
                                    .append("(工号已存在); ");
                            continue;
                        }
                    }
                    // 兼任判定：检查是否有加工号前缀的版本
                    String resolvedCode = resolveTeacherCode(teacher);
                    if (!resolvedCode.equals(teacherCode)) {
                        TalkTeacher existingPrefixed = teacherMapper.selectByTeacherCode(resolvedCode);
                        if (existingPrefixed != null) {
                            if (updateSupport) {
                                teacher.setTeacherId(existingPrefixed.getTeacherId());
                                teacher.setCreateBy(existingPrefixed.getCreateBy());
                                teacher.setCreateTime(existingPrefixed.getCreateTime());
                                teacher.setUpdateBy(SecurityUtils.getUsername());
                                teacher.setUpdateTime(new Date());
                                teacherMapper.updateTalkTeacher(teacher);
                                updateCount++;
                                successCount++;
                                continue;
                            } else {
                                failCount++;
                                failMsg.append(
                                        teacher.getTeacherName() != null ? teacher.getTeacherName() : resolvedCode)
                                        .append("(工号已存在); ");
                                continue;
                            }
                        }
                    }
                }

                teacher.setCreateBy(SecurityUtils.getUsername());
                teacher.setStatus("0");
                insertTalkTeacher(teacher);
                successCount++;
            } catch (Exception e) {
                failCount++;
                String teacherName = teacher.getTeacherName() != null ? teacher.getTeacherName()
                        : (teacher.getTeacherCode() != null ? teacher.getTeacherCode() : "未知");
                String errMsg = translateErrorMessage(e);
                failMsg.append(teacherName).append("(").append(errMsg).append("); ");
                log.warn("导入教师失败: {} - {}", teacherName, errMsg);
            }
        }

        String message = "成功导入 " + successCount + " 条";
        if (updateCount > 0) {
            message += "（其中更新 " + updateCount + " 条）";
        }
        if (failCount > 0) {
            message += "，失败 " + failCount + " 条: " + failMsg;
        }
        return message;
    }

    /**
     * 将异常信息翻译为中文提示
     */
    private String translateErrorMessage(Exception e) {
        // 递归获取根因
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String msg = root.getMessage();
        if (msg == null) {
            msg = e.getClass().getSimpleName();
        }

        // 唯一键冲突：工号重复
        if (msg.contains("Duplicate entry") && msg.contains("uk_teacher_code")) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("Duplicate entry '(.*?)'").matcher(msg);
            if (m.find()) {
                return "工号'" + m.group(1) + "'已存在";
            }
            return "工号重复";
        }
        if (msg.contains("Duplicate entry")) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("Duplicate entry '(.*?)'").matcher(msg);
            if (m.find()) {
                return "数据重复：" + m.group(1);
            }
            return "数据重复";
        }

        // 字段不能为空
        if (msg.contains("cannot be null")) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("Column '(.*?)' cannot be null").matcher(msg);
            if (m.find()) {
                String col = m.group(1);
                switch (col) {
                    case "teacher_code":
                        return "工号不能为空";
                    case "teacher_name":
                        return "姓名不能为空";
                    case "dept_id":
                        return "所属学院不能为空";
                    case "position":
                        return "岗位不能为空";
                    default:
                        return col + "不能为空";
                }
            }
            return "必填字段为空";
        }

        // 数据过长
        if (msg.contains("Data too long")) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("Data too long for column '(.*?)'")
                    .matcher(msg);
            if (m.find()) {
                return m.group(1) + "内容过长";
            }
            return "数据内容过长";
        }

        // 数据截断
        if (msg.contains("Data truncation")) {
            return "数据格式不正确";
        }

        // 外键约束
        if (msg.contains("foreign key") || msg.contains("a foreign key constraint")) {
            return "关联数据不存在";
        }

        // 其他：截短显示
        return msg.length() > 80 ? msg.substring(0, 80) + "..." : msg;
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
     * 使用精确匹配（deptName + parentId + deptType）避免模糊匹配导致重复创建
     */
    private Long getOrCreateCollegeDept(String deptName) {
        // 精确查找：名称 + 父部门 + deptType
        SysDept query = new SysDept();
        query.setDeptName(deptName.trim());
        query.setParentId(100L);
        List<SysDept> depts = deptMapper.selectDeptList(query);
        if (depts != null) {
            for (SysDept d : depts) {
                if (d.getDeptName().equals(deptName.trim()) && d.getParentId().equals(100L)) {
                    // 补齐缺失的 deptType
                    if (d.getDeptType() == null) {
                        d.setDeptType("college");
                        deptMapper.updateDept(d);
                    }
                    return d.getDeptId();
                }
            }
        }
        // 二次确认防止并发重复创建
        SysDept checkAgain = deptMapper.checkDeptNameUnique(deptName.trim(), 100L);
        if (checkAgain != null) {
            if (checkAgain.getDeptType() == null) {
                checkAgain.setDeptType("college");
                deptMapper.updateDept(checkAgain);
            }
            return checkAgain.getDeptId();
        }
        // 创建学院
        SysDept newDept = new SysDept();
        newDept.setDeptName(deptName.trim());
        newDept.setParentId(100L); // 顶级部门
        newDept.setAncestors("0,100");
        newDept.setDeptType("college");
        newDept.setStatus("0");
        newDept.setDelFlag("0");
        newDept.setCreateBy(SecurityUtils.getUsername());
        newDept.setCreateTime(new Date());
        deptMapper.insertDept(newDept);
        // 安全检查：如果主键未回填，用查询获取
        if (newDept.getDeptId() == null) {
            SysDept created = deptMapper.checkDeptNameUnique(deptName.trim(), 100L);
            if (created != null) {
                return created.getDeptId();
            }
            throw new RuntimeException("创建学院部门失败: " + deptName);
        }
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
                log.info("用户已存在，跳过创建: {}，更新 teacher.user_id", userName);
                teacher.setUserId(existingUser.getUserId());
                teacherMapper.updateTalkTeacherUserId(teacher);
                return;
            }

            SysUser newUser = new SysUser();
            newUser.setUserName(userName);
            newUser.setNickName(teacher.getTeacherName());
            newUser.setPassword(SecurityUtils.encryptPassword(DEFAULT_PASSWORD));
            newUser.setDeptId(teacher.getDeptId());
            newUser.setPhonenumber(teacher.getPhone());
            newUser.setStatus("0");
            newUser.setDelFlag("0");
            newUser.setCreateBy(teacher.getCreateBy() != null ? teacher.getCreateBy() : "system");
            newUser.setCreateTime(new Date());
            userMapper.insertUser(newUser);
            teacher.setUserId(newUser.getUserId());

            // 回填 talk_teacher.user_id
            teacherMapper.updateTalkTeacherUserId(teacher);

            // 分配角色
            assignRole(newUser.getUserId(), teacher.getPosition());

            log.info("为教师 {} 创建用户成功: {}", teacher.getTeacherName(), userName);
        } catch (Exception e) {
            log.error("为教师创建用户失败: {}", teacher.getTeacherCode(), e);
        }
    }

    /**
     * 根据岗位分配角色
     */
    private void assignRole(Long userId, String position) {
        String roleKey;
        if ("书记".equals(position) || "副书记".equals(position)) {
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
        if (teacher.getDeptId() == null || teacher.getTeacherName() == null) {
            return code;
        }

        String position = teacher.getPosition();
        // 检查同学院下是否有同名但不同岗位的教师
        int count = teacherMapper.countByDeptAndNameExcludePosition(
                teacher.getDeptId(), teacher.getTeacherName(), position);
        if (count == 0) {
            return code; // 无兼任，直接用原工号
        }

        // 有兼任，根据岗位加前缀
        if ("辅导员".equals(position) || "班主任".equals(position)) {
            return "f_" + code;
        } else if ("书记".equals(position) || "副书记".equals(position)) {
            return "s_" + code;
        }
        return code;
    }

    @Override
    public List<String> selectAllClassNames() {
        SysDept query = new SysDept();
        query.setDeptType("class");
        return deptMapper.selectDeptList(query).stream()
                .map(SysDept::getDeptName)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> selectClassNamesByCollegeDept(Long deptId) {
        return teacherMapper.selectClassNamesByCollegeDept(deptId);
    }
}
