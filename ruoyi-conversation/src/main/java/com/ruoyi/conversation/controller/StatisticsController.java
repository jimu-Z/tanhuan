package com.ruoyi.conversation.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.conversation.service.IStatisticsService;

@RestController
@RequestMapping("/conversation/statistics")
public class StatisticsController extends BaseController {
    @Autowired
    private IStatisticsService statisticsService;

    @GetMapping("/college")
    public AjaxResult college() {
        List<Map<String, Object>> list = statisticsService.selectCollegeStatistics();
        return success(list);
    }

    @GetMapping("/counselor")
    public AjaxResult counselor() {
        List<Map<String, Object>> list = statisticsService.selectCounselorStatistics();
        return success(list);
    }

    @GetMapping("/statusSummary")
    public AjaxResult statusSummary() {
        List<Map<String, Object>> list = statisticsService.selectStatusSummary();
        return success(list);
    }

    @GetMapping("/pending")
    public AjaxResult pending() {
        List<Map<String, Object>> list = statisticsService.selectPendingList();
        return success(list);
    }
}