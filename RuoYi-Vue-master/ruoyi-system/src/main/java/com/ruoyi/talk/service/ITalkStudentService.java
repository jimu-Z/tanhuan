package com.ruoyi.talk.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.talk.domain.TalkStudent;

/**
 * 学生信息管理Service接口
 * 
 * @author admin
 * @date 2026-05-27
 */
public interface ITalkStudentService {
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
     * 批量删除学生信息管理
     * 
     * @param studentIds 需要删除的学生信息管理主键集合
     * @return 结果
     */
    public int deleteTalkStudentByStudentIds(Long[] studentIds);

    /**
     * 删除学生信息管理信息
     * 
     * @param studentId 学生信息管理主键
     * @return 结果
     */
    public int deleteTalkStudentByStudentId(Long studentId);

    /**
     * Excel导入预览
     * 
     * @param file Excel文件
     * @return 预览结果
     */
    public Map<String, Object> importPreview(MultipartFile file);

    /**
     * 执行Excel导入
     * 
     * @param confirmedRows 确认导入的行
     * @param importMode    重复处理模式: skip 或 update
     * @return 导入结果
     */
    public Map<String, Object> importExecute(List<Map<String, Object>> confirmedRows, String importMode);

    public Map<String, Object> getStudentDetail(Long studentId);

    public int countStudentsByDeptId(Long deptId);

    public List<TalkStudent> selectUntalkedStudents(Map<String, Object> params);

    /**
     * 查询学生列表（含上次谈话时间）
     */
    public List<TalkStudent> selectTalkStudentListWithLastTalk(TalkStudent talkStudent);

    /**
     * 查询指定时间段内未被谈话的学生
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deptId 可选部门ID过滤
     * @return 学生集合
     */
    public List<TalkStudent> selectUntalkedStudentsInPeriod(Date startTime, Date endTime, Long deptId);

    /**
     * 按教师工号查询学生列表（通过班级关联）
     */
    public List<TalkStudent> selectByTeacherCode(String teacherCode);

    /**
     * 按学院ID查询学生（含子部门：年级+班级）
     */
    public List<TalkStudent> selectByCollegeDeptId(Long deptId);
}
