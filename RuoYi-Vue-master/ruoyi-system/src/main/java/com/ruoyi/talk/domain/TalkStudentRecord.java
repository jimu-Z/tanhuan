package com.ruoyi.talk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 谈话记录管理对象 talk_student_record
 * 
 * @author admin
 * @date 2026-05-27
 */
public class TalkStudentRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 会话ID */
    @Excel(name = "会话ID")
    private Long sessionId;

    /** 学生ID */
    @Excel(name = "学生ID")
    private Long studentId;

    private String studentName;

    private String studentCode;

    /** 学生反馈 */
    @Excel(name = "学生反馈")
    private String studentFeedback;

    /** 原始学生反馈（学生原始提交，不被教师修改） */
    private String originalStudentFeedback;

    /** 跟进计划 */
    @Excel(name = "跟进计划")
    private String followupPlan;

    /** 跟进状态 */
    @Excel(name = "跟进状态")
    private String followupStatus;

    private Integer notified;

    private Integer teacherNotified;

    /** 会话ID，支持逗号分隔多个ID(仅查询时使用) */
    private String sessionIds;

    /** 是否查询未反馈记录（非数据库字段，仅用于查询条件） */
    private Boolean hasNoFeedback;

    /** 谈话类型（individual 个体谈话 / group 集体谈话） */
    private String talkType;

    /** 谈话时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date talkTime;

    /** 谈话地点 */
    private String talkLocation;

    /** 谈话内容 */
    private String talkContent;

    /** 谈话人 */
    private String talkPerson;

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentFeedback(String studentFeedback) {
        this.studentFeedback = studentFeedback;
    }

    public String getStudentFeedback() {
        return studentFeedback;
    }

    public void setOriginalStudentFeedback(String originalStudentFeedback) {
        this.originalStudentFeedback = originalStudentFeedback;
    }

    public String getOriginalStudentFeedback() {
        return originalStudentFeedback;
    }

    public void setFollowupPlan(String followupPlan) {
        this.followupPlan = followupPlan;
    }

    public String getFollowupPlan() {
        return followupPlan;
    }

    public void setFollowupStatus(String followupStatus) {
        this.followupStatus = followupStatus;
    }

    public String getFollowupStatus() {
        return followupStatus;
    }

    public Integer getNotified() {
        return notified;
    }

    public void setNotified(Integer notified) {
        this.notified = notified;
    }

    public Integer getTeacherNotified() {
        return teacherNotified;
    }

    public void setTeacherNotified(Integer teacherNotified) {
        this.teacherNotified = teacherNotified;
    }

    public String getSessionIds() { return sessionIds; }
    public void setSessionIds(String sessionIds) { this.sessionIds = sessionIds; }

    public void setTalkType(String talkType) {
        this.talkType = talkType;
    }

    public String getTalkType() {
        return talkType;
    }

    public void setTalkTime(Date talkTime) {
        this.talkTime = talkTime;
    }

    public Date getTalkTime() {
        return talkTime;
    }

    public void setTalkLocation(String talkLocation) {
        this.talkLocation = talkLocation;
    }

    public String getTalkLocation() {
        return talkLocation;
    }

    public void setTalkContent(String talkContent) {
        this.talkContent = talkContent;
    }

    public String getTalkContent() {
        return talkContent;
    }

    public void setTalkPerson(String talkPerson) {
        this.talkPerson = talkPerson;
    }

    public String getTalkPerson() {
        return talkPerson;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("recordId", getRecordId())
                .append("sessionId", getSessionId())
                .append("studentId", getStudentId())
                .append("studentName", getStudentName())
                .append("studentCode", getStudentCode())
                .append("studentFeedback", getStudentFeedback())
                .append("originalStudentFeedback", getOriginalStudentFeedback())
                .append("followupPlan", getFollowupPlan())
                .append("followupStatus", getFollowupStatus())
                .append("notified", getNotified())
                .append("teacherNotified", getTeacherNotified())
                .append("talkType", getTalkType())
                .append("talkTime", getTalkTime())
                .append("talkLocation", getTalkLocation())
                .append("talkContent", getTalkContent())
                .append("talkPerson", getTalkPerson())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
