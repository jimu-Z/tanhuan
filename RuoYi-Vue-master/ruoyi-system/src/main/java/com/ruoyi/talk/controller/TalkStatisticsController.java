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
            params.put("dataScope", null);
            return params;
        }
        String username = SecurityUtils.getUsername();
        if (username == null) {
            params.put("dataScope", null);
            return params;
        }
        if (SecurityUtils.hasRole("talk_counselor")) {
            String userScope = "ts.create_by = '" + username.replace("'", "''") + "'";
            params.put("dataScope", userScope);
            params.put("sessionScope", userScope);
            params.put("studentScope", "s.student_id in (select tsr.student_id from talk_student_record tsr join talk_session ts on tsr.session_id = ts.session_id where ts.create_by = '" + username.replace("'", "''") + "')");
            params.put("recordScope", "tsr.session_id in (select ts.session_id from talk_session ts where ts.create_by = '" + username.replace("'", "''") + "')");
        } else if (SecurityUtils.hasRole("talk_secretary")) {
            Long deptId = SecurityUtils.getDeptId();
            if (deptId != null) {
                String deptScope = "d.dept_id = " + deptId + " or find_in_set(" + deptId + ", d.ancestors)";
                params.put("sessionScope", "ts.session_id in (select tsr.session_id from talk_student_record tsr join talk_student stu on tsr.student_id = stu.student_id join sys_dept d on stu.dept_id = d.dept_id where (" + deptScope + "))");
                params.put("studentScope", "s.dept_id in (select dept_id from sys_dept d where " + deptScope + ")");
                params.put("recordScope", "tsr.session_id in (select ts.session_id from talk_session ts where ts.session_id in (select tsr2.session_id from talk_student_record tsr2 join talk_student stu on tsr2.student_id = stu.student_id join sys_dept d on stu.dept_id = d.dept_id where (" + deptScope + ")))");
            }
        }
        return params;
    }

    private Map<String, Object> buildOverview() {
        Map<String, Object> params = createQueryParams();

        Map<String, Object> studentParams = new HashMap<>();
        studentParams.put("dataScope", params.get("studentScope"));
        int totalStudents = talkStudentMapper.countTalkStudentsFiltered(studentParams);

        Map<String, Object> sessionParams = new HashMap<>();
        sessionParams.put("dataScope", params.get("sessionScope"));
        int totalSessions = talkSessionMapper.countTalkSessionsFiltered(sessionParams);

        Map<String, Object> recordParams = new HashMap<>();
        recordParams.put("dataScope", params.get("recordScope"));
        int totalRecords = talkStudentRecordMapper.countRecordsFiltered(recordParams);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalStudents", totalStudents);
        data.put("totalSessions", totalSessions);
        data.put("totalRecords", totalRecords);

        List<HashMap<String, Object>> typeStats = talkSessionMapper.countTalkSessionsByTypeFiltered(sessionParams);
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
        Map<String, Object> tagParams = new HashMap<>();
        tagParams.put("dataScope", params.get("sessionScope"));
        List<HashMap<String, Object>> tagStats = talkSessionTagMapper.countTagsByValueFiltered(tagParams);
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
        Map<String, Object> sessionParams = new HashMap<>();
        sessionParams.put("dataScope", params.get("sessionScope"));
        List<HashMap<String, Object>> monthlyStats = talkSessionMapper.countTalkSessionsByMonthFiltered(sessionParams);
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
                ? String.format("%.1f", (double) totalRecords / totalStudents) : "0");

        return success(data);
    }

    @PreAuthorize("@ss.hasPermi('talk:dashboard:view')")
    @GetMapping("/charts")
    public AjaxResult charts() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("tagDistribution", buildTagChart());
        data.put("monthlyTrend", buildMonthlyChart());
        return success(data);
    }

    @PreAuthorize("@ss.hasPermi('talk:alert:view')")
    @GetMapping("/alerts")
    public AjaxResult alerts() {
        Map<String, Object> data = new LinkedHashMap<>();
        Map<String, Object> scopeParams = createQueryParams();

        Map<String, Object> recordParams = new HashMap<>();
        recordParams.put("dataScope", scopeParams.get("recordScope"));
        List<HashMap<String, Object>> followupStats = talkStudentRecordMapper.countRecordsByFollowupStatusFiltered(recordParams);
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
        Map<String, Object> studentParams = new HashMap<>();
        studentParams.put("dataScope", scopeParams.get("studentScope"));
        int totalStudents = talkStudentMapper.countTalkStudentsFiltered(studentParams);
        for (SysDept d : depts) {
            if (!"college".equals(d.getDeptType()) && !"class".equals(d.getDeptType()))
                continue;
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
        Map<String, Object> data = buildOverview();

        int totalStudents = (int) data.get("totalStudents");
        int totalRecords = (int) data.get("totalRecords");
        data.put("coverageRate", totalStudents > 0
                ? String.format("%.1f", Math.min(100.0, 100.0 * totalRecords / Math.max(1, totalStudents))) : "0");

        data.put("monthlyTrend", buildMonthlyChart());
        data.put("tagDistribution", buildTagChart());

        List<SysDept> depts = sysDeptMapper.selectDeptList(new SysDept());
        List<Map<String, Object>> collegeRanking = new ArrayList<>();
        for (SysDept d : depts) {
            if (!"college".equals(d.getDeptType()))
                continue;
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