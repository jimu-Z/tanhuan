package com.ruoyi.talk.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 教师信息对象 talk_teacher
 *
 * @author admin
 * @date 2026-06-06
 */
public class TalkTeacher extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long teacherId;

    @Excel(name = "工号")
    private String teacherCode;

    @Excel(name = "姓名")
    private String name;

    @Excel(name = "所属学院ID")
    private Long deptId;

    @Excel(name = "岗位")
    private String position;

    @Excel(name = "手机号码")
    private String phone;

    /** 关联的sys_user.userId */
    private Long userId;

    private String status;

    /** 学院名称（非数据库字段），导入时匹配"所属学院"列 */
    @Excel(name = "所属学院")
    private String deptName;

    // === getters/setters ===

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getTeacherCode() { return teacherCode; }
    public void setTeacherCode(String teacherCode) { this.teacherCode = teacherCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    @Override
    public String toString() {
        return "TalkTeacher{" +
                "teacherId=" + teacherId +
                ", teacherCode='" + teacherCode + '\'' +
                ", name='" + name + '\'' +
                ", deptId=" + deptId +
                ", position='" + position + '\'' +
                ", phone='" + phone + '\'' +
                ", userId=" + userId +
                ", status='" + status + '\'' +
                '}';
    }
}
