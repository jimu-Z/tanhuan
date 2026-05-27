package com.ruoyi.conversation.mapper;

import java.util.List;
import com.ruoyi.conversation.domain.Major;

public interface MajorMapper {
    public List<Major> selectMajorList(Major major);

    public Major selectMajorById(Long majorId);

    public Major selectMajorByDeptId(Long deptId);

    public int insertMajor(Major major);

    public int updateMajor(Major major);

    public int deleteMajorById(Long majorId);

    public int deleteMajorByIds(Long[] majorIds);
}