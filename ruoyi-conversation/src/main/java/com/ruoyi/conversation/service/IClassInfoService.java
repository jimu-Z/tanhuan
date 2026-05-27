package com.ruoyi.conversation.service;

import java.util.List;
import com.ruoyi.conversation.domain.ClassInfo;

public interface IClassInfoService {
    public List<ClassInfo> selectClassList(ClassInfo classInfo);

    public ClassInfo selectClassById(Long classId);

    public List<ClassInfo> selectClassByMajorId(Long majorId);

    public int insertClassInfo(ClassInfo classInfo);

    public int updateClassInfo(ClassInfo classInfo);

    public int deleteClassInfoByIds(Long[] classIds);
}