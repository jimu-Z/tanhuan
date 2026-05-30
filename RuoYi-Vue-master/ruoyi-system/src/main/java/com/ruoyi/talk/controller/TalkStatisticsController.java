package com.ruoyi.talk.controller;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.mapper.TalkSessionTagMapper;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;

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

    private static final Map<String, String> TAG_LABELS = new LinkedHashMap<>();
    static {
        TAG_LABELS.put("thought_education", "思想理论教育和价值引领");
        TAG_LABELS.put("party_class", "党团和班级建设");
        TAG_LABELS.put("study_style", "学风建设");
        TAG_LABELS.put("daily_affairs", "日常事务");
        TAG_LABELS.put("mental_health", "心理健康教育与咨询");
        TAG_LABELS.put("crisis_response", "危机事件应对");
        TAG_LABELS.put("career_guidance", "职业规划与就业创业指导");
    }

    @PreAuthorize("@ss.hasPermi('talk:dashboard:view')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();

        int totalStudents = talkStudentMapper.countTalkStudents();
        int totalSessions = talkSessionMapper.countTalkSessions();
        int totalRecords = talkStudentRecordMapper.countRecords();

        data.put("totalStudents", totalStudents);
        data.put("totalSessions", totalSessions);
        data.put("totalRecords", totalRecords);
        data.put("avgRecordsPerStudent", totalStudents > 0
                ? String.format("%.1f", (double) totalRecords / totalStudents) : "0");

        List<HashMap<String, Object>> typeStats = talkSessionMapper.countTalkSessionsByType();
        long individualCount = 0, groupCount = 0;
        for (HashMap<String, Object> row : typeStats) {
            if ("individual".equals(row.get("type"))) individualCount = ((Number) row.get("cnt")).longValue();
            if ("group".equals(row.get("type"))) groupCount = ((Number) row.get("cnt")).longValue();
        }
        data.put("individualCount", individualCount);
        data.put("groupCount", groupCount);

        return success(data);
    }

    @PreAuthorize("@ss.hasPermi('talk:dashboard:view')")
    @GetMapping("/charts")
    public AjaxResult charts() {
        Map<String, Object> data = new LinkedHashMap<>();

        List<HashMap<String, Object>> tagStats = talkSessionTagMapper.countTagsByValue();
        List<Map<String, Object>> tagChart = new ArrayList<>();
        for (String key : TAG_LABELS.keySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", TAG_LABELS.get(key));
            long cnt = 0;
            for (HashMap<String, Object> row : tagStats) {
                if (key.equals(row.get("tag"))) { cnt = ((Number) row.get("cnt")).longValue(); break; }
            }
            item.put("value", cnt);
            tagChart.add(item);
        }
        data.put("tagDistribution", tagChart);

        List<HashMap<String, Object>> monthlyStats = talkSessionMapper.countTalkSessionsByMonth();
        List<Map<String, Object>> monthlyChart = new ArrayList<>();
        for (HashMap<String, Object> row : monthlyStats) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", row.get("month").toString());
            item.put("count", ((Number) row.get("cnt")).longValue());
            monthlyChart.add(item);
        }
        data.put("monthlyTrend", monthlyChart);

        return success(data);
    }

    @PreAuthorize("@ss.hasPermi('talk:alert:view')")
    @GetMapping("/alerts")
    public AjaxResult alerts() {
        Map<String, Object> data = new LinkedHashMap<>();

        List<HashMap<String, Object>> followupStats = talkStudentRecordMapper.countRecordsByFollowupStatus();
        long pending = 0, inProgress = 0;
        for (HashMap<String, Object> row : followupStats) {
            if ("pending".equals(row.get("status"))) pending = ((Number) row.get("cnt")).longValue();
            if ("in_progress".equals(row.get("status"))) inProgress = ((Number) row.get("cnt")).longValue();
        }
        data.put("pendingFollowups", pending);
        data.put("inProgressFollowups", inProgress);
        data.put("totalAlerts", pending + inProgress);

        List<SysDept> depts = sysDeptMapper.selectDeptList(new SysDept());
        List<Map<String, Object>> deptCoverage = new ArrayList<>();
        int totalStudents = talkStudentMapper.countTalkStudents();
        for (SysDept d : depts) {
            if (!"college".equals(d.getDeptType()) && !"class".equals(d.getDeptType())) continue;
            int count = talkStudentMapper.countStudentsByDeptId(d.getDeptId());
            if (count > 0) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("deptName", d.getDeptName());
                item.put("studentCount", count);
                item.put("percentage", totalStudents > 0
                        ? String.format("%.1f", 100.0 * count / totalStudents) : "0");
                deptCoverage.add(item);
            }
        }
        data.put("deptCoverage", deptCoverage);

        return success(data);
    }

    @PreAuthorize("@ss.hasPermi('talk:bigscreen:view')")
    @GetMapping("/bigscreen")
    public AjaxResult bigscreen() {
        Map<String, Object> data = new LinkedHashMap<>();

        int totalStudents = talkStudentMapper.countTalkStudents();
        int totalSessions = talkSessionMapper.countTalkSessions();
        int totalRecords = talkStudentRecordMapper.countRecords();
        data.put("totalStudents", totalStudents);
        data.put("totalSessions", totalSessions);
        data.put("totalRecords", totalRecords);

        List<HashMap<String, Object>> typeStats = talkSessionMapper.countTalkSessionsByType();
        long individualCount = 0, groupCount = 0;
        for (HashMap<String, Object> row : typeStats) {
            if ("individual".equals(row.get("type"))) individualCount = ((Number) row.get("cnt")).longValue();
            if ("group".equals(row.get("type"))) groupCount = ((Number) row.get("cnt")).longValue();
        }
        data.put("individualCount", individualCount);
        data.put("groupCount", groupCount);
        data.put("coverageRate", totalStudents > 0
                ? String.format("%.1f", Math.min(100.0, 100.0 * totalRecords / Math.max(1, totalStudents))) : "0");

        List<HashMap<String, Object>> monthlyStats = talkSessionMapper.countTalkSessionsByMonth();
        List<Map<String, Object>> monthlyChart = new ArrayList<>();
        for (HashMap<String, Object> row : monthlyStats) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", row.get("month").toString());
            item.put("count", ((Number) row.get("cnt")).longValue());
            monthlyChart.add(item);
        }
        data.put("monthlyTrend", monthlyChart);

        List<HashMap<String, Object>> tagStats = talkSessionTagMapper.countTagsByValue();
        List<Map<String, Object>> tagChart = new ArrayList<>();
        for (String key : TAG_LABELS.keySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", TAG_LABELS.get(key));
            long cnt = 0;
            for (HashMap<String, Object> row : tagStats) {
                if (key.equals(row.get("tag"))) { cnt = ((Number) row.get("cnt")).longValue(); break; }
            }
            item.put("value", cnt);
            tagChart.add(item);
        }
        data.put("tagDistribution", tagChart);

        List<SysDept> depts = sysDeptMapper.selectDeptList(new SysDept());
        List<Map<String, Object>> collegeRanking = new ArrayList<>();
        for (SysDept d : depts) {
            if (!"college".equals(d.getDeptType())) continue;
            int count = talkStudentMapper.countStudentsByDeptId(d.getDeptId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", d.getDeptName());
            item.put("count", count);
            collegeRanking.add(item);
        }
        collegeRanking.sort((a, b) -> Integer.compare(
                ((Number) b.get("count")).intValue(), ((Number) a.get("count")).intValue()));
        data.put("collegeRanking", collegeRanking);

        return success(data);
    }
}