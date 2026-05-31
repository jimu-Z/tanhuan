package com.ruoyi.talk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 谈话会话管理对象 talk_session
 * 
 * @author admin
 * @date 2026-05-27
 */
public class TalkSession extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long sessionId;

    /** 谈话类型（individual 个体谈话 / group 集体谈话） */
    @Excel(name = "谈话类型", readConverterExp = "individual=个别谈话,group=集体谈话")
    private String talkType;

    /** 谈话时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "谈话时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date talkTime;

    /** 谈话地点 */
    @Excel(name = "谈话地点")
    private String talkLocation;

    /** 谈话人(默认当前班主任) */
    @Excel(name = "谈话人(默认当前班主任)")
    private String talkPerson;

    /** 谈话内容（所有参与学生共享） */
    @Excel(name = "谈话内容")
    private String talkContent;

    public void setSessionId(Long sessionId) 
    {
        this.sessionId = sessionId;
    }

    public Long getSessionId() 
    {
        return sessionId;
    }

    public void setTalkType(String talkType) 
    {
        this.talkType = talkType;
    }

    public String getTalkType() 
    {
        return talkType;
    }

    public void setTalkTime(Date talkTime) 
    {
        this.talkTime = talkTime;
    }

    public Date getTalkTime() 
    {
        return talkTime;
    }

    public void setTalkLocation(String talkLocation) 
    {
        this.talkLocation = talkLocation;
    }

    public String getTalkLocation() 
    {
        return talkLocation;
    }

    public void setTalkPerson(String talkPerson) 
    {
        this.talkPerson = talkPerson;
    }

    public String getTalkPerson() 
    {
        return talkPerson;
    }

    public void setTalkContent(String talkContent) 
    {
        this.talkContent = talkContent;
    }

    public String getTalkContent() 
    {
        return talkContent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("sessionId", getSessionId())
            .append("talkType", getTalkType())
            .append("talkTime", getTalkTime())
            .append("talkLocation", getTalkLocation())
            .append("talkPerson", getTalkPerson())
            .append("talkContent", getTalkContent())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
