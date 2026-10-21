package com.ruoyi.talk.service.impl;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.domain.TalkStudentRecord;
import com.ruoyi.talk.service.ITalkStudentRecordService;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.common.core.domain.entity.SysDept;

/**
 * 谈话记录管理Service业务层处理
 * 
 * @author admin
 * @date 2026-05-27
 */
@Service
public class TalkStudentRecordServiceImpl implements ITalkStudentRecordService {
    @Autowired
    private TalkStudentRecordMapper talkStudentRecordMapper;

    @Autowired
    private TalkStudentMapper talkStudentMapper;

    @Autowired
    private TalkSessionMapper talkSessionMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    public TalkStudent selectTalkStudentByCode(String studentCode) {
        TalkStudent query = new TalkStudent();
        query.setStudentCode(studentCode);
        TalkPermissionUtils.applyCollegeScope(query);
        List<TalkStudent> list = talkStudentMapper.selectTalkStudentList(query);
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    @Override
    public TalkStudentRecord selectTalkStudentRecordByRecordId(Long recordId) {
        TalkStudentRecord record = talkStudentRecordMapper.selectTalkStudentRecordByRecordId(recordId);
        requireRecordAccess(record, false);
        return record;
    }

    @Override
    public List<TalkStudentRecord> selectTalkStudentRecordList(TalkStudentRecord talkStudentRecord) {
        TalkPermissionUtils.applyCollegeScope(talkStudentRecord);
        List<TalkStudentRecord> list = talkStudentRecordMapper.selectTalkStudentRecordList(talkStudentRecord);
        return list;
    }

    @Override
    @Transactional
    public int insertTalkStudentRecord(TalkStudentRecord talkStudentRecord) {
        talkSessionMapper.lockTalkSession(talkStudentRecord.getSessionId());
        TalkSession session = requireSessionWrite(talkStudentRecord.getSessionId());
        List<TalkStudentRecord> current = talkStudentRecordMapper.selectRecordsForUpdate(session.getSessionId());
        if (current.stream().anyMatch(row -> java.util.Objects.equals(row.getStudentId(), talkStudentRecord.getStudentId())))
            throw new ServiceException("参与学生不能重复");
        if ("individual".equals(session.getTalkType()) && !current.isEmpty())
            throw new ServiceException("个别谈话只能选择一名学生");
        TalkStudent student = talkStudentRecord.getStudentId() == null ? null
                : talkStudentMapper.selectTalkStudentByStudentId(talkStudentRecord.getStudentId());
        if (student == null) {
            throw new ServiceException("学生不存在");
        }
        Long deptId = student.getDeptId();
        java.util.Set<Long> visited = new java.util.HashSet<>();
        Long collegeId = null;
        while (deptId != null && visited.add(deptId) && visited.size() <= 32) {
            SysDept dept = sysDeptMapper.selectDeptById(deptId);
            if (dept == null) break;
            if ("college".equals(dept.getDeptType())) { collegeId = dept.getDeptId(); break; }
            deptId = dept.getParentId();
        }
        if (collegeId == null || !collegeId.equals(session.getCollegeDeptId())) {
            throw new ServiceException("学生和谈话必须归属同一学院");
        }
        com.ruoyi.talk.constant.TalkConstants.validateFollowupStatus(talkStudentRecord.getFollowupStatus());
        talkStudentRecord.setCreateTime(DateUtils.getNowDate());
        return talkStudentRecordMapper.insertTalkStudentRecord(talkStudentRecord);
    }

    @Override
    public int updateTalkStudentRecord(TalkStudentRecord talkStudentRecord) {
        TalkStudentRecord existing = talkStudentRecordMapper.selectTalkStudentRecordByRecordId(talkStudentRecord.getRecordId());
        requireRecordAccess(existing, true);
        if ((talkStudentRecord.getSessionId() != null && !talkStudentRecord.getSessionId().equals(existing.getSessionId()))
                || (talkStudentRecord.getStudentId() != null && !talkStudentRecord.getStudentId().equals(existing.getStudentId()))) {
            throw new ServiceException("已有谈话记录不能变更会话或学生");
        }
        com.ruoyi.talk.constant.TalkConstants.validateFollowupStatus(talkStudentRecord.getFollowupStatus());
        talkStudentRecord.setUpdateTime(DateUtils.getNowDate());
        return talkStudentRecordMapper.updateTalkStudentRecord(talkStudentRecord);
    }

    @Override
    @Transactional
    public int deleteTalkStudentRecordByRecordIds(Long[] recordIds) {
        if (recordIds == null || recordIds.length == 0) throw new ServiceException("请选择谈话记录");
        java.util.Set<Long> ids = new java.util.LinkedHashSet<>(java.util.Arrays.asList(recordIds));
        java.util.Set<Long> sessionIds = new java.util.TreeSet<>();
        for (Long id : ids) {
            TalkStudentRecord record = talkStudentRecordMapper.selectTalkStudentRecordByRecordId(id);
            requireRecordAccess(record, true);
            sessionIds.add(record.getSessionId());
        }
        for (Long sessionId : sessionIds) talkSessionMapper.lockTalkSession(sessionId);
        // Re-read after the parent locks: concurrent writers may have removed records while we waited.
        for (Long id : ids) requireRecordAccess(talkStudentRecordMapper.selectTalkStudentRecordByRecordId(id), true);
        for (Long sessionId : sessionIds) {
            List<TalkStudentRecord> current = talkStudentRecordMapper.selectRecordsForUpdate(sessionId);
            if (current.stream().noneMatch(record -> !ids.contains(record.getRecordId())))
                throw new ServiceException("谈话至少保留一名学生，请删除整个谈话会话");
        }
        return talkStudentRecordMapper.deleteTalkStudentRecordByRecordIds(ids.toArray(new Long[0]));
    }

    @Override
    @Transactional
    public int deleteTalkStudentRecordByRecordId(Long recordId) {
        return deleteTalkStudentRecordByRecordIds(new Long[]{recordId});
    }

    private void requireRecordAccess(TalkStudentRecord record, boolean write)
    {
        if (record == null)
        {
            throw new ServiceException("谈话记录不存在");
        }
        TalkSession session = talkSessionMapper.selectTalkSessionBySessionId(record.getSessionId());
        if (session == null)
        {
            throw new ServiceException("谈话会话不存在");
        }
        if (write)
        {
            TalkPermissionUtils.requireOwnedWrite(session.getCollegeDeptId(), session.getCreateBy());
        }
        else
        {
            TalkPermissionUtils.requireCollegeAccess(session.getCollegeDeptId());
        }
    }

    private TalkSession requireSessionWrite(Long sessionId)
    {
        TalkSession session = talkSessionMapper.selectTalkSessionBySessionId(sessionId);
        if (session == null)
        {
            throw new ServiceException("谈话会话不存在");
        }
        TalkPermissionUtils.requireOwnedWrite(session.getCollegeDeptId(), session.getCreateBy());
        return session;
    }
}
