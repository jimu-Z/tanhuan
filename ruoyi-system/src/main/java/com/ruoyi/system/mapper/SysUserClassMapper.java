package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysUserClass;

public interface SysUserClassMapper {
    public List<Long> selectClassIdsByUserId(Long userId);

    public int batchUserClass(List<SysUserClass> userClassList);

    public int deleteUserClassByUserId(Long userId);
}