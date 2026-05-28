package com.ruoyi.talk.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.service.ITalkSessionService;

/**
 * 谈话会话管理Service业务层处理
 * 
 * @author admin
 * @date 2026-05-27
 */
@Service
public class TalkSessionServiceImpl implements ITalkSessionService 
{
    @Autowired
    private TalkSessionMapper talkSessionMapper;

    /**
     * 查询谈话会话管理
     * 
     * @param sessionId 谈话会话管理主键
     * @return 谈话会话管理
     */
    @Override
    public TalkSession selectTalkSessionBySessionId(Long sessionId)
    {
        return talkSessionMapper.selectTalkSessionBySessionId(sessionId);
    }

    /**
     * 查询谈话会话管理列表
     * 
     * @param talkSession 谈话会话管理
     * @return 谈话会话管理
     */
    @Override
    public List<TalkSession> selectTalkSessionList(TalkSession talkSession)
    {
        return talkSessionMapper.selectTalkSessionList(talkSession);
    }

    /**
     * 新增谈话会话管理
     * 
     * @param talkSession 谈话会话管理
     * @return 结果
     */
    @Override
    public int insertTalkSession(TalkSession talkSession)
    {
        talkSession.setCreateTime(DateUtils.getNowDate());
        return talkSessionMapper.insertTalkSession(talkSession);
    }

    /**
     * 修改谈话会话管理
     * 
     * @param talkSession 谈话会话管理
     * @return 结果
     */
    @Override
    public int updateTalkSession(TalkSession talkSession)
    {
        talkSession.setUpdateTime(DateUtils.getNowDate());
        return talkSessionMapper.updateTalkSession(talkSession);
    }

    /**
     * 批量删除谈话会话管理
     * 
     * @param sessionIds 需要删除的谈话会话管理主键
     * @return 结果
     */
    @Override
    public int deleteTalkSessionBySessionIds(Long[] sessionIds)
    {
        return talkSessionMapper.deleteTalkSessionBySessionIds(sessionIds);
    }

    /**
     * 删除谈话会话管理信息
     * 
     * @param sessionId 谈话会话管理主键
     * @return 结果
     */
    @Override
    public int deleteTalkSessionBySessionId(Long sessionId)
    {
        return talkSessionMapper.deleteTalkSessionBySessionId(sessionId);
    }
}
