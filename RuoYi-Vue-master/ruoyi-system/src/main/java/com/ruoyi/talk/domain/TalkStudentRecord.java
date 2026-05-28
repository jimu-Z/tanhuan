package com.ruoyi.talk.domain;

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
public class TalkStudentRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 会话ID */
    @Excel(name = "会话ID")
    private Long sessionId;

    /** 学生ID */
    @Excel(name = "学生ID")
    private Long studentId;

    /** 学生反馈 */
    @Excel(name = "学生反馈")
    private String studentFeedback;

    /** 跟进计划 */
    @Excel(name = "跟进计划")
    private String followupPlan;

    /** 跟进状态 */
    @Excel(name = "跟进状态")
    private String followupStatus;

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
    }

    public void setSessionId(Long sessionId) 
    {
        this.sessionId = sessionId;
    }

    public Long getSessionId() 
    {
        return sessionId;
    }

    public void setStudentId(Long studentId) 
    {
        this.studentId = studentId;
    }

    public Long getStudentId() 
    {
        return studentId;
    }

    public void setStudentFeedback(String studentFeedback) 
    {
        this.studentFeedback = studentFeedback;
    }

    public String getStudentFeedback() 
    {
        return studentFeedback;
    }

    public void setFollowupPlan(String followupPlan) 
    {
        this.followupPlan = followupPlan;
    }

    public String getFollowupPlan() 
    {
        return followupPlan;
    }

    public void setFollowupStatus(String followupStatus) 
    {
        this.followupStatus = followupStatus;
    }

    public String getFollowupStatus() 
    {
        return followupStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("sessionId", getSessionId())
            .append("studentId", getStudentId())
            .append("studentFeedback", getStudentFeedback())
            .append("followupPlan", getFollowupPlan())
            .append("followupStatus", getFollowupStatus())
            .append("createTime", getCreateTime())
            .toString();
    }
}
