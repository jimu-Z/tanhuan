package com.ruoyi.conversation.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.conversation.domain.Conversation;
import com.ruoyi.conversation.domain.Student;
import com.ruoyi.conversation.mapper.ConversationMapper;
import com.ruoyi.conversation.mapper.StudentMapper;
import com.ruoyi.conversation.service.IConversationService;

@Service
public class ConversationServiceImpl implements IConversationService {
    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Override
    @DataScope(deptAlias = "d")
    public List<Conversation> selectConversationList(Conversation conversation) {
        return conversationMapper.selectConversationList(conversation);
    }

    @Override
    public Conversation selectConversationById(Long conversationId) {
        return conversationMapper.selectConversationById(conversationId);
    }

    @Override
    @Transactional
    public int insertConversation(Conversation conversation, Long[] studentIds) {
        int result = 0;
        for (Long studentId : studentIds) {
            Student student = studentMapper.selectStudentById(studentId);
            if (student != null) {
                Conversation conv = new Conversation();
                conv.setStudentNo(student.getStudentNo());
                conv.setStudentName(student.getStudentName());
                conv.setCollegeName(student.getCollegeName());
                conv.setMajorName(student.getMajorName());
                conv.setClassName(student.getClassName());
                conv.setDeptId(student.getDeptId());
                conv.setConversationTime(conversation.getConversationTime());
                conv.setConversationPlace(conversation.getConversationPlace());
                conv.setSpeaker(conversation.getSpeaker());
                conv.setTopic(conversation.getTopic());
                conv.setContent(conversation.getContent());
                conv.setFollowUpItems(conversation.getFollowUpItems());
                conv.setStatus(conversation.getStatus());
                conv.setCreateBy(SecurityUtils.getUsername());
                result += conversationMapper.insertConversation(conv);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public int updateConversation(Conversation conversation) {
        conversation.setUpdateBy(SecurityUtils.getUsername());
        return conversationMapper.updateConversation(conversation);
    }

    @Override
    @Transactional
    public int deleteConversationByIds(Long[] conversationIds) {
        int result = 0;
        for (Long conversationId : conversationIds) {
            Conversation conversation = conversationMapper.selectConversationById(conversationId);
            if (conversation != null) {
                conversation.setDelFlag("2");
                conversation.setUpdateBy(SecurityUtils.getUsername());
                result += conversationMapper.updateConversation(conversation);
            }
        }
        return result;
    }

    @Override
    public List<Conversation> selectConversationByStudentNo(String studentNo) {
        return conversationMapper.selectConversationByStudentNo(studentNo);
    }

    @Override
    public List<Conversation> selectConversationTimeLine(String studentNo) {
        return conversationMapper.selectConversationTimeLine(studentNo);
    }

    @Override
    @DataScope(deptAlias = "d")
    public List<Conversation> selectMyConversationList(Conversation conversation) {
        return conversationMapper.selectConversationList(conversation);
    }
}