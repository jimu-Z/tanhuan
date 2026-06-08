package com.ruoyi.talk.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
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
     * 根据学号查询学生（不经过DataScope）
     */
    public TalkStudent selectTalkStudentByCode(@Param("studentCode") String studentCode);

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

    /**
     * 查询学生列表（含上次谈话时间）
     */
    public List<TalkStudent> selectTalkStudentListWithLastTalk(TalkStudent student);

    /**
     * 查询指定时间段内未被谈话的学生
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deptId 可选部门ID过滤
     */
    public List<TalkStudent> selectUntalkedStudentsInPeriod(@Param("startTime") String startTime, @Param("endTime") String endTime, @Param("deptId") Long deptId);

    public Long selectMinAvailableStudentId();
    public int countRecordsByStudentId(Long studentId);
}
