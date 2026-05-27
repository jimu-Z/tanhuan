package com.ruoyi.conversation.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.conversation.domain.Conversation;
import com.ruoyi.conversation.mapper.ConversationMapper;
import com.ruoyi.conversation.mapper.StudentMapper;
import com.ruoyi.conversation.service.IStatisticsService;

@Service
public class StatisticsServiceImpl implements IStatisticsService {
    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public List<Map<String, Object>> selectCollegeStatistics() {
        List<Map<String, Object>> result = new ArrayList<>();
        return result;
    }

    @Override
    public List<Map<String, Object>> selectCounselorStatistics() {
        List<Map<String, Object>> result = new ArrayList<>();
        return result;
    }

    @Override
    public List<Map<String, Object>> selectStatusSummary() {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> row0 = new HashMap<>();
        row0.put("status", "0");
        row0.put("statusName", "未跟进");
        row0.put("count", conversationMapper.countPendingConversations());
        result.add(row0);
        Map<String, Object> row1 = new HashMap<>();
        row1.put("status", "1");
        row1.put("statusName", "已跟进");
        row1.put("count", 0L);
        result.add(row1);
        Map<String, Object> row2 = new HashMap<>();
        row2.put("status", "2");
        row2.put("statusName", "已完成");
        row2.put("count", conversationMapper.countCompletedConversations());
        result.add(row2);
        return result;
    }

    @Override
    public List<Map<String, Object>> selectPendingList() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Conversation> pendingList = conversationMapper.selectPendingConversations();
        for (Conversation c : pendingList) {
            Map<String, Object> item = new HashMap<>();
            item.put("studentName", c.getStudentName());
            item.put("studentNo", c.getStudentNo());
            item.put("topic", c.getTopic());
            item.put("conversationTime", c.getConversationTime());
            item.put("speaker", c.getSpeaker());
            item.put("status", c.getStatus());
            result.add(item);
        }
        return result;
    }

    @Override
    public Map<String, Object> selectDashboardSummary() {
        Map<String, Object> result = new HashMap<>();
        result.put("pendingCount", conversationMapper.countPendingConversations());
        result.put("thisMonthCount", conversationMapper.countThisMonthConversations());
        result.put("totalStudentCount", studentMapper.countTotalStudents());
        result.put("completedCount", conversationMapper.countCompletedConversations());
        result.put("followedCount", 0L);
        result.put("thisWeekCount", 0L);
        List<Conversation> pendingList = conversationMapper.selectPendingConversations();
        result.put("pendingList", convertToMapList(pendingList));
        return result;
    }

    private List<Map<String, Object>> convertToMapList(List<Conversation> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (list != null) {
            for (Conversation c : list) {
                Map<String, Object> item = new HashMap<>();
                item.put("studentName", c.getStudentName());
                item.put("studentNo", c.getStudentNo());
                item.put("topic", c.getTopic());
                item.put("conversationTime", c.getConversationTime());
                item.put("collegeName", c.getCollegeName());
                item.put("speaker", c.getSpeaker());
                item.put("status", c.getStatus());
                result.add(item);
            }
        }
        return result;
    }
}