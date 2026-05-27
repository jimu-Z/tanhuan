package com.ruoyi.conversation.service;

import java.util.List;
import java.util.Map;

public interface IStatisticsService {
    public List<Map<String, Object>> selectCollegeStatistics();

    public List<Map<String, Object>> selectCounselorStatistics();

    public List<Map<String, Object>> selectStatusSummary();

    public List<Map<String, Object>> selectPendingList();

    public Map<String, Object> selectDashboardSummary();
}