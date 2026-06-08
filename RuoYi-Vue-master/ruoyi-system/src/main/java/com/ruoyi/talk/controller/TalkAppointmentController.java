package com.ruoyi.talk.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.talk.domain.TalkAppointment;
import com.ruoyi.talk.service.ITalkAppointmentService;

/**
 * 学生预约谈话Controller
 *
 * @author admin
 * @date 2026-06-06
 */
@RestController
@RequestMapping("/ruoyi-system/talk")
public class TalkAppointmentController extends BaseController {
    @Autowired
    private ITalkAppointmentService talkAppointmentService;

    /**
     * 查询预约列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('talk:appointment:list')")
    @GetMapping("/appointment/list")
    public TableDataInfo list(TalkAppointment appointment) {
        startPage();
        List<TalkAppointment> list = talkAppointmentService.selectTalkAppointmentList(appointment);
        return getDataTable(list);
    }

    /**
     * 获取预约详细信息
     */
    @PreAuthorize("@ss.hasPermi('talk:appointment:query')")
    @GetMapping("/appointment/{appointmentId}")
    public AjaxResult getInfo(@PathVariable("appointmentId") Long appointmentId) {
        return success(talkAppointmentService.selectTalkAppointmentById(appointmentId));
    }

    /**
     * 学生发起预约
     */
    @PreAuthorize("@ss.hasPermi('talk:appointment:add')")
    @Log(title = "学生预约谈话", businessType = BusinessType.INSERT)
    @PostMapping("/appointment")
    public AjaxResult add(@RequestBody TalkAppointment appointment) {
        return toAjax(talkAppointmentService.insertTalkAppointment(appointment));
    }

    /**
     * 修改预约
     */
    @PreAuthorize("@ss.hasPermi('talk:appointment:edit')")
    @Log(title = "学生预约谈话", businessType = BusinessType.UPDATE)
    @PutMapping("/appointment")
    public AjaxResult edit(@RequestBody TalkAppointment appointment) {
        return toAjax(talkAppointmentService.updateTalkAppointment(appointment));
    }

    /**
     * 删除预约
     */
    @PreAuthorize("@ss.hasPermi('talk:appointment:remove')")
    @Log(title = "学生预约谈话", businessType = BusinessType.DELETE)
    @DeleteMapping("/appointment/{appointmentIds}")
    public AjaxResult remove(@PathVariable Long[] appointmentIds) {
        return toAjax(talkAppointmentService.deleteTalkAppointmentByIds(appointmentIds));
    }

    /**
     * 教师确认预约
     */
    @PreAuthorize("@ss.hasPermi('talk:appointment:confirm')")
    @Log(title = "学生预约谈话", businessType = BusinessType.UPDATE)
    @PutMapping("/appointment/confirm/{appointmentId}")
    public AjaxResult confirm(@PathVariable("appointmentId") Long appointmentId) {
        return toAjax(talkAppointmentService.confirmAppointment(appointmentId));
    }

    /**
     * 教师拒绝预约
     */
    @PreAuthorize("@ss.hasPermi('talk:appointment:reject')")
    @Log(title = "学生预约谈话", businessType = BusinessType.UPDATE)
    @PutMapping("/appointment/reject/{appointmentId}")
    public AjaxResult reject(@PathVariable("appointmentId") Long appointmentId, @RequestParam(required = false) String rejectReason) {
        return toAjax(talkAppointmentService.rejectAppointment(appointmentId, rejectReason));
    }

    /**
     * 学生取消预约
     */
    @PreAuthorize("@ss.hasPermi('talk:appointment:cancel')")
    @Log(title = "学生预约谈话", businessType = BusinessType.UPDATE)
    @PutMapping("/appointment/cancel/{appointmentId}")
    public AjaxResult cancel(@PathVariable("appointmentId") Long appointmentId) {
        return toAjax(talkAppointmentService.cancelAppointment(appointmentId));
    }

    /**
     * 标记预约为已完成
     */
    @PreAuthorize("@ss.hasPermi('talk:appointment:complete')")
    @Log(title = "学生预约谈话", businessType = BusinessType.UPDATE)
    @PutMapping("/appointment/complete/{appointmentId}")
    public AjaxResult complete(@PathVariable("appointmentId") Long appointmentId) {
        return toAjax(talkAppointmentService.completeAppointment(appointmentId));
    }
}
