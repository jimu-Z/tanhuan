package com.ruoyi.talk.mapper;

import java.util.List;
import com.ruoyi.talk.domain.TalkAttachment;

public interface TalkAttachmentMapper
{
    public List<TalkAttachment> selectTalkAttachmentList(TalkAttachment attachment);
    public List<TalkAttachment> selectTalkAttachmentBySessionId(Long sessionId);
    public TalkAttachment selectTalkAttachmentById(Long attachmentId);
    public int insertTalkAttachment(TalkAttachment attachment);
    public int deleteTalkAttachmentById(Long attachmentId);
    public int deleteTalkAttachmentBySessionId(Long sessionId);
}
