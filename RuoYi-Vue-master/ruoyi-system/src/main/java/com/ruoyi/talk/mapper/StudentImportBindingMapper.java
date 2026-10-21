package com.ruoyi.talk.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.talk.domain.TalkTeacher;

/** ID-based student/teacher and class/teacher bindings created by student import. */
public interface StudentImportBindingMapper {
    int bindStudent(@Param("studentId") Long studentId, @Param("teacherId") Long teacherId);
    int bindClass(@Param("classDeptId") Long classDeptId, @Param("teacherId") Long teacherId);
    int bindLegacyClass(@Param("teacherCode") String teacherCode, @Param("className") String className);
    List<TalkTeacher> selectStudentManagers(@Param("studentId") Long studentId);
    Map<String,Object> selectAccountOwner(@Param("userId") Long userId);
    Map<String,Object> selectAccountByStudentId(@Param("studentId") Long studentId);
    int renameAccountOwner(@Param("userId") Long userId, @Param("studentId") Long studentId,
                          @Param("studentCode") String studentCode, @Param("collegeId") Long collegeId);
    int renameStudentUsername(@Param("userId") Long userId, @Param("studentId") Long studentId, @Param("studentCode") String studentCode);
    int bindAccount(@Param("studentCode") String studentCode, @Param("userId") Long userId,
                    @Param("studentId") Long studentId, @Param("collegeId") Long collegeId);
    int updateAccountOwner(@Param("studentCode") String studentCode, @Param("userId") Long userId,
                          @Param("studentId") Long studentId, @Param("collegeId") Long collegeId);
    int materializeLegacyClassBindings(@Param("teacherId") Long teacherId);
    int bindingExists(@Param("studentId") Long studentId, @Param("classDeptId") Long classDeptId, @Param("teacherId") Long teacherId);
}
