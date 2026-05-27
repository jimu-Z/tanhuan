package com.ruoyi.conversation.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class Conversation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long conversationId;

    private String studentNo;

    private String studentName;

    private String collegeName;

    private String majorName;

    private String className;

    private Long deptId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date conversationTime;

    private String conversationPlace;

    private String speaker;

    private String topic;

    private String content;

    private String followUpItems;

    private String status;

    private String delFlag;

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Date getConversationTime() {
        return conversationTime;
    }

    public void setConversationTime(Date conversationTime) {
        this.conversationTime = conversationTime;
    }

    public String getConversationPlace() {
        return conversationPlace;
    }

    public void setConversationPlace(String conversationPlace) {
        this.conversationPlace = conversationPlace;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFollowUpItems() {
        return followUpItems;
    }

    public void setFollowUpItems(String followUpItems) {
        this.followUpItems = followUpItems;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }
}