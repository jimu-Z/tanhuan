package com.ruoyi.talk.mapper;

import java.util.List;
import com.ruoyi.talk.domain.TalkAlert;

public interface TalkAlertMapper
{
    public List<TalkAlert> selectTalkAlertList(TalkAlert alert);
    public TalkAlert selectTalkAlertById(Long alertId);
    public int insertTalkAlert(TalkAlert alert);
    public int updateTalkAlert(TalkAlert alert);
    public int deleteTalkAlertByIds(Long[] alertIds);
    public List<TalkAlert> selectPendingAutoAlertsByStudentId(Long studentId);
}
