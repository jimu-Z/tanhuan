package com.ruoyi.talk.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.talk.domain.TalkTeacher;
import com.ruoyi.talk.mapper.TalkTeacherMapper;
import com.ruoyi.talk.security.TalkPermissionUtils;
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
    private static final String ROLE_TEACHER = "talk_teacher";

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

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private RedisCache redisCache;

    @Override
    public TalkTeacher selectTalkTeacherById(Long teacherId) {
        TalkTeacher teacher = teacherMapper.selectTalkTeacherById(teacherId);
        requireTeacherAccess(teacher);
        return teacher;
    }

    @Override
    public List<TalkTeacher> selectTalkTeacherList(TalkTeacher teacher) {
        if (!TalkPermissionUtils.isGlobalAdmin()) {
            teacher.setDeptId(TalkPermissionUtils.currentCollegeId());
        }
        return teacherMapper.selectTalkTeacherList(teacher);
    }

    @Override
    @Transactional
    public int insertTalkTeacher(TalkTeacher teacher) {
        TalkPermissionUtils.requireCollegeManager();
        validateTeacherFields(teacher, true);
        validateStatus(teacher.getStatus());
        requireCollegeDept(teacher.getDeptId());
        TalkPermissionUtils.requireCollegeAccess(teacher.getDeptId());
        validateTeacherAccountRole(teacher);
        // 规范化岗位值（如"学院党委书记" → "书记"）
        teacher.setPosition(normalizePosition(teacher.getPosition()));

        // 兼任自动判定：同学院同名但不同岗位 → 加工号前缀
        teacher.setTeacherCode(resolveTeacherCode(teacher));
        validateTeacherAccountRole(teacher);

        teacher.setCreateTime(new Date());
        if (teacher.getStatus() == null) {
            teacher.setStatus("0");
        }
        int rows = teacherMapper.insertTalkTeacher(teacher);

        // 自动创建 sys_user
        createSysUserForTeacher(teacher);

        return rows;
    }

    @Override
    @Transactional
    public int updateTalkTeacher(TalkTeacher teacher) {
        TalkPermissionUtils.requireCollegeManager();
        validateTeacherFields(teacher, false);
        validateStatus(teacher.getStatus());
        TalkTeacher existing = teacherMapper.selectTalkTeacherById(teacher.getTeacherId());
        requireTeacherAccess(existing);
        Long targetDeptId = teacher.getDeptId() != null ? teacher.getDeptId() : existing.getDeptId();
        requireCollegeDept(targetDeptId);
        TalkPermissionUtils.requireCollegeAccess(targetDeptId);
        teacher.setTeacherCode(existing.getTeacherCode());
        SysUser account = existing.getUserId() != null ? userMapper.selectUserById(existing.getUserId())
                : userMapper.selectUserByUserName(existing.getTeacherCode());
        validateTeacherAccountRole(teacher, account);
        if (account == null && StringUtils.isNotEmpty(teacher.getRoleKey())) {
            throw new ServiceException("教师关联账号不存在，无法分配角色");
        }
        teacher.setPosition(normalizePosition(teacher.getPosition()));
        teacher.setUpdateBy(SecurityUtils.getUsername());
        teacher.setUpdateTime(new Date());
        int rows = teacherMapper.updateTalkTeacher(teacher);
        syncTeacherUser(existing, teacher, targetDeptId);
        return rows;
    }

    @Override
    @Transactional
    public int deleteTalkTeacherByIds(Long[] teacherIds) {
        TalkPermissionUtils.requireCollegeManager();
        java.util.Map<Long, Long> accounts = new java.util.LinkedHashMap<>();
        // Validate the entire batch before deleting any teacher or login account.
        for (Long id : teacherIds) {
            TalkTeacher teacher = teacherMapper.selectTalkTeacherById(id);
            if (teacher == null) {
                continue;
            }
            requireTeacherAccess(teacher);
            Long userId = teacher.getUserId();
            SysUser account = userId == null ? userMapper.selectUserByUserName(teacher.getTeacherCode())
                    : userMapper.selectUserById(userId);
            if (account != null) {
                userId = account.getUserId();
            }
            if (userId != null) {
                if (Long.valueOf(1L).equals(userId) || hasRole(userId, "admin")) {
                    throw new ServiceException("不能通过教师管理删除内置管理员账号");
                }
                if (!TalkPermissionUtils.isGlobalAdmin()) {
                    if (account != null) {
                        TalkPermissionUtils.requireCollegeAccess(account.getDeptId());
                    }
                    if (hasRole(userId, TalkPermissionUtils.ROLE_SCHOOL_ADMIN)) {
                        throw new ServiceException("院管理员不能删除校管理员账号");
                    }
                    if (hasRole(userId, TalkPermissionUtils.ROLE_COLLEGE_ADMIN)) {
                        throw new ServiceException("院管理员账号不能从教师管理中删除，请先按院管理员安全规则降级");
                    }
                }
            }
            accounts.put(id, userId);
        }
        int count = 0;
        for (java.util.Map.Entry<Long, Long> item : accounts.entrySet()) {
            if (item.getValue() != null) {
                userRoleMapper.deleteUserRoleByUserId(item.getValue());
                userMapper.deleteUserPhysically(item.getValue());
            }
            count += teacherMapper.deleteTalkTeacherById(item.getKey());
        }
        return count;
    }

    @Override
    @Transactional
    public int changeCollegeAdminRole(Long teacherId, boolean promote) {
        TalkPermissionUtils.requireCollegeManager();
        TalkTeacher teacher = teacherMapper.selectTalkTeacherById(teacherId);
        requireTeacherAccess(teacher);
        if (teacher.getUserId() == null) {
            throw new ServiceException("该教师尚未关联登录账号");
        }
        SysUser account = userMapper.selectUserById(teacher.getUserId());
        if (account == null || "2".equals(account.getDelFlag())) {
            throw new ServiceException("关联登录账号不存在");
        }
        if (account.isAdmin() || hasRole(account.getUserId(), "admin")) {
            throw new ServiceException("不能通过教师管理调整内置管理员账号");
        }
        if (hasRole(account.getUserId(), TalkPermissionUtils.ROLE_SCHOOL_ADMIN)) {
            throw new ServiceException("校级管理员账号不能通过教师管理调整为院管理员");
        }
        if (!promote && !hasRole(teacher.getUserId(), TalkPermissionUtils.ROLE_COLLEGE_ADMIN)) {
            throw new ServiceException("该账号不是院管理员");
        }

        teacher.setRoleKey(promote ? TalkPermissionUtils.ROLE_COLLEGE_ADMIN : ROLE_TEACHER);
        validateTeacherAccountRole(teacher, account);
        if (assignRole(account.getUserId(), teacher.getRoleKey())) {
            invalidateTeacherSessions(account.getUserId());
        }
        return 1;
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
        TalkPermissionUtils.requireCollegeAccess(collegeId);
        return teacherMapper.selectByDeptIdAndName(collegeId, teacherName);
    }

    @Override
    public List<TalkTeacher> selectCounselorsByDeptId(Long deptId) {
        TalkPermissionUtils.requireCollegeAccess(deptId);
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
        TalkPermissionUtils.requireCollegeAccess(collegeId);
        return teacherMapper.selectCounselorsByDeptId(collegeId);
    }

    @Override
    public String importTeacher(List<TalkTeacher> teacherList, boolean updateSupport) {
        TalkPermissionUtils.requireCollegeManager();
        if (teacherList == null || teacherList.isEmpty()) {
            return "导入数据为空";
        }
        int successCount = 0;
        int failCount = 0;
        int updateCount = 0;
        StringBuilder failMsg = new StringBuilder();
        TransactionTemplate rowTransaction = new TransactionTemplate(transactionManager);
        rowTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        for (TalkTeacher teacher : teacherList) {
            if (teacher == null) {
                failCount++;
                failMsg.append("空行; ");
                continue;
            }
            try {
                Boolean updated = rowTransaction.execute(status -> importTeacherRow(teacher, updateSupport));
                if (Boolean.TRUE.equals(updated)) {
                    updateCount++;
                }
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

    /** Each row commits teacher, account and role changes together. */
    private boolean importTeacherRow(TalkTeacher teacher, boolean updateSupport) {
        validateTeacherFields(teacher, true);
        validateStatus(teacher.getStatus());
        teacher.setPosition(normalizePosition(teacher.getPosition()));
        validateTeacherAccountRole(teacher);
        if (teacher.getDeptId() == null && teacher.getDeptName() != null) {
            teacher.setDeptId(resolveImportCollege(teacher.getDeptName()));
        }
        requireCollegeDept(teacher.getDeptId());
        TalkPermissionUtils.requireCollegeAccess(teacher.getDeptId());
        TalkTeacher existing = teacherMapper.selectByTeacherCode(teacher.getTeacherCode());
        if (existing == null) {
            String resolvedCode = resolveTeacherCode(teacher);
            if (!resolvedCode.equals(teacher.getTeacherCode())) {
                existing = teacherMapper.selectByTeacherCode(resolvedCode);
                if (existing != null) {
                    teacher.setTeacherCode(resolvedCode);
                    validateTeacherAccountRole(teacher);
                }
            }
        }
        if (existing != null) {
            if (!updateSupport) {
                throw new ServiceException("工号已存在");
            }
            requireTeacherAccess(existing);
            teacher.setTeacherId(existing.getTeacherId());
            teacher.setCreateBy(existing.getCreateBy());
            teacher.setCreateTime(existing.getCreateTime());
            teacher.setUpdateBy(SecurityUtils.getUsername());
            teacher.setUpdateTime(new Date());
            teacherMapper.updateTalkTeacher(teacher);
            createSysUserForTeacher(teacher);
            return true;
        }
        teacher.setCreateBy(SecurityUtils.getUsername());
        if (teacher.getStatus() == null) teacher.setStatus("0");
        insertTalkTeacher(teacher);
        return false;
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
        String userName = teacher.getTeacherCode();
        SysUser existingUser = userMapper.selectUserByUserName(userName);
        if (existingUser != null) {
            log.info("用户已存在，更新 teacher.user_id 并刷新角色: {}", userName);
            existingUser.setNickName(teacher.getTeacherName());
            existingUser.setDeptId(teacher.getDeptId());
            existingUser.setPhonenumber(teacher.getPhone());
            if (teacher.getStatus() != null) {
                existingUser.setStatus(teacher.getStatus());
            }
            userMapper.updateUser(existingUser);
            teacher.setUserId(existingUser.getUserId());
            teacherMapper.updateTalkTeacherUserId(teacher);
            if (assignRole(existingUser.getUserId(), teacher.getRoleKey())) {
                invalidateTeacherSessions(existingUser.getUserId());
            }
            return;
        }

        SysUser newUser = new SysUser();
        newUser.setUserName(userName);
        newUser.setNickName(teacher.getTeacherName());
        newUser.setPassword(SecurityUtils.encryptPassword(DEFAULT_PASSWORD));
        newUser.setDeptId(teacher.getDeptId());
        newUser.setPhonenumber(teacher.getPhone());
        newUser.setStatus(teacher.getStatus() == null ? "0" : teacher.getStatus());
        newUser.setDelFlag("0");
        newUser.setCreateBy(teacher.getCreateBy() != null ? teacher.getCreateBy() : "system");
        newUser.setCreateTime(new Date());
        userMapper.insertUser(newUser);
        teacher.setUserId(newUser.getUserId());
        teacherMapper.updateTalkTeacherUserId(teacher);
        assignRole(newUser.getUserId(), teacher.getRoleKey());
        log.info("为教师 {} 创建用户成功: {}", teacher.getTeacherName(), userName);
    }

    /**
     * 按导入角色分配权限。旧模板不含角色时保留管理员角色，新账号默认老师。
     */
    private boolean assignRole(Long userId, String requestedRole) {
        if (StringUtils.isEmpty(requestedRole) && (hasRole(userId, TalkPermissionUtils.ROLE_COLLEGE_ADMIN)
                || hasRole(userId, TalkPermissionUtils.ROLE_SCHOOL_ADMIN)
                || hasRole(userId, "admin"))) {
            return false;
        }
        String targetKey = StringUtils.isEmpty(requestedRole) ? ROLE_TEACHER : requestedRole;
        Long targetId = requireActiveRoleId(targetKey);
        boolean changed = false;
        List<SysRole> currentRoles = roleMapper.selectRolePermissionByUserId(userId);
        if (StringUtils.isNotEmpty(requestedRole) && currentRoles != null) {
            for (SysRole role : currentRoles) {
                String key = role.getRoleKey();
                if (!targetKey.equals(key) && (ROLE_TEACHER.equals(key)
                        || TalkPermissionUtils.ROLE_COLLEGE_ADMIN.equals(key)
                        || TalkPermissionUtils.ROLE_SCHOOL_ADMIN.equals(key))) {
                    SysUserRole source = new SysUserRole();
                    source.setUserId(userId);
                    source.setRoleId(role.getRoleId());
                    userRoleMapper.deleteUserRoleInfo(source);
                    changed = true;
                }
            }
        }
        List<Long> userRoleIds = roleMapper.selectRoleListByUserId(userId);
        if (userRoleIds == null || !userRoleIds.contains(targetId)) {
            SysUserRole target = new SysUserRole();
            target.setUserId(userId);
            target.setRoleId(targetId);
            userRoleMapper.batchUserRole(List.of(target));
            changed = true;
        }
        return changed;
    }

    private void invalidateTeacherSessions(Long userId) {
        Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        if (keys == null) {
            return;
        }
        // Revoke before committing: cache failure must roll back the role change,
        // otherwise a downgraded account could retain administrator permissions.
        // A later commit failure may require re-login, but cannot leave stale privilege.
        for (String key : keys) {
            LoginUser login = redisCache.getCacheObject(key);
            if (login != null && userId.equals(login.getUserId())) {
                redisCache.deleteObject(key);
            }
        }
    }

    private String normalizeRoleKey(String value) {
        if (StringUtils.isEmpty(value) || value.trim().isEmpty()) {
            return null;
        }
        switch (value.trim()) {
            case "老师": case ROLE_TEACHER: return ROLE_TEACHER;
            case "院管理员": case TalkPermissionUtils.ROLE_COLLEGE_ADMIN:
                return TalkPermissionUtils.ROLE_COLLEGE_ADMIN;
            case "校管理员": case TalkPermissionUtils.ROLE_SCHOOL_ADMIN:
                return TalkPermissionUtils.ROLE_SCHOOL_ADMIN;
            default: throw new ServiceException("角色只能为老师、院管理员或校管理员");
        }
    }

    private Long requireActiveRoleId(String key) {
        SysRole query = new SysRole();
        query.setRoleKey(key);
        List<SysRole> candidates = roleMapper.selectRoleList(query);
        if (candidates != null) {
            for (SysRole role : candidates) {
                if (key.equals(role.getRoleKey()) && !"1".equals(role.getStatus())) {
                    return role.getRoleId();
                }
            }
        }
        throw new ServiceException("目标角色尚未初始化或已停用，请检查系统角色");
    }

    /** Validate account scope and role changes before writing teacher/account data. */
    private void validateTeacherAccountRole(TalkTeacher teacher) {
        validateTeacherAccountRole(teacher, userMapper.selectUserByUserName(teacher.getTeacherCode()));
    }

    private void validateTeacherAccountRole(TalkTeacher teacher, SysUser account) {
        String target = normalizeRoleKey(teacher.getRoleKey());
        teacher.setRoleKey(target);
        boolean global = TalkPermissionUtils.isGlobalAdmin();
        if (TalkPermissionUtils.ROLE_SCHOOL_ADMIN.equals(target) && !global) {
            throw new ServiceException("仅校级管理员可以分配校管理员角色");
        }
        if (target != null) {
            requireActiveRoleId(target);
        }
        if (account == null) {
            if (target == null) {
                requireActiveRoleId(ROLE_TEACHER);
            }
            return;
        }
        if (account.isAdmin() || hasRole(account.getUserId(), "admin")) {
            throw new ServiceException("不能通过教师导入修改内置管理员账号");
        }
        if (!global) {
            TalkPermissionUtils.requireCollegeAccess(account.getDeptId());
            if (hasRole(account.getUserId(), TalkPermissionUtils.ROLE_SCHOOL_ADMIN)) {
                throw new ServiceException("院管理员不能修改校管理员账号");
            }
            boolean removesAdmin = target != null && !TalkPermissionUtils.ROLE_COLLEGE_ADMIN.equals(target);
            boolean disablesAdmin = "1".equals(teacher.getStatus()) && !"1".equals(account.getStatus());
            if ((removesAdmin || disablesAdmin)
                    && hasRole(account.getUserId(), TalkPermissionUtils.ROLE_COLLEGE_ADMIN)) {
                if (account.getUserId().equals(SecurityUtils.getUserId())) {
                    throw new ServiceException("不能降级或停用自己的院管理员账号");
                }
                if (teacherMapper.countActiveCollegeAdminsByDeptId(account.getDeptId()) <= 1) {
                    throw new ServiceException("学院必须至少保留一名启用状态的院管理员");
                }
            }
        }
    }

    private void validateStatus(String status) {
        if (status != null && !"0".equals(status) && !"1".equals(status)) {
            throw new ServiceException("教师状态只能为正常或停用");
        }
    }

    private void validateTeacherFields(TalkTeacher teacher, boolean required) {
        if (required) {
            if (teacher.getTeacherCode() == null || teacher.getTeacherCode().trim().isEmpty()) {
                throw new ServiceException("教师工号不能为空");
            }
            teacher.setTeacherCode(teacher.getTeacherCode().trim());
        }
        if (required || teacher.getTeacherName() != null) {
            if (teacher.getTeacherName() == null || teacher.getTeacherName().trim().isEmpty()) {
                throw new ServiceException("教师姓名不能为空");
            }
            teacher.setTeacherName(teacher.getTeacherName().trim());
        }
        if (required || teacher.getPosition() != null) {
            if (teacher.getPosition() == null || teacher.getPosition().trim().isEmpty()) {
                throw new ServiceException("教师岗位不能为空");
            }
        }
    }

    /**
     * 规范化岗位值
     * "学院党委书记" → "书记"，"学院辅导员" → "辅导员" 等
     * 注意：先检查"副书记"再检查"书记"，因为"副书记"包含"书记"
     */
    private String normalizePosition(String position) {
        if (StringUtils.isEmpty(position)) {
            return position;
        }
        String trimmed = position.trim();
        if (trimmed.contains("副书记")) {
            return "副书记";
        }
        if (trimmed.contains("书记")) {
            return "书记";
        }
        if (trimmed.contains("辅导员")) {
            return "辅导员";
        }
        if (trimmed.contains("班主任")) {
            return "班主任";
        }
        return trimmed;
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
        if (!TalkPermissionUtils.isGlobalAdmin()) {
            return selectClassNamesByCollegeDept(TalkPermissionUtils.currentCollegeId());
        }
        SysDept query = new SysDept();
        query.setDeptType("class");
        return deptMapper.selectDeptList(query).stream()
                .map(SysDept::getDeptName)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> selectClassNamesByCollegeDept(Long deptId) {
        TalkPermissionUtils.requireCollegeAccess(deptId);
        if (deptId == null) {
            return List.of();
        }
        SysDept college = deptMapper.selectDeptById(deptId);
        if (college == null || !"college".equals(college.getDeptType()) || "2".equals(college.getDelFlag())) {
            return List.of();
        }
        return teacherMapper.selectClassNamesByCollegeDept(deptId);
    }

    private void requireTeacherAccess(TalkTeacher teacher) {
        if (teacher == null) {
            throw new ServiceException("教师不存在");
        }
        TalkPermissionUtils.requireCollegeAccess(teacher.getDeptId());
    }

    private void requireCollegeDept(Long deptId) {
        SysDept dept = deptId == null ? null : deptMapper.selectDeptById(deptId);
        if (dept == null || !"college".equals(dept.getDeptType())) {
            throw new ServiceException("教师所属部门必须是学院节点");
        }
    }

    private Long resolveImportCollege(String deptName) {
        if (TalkPermissionUtils.isGlobalAdmin()) {
            return getOrCreateCollegeDept(deptName);
        }
        Long collegeId = TalkPermissionUtils.currentCollegeId();
        SysDept dept = deptMapper.selectDeptById(collegeId);
        if (dept == null || !dept.getDeptName().equals(deptName.trim())) {
            throw new ServiceException("院管理员只能导入本学院教师");
        }
        return collegeId;
    }

    private boolean hasRole(Long userId, String roleKey) {
        List<SysRole> roles = roleMapper.selectRolePermissionByUserId(userId);
        return roles != null && roles.stream().anyMatch(role -> roleKey.equals(role.getRoleKey()));
    }

    private void syncTeacherUser(TalkTeacher existing, TalkTeacher update, Long targetDeptId) {
        Long userId = existing.getUserId();
        SysUser user = userId != null ? userMapper.selectUserById(userId)
                : userMapper.selectUserByUserName(existing.getTeacherCode());
        if (user == null) {
            return;
        }
        user.setNickName(StringUtils.isNotEmpty(update.getTeacherName()) ? update.getTeacherName() : existing.getTeacherName());
        user.setDeptId(targetDeptId);
        if (update.getPhone() != null) {
            user.setPhonenumber(update.getPhone());
        }
        if (update.getStatus() != null) {
            user.setStatus(update.getStatus());
        }
        user.setUpdateBy(SecurityUtils.getUsername());
        userMapper.updateUser(user);
        if (StringUtils.isNotEmpty(update.getRoleKey()) && assignRole(user.getUserId(), update.getRoleKey())) {
            invalidateTeacherSessions(user.getUserId());
        }
    }
}
