package com.ruoyi.conversation.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.conversation.domain.ClassInfo;
import com.ruoyi.conversation.mapper.ClassInfoMapper;
import com.ruoyi.conversation.service.IClassInfoService;

@Service
public class ClassInfoServiceImpl implements IClassInfoService {
    @Autowired
    private ClassInfoMapper classInfoMapper;

    @Override
    public List<ClassInfo> selectClassList(ClassInfo classInfo) {
        return classInfoMapper.selectClassList(classInfo);
    }

    @Override
    public ClassInfo selectClassById(Long classId) {
        return classInfoMapper.selectClassById(classId);
    }

    @Override
    public List<ClassInfo> selectClassByMajorId(Long majorId) {
        return classInfoMapper.selectClassByMajorId(majorId);
    }

    @Override
    @Transactional
    public int insertClassInfo(ClassInfo classInfo) {
        classInfo.setCreateBy(SecurityUtils.getUsername());
        return classInfoMapper.insertClassInfo(classInfo);
    }

    @Override
    @Transactional
    public int updateClassInfo(ClassInfo classInfo) {
        classInfo.setUpdateBy(SecurityUtils.getUsername());
        return classInfoMapper.updateClassInfo(classInfo);
    }

    @Override
    @Transactional
    public int deleteClassInfoByIds(Long[] classIds) {
        return classInfoMapper.deleteClassInfoByIds(classIds);
    }
}