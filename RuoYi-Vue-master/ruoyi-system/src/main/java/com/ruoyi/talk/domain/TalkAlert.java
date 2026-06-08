package com.ruoyi.talk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 心理健康预警对象 talk_alert
 *
 * @author admin
 * @date 2026-06-06
 */
public class TalkAlert extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long alertId;

    private Long studentId;

    /** 预警类型：auto/manual/keyword */
    private String alertType;

    /** 预警等级：red/orange/yellow */
    private String alertLevel;

    private String alertReason;

    /** 处理状态：pending/in_progress/resolved */
    private String alertStatus;

    private String handler;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    private String handleRemark;

    /** 以下为非数据库字段，仅用于查询展示 */
    private String studentName;
    private String studentCode;
    private String deptName;

    // === getters/setters ===

    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }

    public String getAlertReason() { return alertReason; }
    public void setAlertReason(String alertReason) { this.alertReason = alertReason; }

    public String getAlertStatus() { return alertStatus; }
    public void setAlertStatus(String alertStatus) { this.alertStatus = alertStatus; }

    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }

    public Date getHandleTime() { return handleTime; }
    public void setHandleTime(Date handleTime) { this.handleTime = handleTime; }

    public String getHandleRemark() { return handleRemark; }
    public void setHandleRemark(String handleRemark) { this.handleRemark = handleRemark; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    @Override
    public String toString() {
        return "TalkAlert{" +
                "alertId=" + alertId +
                ", studentId=" + studentId +
                ", alertType='" + alertType + '\'' +
                ", alertLevel='" + alertLevel + '\'' +
                ", alertStatus='" + alertStatus + '\'' +
                '}';
    }
}
