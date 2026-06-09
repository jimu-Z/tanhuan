package com.ruoyi.talk.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.talk.domain.TalkAppointment;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.domain.TalkStudentRecord;
import com.ruoyi.talk.domain.TalkTeacher;
import com.ruoyi.talk.mapper.TalkAppointmentMapper;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;
import com.ruoyi.talk.mapper.TalkTeacherMapper;
import com.ruoyi.talk.service.ITalkAppointmentService;

/**
 * 学生预约谈话Service业务层处理
 *
 * @author admin
 * @date 2026-06-06
 */
@Service
public class TalkAppointmentServiceImpl implements ITalkAppointmentService {
    @Autowired
    private TalkAppointmentMapper talkAppointmentMapper;

    @Autowired
    private TalkSessionMapper talkSessionMapper;

    @Autowired
    private TalkStudentRecordMapper talkStudentRecordMapper;

    @Autowired
    private TalkTeacherMapper talkTeacherMapper;

    @Override
    public List<TalkAppointment> selectTalkAppointmentList(TalkAppointment appointment) {
        applyDataScopeFilter(appointment);
        return talkAppointmentMapper.selectTalkAppointmentList(appointment);
    }

    @Override
    public TalkAppointment selectTalkAppointmentById(Long appointmentId) {
        return talkAppointmentMapper.selectTalkAppointmentById(appointmentId);
    }

    private void applyDataScopeFilter(TalkAppointment appointment) {
        if (SecurityUtils.isAdmin()) {
            return;
        }
        String username = SecurityUtils.getUsername();
        if (username == null) {
            return;
        }
        if (appointment.getParams() == null) {
            appointment.setParams(new HashMap<>());
        }
        if (SecurityUtils.hasRole("talk_student")) {
            // 学生只能看到自己创建的预约
            appointment.getParams().put("studentUsername", username);
        } else if (SecurityUtils.hasRole("talk_counselor")) {
            appointment.getParams().put("counselorUsername", username);
        } else if (SecurityUtils.hasRole("talk_secretary")) {
            Long deptId = SecurityUtils.getDeptId();
            if (deptId != null) {
                appointment.getParams().put("secretaryDeptId", deptId);
            }
        }
    }

    @Override
    public int insertTalkAppointment(TalkAppointment appointment) {
        appointment.setStatus("pending");
        appointment.setCreateBy(SecurityUtils.getUsername());
        appointment.setCreateTime(DateUtils.getNowDate());
        return talkAppointmentMapper.insertTalkAppointment(appointment);
    }

    @Override
    public int updateTalkAppointment(TalkAppointment appointment) {
        appointment.setUpdateBy(SecurityUtils.getUsername());
        appointment.setUpdateTime(DateUtils.getNowDate());
        return talkAppointmentMapper.updateTalkAppointment(appointment);
    }

    @Override
    public int deleteTalkAppointmentByIds(Long[] appointmentIds) {
        return talkAppointmentMapper.deleteTalkAppointmentByIds(appointmentIds);
    }

    @Override
    @Transactional
    public int confirmAppointment(Long appointmentId) {
        TalkAppointment appointment = talkAppointmentMapper.selectTalkAppointmentById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!"pending".equals(appointment.getStatus())) {
            throw new RuntimeException("当前预约状态不允许确认，仅待确认的预约可确认");
        }

        // 1. 更新预约状态为confirmed
        appointment.setStatus("confirmed");
        appointment.setUpdateBy(SecurityUtils.getUsername());
        appointment.setUpdateTime(DateUtils.getNowDate());
        talkAppointmentMapper.updateTalkAppointment(appointment);

        // 2. 查询教师信息获取教师姓名
        TalkTeacher teacher = talkTeacherMapper.selectTalkTeacherById(appointment.getTeacherId());
        String teacherName = teacher != null ? teacher.getTeacherName() : "";

        // 3. 创建 TalkSession
        TalkSession session = new TalkSession();
        session.setTalkType("individual");
        session.setTalkTime(appointment.getAppointmentTime());
        session.setTalkLocation(appointment.getLocation());
        session.setTalkPerson(teacherName);
        session.setTalkContent("");
        session.setCreateBy(appointment.getCreateBy());
        session.setCreateTime(new Date());
        talkSessionMapper.insertTalkSession(session);

        // 4. 创建 TalkStudentRecord
        TalkStudentRecord record = new TalkStudentRecord();
        record.setSessionId(session.getSessionId());
        record.setStudentId(appointment.getStudentId());
        record.setStudentFeedback("");
        record.setFollowupPlan("");
        record.setFollowupStatus("none");
        record.setCreateTime(new Date());
        talkStudentRecordMapper.insertTalkStudentRecord(record);

        // 5. 更新预约的 session_id
        appointment.setSessionId(session.getSessionId());
        appointment.setUpdateTime(DateUtils.getNowDate());
        return talkAppointmentMapper.updateTalkAppointment(appointment);
    }

    @Override
    public int rejectAppointment(Long appointmentId, String rejectReason) {
        TalkAppointment appointment = talkAppointmentMapper.selectTalkAppointmentById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!"pending".equals(appointment.getStatus())) {
            throw new RuntimeException("当前预约状态不允许拒绝，仅待确认的预约可拒绝");
        }
        appointment.setStatus("rejected");
        appointment.setRejectReason(rejectReason);
        appointment.setUpdateBy(SecurityUtils.getUsername());
        appointment.setUpdateTime(DateUtils.getNowDate());
        return talkAppointmentMapper.updateTalkAppointment(appointment);
    }

    @Override
    public int cancelAppointment(Long appointmentId) {
        TalkAppointment appointment = talkAppointmentMapper.selectTalkAppointmentById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!"pending".equals(appointment.getStatus())) {
            throw new RuntimeException("当前预约状态不允许取消，仅待确认的预约可取消");
        }
        appointment.setStatus("cancelled");
        appointment.setUpdateBy(SecurityUtils.getUsername());
        appointment.setUpdateTime(DateUtils.getNowDate());
        return talkAppointmentMapper.updateTalkAppointment(appointment);
    }

    @Override
    public int completeAppointment(Long appointmentId) {
        TalkAppointment appointment = talkAppointmentMapper.selectTalkAppointmentById(appointmentId);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!"confirmed".equals(appointment.getStatus())) {
            throw new RuntimeException("当前预约状态不允许标记完成，仅已确认的预约可完成");
        }
        appointment.setStatus("completed");
        appointment.setUpdateBy(SecurityUtils.getUsername());
        appointment.setUpdateTime(DateUtils.getNowDate());
        return talkAppointmentMapper.updateTalkAppointment(appointment);
    }
}
