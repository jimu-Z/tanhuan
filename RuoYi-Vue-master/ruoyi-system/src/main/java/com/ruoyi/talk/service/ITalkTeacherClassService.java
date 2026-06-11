package com.ruoyi.talk.service;

import java.util.List;
import com.ruoyi.talk.domain.TalkTeacherClass;

public interface ITalkTeacherClassService {
    List<TalkTeacherClass> selectByTeacherCode(String teacherCode);
    List<TalkTeacherClass> selectByClassName(String className);
    int saveTeacherClasses(String teacherCode, List<String> classNames);
    List<String> getClassNamesByTeacherCode(String teacherCode);
}
