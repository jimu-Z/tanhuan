package com.ruoyi.talk.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.talk.domain.TalkSessionTag;

public interface TalkSessionTagMapper {

    public int insertTalkSessionTag(TalkSessionTag tag);

    public int deleteTalkSessionTagBySessionId(Long sessionId);

    public List<TalkSessionTag> selectTalkSessionTagBySessionId(Long sessionId);

    public List<java.util.HashMap<String, Object>> countTagsByValue();
    public List<java.util.HashMap<String, Object>> countTagsByValueFiltered(Map<String, Object> params);

    public List<TalkSessionTag> selectTagsBySessionIds(List<Long> sessionIds);
}
