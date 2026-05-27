package com.ruoyi.conversation.service;

import java.util.List;
import com.ruoyi.conversation.domain.ConversationFollowUp;

public interface IConversationFollowUpService {
    public List<ConversationFollowUp> selectFollowUpList(ConversationFollowUp followUp);

    public ConversationFollowUp selectFollowUpById(Long followId);

    public List<ConversationFollowUp> selectFollowUpByConversationId(Long conversationId);

    public int insertFollowUp(ConversationFollowUp followUp);

    public int updateFollowUp(ConversationFollowUp followUp);

    public int deleteFollowUpById(Long followId);
}