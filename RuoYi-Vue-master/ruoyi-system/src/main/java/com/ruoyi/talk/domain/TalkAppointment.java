package com.ruoyi.talk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学生预约谈话对象 talk_appointment
 *
 * @author admin
 * @date 2026-06-06
 */
public class TalkAppointment extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long appointmentId;

    private Long studentId;

    private Long teacherId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date appointmentTime;

    private String location;

    private String reason;

    /** 状态：pending/confirmed/rejected/completed/cancelled */
    private String status;

    private String rejectReason;

    /** 确认后关联的talk_session.sessionId */
    private Long sessionId;

    /** 以下为非数据库字段，仅用于查询展示 */
    private String studentName;
    private String studentCode;
    private String teacherName;

    // === getters/setters ===

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public Date getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(Date appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    @Override
    public String toString() {
        return "TalkAppointment{" +
                "appointmentId=" + appointmentId +
                ", studentId=" + studentId +
                ", teacherId=" + teacherId +
                ", status='" + status + '\'' +
                '}';
    }
}
