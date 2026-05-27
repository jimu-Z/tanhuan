package com.ruoyi.conversation.mapper;

import java.util.List;
import com.ruoyi.conversation.domain.Conversation;

public interface ConversationMapper {
    public List<Conversation> selectConversationList(Conversation conversation);

    public Conversation selectConversationById(Long conversationId);

    public int insertConversation(Conversation conversation);

    public int updateConversation(Conversation conversation);

    public int deleteConversationById(Long conversationId);

    public int deleteConversationByIds(Long[] conversationIds);

    public List<Conversation> selectConversationByStudentNo(String studentNo);

    public List<Conversation> selectConversationTimeLine(String studentNo);

    public Long countPendingConversations();

    public Long countThisMonthConversations();

    public Long countCompletedConversations();

    public List<Conversation> selectRecentConversations();

    public List<Conversation> selectPendingConversations();
}