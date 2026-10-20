package com.ruoyi.talk.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.talk.domain.TalkTag;
import com.ruoyi.talk.mapper.TalkTagMapper;
import com.ruoyi.talk.service.ITalkTagService;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.common.exception.ServiceException;

@Service
public class TalkTagServiceImpl implements ITalkTagService
{
    @Autowired
    private TalkTagMapper talkTagMapper;

    @Override
    public List<TalkTag> selectTalkTagList(TalkTag tag)
    {
        TalkPermissionUtils.applyCollegeScope(tag);
        return talkTagMapper.selectTalkTagList(tag);
    }

    @Override
    public TalkTag selectTalkTagById(Long tagId)
    {
        TalkTag tag = talkTagMapper.selectTalkTagById(tagId);
        requireRead(tag);
        return tag;
    }

    @Override
    public List<TalkTag> selectActiveTags()
    {
        TalkTag query = new TalkTag();
        TalkPermissionUtils.applyCollegeScope(query);
        return talkTagMapper.selectActiveTags(query);
    }

    @Override
    public int insertTalkTag(TalkTag tag)
    {
        prepareNewScope(tag);
        tag.setCreateBy(SecurityUtils.getUsername());
        tag.setCreateTime(DateUtils.getNowDate());
        return talkTagMapper.insertTalkTag(tag);
    }

    @Override
    public int updateTalkTag(TalkTag tag)
    {
        TalkTag existing = requireExisting(tag.getTagId());
        requireWrite(existing);
        tag.setScopeType(null);
        tag.setCollegeDeptId(null);
        tag.setUpdateBy(SecurityUtils.getUsername());
        tag.setUpdateTime(DateUtils.getNowDate());
        return talkTagMapper.updateTalkTag(tag);
    }

    @Override
    @Transactional
    public int deleteTalkTagByIds(Long[] tagIds)
    {
        java.util.Set<Long> uniqueIds = new java.util.LinkedHashSet<>(java.util.Arrays.asList(tagIds));
        for (Long tagId : uniqueIds) requireWrite(requireExisting(tagId));
        int result = 0;
        for (Long tagId : uniqueIds)
        {
            TalkTag tag = new TalkTag();
            tag.setTagId(tagId);
            tag.setDelFlag("2");
            tag.setUpdateBy(SecurityUtils.getUsername());
            tag.setUpdateTime(DateUtils.getNowDate());
            result += talkTagMapper.updateTalkTag(tag);
        }
        return result;
    }

    private void prepareNewScope(TalkTag tag)
    {
        if (TalkPermissionUtils.isGlobalAdmin())
        {
            if (!"college".equals(tag.getScopeType()))
            {
                tag.setScopeType("system");
                tag.setCollegeDeptId(null);
            }
            else if (tag.getCollegeDeptId() == null)
            {
                throw new ServiceException("学院标签必须指定所属学院");
            }
            return;
        }
        TalkPermissionUtils.requireCollegeAccess(TalkPermissionUtils.currentCollegeId());
        tag.setScopeType("college");
        tag.setCollegeDeptId(TalkPermissionUtils.currentCollegeId());
    }

    private TalkTag requireExisting(Long tagId)
    {
        TalkTag tag = talkTagMapper.selectTalkTagById(tagId);
        if (tag == null)
        {
            throw new ServiceException("标签不存在");
        }
        return tag;
    }

    private void requireRead(TalkTag tag)
    {
        if (tag == null || "system".equals(tag.getScopeType()) || TalkPermissionUtils.isGlobalAdmin())
        {
            return;
        }
        TalkPermissionUtils.requireCollegeAccess(tag.getCollegeDeptId());
    }

    private void requireWrite(TalkTag tag)
    {
        if ("system".equals(tag.getScopeType()))
        {
            if (!TalkPermissionUtils.canManageSystemDefinitions())
            {
                throw new ServiceException("系统内置标签仅限全校管理员维护");
            }
            return;
        }
        TalkPermissionUtils.requireCollegeAccess(tag.getCollegeDeptId());
    }
}
