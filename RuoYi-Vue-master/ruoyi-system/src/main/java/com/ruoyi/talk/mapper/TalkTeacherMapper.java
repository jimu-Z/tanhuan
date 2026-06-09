package com.ruoyi.talk.mapper;

import java.util.List;
import com.ruoyi.talk.domain.TalkTeacher;

/**
 * 教师信息Mapper接口
 *
 * @author admin
 * @date 2026-06-06
 */
public interface TalkTeacherMapper {

    TalkTeacher selectTalkTeacherById(Long teacherId);

    List<TalkTeacher> selectTalkTeacherList(TalkTeacher teacher);

    int insertTalkTeacher(TalkTeacher teacher);

    int updateTalkTeacher(TalkTeacher teacher);

    int deleteTalkTeacherById(Long teacherId);

    int checkTeacherCodeUnique(String teacherCode);

    /**
     * 查询指定学院下的辅导员/班主任
     */
    List<TalkTeacher> selectCounselorsByDeptId(Long deptId);

    /**
     * 在学院下按姓名查找教师
     */
    TalkTeacher selectByDeptIdAndName(Long deptId, String teacherName);

    /**
     * 检查同学院下是否存在同名但不同岗位的教师（用于兼任判定）
     */
    int countByDeptAndNameExcludePosition(Long deptId, String teacherName, String position);
}
