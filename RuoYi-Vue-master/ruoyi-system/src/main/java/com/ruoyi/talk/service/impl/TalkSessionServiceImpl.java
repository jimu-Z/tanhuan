package com.ruoyi.talk.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.talk.utils.AttachmentPathUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.exception.ServiceException;
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
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.domain.TalkAttachment;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.domain.TalkSessionCreateRequest;
import com.ruoyi.talk.domain.TalkSessionTag;
import com.ruoyi.talk.domain.TalkStudentRecord;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.security.TalkPermissionUtils;
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

    @Autowired
    private TalkStudentMapper talkStudentMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    public TalkSession selectTalkSessionBySessionId(Long sessionId) {
        TalkSession session = talkSessionMapper.selectTalkSessionBySessionId(sessionId);
        if (session != null) {
            TalkPermissionUtils.requireCollegeAccess(session.getCollegeDeptId());
        }
        return session;
    }

    @Override
    public List<TalkSession> selectTalkSessionList(TalkSession talkSession) {
        TalkPermissionUtils.applyCollegeScope(talkSession);
        return talkSessionMapper.selectTalkSessionList(talkSession);
    }

    @Override
    public int insertTalkSession(TalkSession talkSession) {
        if (talkSession.getCollegeDeptId() == null) {
            throw new ServiceException("谈话必须指定所属学院");
        }
        TalkPermissionUtils.requireCollegeAccess(talkSession.getCollegeDeptId());
        talkSession.setCreateBy(SecurityUtils.getUsername());
        talkSession.setCreateTime(DateUtils.getNowDate());
        return talkSessionMapper.insertTalkSession(talkSession);
    }

    @Override
    @Transactional
    public int updateTalkSession(TalkSession talkSession) {
        talkSessionMapper.lockTalkSession(talkSession.getSessionId());
        TalkSession existing = requireSession(talkSession.getSessionId());
        TalkPermissionUtils.requireOwnedWrite(existing.getCollegeDeptId(), existing.getCreateBy());
        if (talkSession.getTalkType() == null) talkSession.setTalkType(existing.getTalkType());
        if (talkSession.getTalkTime() == null) talkSession.setTalkTime(existing.getTalkTime());
        if (talkSession.getTalkPerson() == null) talkSession.setTalkPerson(existing.getTalkPerson());
        if (!"individual".equals(talkSession.getTalkType()) && !"group".equals(talkSession.getTalkType()))
            throw new ServiceException("谈话类型只能为个别谈话或集体谈话");
        if (talkSession.getTalkTime() == null) throw new ServiceException("谈话日期无效");
        if (talkSession.getTalkPerson() == null || talkSession.getTalkPerson().trim().isEmpty())
            throw new ServiceException("谈话人不能为空");
        List<TalkStudentRecord> records = talkStudentRecordMapper.selectRecordsForUpdate(talkSession.getSessionId());
        java.util.Set<Long> students = new java.util.HashSet<>();
        if (records.isEmpty()) throw new ServiceException("谈话至少保留一名学生");
        for (TalkStudentRecord record : records) {
            if (record.getStudentId() == null || !students.add(record.getStudentId()))
                throw new ServiceException("参与学生无效或重复");
        }
        if ("individual".equals(talkSession.getTalkType()) && students.size() != 1)
            throw new ServiceException("个别谈话只能选择一名学生");
        talkSession.setCollegeDeptId(null);
        talkSession.setUpdateBy(SecurityUtils.getUsername());
        talkSession.setUpdateTime(DateUtils.getNowDate());
        return talkSessionMapper.updateTalkSession(talkSession);
    }

    @Override
    @Transactional
    public int deleteTalkSessionBySessionIds(Long[] sessionIds) {
        for (Long sessionId : new java.util.TreeSet<>(java.util.Arrays.asList(sessionIds))) {
            talkSessionMapper.lockTalkSession(sessionId);
            TalkSession session = requireSession(sessionId);
            TalkPermissionUtils.requireOwnedWrite(session.getCollegeDeptId(), session.getCreateBy());
        }
        int total = 0;
        for (Long sessionId : new java.util.LinkedHashSet<>(java.util.Arrays.asList(sessionIds))) {
            total += deleteTalkSessionBySessionId(sessionId);
        }
        return total;
    }

    @Override
    @Transactional
    public int deleteTalkSessionBySessionId(Long sessionId) {
        talkSessionMapper.lockTalkSession(sessionId);
        TalkSession existing = requireSession(sessionId);
        TalkPermissionUtils.requireOwnedWrite(existing.getCollegeDeptId(), existing.getCreateBy());
        List<TalkAttachment> attachments = talkAttachmentMapper.selectTalkAttachmentBySessionId(sessionId);
        for (TalkAttachment att : attachments) {
            try {
                AttachmentPathUtils.deleteWithinProfile(profile, att.getFilePath());
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
        java.util.Date talkTime = validateCreateRequest(request);
        Long collegeDeptId = resolveSessionCollege(request.getStudentIds());
        TalkPermissionUtils.requireCollegeAccess(collegeDeptId);
        TalkSession session = new TalkSession();
        session.setCollegeDeptId(collegeDeptId);
        session.setTalkType(request.getTalkType());
        session.setTalkTime(talkTime);
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

    /** Reject malformed requests before the first database write. */
    private java.util.Date validateCreateRequest(TalkSessionCreateRequest request) {
        if (request == null) throw new ServiceException("谈话信息不能为空");
        if (!"individual".equals(request.getTalkType()) && !"group".equals(request.getTalkType())) {
            throw new ServiceException("谈话类型只能为个别谈话或集体谈话");
        }
        String time = request.getTalkTime();
        java.util.Date parsed = null;
        if (time != null && time.matches("\\d{4}-\\d{2}-\\d{2}( \\d{2}:\\d{2}(:\\d{2})?)?")) {
            String format = time.length() == 10 ? "yyyy-MM-dd" : time.length() == 16 ? "yyyy-MM-dd HH:mm" : "yyyy-MM-dd HH:mm:ss";
            java.text.SimpleDateFormat parser = new java.text.SimpleDateFormat(format);
            parser.setLenient(false);
            try { parsed = parser.parse(time); } catch (java.text.ParseException ignored) { }
        }
        if (parsed == null) throw new ServiceException("谈话日期无效，请使用正确的年月日或日期时间");
        List<Long> ids = request.getStudentIds();
        if (ids == null || ids.isEmpty()) throw new ServiceException("谈话对象不能为空");
        java.util.Set<Long> distinct = new java.util.HashSet<>();
        for (Long id : ids) {
            if (id == null || id <= 0) throw new ServiceException("参与学生编号无效");
            if (!distinct.add(id)) throw new ServiceException("参与学生不能重复");
        }
        if ("individual".equals(request.getTalkType()) && ids.size() != 1) {
            throw new ServiceException("个别谈话只能选择一名学生");
        }
        if (request.getTalkPerson() == null || request.getTalkPerson().trim().isEmpty()) {
            throw new ServiceException("谈话人不能为空");
        }
        if (request.getStudentDataList() != null) {
            java.util.Set<Long> feedbackStudents = new java.util.HashSet<>();
            for (TalkSessionCreateRequest.StudentDataItem item : request.getStudentDataList()) {
                if (item == null) throw new ServiceException("学生记录不能为空");
                if (!distinct.contains(item.getStudentId())) throw new ServiceException("学生记录必须属于参与学生");
                if (!feedbackStudents.add(item.getStudentId())) throw new ServiceException("学生记录不能重复");
                TalkConstants.validateFollowupStatus(item.getFollowupStatus());
            }
        }
        return parsed;
    }

    @Override
    public List<TalkSessionTag> selectTalkSessionTags(Long sessionId) {
        TalkPermissionUtils.requireCollegeAccess(requireSession(sessionId).getCollegeDeptId());
        return talkSessionTagMapper.selectTalkSessionTagBySessionId(sessionId);
    }

    @Override
    public Map<Long, List<TalkSessionTag>> selectTalkSessionTagsBatch(String sessionIds) {
        java.util.Set<Long> distinctIds = new java.util.LinkedHashSet<>();
        if (sessionIds != null && !sessionIds.trim().isEmpty()) {
            for (String s : sessionIds.split(",", -1)) {
                try {
                    long id = Long.parseLong(s.trim());
                    if (id <= 0) throw new NumberFormatException();
                    distinctIds.add(id);
                } catch (NumberFormatException ignored) {
                    throw new ServiceException("谈话编号必须为有效的正整数");
                }
            }
        }
        List<Long> ids = new ArrayList<>(distinctIds);
        Map<Long, List<TalkSessionTag>> result = new HashMap<>();
        if (ids.isEmpty()) {
            return result;
        }
        List<TalkSession> sessions = talkSessionMapper.selectTalkSessionBySessionIds(ids.toArray(new Long[0]));
        java.util.Set<Long> found = new java.util.HashSet<>();
        if (sessions != null) {
            for (TalkSession session : sessions) {
                TalkPermissionUtils.requireCollegeAccess(session.getCollegeDeptId());
                found.add(session.getSessionId());
            }
        }
        if (!found.containsAll(ids)) throw new ServiceException("谈话会话不存在");
        List<TalkSessionTag> tags = talkSessionTagMapper.selectTagsBySessionIds(ids);
        for (TalkSessionTag tag : tags) {
            result.computeIfAbsent(tag.getSessionId(), k -> new ArrayList<>()).add(tag);
        }
        return result;
    }

    private TalkSession requireSession(Long sessionId)
    {
        TalkSession session = talkSessionMapper.selectTalkSessionBySessionId(sessionId);
        if (session == null)
        {
            throw new ServiceException("谈话会话不存在");
        }
        return session;
    }

    private Long resolveSessionCollege(List<Long> studentIds)
    {
        if (studentIds == null || studentIds.isEmpty())
        {
            throw new ServiceException("谈话对象不能为空");
        }
        Long collegeDeptId = null;
        for (Long studentId : studentIds)
        {
            TalkStudent student = talkStudentMapper.selectTalkStudentByStudentId(studentId);
            if (student == null)
            {
                throw new ServiceException("学生不存在: " + studentId);
            }
            Long current = resolveCollegeDeptId(student.getDeptId());
            if (collegeDeptId == null)
            {
                collegeDeptId = current;
            }
            else if (!collegeDeptId.equals(current))
            {
                throw new ServiceException("同一谈话只能选择同一学院的学生");
            }
        }
        return collegeDeptId;
    }

    private Long resolveCollegeDeptId(Long deptId)
    {
        Long currentId = deptId;
        java.util.Set<Long> visited = new java.util.HashSet<>();
        while (currentId != null && currentId > 0 && visited.add(currentId) && visited.size() <= 32)
        {
            SysDept dept = sysDeptMapper.selectDeptById(currentId);
            if (dept == null)
            {
                break;
            }
            if ("college".equals(dept.getDeptType()))
            {
                return dept.getDeptId();
            }
            currentId = dept.getParentId();
        }
        throw new ServiceException("学生未归属有效学院");
    }
}
