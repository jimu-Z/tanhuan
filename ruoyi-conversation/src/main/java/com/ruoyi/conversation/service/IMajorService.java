package com.ruoyi.conversation.service;

import java.util.List;
import com.ruoyi.conversation.domain.Major;

public interface IMajorService {
    public List<Major> selectMajorList(Major major);

    public Major selectMajorById(Long majorId);

    public List<Major> selectMajorByDeptId(Long deptId);

    public int insertMajor(Major major);

    public int updateMajor(Major major);

    public int deleteMajorByIds(Long[] majorIds);
}