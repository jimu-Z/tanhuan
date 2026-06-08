package com.ruoyi.talk.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.domain.entity.SysDictData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.talk.domain.TalkAlert;
import com.ruoyi.talk.mapper.TalkAlertMapper;
import com.ruoyi.talk.service.ITalkAlertService;
import com.ruoyi.system.service.ISysDictTypeService;

@Service
public class TalkAlertServiceImpl implements ITalkAlertService
{
    @Autowired
    private TalkAlertMapper talkAlertMapper;

    @Autowired
    private ISysDictTypeService dictTypeService;

    @Override
    public List<TalkAlert> selectTalkAlertList(TalkAlert alert)
    {
        applyDataScopeFilter(alert);
        return talkAlertMapper.selectTalkAlertList(alert);
    }

    @Override
    public TalkAlert selectTalkAlertById(Long alertId)
    {
        return talkAlertMapper.selectTalkAlertById(alertId);
    }

    private void applyDataScopeFilter(TalkAlert alert)
    {
        if (SecurityUtils.isAdmin())
        {
            return;
        }
        String username = SecurityUtils.getUsername();
        if (username == null)
        {
            return;
        }
        if (alert.getParams() == null)
        {
            alert.setParams(new HashMap<>());
        }
        if (SecurityUtils.hasRole("talk_counselor"))
        {
            alert.getParams().put("counselorUsername", username);
        }
        else if (SecurityUtils.hasRole("talk_secretary"))
        {
            Long deptId = SecurityUtils.getDeptId();
            if (deptId != null)
            {
                alert.getParams().put("secretaryDeptId", deptId);
            }
        }
    }

    @Override
    public int insertTalkAlert(TalkAlert alert)
    {
        alert.setCreateBy(SecurityUtils.getUsername());
        alert.setCreateTime(DateUtils.getNowDate());
        return talkAlertMapper.insertTalkAlert(alert);
    }

    @Override
    public int updateTalkAlert(TalkAlert alert)
    {
        alert.setUpdateBy(SecurityUtils.getUsername());
        alert.setUpdateTime(DateUtils.getNowDate());
        return talkAlertMapper.updateTalkAlert(alert);
    }

    @Override
    public int deleteTalkAlertByIds(Long[] alertIds)
    {
        return talkAlertMapper.deleteTalkAlertByIds(alertIds);
    }

    @Override
    public int handleAlert(Long alertId, String alertStatus, String handleRemark)
    {
        // 校验目标状态
        if (!"in_progress".equals(alertStatus) && !"resolved".equals(alertStatus))
        {
            throw new IllegalArgumentException("无效的处理状态，仅支持 in_progress 或 resolved");
        }
        // 校验当前状态
        TalkAlert existing = talkAlertMapper.selectTalkAlertById(alertId);
        if (existing == null)
        {
            throw new RuntimeException("预警不存在");
        }
        if (!"pending".equals(existing.getAlertStatus()) && !"in_progress".equals(existing.getAlertStatus()))
        {
            throw new RuntimeException("当前预警状态不允许处理，仅 pending 或 in_progress 状态可处理");
        }
        TalkAlert alert = new TalkAlert();
        alert.setAlertId(alertId);
        alert.setAlertStatus(alertStatus);
        alert.setHandleRemark(handleRemark);
        alert.setHandler(SecurityUtils.getUsername());
        alert.setHandleTime(DateUtils.getNowDate());
        alert.setUpdateBy(SecurityUtils.getUsername());
        alert.setUpdateTime(DateUtils.getNowDate());
        return talkAlertMapper.updateTalkAlert(alert);
    }

    @Override
    public void checkStudentFeedbackForKeywords(Long studentId, String feedback)
    {
        if (StringUtils.isEmpty(feedback))
        {
            return;
        }
        List<SysDictData> keywords = dictTypeService.selectDictDataByType("alert_keywords");
        if (keywords == null || keywords.isEmpty())
        {
            return;
        }
        for (SysDictData keyword : keywords)
        {
            if (feedback.contains(keyword.getDictLabel()))
            {
                TalkAlert alert = new TalkAlert();
                alert.setStudentId(studentId);
                alert.setAlertType("keyword");
                alert.setAlertLevel("red");
                alert.setAlertReason("学生反馈包含敏感词：" + keyword.getDictLabel());
                alert.setAlertStatus("pending");
                insertTalkAlert(alert);
            }
        }
    }

    @Override
    public void autoGenerateAlertForStudent(Long studentId, String mentalHealthStatus)
    {
        if (StringUtils.isEmpty(mentalHealthStatus))
        {
            return;
        }
        // 去重检查：相同学生+自动预警+待处理状态
        TalkAlert dupCheck = new TalkAlert();
        dupCheck.setStudentId(studentId);
        dupCheck.setAlertType("auto");
        dupCheck.setAlertStatus("pending");
        List<TalkAlert> existingAlerts = talkAlertMapper.selectTalkAlertList(dupCheck);
        if (existingAlerts != null && !existingAlerts.isEmpty())
        {
            return;
        }
        String alertLevel;
        switch (mentalHealthStatus)
        {
            case "severe_warning":
                alertLevel = "red";
                break;
            case "moderate_warning":
                alertLevel = "orange";
                break;
            case "mild_warning":
                alertLevel = "yellow";
                break;
            default:
                return;
        }
        TalkAlert alert = new TalkAlert();
        alert.setStudentId(studentId);
        alert.setAlertType("auto");
        alert.setAlertLevel(alertLevel);
        alert.setAlertReason("系统根据心理健康状态自动生成预警：" + mentalHealthStatus);
        alert.setAlertStatus("pending");
        insertTalkAlert(alert);
    }
}
