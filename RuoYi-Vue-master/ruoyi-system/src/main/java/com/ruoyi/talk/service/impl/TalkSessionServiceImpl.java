package com.ruoyi.talk.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.talk.constant.TalkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.talk.mapper.TalkAttachmentMapper;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.mapper.TalkSessionTagMapper;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;
import com.ruoyi.talk.domain.TalkAttachment;
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
    private static final Logger log = LoggerFactory.getLogger(TalkSessionServiceImpl.class);

    @Value("${ruoyi.profile}")
    private String profile;

    @Autowired
    private TalkSessionMapper talkSessionMapper;

    @Autowired
    private TalkStudentRecordMapper talkStudentRecordMapper;

    @Autowired
    private TalkSessionTagMapper talkSessionTagMapper;

    @Autowired
    private TalkAttachmentMapper talkAttachmentMapper;

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
            talkSession.getParams().put("counselorUsername", username);
        } else if (SecurityUtils.hasRole("talk_secretary")) {
            Long deptId = SecurityUtils.getDeptId();
            if (deptId != null) {
                talkSession.getParams().put("secretaryDeptId", deptId);
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
        List<TalkAttachment> attachments = talkAttachmentMapper.selectTalkAttachmentBySessionId(sessionId);
        for (TalkAttachment att : attachments) {
            try {
                String fullPath = profile + att.getFilePath();
                File file = new File(fullPath);
                if (file.exists() && !file.delete()) {
                    log.warn("删除附件文件失败: {}", fullPath);
                }
            } catch (Exception e) {
                log.warn("删除附件文件异常: {}", e.getMessage());
            }
        }
        talkAttachmentMapper.deleteTalkAttachmentBySessionId(sessionId);
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
                record.setNotified(1);
                record.setTeacherNotified(0);
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

    @Override
    public Map<Long, List<TalkSessionTag>> selectTalkSessionTagsBatch(String sessionIds) {
        List<Long> ids = new ArrayList<>();
        if (sessionIds != null && !sessionIds.trim().isEmpty()) {
            for (String s : sessionIds.split(",")) {
                try {
                    ids.add(Long.parseLong(s.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        Map<Long, List<TalkSessionTag>> result = new HashMap<>();
        if (ids.isEmpty()) {
            return result;
        }
        List<TalkSessionTag> tags = talkSessionTagMapper.selectTagsBySessionIds(ids);
        for (TalkSessionTag tag : tags) {
            result.computeIfAbsent(tag.getSessionId(), k -> new ArrayList<>()).add(tag);
        }
        return result;
    }
}
