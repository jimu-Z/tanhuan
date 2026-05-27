package com.ruoyi.conversation.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.conversation.domain.Major;
import com.ruoyi.conversation.mapper.MajorMapper;
import com.ruoyi.conversation.service.IMajorService;

@Service
public class MajorServiceImpl implements IMajorService {
    @Autowired
    private MajorMapper majorMapper;

    @Override
    @DataScope(deptAlias = "d")
    public List<Major> selectMajorList(Major major) {
        return majorMapper.selectMajorList(major);
    }

    public List<Major> selectMajorListInner(Major major) {
        return SpringUtils.getAopProxy(this).selectMajorList(major);
    }

    @Override
    public Major selectMajorById(Long majorId) {
        return majorMapper.selectMajorById(majorId);
    }

    @Override
    public List<Major> selectMajorByDeptId(Long deptId) {
        return majorMapper.selectMajorList(new Major());
    }

    @Override
    @Transactional
    public int insertMajor(Major major) {
        major.setCreateBy(SecurityUtils.getUsername());
        return majorMapper.insertMajor(major);
    }

    @Override
    @Transactional
    public int updateMajor(Major major) {
        major.setUpdateBy(SecurityUtils.getUsername());
        return majorMapper.updateMajor(major);
    }

    @Override
    @Transactional
    public int deleteMajorByIds(Long[] majorIds) {
        return majorMapper.deleteMajorByIds(majorIds);
    }
}