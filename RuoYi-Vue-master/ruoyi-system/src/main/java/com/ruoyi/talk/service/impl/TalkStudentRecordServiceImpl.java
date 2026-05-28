package com.ruoyi.talk.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
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

    /**
     * 查询谈话记录管理
     * 
     * @param recordId 谈话记录管理主键
     * @return 谈话记录管理
     */
    @Override
    public TalkStudentRecord selectTalkStudentRecordByRecordId(Long recordId)
    {
        return talkStudentRecordMapper.selectTalkStudentRecordByRecordId(recordId);
    }

    /**
     * 查询谈话记录管理列表
     * 
     * @param talkStudentRecord 谈话记录管理
     * @return 谈话记录管理
     */
    @Override
    public List<TalkStudentRecord> selectTalkStudentRecordList(TalkStudentRecord talkStudentRecord)
    {
        return talkStudentRecordMapper.selectTalkStudentRecordList(talkStudentRecord);
    }

    /**
     * 新增谈话记录管理
     * 
     * @param talkStudentRecord 谈话记录管理
     * @return 结果
     */
    @Override
    public int insertTalkStudentRecord(TalkStudentRecord talkStudentRecord)
    {
        talkStudentRecord.setCreateTime(DateUtils.getNowDate());
        return talkStudentRecordMapper.insertTalkStudentRecord(talkStudentRecord);
    }

    /**
     * 修改谈话记录管理
     * 
     * @param talkStudentRecord 谈话记录管理
     * @return 结果
     */
    @Override
    public int updateTalkStudentRecord(TalkStudentRecord talkStudentRecord)
    {
        return talkStudentRecordMapper.updateTalkStudentRecord(talkStudentRecord);
    }

    /**
     * 批量删除谈话记录管理
     * 
     * @param recordIds 需要删除的谈话记录管理主键
     * @return 结果
     */
    @Override
    public int deleteTalkStudentRecordByRecordIds(Long[] recordIds)
    {
        return talkStudentRecordMapper.deleteTalkStudentRecordByRecordIds(recordIds);
    }

    /**
     * 删除谈话记录管理信息
     * 
     * @param recordId 谈话记录管理主键
     * @return 结果
     */
    @Override
    public int deleteTalkStudentRecordByRecordId(Long recordId)
    {
        return talkStudentRecordMapper.deleteTalkStudentRecordByRecordId(recordId);
    }
}
