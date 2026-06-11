package com.ruoyi.talk.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.talk.domain.TalkTeacherClass;
import com.ruoyi.talk.mapper.TalkTeacherClassMapper;
import com.ruoyi.talk.service.ITalkTeacherClassService;

@Service
public class TalkTeacherClassServiceImpl implements ITalkTeacherClassService {

    @Autowired
    private TalkTeacherClassMapper mapper;

    @Override
    public List<TalkTeacherClass> selectByTeacherCode(String teacherCode) {
        return mapper.selectByTeacherCode(teacherCode);
    }

    @Override
    public List<TalkTeacherClass> selectByClassName(String className) {
        return mapper.selectByClassName(className);
    }

    @Override
    @Transactional
    public int saveTeacherClasses(String teacherCode, List<String> classNames) {
        mapper.deleteByTeacherCode(teacherCode);
        if (classNames == null || classNames.isEmpty()) return 0;
        List<TalkTeacherClass> list = classNames.stream().map(cn -> {
            TalkTeacherClass t = new TalkTeacherClass();
            t.setTeacherCode(teacherCode);
            t.setClassName(cn);
            t.setCreateTime(new Date());
            return t;
        }).collect(Collectors.toList());
        return mapper.batchInsert(list);
    }

    @Override
    public List<String> getClassNamesByTeacherCode(String teacherCode) {
        return mapper.selectByTeacherCode(teacherCode).stream()
                .map(TalkTeacherClass::getClassName)
                .collect(Collectors.toList());
    }
}
