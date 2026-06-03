package com.ruoyi.talk.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.talk.domain.TalkStudent;

/**
 * 学生信息管理Mapper接口
 * 
 * @author admin
 * @date 2026-05-27
 */
public interface TalkStudentMapper 
{
    /**
     * 查询学生信息管理
     * 
     * @param studentId 学生信息管理主键
     * @return 学生信息管理
     */
    public TalkStudent selectTalkStudentByStudentId(Long studentId);

    /**
     * 查询学生信息管理列表
     * 
     * @param talkStudent 学生信息管理
     * @return 学生信息管理集合
     */
    public List<TalkStudent> selectTalkStudentList(TalkStudent talkStudent);

    /**
     * 新增学生信息管理
     * 
     * @param talkStudent 学生信息管理
     * @return 结果
     */
    public int insertTalkStudent(TalkStudent talkStudent);

    /**
     * 修改学生信息管理
     * 
     * @param talkStudent 学生信息管理
     * @return 结果
     */
    public int updateTalkStudent(TalkStudent talkStudent);

    /**
     * 删除学生信息管理
     * 
     * @param studentId 学生信息管理主键
     * @return 结果
     */
    public int deleteTalkStudentByStudentId(Long studentId);

    /**
     * 批量删除学生信息管理
     * 
     * @param studentIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTalkStudentByStudentIds(Long[] studentIds);

    public int countTalkStudents();
    public int countStudentsByDeptId(Long deptId);
    public int countTalkStudentsFiltered(Map<String, Object> params);
    public List<TalkStudent> selectUntalkedStudents(Map<String, Object> params);
    public Long selectMinAvailableStudentId();
    public int countRecordsByStudentId(Long studentId);
}
