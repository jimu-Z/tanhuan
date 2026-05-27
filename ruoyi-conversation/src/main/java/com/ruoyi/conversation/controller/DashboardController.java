package com.ruoyi.conversation.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.conversation.domain.Conversation;
import com.ruoyi.conversation.mapper.ConversationMapper;
import com.ruoyi.conversation.service.IStatisticsService;

@RestController
@RequestMapping("/conversation/dashboard")
public class DashboardController extends BaseController {
    @Autowired
    private IStatisticsService statisticsService;

    @Autowired
    private ConversationMapper conversationMapper;

    @GetMapping("/data")
    public AjaxResult data() {
        return success(statisticsService.selectDashboardSummary());
    }

    @GetMapping("/recent")
    public AjaxResult recent() {
        List<Conversation> recentList = conversationMapper.selectRecentConversations();
        return success(recentList);
    }

    @GetMapping("/summary")
    public AjaxResult summary() {
        Map<String, Object> data = statisticsService.selectDashboardSummary();
        if (data == null) {
            data = new HashMap<>();
            data.put("pendingCount", 0);
            data.put("thisMonthCount", 0);
            data.put("recentConversations", new ArrayList<>());
            data.put("totalStudentCount", 0);
        }
        return success(data);
    }
}