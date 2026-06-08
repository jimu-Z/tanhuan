package com.ruoyi.talk.mapper;

import java.util.List;
import com.ruoyi.talk.domain.TalkAppointment;

/**
 * 学生预约谈话Mapper接口
 *
 * @author admin
 * @date 2026-06-06
 */
public interface TalkAppointmentMapper {

    /**
     * 查询预约列表
     *
     * @param appointment 预约查询条件
     * @return 预约集合
     */
    List<TalkAppointment> selectTalkAppointmentList(TalkAppointment appointment);

    /**
     * 查询预约详情
     *
     * @param appointmentId 预约主键
     * @return 预约
     */
    TalkAppointment selectTalkAppointmentById(Long appointmentId);

    /**
     * 新增预约
     *
     * @param appointment 预约
     * @return 结果
     */
    int insertTalkAppointment(TalkAppointment appointment);

    /**
     * 修改预约
     *
     * @param appointment 预约
     * @return 结果
     */
    int updateTalkAppointment(TalkAppointment appointment);

    /**
     * 批量删除预约
     *
     * @param appointmentIds 需要删除的预约主键集合
     * @return 结果
     */
    int deleteTalkAppointmentByIds(Long[] appointmentIds);

    /**
     * 查询指定学生的预约列表
     *
     * @param studentId 学生ID
     * @return 预约集合
     */
    List<TalkAppointment> selectByStudentId(Long studentId);
}
