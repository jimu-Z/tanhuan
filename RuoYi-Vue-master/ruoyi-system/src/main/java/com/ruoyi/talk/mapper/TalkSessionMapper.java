package com.ruoyi.talk.mapper;

import java.util.List;
import com.ruoyi.talk.domain.TalkSession;

/**
 * 谈话会话管理Mapper接口
 * 
 * @author admin
 * @date 2026-05-27
 */
public interface TalkSessionMapper 
{
    /**
     * 查询谈话会话管理
     * 
     * @param sessionId 谈话会话管理主键
     * @return 谈话会话管理
     */
    public TalkSession selectTalkSessionBySessionId(Long sessionId);

    /**
     * 查询谈话会话管理列表
     * 
     * @param talkSession 谈话会话管理
     * @return 谈话会话管理集合
     */
    public List<TalkSession> selectTalkSessionList(TalkSession talkSession);

    /**
     * 新增谈话会话管理
     * 
     * @param talkSession 谈话会话管理
     * @return 结果
     */
    public int insertTalkSession(TalkSession talkSession);

    /**
     * 修改谈话会话管理
     * 
     * @param talkSession 谈话会话管理
     * @return 结果
     */
    public int updateTalkSession(TalkSession talkSession);

    /**
     * 删除谈话会话管理
     * 
     * @param sessionId 谈话会话管理主键
     * @return 结果
     */
    public int deleteTalkSessionBySessionId(Long sessionId);

    /**
     * 批量删除谈话会话管理
     * 
     * @param sessionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTalkSessionBySessionIds(Long[] sessionIds);

    public int countTalkSessions();
    public List<java.util.HashMap<String, Object>> countTalkSessionsByMonth();
    public List<java.util.HashMap<String, Object>> countTalkSessionsByType();
}
