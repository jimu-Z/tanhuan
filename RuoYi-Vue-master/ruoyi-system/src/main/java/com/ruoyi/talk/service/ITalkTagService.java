package com.ruoyi.talk.service;

import java.util.List;
import com.ruoyi.talk.domain.TalkTag;

public interface ITalkTagService
{
    public List<TalkTag> selectTalkTagList(TalkTag tag);
    public TalkTag selectTalkTagById(Long tagId);
    public List<TalkTag> selectActiveTags();
    public int insertTalkTag(TalkTag tag);
    public int updateTalkTag(TalkTag tag);
    public int deleteTalkTagByIds(Long[] tagIds);
}
