package com.ruoyi.talk.service;

import java.util.List;
import com.ruoyi.talk.domain.TalkStudentRecord;

/**
 * 谈话记录管理Service接口
 * 
 * @author admin
 * @date 2026-05-27
 */
public interface ITalkStudentRecordService {
    /**
     * 通过学号查询学生信息
     * 
     * @param studentCode 学号
     * @return 学生信息
     */
    public com.ruoyi.talk.domain.TalkStudent selectTalkStudentByCode(String studentCode);

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
     * 批量删除谈话记录管理
     * 
     * @param recordIds 需要删除的谈话记录管理主键集合
     * @return 结果
     */
    public int deleteTalkStudentRecordByRecordIds(Long[] recordIds);

    /**
     * 删除谈话记录管理信息
     * 
     * @param recordId 谈话记录管理主键
     * @return 结果
     */
    public int deleteTalkStudentRecordByRecordId(Long recordId);
}
