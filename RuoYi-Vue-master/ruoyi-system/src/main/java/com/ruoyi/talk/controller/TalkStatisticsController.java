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
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.constant.TalkConstants;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.mapper.TalkSessionTagMapper;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.talk.service.ITalkTagService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.talk.domain.TalkStudentRecord;
import com.ruoyi.talk.domain.TalkTag;

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
    @Autowired
    private ITalkTagService talkTagService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Map<String, Object> createQueryParams() {
        Map<String, Object> params = new HashMap<>();
        if (!TalkPermissionUtils.isGlobalAdmin()) {
            params.put("collegeDeptId", TalkPermissionUtils.currentCollegeId());
        }
        return params;
    }

    private Map<String, Object> buildOverview() {
        Map<String, Object> params = createQueryParams();

        int totalStudents = talkStudentMapper.countTalkStudentsFiltered(params);

        int totalSessions = talkSessionMapper.countTalkSessionsFiltered(params);

        int totalRecords = talkStudentRecordMapper.countRecordsFiltered(params);
        int coveredStudents = talkStudentRecordMapper.countCoveredStudentsFiltered(params);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalStudents", totalStudents);
        data.put("totalSessions", totalSessions);
        data.put("totalRecords", totalRecords);
        data.put("coveredStudents", coveredStudents);

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
        Map<String, String> labels = new LinkedHashMap<>(TalkConstants.TAG_LABELS);
        for (TalkTag tag : talkTagService.selectActiveTags()) {
            labels.put(tag.getTagKey(), tag.getTagName());
        }
        Map<String, Long> counts = new LinkedHashMap<>();
        for (HashMap<String, Object> row : tagStats) {
            long count = ((Number) row.get("cnt")).longValue();
            for (String key : parseTagKeys(String.valueOf(row.get("tag")))) {
                counts.merge(key, count, Long::sum);
            }
        }
        List<Map<String, Object>> tagChart = new ArrayList<>();
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", entry.getValue());
            Long count = counts.remove(entry.getKey());
            item.put("value", count == null ? 0 : count);
            tagChart.add(item);
        }
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", entry.getKey());
            item.put("value", entry.getValue());
            tagChart.add(item);
        }
        return tagChart;
    }

    private List<String> parseTagKeys(String value) {
        if (value == null || value.isBlank() || "null".equals(value)) {
            return List.of();
        }
        if (value.startsWith("[") && value.endsWith("]")) {
            try {
                return objectMapper.readValue(value, new TypeReference<List<String>>() { });
            } catch (Exception ignored) {
                // 兼容历史非标准 JSON 标签值，按普通字符串继续处理。
            }
        }
        return List.of(value);
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
        int coveredStudents = (int) data.get("coveredStudents");
        data.put("avgRecordsPerStudent", totalStudents > 0
                ? String.format("%.1f", (double) totalRecords / totalStudents)
                : "0");
        data.put("coverageRate", totalStudents > 0
                ? String.format("%.1f", 100.0 * coveredStudents / totalStudents)
                : "0");

        Map<String, Object> scopeParams = createQueryParams();

        data.put("tagDistribution", buildTagChart());
        data.put("monthlyTrend", buildMonthlyChart());

        List<SysDept> depts = sysDeptMapper.selectDeptList(new SysDept());
        List<Map<String, Object>> collegeRanking = new ArrayList<>();
        int totalColleges = 0;
        Long collegeDeptId = scopeParams.containsKey("collegeDeptId")
                ? ((Number) scopeParams.get("collegeDeptId")).longValue()
                : null;
        for (SysDept d : depts) {
            if (!"college".equals(d.getDeptType()))
                continue;
            if (collegeDeptId != null && !collegeDeptId.equals(d.getDeptId()))
                continue;
            totalColleges++;
            int count = talkStudentMapper.countStudentsByDeptId(d.getDeptId());
            Map<String, Object> collegeParams = new HashMap<>();
            collegeParams.put("collegeDeptId", d.getDeptId());
            int coveredCount = talkStudentRecordMapper.countCoveredStudentsFiltered(collegeParams);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", d.getDeptName());
            item.put("count", count);
            item.put("coveredCount", coveredCount);
            item.put("coverageRate", count > 0
                    ? String.format("%.1f", 100.0 * coveredCount / count)
                    : "0");
            collegeRanking.add(item);
        }
        collegeRanking.sort((left, right) -> Integer.compare(
                ((Number) right.get("count")).intValue(), ((Number) left.get("count")).intValue()));
        data.put("collegeRanking", collegeRanking);
        data.put("totalColleges", totalColleges);

        // 当前上线业务仅保留未谈学生提醒。
        int untalkedCount = talkStudentMapper.countUntalkedStudentsFiltered(scopeParams);
        data.put("untalkedStudents", untalkedCount);
        List<Map<String, Object>> todoItems = new ArrayList<>();
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

        // 最近谈话动态
        List<TalkStudentRecord> recentRecords = talkStudentRecordMapper.selectRecentRecordsFiltered(scopeParams);
        List<Map<String, Object>> recentActivities = new ArrayList<>();
        for (TalkStudentRecord r : recentRecords) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("studentName", r.getStudentName());
            item.put("talkType", r.getTalkType());
            item.put("talkTime", r.getTalkTime());
            item.put("talkPerson", r.getTalkPerson());
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
        Long collegeDeptId = scopeParams.containsKey("collegeDeptId")
                ? ((Number) scopeParams.get("collegeDeptId")).longValue()
                : null;
        for (SysDept d : depts) {
            if (!"college".equals(d.getDeptType()) && !"class".equals(d.getDeptType()))
                continue;
            if (collegeDeptId != null) {
                boolean isDescendant = d.getAncestors() != null
                        && java.util.Arrays.asList(d.getAncestors().split(",")).contains(String.valueOf(collegeDeptId))
                        || collegeDeptId.equals(d.getDeptId());
                if (!isDescendant)
                    continue;
            }
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
