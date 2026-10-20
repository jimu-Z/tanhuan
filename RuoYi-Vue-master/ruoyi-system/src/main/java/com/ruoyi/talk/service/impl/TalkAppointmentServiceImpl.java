package com.ruoyi.talk.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.talk.domain.TalkAppointment;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.domain.TalkStudentRecord;
import com.ruoyi.talk.domain.TalkTeacher;
import com.ruoyi.talk.mapper.TalkAppointmentMapper;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;
import com.ruoyi.talk.mapper.TalkTeacherMapper;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.security.TalkPermissionUtils;
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

    @Autowired
    private TalkStudentMapper talkStudentMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

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
            throw new ServiceException("预约不存在");
        }
        if (!"pending".equals(appointment.getStatus())) {
            throw new ServiceException("当前预约状态不允许确认，仅待确认的预约可确认");
        }

        TalkStudent student = talkStudentMapper.selectTalkStudentByStudentId(appointment.getStudentId());
        if (student == null) {
            throw new ServiceException("预约学生不存在");
        }
        TalkTeacher teacher = talkTeacherMapper.selectTalkTeacherById(appointment.getTeacherId());
        if (teacher == null) {
            throw new ServiceException("预约教师不存在");
        }
        Long studentCollegeId = resolveCollegeDeptId(student.getDeptId());
        Long teacherCollegeId = resolveCollegeDeptId(teacher.getDeptId());
        if (!studentCollegeId.equals(teacherCollegeId)) {
            throw new ServiceException("预约学生和教师必须属于同一学院");
        }
        TalkPermissionUtils.requireCollegeAccess(studentCollegeId);

        // 1. 更新预约状态为confirmed
        appointment.setStatus("confirmed");
        appointment.setUpdateBy(SecurityUtils.getUsername());
        appointment.setUpdateTime(DateUtils.getNowDate());
        talkAppointmentMapper.updateTalkAppointment(appointment);

        // 2. 创建 TalkSession，并固化学生所属学院作为历史数据范围快照。
        TalkSession session = new TalkSession();
        session.setCollegeDeptId(studentCollegeId);
        session.setTalkType("individual");
        session.setTalkTime(appointment.getAppointmentTime());
        session.setTalkLocation(appointment.getLocation());
        session.setTalkPerson(teacher.getTeacherName());
        session.setTalkContent("");
        session.setCreateBy(SecurityUtils.getUsername());
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

    private Long resolveCollegeDeptId(Long deptId) {
        Long currentId = deptId;
        while (currentId != null && currentId > 0) {
            SysDept dept = sysDeptMapper.selectDeptById(currentId);
            if (dept == null) {
                break;
            }
            if ("college".equals(dept.getDeptType())) {
                return dept.getDeptId();
            }
            currentId = dept.getParentId();
        }
        throw new ServiceException("预约对象未归属有效学院");
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
