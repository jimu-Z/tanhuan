package com.ruoyi.talk.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.talk.domain.TalkTeacherClass;

public interface TalkTeacherClassMapper {
    /** 查某教师管理的所有班级 */
    List<TalkTeacherClass> selectByTeacherCode(@Param("teacherCode") String teacherCode);

    /** 查某班级的所有教师 */
    List<TalkTeacherClass> selectByClassName(@Param("className") String className);

    /** 批量插入 */
    int batchInsert(@Param("list") List<TalkTeacherClass> list);

    /** 删除某教师的所有班级关联 */
    int deleteByTeacherCode(@Param("teacherCode") String teacherCode);

    /** 删除某班级的所有教师关联 */
    int deleteByClassName(@Param("className") String className);
}
