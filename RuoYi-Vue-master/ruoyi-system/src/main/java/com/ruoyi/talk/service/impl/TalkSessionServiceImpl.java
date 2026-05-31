package com.ruoyi.talk.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.talk.constant.TalkConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.mapper.TalkSessionTagMapper;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.domain.TalkSessionCreateRequest;
import com.ruoyi.talk.domain.TalkSessionTag;
import com.ruoyi.talk.domain.TalkStudentRecord;
import com.ruoyi.talk.service.ITalkSessionService;

/**
 * 谈话会话管理Service业务层处理
 * 
 * @author admin
 * @date 2026-05-27
 */
@Service
public class TalkSessionServiceImpl implements ITalkSessionService {
    @Autowired
    private TalkSessionMapper talkSessionMapper;

    @Autowired
    private TalkStudentRecordMapper talkStudentRecordMapper;

    @Autowired
    private TalkSessionTagMapper talkSessionTagMapper;

    @Override
    public TalkSession selectTalkSessionBySessionId(Long sessionId) {
        return talkSessionMapper.selectTalkSessionBySessionId(sessionId);
    }

    @Override
    public List<TalkSession> selectTalkSessionList(TalkSession talkSession) {
        applyDataScopeFilter(talkSession);
        return talkSessionMapper.selectTalkSessionList(talkSession);
    }

    private void applyDataScopeFilter(TalkSession talkSession) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        String username = SecurityUtils.getUsername();
        if (username == null) {
            return;
        }
        if (talkSession.getParams() == null) {
            talkSession.setParams(new HashMap<>());
        }
        if (SecurityUtils.hasRole("talk_counselor")) {
            talkSession.getParams().put("dataScope",
                    " and ts.create_by = '" + username.replace("'", "''") + "'");
        } else if (SecurityUtils.hasRole("talk_secretary")) {
            Long deptId = SecurityUtils.getDeptId();
            if (deptId != null) {
                talkSession.getParams().put("dataScope",
                        " and ts.session_id in (select tsr.session_id from talk_student_record tsr" +
                                " join talk_student stu on tsr.student_id = stu.student_id" +
                                " join sys_dept d on stu.dept_id = d.dept_id" +
                                " where (d.dept_id = " + deptId + " or find_in_set(" + deptId + ", d.ancestors)))");
            }
        }
    }

    @Override
    public int insertTalkSession(TalkSession talkSession) {
        talkSession.setCreateTime(DateUtils.getNowDate());
        return talkSessionMapper.insertTalkSession(talkSession);
    }

    @Override
    public int updateTalkSession(TalkSession talkSession) {
        talkSession.setUpdateBy(SecurityUtils.getUsername());
        talkSession.setUpdateTime(DateUtils.getNowDate());
        return talkSessionMapper.updateTalkSession(talkSession);
    }

    @Override
    @Transactional
    public int deleteTalkSessionBySessionIds(Long[] sessionIds) {
        for (Long sessionId : sessionIds) {
            deleteTalkSessionBySessionId(sessionId);
        }
        return sessionIds.length;
    }

    @Override
    @Transactional
    public int deleteTalkSessionBySessionId(Long sessionId) {
        talkStudentRecordMapper.deleteTalkStudentRecordBySessionId(sessionId);
        talkSessionTagMapper.deleteTalkSessionTagBySessionId(sessionId);
        return talkSessionMapper.deleteTalkSessionBySessionId(sessionId);
    }

    @Override
    @Transactional
    public TalkSession createTalkWithRecords(TalkSessionCreateRequest request) {
        TalkSession session = new TalkSession();
        session.setTalkType(request.getTalkType());
        session.setTalkTime(DateUtils.parseDate(request.getTalkTime()));
        session.setTalkLocation(request.getTalkLocation());
        session.setTalkPerson(request.getTalkPerson());
        session.setTalkContent(request.getTalkContent());
        session.setCreateBy(SecurityUtils.getUsername());
        session.setCreateTime(DateUtils.getNowDate());

        talkSessionMapper.insertTalkSession(session);

        if (request.getTags() != null) {
            for (String tagValue : request.getTags()) {
                TalkSessionTag tag = new TalkSessionTag();
                tag.setSessionId(session.getSessionId());
                tag.setTagValue(tagValue);
                talkSessionTagMapper.insertTalkSessionTag(tag);
            }
        }

        Map<Long, TalkSessionCreateRequest.StudentDataItem> dataMap = new HashMap<>();
        if (request.getStudentDataList() != null) {
            for (TalkSessionCreateRequest.StudentDataItem item : request.getStudentDataList()) {
                dataMap.put(item.getStudentId(), item);
            }
        }

        List<Long> studentIds = request.getStudentIds();
        if (studentIds != null && !studentIds.isEmpty()) {
            for (Long studentId : studentIds) {
                TalkSessionCreateRequest.StudentDataItem item = dataMap.get(studentId);
                TalkStudentRecord record = new TalkStudentRecord();
                record.setSessionId(session.getSessionId());
                record.setStudentId(studentId);
                record.setStudentFeedback(item != null && item.getStudentFeedback() != null
                        ? item.getStudentFeedback()
                        : TalkConstants.DEFAULT_FEEDBACK);
                record.setFollowupPlan(item != null && item.getFollowupPlan() != null
                        ? item.getFollowupPlan()
                        : TalkConstants.DEFAULT_FOLLOWUP_PLAN);
                record.setFollowupStatus(item != null && item.getFollowupStatus() != null
                        ? item.getFollowupStatus()
                        : TalkConstants.DEFAULT_FOLLOWUP_STATUS);
                record.setCreateTime(DateUtils.getNowDate());
                talkStudentRecordMapper.insertTalkStudentRecord(record);
            }
        }

        return session;
    }

    @Override
    public List<TalkSessionTag> selectTalkSessionTags(Long sessionId) {
        return talkSessionTagMapper.selectTalkSessionTagBySessionId(sessionId);
    }
}
