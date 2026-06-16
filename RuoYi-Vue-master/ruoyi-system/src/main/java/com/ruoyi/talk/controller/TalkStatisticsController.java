package com.ruoyi.talk.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.constant.TalkConstants;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.mapper.TalkSessionTagMapper;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;

import com.ruoyi.talk.domain.TalkStudentRecord;

@RestController
@RequestMapping("/ruoyi-system/talk/statistics")
public class TalkStatisticsController extends BaseController {

    @Autowired
    private TalkSessionMapper talkSessionMapper;
    @Autowired
    private TalkStudentMapper talkStudentMapper;
    @Autowired
    private TalkStudentRecordMapper talkStudentRecordMapper;
    @Autowired
    private TalkSessionTagMapper talkSessionTagMapper;
    @Autowired
    private SysDeptMapper sysDeptMapper;

    private Map<String, Object> createQueryParams() {
        Map<String, Object> params = new HashMap<>();
        if (SecurityUtils.isAdmin()) {
            return params;
        }
        String username = SecurityUtils.getUsername();
        if (username == null) {
            return params;
        }
        if (SecurityUtils.hasRole("talk_counselor")) {
            params.put("counselorUsername", username);
        } else if (SecurityUtils.hasRole("talk_secretary")) {
            Long deptId = SecurityUtils.getDeptId();
            if (deptId != null) {
                params.put("secretaryDeptId", deptId);
            }
        }
        return params;
    }

    private Map<String, Object> buildOverview() {
        Map<String, Object> params = createQueryParams();

        int totalStudents = talkStudentMapper.countTalkStudentsFiltered(params);

        int totalSessions = talkSessionMapper.countTalkSessionsFiltered(params);

        int totalRecords = talkStudentRecordMapper.countRecordsFiltered(params);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalStudents", totalStudents);
        data.put("totalSessions", totalSessions);
        data.put("totalRecords", totalRecords);

        List<HashMap<String, Object>> typeStats = talkSessionMapper.countTalkSessionsByTypeFiltered(params);
        long individualCount = 0, groupCount = 0;
        for (HashMap<String, Object> row : typeStats) {
            if ("individual".equals(row.get("type")))
                individualCount = ((Number) row.get("cnt")).longValue();
            if ("group".equals(row.get("type")))
                groupCount = ((Number) row.get("cnt")).longValue();
        }
        data.put("individualCount", individualCount);
        data.put("groupCount", groupCount);

        return data;
    }

    private List<Map<String, Object>> buildTagChart() {
        Map<String, Object> params = createQueryParams();
        List<HashMap<String, Object>> tagStats = talkSessionTagMapper.countTagsByValueFiltered(params);
        List<Map<String, Object>> tagChart = new ArrayList<>();
        for (String key : TalkConstants.TAG_LABELS.keySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", TalkConstants.TAG_LABELS.get(key));
            long cnt = 0;
            for (HashMap<String, Object> row : tagStats) {
                if (key.equals(row.get("tag"))) {
                    cnt = ((Number) row.get("cnt")).longValue();
                    break;
                }
            }
            item.put("value", cnt);
            tagChart.add(item);
        }
        return tagChart;
    }

    private List<Map<String, Object>> buildMonthlyChart() {
        Map<String, Object> params = createQueryParams();
        List<HashMap<String, Object>> monthlyStats = talkSessionMapper.countTalkSessionsByMonthFiltered(params);
        List<Map<String, Object>> monthlyChart = new ArrayList<>();
        for (HashMap<String, Object> row : monthlyStats) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", row.get("month").toString());
            item.put("count", ((Number) row.get("cnt")).longValue());
            monthlyChart.add(item);
        }
        return monthlyChart;
    }

    @PreAuthorize("@ss.hasPermi('talk:dashboard:view')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard() {
        Map<String, Object> data = buildOverview();

        int totalStudents = (int) data.get("totalStudents");
        int totalRecords = (int) data.get("totalRecords");
        data.put("avgRecordsPerStudent", totalStudents > 0
                ? String.format("%.1f", (double) totalRecords / totalStudents)
                : "0");
        data.put("coverageRate", totalStudents > 0
                ? String.format("%.1f", Math.min(100.0, 100.0 * totalRecords / Math.max(1, totalStudents)))
                : "0");

        Map<String, Object> scopeParams = createQueryParams();
        int pendingFeedback = talkStudentRecordMapper.countPendingFeedback(scopeParams);
        data.put("pendingFeedback", pendingFeedback);

        data.put("tagDistribution", buildTagChart());
        data.put("monthlyTrend", buildMonthlyChart());

        List<SysDept> depts = sysDeptMapper.selectDeptList(new SysDept());
        List<Map<String, Object>> collegeRanking = new ArrayList<>();
        int totalColleges = 0;
        // 数据权限过滤：书记只看本学院，辅导员不看排名（返回空）
        boolean isCounselor = SecurityUtils.hasRole("talk_counselor");
        Long secretaryDeptId = scopeParams.containsKey("secretaryDeptId")
                ? ((Number) scopeParams.get("secretaryDeptId")).longValue()
                : null;
        for (SysDept d : depts) {
            if (!"college".equals(d.getDeptType()))
                continue;
            totalColleges++;
            // 书记只能看到自己学院的数据
            if (secretaryDeptId != null && !secretaryDeptId.equals(d.getDeptId()))
                continue;
            // 辅导员不展示学院排名（权限不足）
            if (isCounselor && secretaryDeptId == null && !SecurityUtils.isAdmin())
                continue;
            int count = talkStudentMapper.countStudentsByDeptId(d.getDeptId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", d.getDeptName());
            item.put("count", count);
            collegeRanking.add(item);
        }
        data.put("collegeRanking", collegeRanking);
        data.put("totalColleges", totalColleges);

        // 工作提醒数据
        Map<String, Object> todoParams = createQueryParams();
        int untalkedCount = talkStudentMapper.countUntalkedStudentsFiltered(todoParams);
        int pendingFeedbackCount = talkStudentRecordMapper.countPendingFeedback(todoParams);
        List<HashMap<String, Object>> followupStats = talkStudentRecordMapper
                .countRecordsByFollowupStatusFiltered(todoParams);
        long pendingFollowup = 0;
        for (HashMap<String, Object> row : followupStats) {
            if ("pending".equals(row.get("status"))) {
                pendingFollowup = ((Number) row.get("cnt")).longValue();
            }
        }
        List<Map<String, Object>> todoItems = new ArrayList<>();
        if (pendingFeedbackCount > 0) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "feedback");
            item.put("label", "待处理反馈");
            item.put("count", pendingFeedbackCount);
            item.put("icon", "el-icon-chat-dot-round");
            item.put("color", "#f56c6c");
            todoItems.add(item);
        }
        if (pendingFollowup > 0) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "followup");
            item.put("label", "待跟进记录");
            item.put("count", pendingFollowup);
            item.put("icon", "el-icon-warning-outline");
            item.put("color", "#e6a23c");
            todoItems.add(item);
        }
        if (untalkedCount > 0) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "untalked");
            item.put("label", "未谈话学生");
            item.put("count", untalkedCount);
            item.put("icon", "el-icon-user");
            item.put("color", "#409eff");
            todoItems.add(item);
        }
        data.put("todoItems", todoItems);

        // 预警概览数据
        List<HashMap<String, Object>> mentalStats = talkStudentMapper.countByMentalHealthFiltered(todoParams);
        List<HashMap<String, Object>> povertyStats = talkStudentMapper.countByPovertyLevelFiltered(todoParams);
        List<HashMap<String, Object>> enrollmentStats = talkStudentMapper.countByEnrollmentStatusFiltered(todoParams);
        List<Map<String, Object>> alertItems = new ArrayList<>();
        long mentalTotal = 0;
        for (HashMap<String, Object> row : mentalStats) {
            mentalTotal += ((Number) row.get("cnt")).longValue();
        }
        if (mentalTotal > 0) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "mental");
            item.put("label", "心理健康关注");
            item.put("count", mentalTotal);
            item.put("detail", mentalStats);
            item.put("color", "#f56c6c");
            alertItems.add(item);
        }
        long povertyTotal = 0;
        for (HashMap<String, Object> row : povertyStats) {
            povertyTotal += ((Number) row.get("cnt")).longValue();
        }
        if (povertyTotal > 0) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "poverty");
            item.put("label", "贫困生关注");
            item.put("count", povertyTotal);
            item.put("detail", povertyStats);
            item.put("color", "#e6a23c");
            alertItems.add(item);
        }
        long enrollmentTotal = 0;
        for (HashMap<String, Object> row : enrollmentStats) {
            enrollmentTotal += ((Number) row.get("cnt")).longValue();
        }
        if (enrollmentTotal > 0) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "enrollment");
            item.put("label", "学籍异常");
            item.put("count", enrollmentTotal);
            item.put("detail", enrollmentStats);
            item.put("color", "#909399");
            alertItems.add(item);
        }
        data.put("alertItems", alertItems);

        // 最近谈话动态
        List<TalkStudentRecord> recentRecords = talkStudentRecordMapper.selectRecentRecordsFiltered(todoParams);
        List<Map<String, Object>> recentActivities = new ArrayList<>();
        for (TalkStudentRecord r : recentRecords) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("studentName", r.getStudentName());
            item.put("talkType", r.getTalkType());
            item.put("talkTime", r.getTalkTime());
            item.put("talkPerson", r.getTalkPerson());
            item.put("followupStatus", r.getFollowupStatus());
            recentActivities.add(item);
        }
        data.put("recentActivities", recentActivities);

        return success(data);
    }

    @PreAuthorize("@ss.hasPermi('talk:dashboard:view')")
    @GetMapping("/charts")
    public AjaxResult charts() {
        return dashboard();
    }

    @PreAuthorize("@ss.hasPermi('talk:alert:view')")
    @GetMapping("/alerts")
    public AjaxResult alerts() {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> scopeParams = createQueryParams();

        List<HashMap<String, Object>> followupStats = talkStudentRecordMapper
                .countRecordsByFollowupStatusFiltered(scopeParams);
        long pending = 0, inProgress = 0, completed = 0, none = 0;
        for (HashMap<String, Object> row : followupStats) {
            String status = (String) row.get("status");
            long cnt = ((Number) row.get("cnt")).longValue();
            if ("pending".equals(status))
                pending = cnt;
            else if ("in_progress".equals(status))
                inProgress = cnt;
            else if ("completed".equals(status))
                completed = cnt;
            else if ("none".equals(status))
                none = cnt;
        }
        data.put("pendingFollowups", pending);
        data.put("inProgressFollowups", inProgress);
        data.put("completedFollowups", completed);
        data.put("noneFollowups", none);
        data.put("totalAlerts", pending + inProgress);

        List<SysDept> depts = sysDeptMapper.selectDeptList(new SysDept());
        List<Map<String, Object>> deptCoverage = new ArrayList<>();
        int totalStudents = talkStudentMapper.countTalkStudentsFiltered(scopeParams);
        // 数据权限过滤：只展示权限范围内的部门
        boolean isCounselor = SecurityUtils.hasRole("talk_counselor");
        Long secretaryDeptId = scopeParams.containsKey("secretaryDeptId")
                ? ((Number) scopeParams.get("secretaryDeptId")).longValue()
                : null;
        for (SysDept d : depts) {
            if (!"college".equals(d.getDeptType()) && !"class".equals(d.getDeptType()))
                continue;
            // 书记只能看到本学院及下属部门
            if (secretaryDeptId != null) {
                boolean isDescendant = String.valueOf(secretaryDeptId).equals(d.getAncestors())
                        || d.getAncestors() != null && d.getAncestors().contains(String.valueOf(secretaryDeptId))
                        || secretaryDeptId.equals(d.getDeptId());
                if (!isDescendant)
                    continue;
            }
            // 辅导员不展示部门覆盖率（权限不足）
            if (isCounselor && secretaryDeptId == null && !SecurityUtils.isAdmin())
                continue;
            int count = talkStudentMapper.countStudentsByDeptId(d.getDeptId());
            if (count > 0) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("deptName", d.getDeptName());
                item.put("studentCount", count);
                item.put("percentage", totalStudents > 0
                        ? String.format("%.1f", 100.0 * count / totalStudents)
                        : "0");
                deptCoverage.add(item);
            }
        }
        data.put("deptCoverage", deptCoverage);

        return success(data);
    }

    @PreAuthorize("@ss.hasPermi('talk:bigscreen:view')")
    @GetMapping("/bigscreen")
    public AjaxResult bigscreen() {
        return dashboard();
    }
}