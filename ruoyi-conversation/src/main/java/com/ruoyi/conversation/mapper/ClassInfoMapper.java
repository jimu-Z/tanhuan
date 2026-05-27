package com.ruoyi.conversation.mapper;

import java.util.List;
import com.ruoyi.conversation.domain.ClassInfo;

public interface ClassInfoMapper {
    public List<ClassInfo> selectClassList(ClassInfo classInfo);

    public ClassInfo selectClassById(Long classId);

    public List<ClassInfo> selectClassByMajorId(Long majorId);

    public int insertClassInfo(ClassInfo classInfo);

    public int updateClassInfo(ClassInfo classInfo);

    public int deleteClassInfoById(Long classId);

    public int deleteClassInfoByIds(Long[] classIds);
}