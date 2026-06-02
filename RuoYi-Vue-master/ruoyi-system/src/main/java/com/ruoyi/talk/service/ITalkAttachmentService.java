package com.ruoyi.talk.service;

import java.util.List;
import com.ruoyi.talk.domain.TalkAttachment;

public interface ITalkAttachmentService
{
    public List<TalkAttachment> selectTalkAttachmentBySessionId(Long sessionId);
    public TalkAttachment selectTalkAttachmentById(Long attachmentId);
    public int insertTalkAttachment(TalkAttachment attachment);
    public int deleteTalkAttachmentById(Long attachmentId);
    public int deleteTalkAttachmentBySessionId(Long sessionId);
    public int deleteTalkAttachmentByIds(Long[] attachmentIds);
}
