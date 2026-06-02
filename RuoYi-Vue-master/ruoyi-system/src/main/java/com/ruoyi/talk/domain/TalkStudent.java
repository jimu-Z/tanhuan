package com.ruoyi.talk.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学生信息管理对象 talk_student
 * 
 * @author admin
 * @date 2026-05-27
 */
public class TalkStudent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 学生ID */
    private Long studentId;

    /** 学号 */
    @Excel(name = "学号")
    private String studentCode;

    /** 姓名 */
    @Excel(name = "姓名")
    private String studentName;

    /** 部门ID(班级) */
    @Excel(name = "部门ID(班级)")
    private Long deptId;

    /** 性别（0男 1女） */
    @Excel(name = "性别", readConverterExp = "0=男,1=女")
    private String gender;

    /** 政治面貌 */
    @Excel(name = "政治面貌")
    private String politicalStatus;

    /** 民族 */
    @Excel(name = "民族")
    private String nation;

    /** 本人联系电话 */
    @Excel(name = "本人联系电话")
    private String phone;

    /** 身份证号 */
    @Excel(name = "身份证号")
    private String idCard;

    /** 家庭住址 */
    @Excel(name = "家庭住址")
    private String address;

    /** 父亲姓名 */
    @Excel(name = "父亲姓名")
    private String fatherName;

    /** 父亲电话 */
    @Excel(name = "父亲电话")
    private String fatherPhone;

    /** 母亲姓名 */
    @Excel(name = "母亲姓名")
    private String motherName;

    /** 母亲电话 */
    @Excel(name = "母亲电话")
    private String motherPhone;

    /** 班长 */
    @Excel(name = "班长")
    private String classMonitor;

    /** 舍长 */
    @Excel(name = "舍长")
    private String dormLeader;

    /** 宿舍楼 */
    @Excel(name = "宿舍楼")
    private String dormBuilding;

    /** 宿舍号 */
    @Excel(name = "宿舍号")
    private String dormRoom;

    /** 学籍状态 */
    @Excel(name = "学籍状态")
    private String enrollmentStatus;

    /** 心理健康状态 */
    @Excel(name = "心理健康状态")
    private String mentalHealthStatus;

    /** 贫困等级认定 */
    @Excel(name = "贫困等级认定")
    private String povertyLevel;

    /** 班级名称（非数据库字段，仅用于查询展示） */
    private String deptName;

    public void setStudentId(Long studentId) 
    {
        this.studentId = studentId;
    }

    public Long getStudentId() 
    {
        return studentId;
    }

    public void setStudentCode(String studentCode) 
    {
        this.studentCode = studentCode;
    }

    public String getStudentCode() 
    {
        return studentCode;
    }

    public void setStudentName(String studentName) 
    {
        this.studentName = studentName;
    }

    public String getStudentName() 
    {
        return studentName;
    }

    public void setDeptId(Long deptId) 
    {
        this.deptId = deptId;
    }

    public Long getDeptId() 
    {
        return deptId;
    }

    public void setGender(String gender) 
    {
        this.gender = gender;
    }

    public String getGender() 
    {
        return gender;
    }

    public void setPoliticalStatus(String politicalStatus) 
    {
        this.politicalStatus = politicalStatus;
    }

    public String getPoliticalStatus() 
    {
        return politicalStatus;
    }

    public void setNation(String nation) 
    {
        this.nation = nation;
    }

    public String getNation() 
    {
        return nation;
    }

    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }

    public void setIdCard(String idCard) 
    {
        this.idCard = idCard;
    }

    public String getIdCard() 
    {
        return idCard;
    }

    public void setAddress(String address) 
    {
        this.address = address;
    }

    public String getAddress() 
    {
        return address;
    }

    public void setFatherName(String fatherName) 
    {
        this.fatherName = fatherName;
    }

    public String getFatherName() 
    {
        return fatherName;
    }

    public void setFatherPhone(String fatherPhone) 
    {
        this.fatherPhone = fatherPhone;
    }

    public String getFatherPhone() 
    {
        return fatherPhone;
    }

    public void setMotherName(String motherName) 
    {
        this.motherName = motherName;
    }

    public String getMotherName() 
    {
        return motherName;
    }

    public void setMotherPhone(String motherPhone) 
    {
        this.motherPhone = motherPhone;
    }

    public String getMotherPhone() 
    {
        return motherPhone;
    }

    public void setClassMonitor(String classMonitor) 
    {
        this.classMonitor = classMonitor;
    }

    public String getClassMonitor() 
    {
        return classMonitor;
    }

    public void setDormLeader(String dormLeader) 
    {
        this.dormLeader = dormLeader;
    }

    public String getDormLeader() 
    {
        return dormLeader;
    }

    public void setDormBuilding(String dormBuilding) 
    {
        this.dormBuilding = dormBuilding;
    }

    public String getDormBuilding() 
    {
        return dormBuilding;
    }

    public void setDormRoom(String dormRoom) 
    {
        this.dormRoom = dormRoom;
    }

    public String getDormRoom() 
    {
        return dormRoom;
    }

    public void setEnrollmentStatus(String enrollmentStatus) 
    {
        this.enrollmentStatus = enrollmentStatus;
    }

    public String getEnrollmentStatus() 
    {
        return enrollmentStatus;
    }

    public void setMentalHealthStatus(String mentalHealthStatus) 
    {
        this.mentalHealthStatus = mentalHealthStatus;
    }

    public String getMentalHealthStatus() 
    {
        return mentalHealthStatus;
    }

    public void setPovertyLevel(String povertyLevel) 
    {
        this.povertyLevel = povertyLevel;
    }

    public String getPovertyLevel() 
    {
        return povertyLevel;
    }

    public void setDeptName(String deptName) 
    {
        this.deptName = deptName;
    }

    public String getDeptName() 
    {
        return deptName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("studentId", getStudentId())
            .append("studentCode", getStudentCode())
            .append("studentName", getStudentName())
            .append("deptId", getDeptId())
            .append("gender", getGender())
            .append("politicalStatus", getPoliticalStatus())
            .append("nation", getNation())
            .append("phone", getPhone())
            .append("idCard", getIdCard())
            .append("address", getAddress())
            .append("fatherName", getFatherName())
            .append("fatherPhone", getFatherPhone())
            .append("motherName", getMotherName())
            .append("motherPhone", getMotherPhone())
            .append("classMonitor", getClassMonitor())
            .append("dormLeader", getDormLeader())
            .append("dormBuilding", getDormBuilding())
            .append("dormRoom", getDormRoom())
            .append("enrollmentStatus", getEnrollmentStatus())
            .append("mentalHealthStatus", getMentalHealthStatus())
            .append("povertyLevel", getPovertyLevel())
            .append("deptName", getDeptName())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
