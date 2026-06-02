package com.ruoyi.talk.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.domain.TalkSessionCreateRequest;
import com.ruoyi.talk.domain.TalkSessionTag;

/**
 * 谈话会话管理Service接口
 * 
 * @author admin
 * @date 2026-05-27
 */
public interface ITalkSessionService 
{
    public TalkSession selectTalkSessionBySessionId(Long sessionId);

    public List<TalkSession> selectTalkSessionList(TalkSession talkSession);

    public int insertTalkSession(TalkSession talkSession);

    public int updateTalkSession(TalkSession talkSession);

    public int deleteTalkSessionBySessionIds(Long[] sessionIds);

    public int deleteTalkSessionBySessionId(Long sessionId);

    /**
     * 发起谈话（集体/个体）— 创建 session + N条 record，事务性
     * @param request 包含 session 信息 + studentIds 列表
     * @return 创建的 session
     */
    public TalkSession createTalkWithRecords(TalkSessionCreateRequest request);
    public List<TalkSessionTag> selectTalkSessionTags(Long sessionId);
    public Map<Long, List<TalkSessionTag>> selectTalkSessionTagsBatch(String sessionIds);
}
