package com.ruoyi.talk.service;

import java.util.List;
import com.ruoyi.talk.domain.TalkAppointment;

/**
 * 学生预约谈话Service接口
 *
 * @author admin
 * @date 2026-06-06
 */
public interface ITalkAppointmentService {

    /**
     * 查询预约列表
     */
    List<TalkAppointment> selectTalkAppointmentList(TalkAppointment appointment);

    /**
     * 查询预约详情
     */
    TalkAppointment selectTalkAppointmentById(Long appointmentId);

    /**
     * 学生发起预约
     */
    int insertTalkAppointment(TalkAppointment appointment);

    /**
     * 修改预约
     */
    int updateTalkAppointment(TalkAppointment appointment);

    /**
     * 批量删除预约
     */
    int deleteTalkAppointmentByIds(Long[] appointmentIds);

    /**
     * 教师确认预约，自动创建 talk_session + talk_student_record
     */
    int confirmAppointment(Long appointmentId);

    /**
     * 教师拒绝预约
     */
    int rejectAppointment(Long appointmentId, String rejectReason);

    /**
     * 学生取消预约
     */
    int cancelAppointment(Long appointmentId);

    /**
     * 标记已完成
     */
    int completeAppointment(Long appointmentId);
}
