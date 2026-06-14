package com.ruoyi.talk.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.talk.domain.TalkAlert;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.mapper.TalkAlertMapper;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.service.ITalkAlertService;
import com.ruoyi.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/talk/alert")
public class TalkAlertController extends BaseController {
    @Autowired
    private ITalkAlertService talkAlertService;
    @Autowired
    private TalkAlertMapper talkAlertMapper;
    @Autowired
    private TalkStudentMapper talkStudentMapper;

    @PreAuthorize("@ss.hasPermi('talk:alert:list')")
    @GetMapping("/list")
    public TableDataInfo list(TalkAlert talkAlert) {
        startPage();
        List<TalkAlert> list = talkAlertService.selectTalkAlertList(talkAlert);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('talk:alert:query')")
    @GetMapping(value = "/{alertId}")
    public AjaxResult getInfo(@PathVariable Long alertId) {
        return success(talkAlertService.selectTalkAlertById(alertId));
    }

    @PreAuthorize("@ss.hasPermi('talk:alert:add')")
    @Log(title = "心理健康预警管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TalkAlert talkAlert) {
        return toAjax(talkAlertService.insertTalkAlert(talkAlert));
    }

    @PreAuthorize("@ss.hasPermi('talk:alert:edit')")
    @Log(title = "心理健康预警管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TalkAlert talkAlert) {
        return toAjax(talkAlertService.updateTalkAlert(talkAlert));
    }

    @PreAuthorize("@ss.hasPermi('talk:alert:remove')")
    @Log(title = "心理健康预警管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{alertIds}")
    public AjaxResult remove(@PathVariable Long[] alertIds) {
        return toAjax(talkAlertService.deleteTalkAlertByIds(alertIds));
    }

    @PreAuthorize("@ss.hasPermi('talk:alert:edit')")
    @Log(title = "心理健康预警管理", businessType = BusinessType.UPDATE)
    @PutMapping("/handle/{alertId}")
    public AjaxResult handle(
            @PathVariable Long alertId,
            @RequestBody TalkAlert talkAlert) {
        return toAjax(talkAlertService.handleAlert(alertId,
                talkAlert.getAlertStatus(), talkAlert.getHandleRemark()));
    }

    @PreAuthorize("@ss.hasPermi('talk:alert:add')")
    @Log(title = "心理健康预警管理", businessType = BusinessType.INSERT)
    @PostMapping("/batch-init")
    public AjaxResult initAlerts() {
        Map<String, Object> result = talkAlertService.initAlertsFromStudents();
        int generated = (Integer) result.get("generated");
        int abnormal = (Integer) result.get("abnormal");
        int closed = (Integer) result.get("closed");
        int existing = (Integer) result.get("existing");
        int total = (Integer) result.get("totalStudents");
        if (generated == 0 && abnormal == 0) {
            return success("共扫描 " + total + " 名学生，未发现心理健康状态异常的学生，未生成预警。");
        }
        StringBuilder msg = new StringBuilder();
        msg.append("共扫描 ").append(total).append(" 名学生，发现 ").append(abnormal).append(" 名异常");
        if (generated > 0) {
            msg.append("，成功生成 ").append(generated).append(" 条预警");
        }
        if (existing > 0) {
            msg.append("，").append(existing).append(" 名学生已有待处理预警（跳过）");
        }
        if (closed > 0) {
            msg.append("，关闭 ").append(closed).append(" 条已恢复预警");
        }
        msg.append("。");
        return success(msg.toString());
    }

    @Anonymous
    @GetMapping("/debug")
    public AjaxResult debug() {
        // 直接查询所有预警（绕过 Service 层数据范围过滤）
        TalkAlert query = new TalkAlert();
        List<TalkAlert> all = talkAlertMapper.selectTalkAlertList(query);
        // 查询学生
        TalkStudent stuQuery = new TalkStudent();
        List<TalkStudent> students = talkStudentMapper.selectTalkStudentList(stuQuery);
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("currentUser", SecurityUtils.getUsername());
            result.put("userId", SecurityUtils.getUserId());
            result.put("isAdmin", SecurityUtils.isAdmin());
        } catch (Exception e) {
            result.put("currentUser", "anonymous");
            result.put("userId", -1);
            result.put("isAdmin", false);
        }
        result.put("alertCount", all.size());
        result.put("alerts", all.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getAlertId());
            m.put("studentId", a.getStudentId());
            m.put("level", a.getAlertLevel());
            m.put("type", a.getAlertType());
            m.put("status", a.getAlertStatus());
            m.put("reason", a.getAlertReason());
            m.put("createBy", a.getCreateBy());
            return m;
        }).collect(Collectors.toList()));
        result.put("studentCount", students.size());
        result.put("students", students.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getStudentId());
            m.put("name", s.getStudentName());
            m.put("status", s.getMentalHealthStatus());
            return m;
        }).collect(Collectors.toList()));
        // 统计心理健康状态值分布
        Map<String, Long> statusDistribution = students.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getMentalHealthStatus() != null ? s.getMentalHealthStatus() : "null",
                        Collectors.counting()));
        result.put("statusDistribution", statusDistribution);
        return success(result);
    }
}
