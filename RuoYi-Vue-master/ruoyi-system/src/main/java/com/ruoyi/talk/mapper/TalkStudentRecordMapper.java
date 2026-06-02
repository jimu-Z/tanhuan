package com.ruoyi.talk.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.talk.domain.TalkStudentRecord;

/**
 * 谈话记录管理Mapper接口
 * 
 * @author admin
 * @date 2026-05-27
 */
public interface TalkStudentRecordMapper 
{
    /**
     * 查询谈话记录管理
     * 
     * @param recordId 谈话记录管理主键
     * @return 谈话记录管理
     */
    public TalkStudentRecord selectTalkStudentRecordByRecordId(Long recordId);

    /**
     * 查询谈话记录管理列表
     * 
     * @param talkStudentRecord 谈话记录管理
     * @return 谈话记录管理集合
     */
    public List<TalkStudentRecord> selectTalkStudentRecordList(TalkStudentRecord talkStudentRecord);

    /**
     * 新增谈话记录管理
     * 
     * @param talkStudentRecord 谈话记录管理
     * @return 结果
     */
    public int insertTalkStudentRecord(TalkStudentRecord talkStudentRecord);

    /**
     * 修改谈话记录管理
     * 
     * @param talkStudentRecord 谈话记录管理
     * @return 结果
     */
    public int updateTalkStudentRecord(TalkStudentRecord talkStudentRecord);

    /**
     * 删除谈话记录管理
     * 
     * @param recordId 谈话记录管理主键
     * @return 结果
     */
    public int deleteTalkStudentRecordByRecordId(Long recordId);

    /**
     * 批量删除谈话记录管理
     * 
     * @param recordIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTalkStudentRecordByRecordIds(Long[] recordIds);

    public int deleteTalkStudentRecordBySessionId(Long sessionId);

    public int deleteTalkStudentRecordByStudentId(Long studentId);

    public List<TalkStudentRecord> selectTalkStudentRecordBySessionId(Long sessionId);

    public List<TalkStudentRecord> selectTalkStudentRecordByStudentId(Long studentId);

    public int countRecords();
    public List<java.util.HashMap<String, Object>> countRecordsByFollowupStatus();
    public int countRecordsBySessionId(Long sessionId);
    public int countRecordsFiltered(Map<String, Object> params);
    public List<java.util.HashMap<String, Object>> countRecordsByFollowupStatusFiltered(Map<String, Object> params);
    public int countPendingFeedback(Map<String, Object> params);
}
