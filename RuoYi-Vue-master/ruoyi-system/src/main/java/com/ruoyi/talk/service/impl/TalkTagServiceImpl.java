package com.ruoyi.talk.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.talk.domain.TalkTag;
import com.ruoyi.talk.mapper.TalkTagMapper;
import com.ruoyi.talk.service.ITalkTagService;

@Service
public class TalkTagServiceImpl implements ITalkTagService
{
    @Autowired
    private TalkTagMapper talkTagMapper;

    @Override
    public List<TalkTag> selectTalkTagList(TalkTag tag)
    {
        return talkTagMapper.selectTalkTagList(tag);
    }

    @Override
    public TalkTag selectTalkTagById(Long tagId)
    {
        return talkTagMapper.selectTalkTagById(tagId);
    }

    @Override
    public List<TalkTag> selectActiveTags()
    {
        return talkTagMapper.selectActiveTags();
    }

    @Override
    public int insertTalkTag(TalkTag tag)
    {
        tag.setCreateBy(SecurityUtils.getUsername());
        tag.setCreateTime(DateUtils.getNowDate());
        return talkTagMapper.insertTalkTag(tag);
    }

    @Override
    public int updateTalkTag(TalkTag tag)
    {
        tag.setUpdateBy(SecurityUtils.getUsername());
        tag.setUpdateTime(DateUtils.getNowDate());
        return talkTagMapper.updateTalkTag(tag);
    }

    @Override
    public int deleteTalkTagByIds(Long[] tagIds)
    {
        int result = 0;
        for (Long tagId : tagIds)
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
}
