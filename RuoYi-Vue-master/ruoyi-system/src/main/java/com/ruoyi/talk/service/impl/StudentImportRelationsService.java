package com.ruoyi.talk.service.impl;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.*;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.*;
import com.ruoyi.talk.domain.*;
import com.ruoyi.talk.mapper.StudentImportBindingMapper;
import com.ruoyi.talk.service.ITalkTeacherService;
import com.ruoyi.talk.security.TalkPermissionUtils;

/** Runs inside the importing row's transaction. Never grants a staff role or enables student login. */
@Service
public class StudentImportRelationsService {
    @Autowired private ITalkTeacherService teacherService;
    @Autowired private SysUserMapper userMapper;
    @Autowired private SysRoleMapper roleMapper;
    @Autowired private SysUserRoleMapper userRoleMapper;
    @Autowired private SysDeptMapper deptMapper;
    @Autowired private StudentImportBindingMapper bindings;

    public List<TalkTeacher> resolveTeachers(Long collegeId, Map<String,String> data) {
        Set<String> requested = new LinkedHashSet<>();
        for (String field : List.of("counselor","head_teacher","secretary","vice_secretary")) {
            String text=data.get(field);
            if (text!=null) for(String name:text.split("[、,，;；\\r\\n]+")) if(!name.isBlank()) requested.add(name.trim());
        }
        if(requested.isEmpty()) return List.of();
        TalkTeacher query=new TalkTeacher();query.setDeptId(collegeId);
        List<TalkTeacher> all=teacherService.selectTalkTeacherList(query);
        Map<Long,TalkTeacher> found=new LinkedHashMap<>();
        for(String name:requested) {
            List<TalkTeacher> exactCodes=all.stream().filter(t->collegeId.equals(t.getDeptId()) && name.equals(t.getTeacherCode())).toList();
            List<TalkTeacher> matches=exactCodes.isEmpty()?all.stream().filter(t->collegeId.equals(t.getDeptId()) && name.equals(t.getTeacherName())).toList():exactCodes;
            if(matches.isEmpty()) throw new ServiceException("教师["+name+"]在该学院不存在");
            if(matches.size()>1) throw new ServiceException("教师["+name+"]存在同名记录，请填写工号");
            TalkTeacher teacher=matches.get(0);found.put(teacher.getTeacherId(),teacher);
        }
        return new ArrayList<>(found.values());
    }

    public SysUser validateAccount(String studentCode, TalkStudent existing) {
        if(studentCode.length()>30 || studentCode.isBlank()) throw new ServiceException("学号必须为1至30个字符，才能作为学生用户名");
        SysUser account=userMapper.selectUserByUserName(studentCode);
        if(account!=null) {
            List<SysRole> roles=roleMapper.selectRolePermissionByUserId(account.getUserId());
            if(Long.valueOf(1L).equals(account.getUserId()) || roles==null || roles.isEmpty() || roles.stream().anyMatch(r->!"talk_student".equals(r.getRoleKey())))
                throw new ServiceException("学号对应用户名已被非本学生账号占用，不能覆盖或分配学生角色");
            Map<String,Object> owner=bindings.selectAccountOwner(account.getUserId());
            if(owner!=null) {
                Object ownerStudent=owner.get("studentId");
                if(!studentCode.equals(owner.get("studentCode")) || (ownerStudent!=null && (existing==null || !existing.getStudentId().equals(((Number)ownerStudent).longValue()))))
                    throw new ServiceException("学生账号已有其他学生归属，不能接管");
                TalkPermissionUtils.requireCollegeAccess(((Number)owner.get("collegeId")).longValue());
            } else {
                if(existing==null) throw new ServiceException("同名学生账号缺少可核验归属，不能接管，请管理员核对");
                Long accountCollege=collegeOf(account.getDeptId());
                TalkPermissionUtils.requireCollegeAccess(accountCollege);
                if(!accountCollege.equals(collegeOf(existing.getDeptId())))
                    throw new ServiceException("遗留学生账号与学生资料所属学院不一致，不能自动接管");
            }
        }
        return account;
    }

    public int saveAccountAndBindings(TalkStudent student, String className, Long collegeId, List<TalkTeacher> teachers, SysUser existingAccount) {
        if(student.getStudentId()==null) throw new ServiceException("学生保存失败，未获得学生ID");
        int created=0;
        Long userId=existingAccount==null?null:existingAccount.getUserId();
        if(existingAccount==null) {
            SysRole query=new SysRole();query.setRoleKey("talk_student");
            List<SysRole> roles=roleMapper.selectRoleList(query).stream().filter(r->"talk_student".equals(r.getRoleKey()) && "0".equals(r.getDelFlag())).toList();
            if(roles.size()!=1) throw new ServiceException("学生角色不存在或不唯一，请先执行学生导入增量升级脚本");
            SysUser user=new SysUser();user.setUserName(student.getStudentCode());
            String name=student.getStudentName();user.setNickName(name.length()>30?name.substring(0,30):name);
            user.setDeptId(student.getDeptId());user.setPassword(SecurityUtils.encryptPassword("123456"));
            user.setStatus("1");user.setDelFlag("0");user.setCreateBy(SecurityUtils.getUsername());user.setCreateTime(new Date());
            if(userMapper.insertUser(user)!=1 || user.getUserId()==null) throw new ServiceException("学生账号创建失败");
            userId=user.getUserId();
            SysUserRole relation=new SysUserRole();relation.setUserId(user.getUserId());relation.setRoleId(roles.get(0).getRoleId());
            if(userRoleMapper.batchUserRole(List.of(relation))!=1) throw new ServiceException("学生角色分配失败");
            created=1;
        } else {
            SysUser changes=new SysUser();changes.setUserId(existingAccount.getUserId());changes.setDeptId(student.getDeptId());
            String name=student.getStudentName();changes.setNickName(name.length()>30?name.substring(0,30):name);
            // Do not reset the password, roles or activation state on a repeated import.
            if(userMapper.updateUser(changes)!=1) throw new ServiceException("学生账号资料同步失败");
        }
        bindings.bindAccount(student.getStudentCode(),userId,student.getStudentId(),collegeId);
        bindings.updateAccountOwner(student.getStudentCode(),userId,student.getStudentId(),collegeId);
        Map<String,Object> savedOwner=bindings.selectAccountOwner(userId);
        if(savedOwner==null || !student.getStudentCode().equals(savedOwner.get("studentCode")) || savedOwner.get("studentId")==null || !student.getStudentId().equals(((Number)savedOwner.get("studentId")).longValue()))
            throw new ServiceException("学生账号归属保存失败");
        for(TalkTeacher teacher:teachers) {
            bindings.materializeLegacyClassBindings(teacher.getTeacherId());
            bindings.bindStudent(student.getStudentId(),teacher.getTeacherId());
            bindings.bindClass(student.getDeptId(),teacher.getTeacherId());
            bindings.bindLegacyClass(teacher.getTeacherCode(),className);
            if(bindings.bindingExists(student.getStudentId(),student.getDeptId(),teacher.getTeacherId())!=1)
                throw new ServiceException("教师管理关联保存失败");
        }
        return created;
    }

    public List<TalkTeacher> managers(Long studentId) { return bindings.selectStudentManagers(studentId); }

    public boolean hasImportedAccount(Long studentId) { return bindings.selectAccountByStudentId(studentId)!=null; }

    /** Keep an imported account attached when student number, name or class is edited manually. */
    public void syncExistingAccount(TalkStudent existing, TalkStudent updated, Long collegeId) {
        Map<String,Object> owner=bindings.selectAccountByStudentId(existing.getStudentId());
        if(owner==null) return;
        Long userId=((Number)owner.get("userId")).longValue();
        List<SysRole> roles=roleMapper.selectRolePermissionByUserId(userId);
        if(roles==null||roles.isEmpty()||roles.stream().anyMatch(r->!"talk_student".equals(r.getRoleKey())))
            throw new ServiceException("学生关联账号角色异常，请管理员核对，不能自动修改");
        String code=updated.getStudentCode()==null||updated.getStudentCode().isBlank()?existing.getStudentCode():updated.getStudentCode().trim();
        if(code.length()>30) throw new ServiceException("学号超过学生用户名允许的30字符");
        SysUser collision=userMapper.selectUserByUserName(code);
        if(collision!=null&&!userId.equals(collision.getUserId())) throw new ServiceException("新学号对应用户名已被占用");
        SysUser changes=new SysUser();changes.setUserId(userId);changes.setUserName(code);
        String name=updated.getStudentName()==null||updated.getStudentName().isBlank()?existing.getStudentName():updated.getStudentName();
        changes.setNickName(name.length()>30?name.substring(0,30):name);
        changes.setDeptId(updated.getDeptId()==null?existing.getDeptId():updated.getDeptId());
        if(userMapper.updateUser(changes)!=1) throw new ServiceException("学生账号资料同步失败");
        if(!code.equals(owner.get("studentCode")) && bindings.renameStudentUsername(userId,existing.getStudentId(),code)!=1)
            throw new ServiceException("学生登录用户名同步失败");
        bindings.renameAccountOwner(userId,existing.getStudentId(),code,collegeId);
    }

    private Long collegeOf(Long deptId) {
        for(int depth=0;deptId!=null&&depth<12;depth++) {
            SysDept dept=deptMapper.selectDeptById(deptId);
            if(dept==null) break;
            if("college".equals(dept.getDeptType())) return dept.getDeptId();
            deptId=dept.getParentId();
        }
        throw new ServiceException("学生账号或资料未归属有效学院");
    }
}
