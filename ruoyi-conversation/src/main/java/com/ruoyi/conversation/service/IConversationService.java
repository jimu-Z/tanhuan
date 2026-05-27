package com.ruoyi.conversation.service;

import java.util.List;
import com.ruoyi.conversation.domain.Conversation;

public interface IConversationService {
    public List<Conversation> selectConversationList(Conversation conversation);

    public Conversation selectConversationById(Long conversationId);

    public int insertConversation(Conversation conversation, Long[] studentIds);

    public int updateConversation(Conversation conversation);

    public int deleteConversationByIds(Long[] conversationIds);

    public List<Conversation> selectConversationByStudentNo(String studentNo);

    public List<Conversation> selectConversationTimeLine(String studentNo);

    public List<Conversation> selectMyConversationList(Conversation conversation);
}