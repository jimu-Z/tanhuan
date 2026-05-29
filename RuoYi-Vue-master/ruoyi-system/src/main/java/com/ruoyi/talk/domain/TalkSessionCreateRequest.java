package com.ruoyi.talk.domain;

import java.util.List;

public class TalkSessionCreateRequest {

    private String talkType;
    private String talkTime;
    private String talkLocation;
    private String talkPerson;
    private String talkContent;
    private List<Long> studentIds;
    private List<String> tags;
    private List<StudentDataItem> studentDataList;

    public static class StudentDataItem {
        private Long studentId;
        private String studentFeedback;
        private String followupPlan;
        private String followupStatus;

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }
        public String getStudentFeedback() { return studentFeedback; }
        public void setStudentFeedback(String studentFeedback) { this.studentFeedback = studentFeedback; }
        public String getFollowupPlan() { return followupPlan; }
        public void setFollowupPlan(String followupPlan) { this.followupPlan = followupPlan; }
        public String getFollowupStatus() { return followupStatus; }
        public void setFollowupStatus(String followupStatus) { this.followupStatus = followupStatus; }
    }

    public String getTalkType() { return talkType; }
    public void setTalkType(String talkType) { this.talkType = talkType; }

    public String getTalkTime() { return talkTime; }
    public void setTalkTime(String talkTime) { this.talkTime = talkTime; }

    public String getTalkLocation() { return talkLocation; }
    public void setTalkLocation(String talkLocation) { this.talkLocation = talkLocation; }

    public String getTalkPerson() { return talkPerson; }
    public void setTalkPerson(String talkPerson) { this.talkPerson = talkPerson; }

    public String getTalkContent() { return talkContent; }
    public void setTalkContent(String talkContent) { this.talkContent = talkContent; }

    public List<Long> getStudentIds() { return studentIds; }
    public void setStudentIds(List<Long> studentIds) { this.studentIds = studentIds; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<StudentDataItem> getStudentDataList() { return studentDataList; }
    public void setStudentDataList(List<StudentDataItem> studentDataList) { this.studentDataList = studentDataList; }
}
