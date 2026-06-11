package com.ruoyi.talk.service;

import java.util.List;
import com.ruoyi.talk.domain.TalkTeacher;

/**
 * 教师信息Service接口
 *
 * @author admin
 * @date 2026-06-06
 */
public interface ITalkTeacherService {

    TalkTeacher selectTalkTeacherById(Long teacherId);

    List<TalkTeacher> selectTalkTeacherList(TalkTeacher teacher);

    /**
     * 新增教师，同时自动创建 sys_user（账号=工号，密码=123456）
     * 并分配角色（辅导员/班主任→talk_counselor，副书记/书记→talk_secretary）
     * 如果学院不存在则自动创建
     */
    int insertTalkTeacher(TalkTeacher teacher);

    int updateTalkTeacher(TalkTeacher teacher);

    int deleteTalkTeacherByIds(Long[] teacherIds);

    /**
     * 校验工号唯一性
     */
    boolean checkTeacherCodeUnique(TalkTeacher teacher);

    /**
     * 通过学生deptId上溯学院，在该学院下按姓名查找辅导员
     * @param studentDeptId 学生所在班级ID
     * @param teacherName 辅导员姓名
     * @return 匹配的教师，找不到返回null
     */
    TalkTeacher findCounselorByStudentDeptAndName(Long studentDeptId, String teacherName);

    /**
     * 获取指定学院下的辅导员/班主任列表（供书记新增和预约选择用）
     */
    List<TalkTeacher> selectCounselorsByDeptId(Long deptId);

    String importTeacher(List<TalkTeacher> teacherList, boolean updateSupport);

    /** 获取全校所有班级名 */
    List<String> selectAllClassNames();
}
