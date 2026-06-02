package com.ruoyi.talk.mapper;

import java.util.List;
import com.ruoyi.talk.domain.TalkTag;

public interface TalkTagMapper
{
    public List<TalkTag> selectTalkTagList(TalkTag tag);
    public TalkTag selectTalkTagById(Long tagId);
    public List<TalkTag> selectActiveTags();
    public int insertTalkTag(TalkTag tag);
    public int updateTalkTag(TalkTag tag);
    public int deleteTalkTagById(Long tagId);
}
