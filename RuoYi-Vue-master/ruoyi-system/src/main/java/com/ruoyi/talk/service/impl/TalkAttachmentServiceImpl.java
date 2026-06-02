package com.ruoyi.talk.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.talk.domain.TalkAttachment;
import com.ruoyi.talk.mapper.TalkAttachmentMapper;
import com.ruoyi.talk.service.ITalkAttachmentService;

@Service
public class TalkAttachmentServiceImpl implements ITalkAttachmentService
{
    @Autowired
    private TalkAttachmentMapper talkAttachmentMapper;

    @Override
    public List<TalkAttachment> selectTalkAttachmentBySessionId(Long sessionId)
    {
        return talkAttachmentMapper.selectTalkAttachmentBySessionId(sessionId);
    }

    @Override
    public TalkAttachment selectTalkAttachmentById(Long attachmentId)
    {
        return talkAttachmentMapper.selectTalkAttachmentById(attachmentId);
    }

    @Override
    public int insertTalkAttachment(TalkAttachment attachment)
    {
        attachment.setCreateBy(SecurityUtils.getUsername());
        attachment.setCreateTime(DateUtils.getNowDate());
        return talkAttachmentMapper.insertTalkAttachment(attachment);
    }

    @Override
    public int deleteTalkAttachmentById(Long attachmentId)
    {
        return talkAttachmentMapper.deleteTalkAttachmentById(attachmentId);
    }

    @Override
    public int deleteTalkAttachmentBySessionId(Long sessionId)
    {
        return talkAttachmentMapper.deleteTalkAttachmentBySessionId(sessionId);
    }

    @Override
    public int deleteTalkAttachmentByIds(Long[] attachmentIds)
    {
        int result = 0;
        for (Long id : attachmentIds)
        {
            result += talkAttachmentMapper.deleteTalkAttachmentById(id);
        }
        return result;
    }
}
