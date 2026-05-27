package com.ruoyi.conversation.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class ConversationFollowUp extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long followId;

    private Long conversationId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date followTime;

    private String followContent;

    private String followStatus;

    private String followBy;

    public Long getFollowId() {
        return followId;
    }

    public void setFollowId(Long followId) {
        this.followId = followId;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Date getFollowTime() {
        return followTime;
    }

    public void setFollowTime(Date followTime) {
        this.followTime = followTime;
    }

    public String getFollowContent() {
        return followContent;
    }

    public void setFollowContent(String followContent) {
        this.followContent = followContent;
    }

    public String getFollowStatus() {
        return followStatus;
    }

    public void setFollowStatus(String followStatus) {
        this.followStatus = followStatus;
    }

    public String getFollowBy() {
        return followBy;
}

public void setFollowBy(String followBy)
    {
        this.followBy = followBy;
    }
}