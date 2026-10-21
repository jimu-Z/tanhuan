package com.ruoyi.talk.service.impl;

import java.util.List;
import com.ruoyi.talk.utils.AttachmentPathUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.talk.domain.TalkAttachment;
import com.ruoyi.talk.mapper.TalkAttachmentMapper;
import com.ruoyi.talk.service.ITalkAttachmentService;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TalkAttachmentServiceImpl implements ITalkAttachmentService
{
    private static final Logger log = LoggerFactory.getLogger(TalkAttachmentServiceImpl.class);

    @Autowired
    private TalkAttachmentMapper talkAttachmentMapper;

    @Autowired
    private TalkSessionMapper talkSessionMapper;

    @Value("${ruoyi.profile}")
    private String profile;

    @Override
    public boolean isRegisteredResource(String resourcePath)
    {
        try {
            java.nio.file.Path requested = AttachmentPathUtils.resourceIdentity(profile, resourcePath);
            for (TalkAttachment attachment : talkAttachmentMapper.selectTalkAttachmentList(new TalkAttachment())) {
                try {
                    if (requested.equals(AttachmentPathUtils.resourceIdentity(profile, attachment.getFilePath()))) return true;
                } catch (java.io.IOException | IllegalArgumentException ignored) {
                    // Invalid historical metadata never authorizes access to a different file.
                }
            }
            return false;
        } catch (java.io.IOException | IllegalArgumentException error) {
            // Invalid or escaping paths fail closed even on legacy resource endpoints.
            return true;
        }
    }

    @Override
    public List<TalkAttachment> selectTalkAttachmentBySessionId(Long sessionId)
    {
        requireSession(sessionId, false);
        return talkAttachmentMapper.selectTalkAttachmentBySessionId(sessionId);
    }

    @Override
    public TalkAttachment selectTalkAttachmentById(Long attachmentId)
    {
        TalkAttachment attachment = talkAttachmentMapper.selectTalkAttachmentById(attachmentId);
        if (attachment != null) {
            requireSession(attachment.getSessionId(), false);
        }
        return attachment;
    }

    @Override
    public int insertTalkAttachment(TalkAttachment attachment)
    {
        requireSession(attachment.getSessionId(), true);
        attachment.setCreateBy(SecurityUtils.getUsername());
        attachment.setCreateTime(DateUtils.getNowDate());
        return talkAttachmentMapper.insertTalkAttachment(attachment);
    }

    @Override
    public void checkWriteAccess(Long sessionId)
    {
        requireSession(sessionId, true);
    }

    @Override
    public int deleteTalkAttachmentById(Long attachmentId)
    {
        TalkAttachment attachment = talkAttachmentMapper.selectTalkAttachmentById(attachmentId);
        if (attachment == null) {
            return 0;
        }
        requireSession(attachment.getSessionId(), true);
        deletePhysicalFile(attachment);
        return talkAttachmentMapper.deleteTalkAttachmentById(attachmentId);
    }

    @Override
    public int deleteTalkAttachmentBySessionId(Long sessionId)
    {
        requireSession(sessionId, true);
        for (TalkAttachment attachment : talkAttachmentMapper.selectTalkAttachmentBySessionId(sessionId)) {
            deletePhysicalFile(attachment);
        }
        return talkAttachmentMapper.deleteTalkAttachmentBySessionId(sessionId);
    }

    @Override
    @Transactional
    public int deleteTalkAttachmentByIds(Long[] attachmentIds)
    {
        for (Long id : attachmentIds) {
            TalkAttachment attachment = talkAttachmentMapper.selectTalkAttachmentById(id);
            if (attachment != null) requireSession(attachment.getSessionId(), true);
        }
        int result = 0;
        for (Long id : new java.util.LinkedHashSet<>(java.util.Arrays.asList(attachmentIds)))
        {
            result += deleteTalkAttachmentById(id);
        }
        return result;
    }

    private TalkSession requireSession(Long sessionId, boolean write)
    {
        TalkSession session = talkSessionMapper.selectTalkSessionBySessionId(sessionId);
        if (session == null) {
            throw new ServiceException("谈话会话不存在");
        }
        if (write) {
            TalkPermissionUtils.requireOwnedWrite(session.getCollegeDeptId(), session.getCreateBy());
        } else {
            TalkPermissionUtils.requireCollegeAccess(session.getCollegeDeptId());
        }
        return session;
    }

    private void deletePhysicalFile(TalkAttachment attachment)
    {
        if (attachment.getFilePath() == null) {
            return;
        }
        try {
            AttachmentPathUtils.deleteWithinProfile(profile, attachment.getFilePath());
        } catch (Exception e) {
            log.warn("删除附件文件异常: {}", e.getMessage());
        }
    }
}
