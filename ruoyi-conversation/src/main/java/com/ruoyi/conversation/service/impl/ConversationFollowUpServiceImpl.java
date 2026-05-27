package com.ruoyi.conversation.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.conversation.domain.Conversation;
import com.ruoyi.conversation.domain.ConversationFollowUp;
import com.ruoyi.conversation.mapper.ConversationFollowUpMapper;
import com.ruoyi.conversation.mapper.ConversationMapper;
import com.ruoyi.conversation.service.IConversationFollowUpService;

@Service
public class ConversationFollowUpServiceImpl implements IConversationFollowUpService {
    @Autowired
    private ConversationFollowUpMapper followUpMapper;

    @Autowired
    private ConversationMapper conversationMapper;

    @Override
    public List<ConversationFollowUp> selectFollowUpList(ConversationFollowUp followUp) {
        return followUpMapper.selectFollowUpList(followUp);
    }

    @Override
    public ConversationFollowUp selectFollowUpById(Long followId) {
        return followUpMapper.selectFollowUpById(followId);
    }

    @Override
    public List<ConversationFollowUp> selectFollowUpByConversationId(Long conversationId) {
        return followUpMapper.selectFollowUpByConversationId(conversationId);
    }

    @Override
    @Transactional
    public int insertFollowUp(ConversationFollowUp followUp) {
        followUp.setCreateBy(SecurityUtils.getUsername());
        int result = followUpMapper.insertFollowUp(followUp);
        if (result > 0) {
            Conversation conversation = conversationMapper.selectConversationById(followUp.getConversationId());
            if (conversation != null) {
                conversation.setStatus(followUp.getFollowStatus());
                conversation.setUpdateBy(SecurityUtils.getUsername());
                conversationMapper.updateConversation(conversation);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public int updateFollowUp(ConversationFollowUp followUp) {
        followUp.setUpdateBy(SecurityUtils.getUsername());
        return followUpMapper.updateFollowUp(followUp);
    }

    @Override
    @Transactional
    public int deleteFollowUpById(Long followId) {
        return followUpMapper.deleteFollowUpById(followId);
    }
}