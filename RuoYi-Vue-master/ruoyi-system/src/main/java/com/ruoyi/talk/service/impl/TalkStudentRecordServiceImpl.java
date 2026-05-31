package com.ruoyi.talk.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;
import com.ruoyi.talk.domain.TalkStudentRecord;
import com.ruoyi.talk.service.ITalkStudentRecordService;

/**
 * 谈话记录管理Service业务层处理
 * 
 * @author admin
 * @date 2026-05-27
 */
@Service
public class TalkStudentRecordServiceImpl implements ITalkStudentRecordService 
{
    @Autowired
    private TalkStudentRecordMapper talkStudentRecordMapper;

    @Override
    public TalkStudentRecord selectTalkStudentRecordByRecordId(Long recordId)
    {
        return talkStudentRecordMapper.selectTalkStudentRecordByRecordId(recordId);
    }

    @Override
    public List<TalkStudentRecord> selectTalkStudentRecordList(TalkStudentRecord talkStudentRecord)
    {
        applyDataScopeFilter(talkStudentRecord);
        return talkStudentRecordMapper.selectTalkStudentRecordList(talkStudentRecord);
    }

    private void applyDataScopeFilter(TalkStudentRecord talkStudentRecord)
    {
        if (SecurityUtils.isAdmin())
        {
            return;
        }
        String username = SecurityUtils.getUsername();
        if (username == null)
        {
            return;
        }
        if (talkStudentRecord.getParams() == null)
        {
            talkStudentRecord.setParams(new java.util.HashMap<>());
        }
        if (SecurityUtils.hasRole("talk_counselor"))
        {
            talkStudentRecord.getParams().put("dataScope",
                    " and tsr.session_id in (select ts.session_id from talk_session ts where ts.create_by = '"
                    + username.replace("'", "''") + "')");
        }
        else if (SecurityUtils.hasRole("talk_secretary"))
        {
            Long deptId = SecurityUtils.getDeptId();
            if (deptId != null)
            {
                talkStudentRecord.getParams().put("dataScope",
                        " and tsr.student_id in (select stu.student_id from talk_student stu" +
                        " join sys_dept d on stu.dept_id = d.dept_id" +
                        " where d.dept_id = " + deptId + " or find_in_set(" + deptId + ", d.ancestors))");
            }
        }
    }

    @Override
    public int insertTalkStudentRecord(TalkStudentRecord talkStudentRecord)
    {
        talkStudentRecord.setCreateTime(DateUtils.getNowDate());
        return talkStudentRecordMapper.insertTalkStudentRecord(talkStudentRecord);
    }

    @Override
    public int updateTalkStudentRecord(TalkStudentRecord talkStudentRecord)
    {
        talkStudentRecord.setUpdateTime(DateUtils.getNowDate());
        return talkStudentRecordMapper.updateTalkStudentRecord(talkStudentRecord);
    }

    @Override
    public int deleteTalkStudentRecordByRecordIds(Long[] recordIds)
    {
        return talkStudentRecordMapper.deleteTalkStudentRecordByRecordIds(recordIds);
    }

    @Override
    public int deleteTalkStudentRecordByRecordId(Long recordId)
    {
        return talkStudentRecordMapper.deleteTalkStudentRecordByRecordId(recordId);
    }
}
