package com.ruoyi.talk.service;

import java.util.List;
import com.ruoyi.talk.domain.TalkAlert;

public interface ITalkAlertService
{
    public List<TalkAlert> selectTalkAlertList(TalkAlert alert);
    public TalkAlert selectTalkAlertById(Long alertId);
    public int insertTalkAlert(TalkAlert alert);
    public int updateTalkAlert(TalkAlert alert);
    public int deleteTalkAlertByIds(Long[] alertIds);
    public int handleAlert(Long alertId, String alertStatus, String handleRemark);
    public void checkStudentFeedbackForKeywords(Long studentId, String feedback);
    public void autoGenerateAlertForStudent(Long studentId, String mentalHealthStatus);
}
