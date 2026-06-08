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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.talk.domain.TalkStudentRecord;
import com.ruoyi.talk.service.ITalkAlertService;
import com.ruoyi.talk.service.ITalkStudentRecordService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 谈话记录管理Controller
 * 
 * @author admin
 * @date 2026-05-27
 */
@RestController
@RequestMapping("/ruoyi-system/talkrecord")
public class TalkStudentRecordController extends BaseController {
    @Autowired
    private ITalkStudentRecordService talkStudentRecordService;

    @Autowired
    private ITalkAlertService talkAlertService;

    /**
     * 查询当前学生自己的谈话记录
     */
    @GetMapping("/myRecords")
    public TableDataInfo myRecords(TalkStudentRecord talkStudentRecord) {
        String username = SecurityUtils.getUsername();
        talkStudentRecord.setStudentCode(username);
        // Also try matching by studentId if user has student info
        try {
            if (talkStudentRecord.getStudentId() == null) {
                com.ruoyi.talk.domain.TalkStudent student = talkStudentRecordService.selectTalkStudentByCode(username);
                if (student != null) {
                    talkStudentRecord.setStudentId(student.getStudentId());
                }
            }
        } catch (Exception e) {
            // Ignore - fallback to studentCode matching only
        }
        startPage();
        List<TalkStudentRecord> list = talkStudentRecordService.selectTalkStudentRecordList(talkStudentRecord);
        return getDataTable(list);
    }

    /**
     * 查询谈话记录管理列表
     */
    @PreAuthorize("@ss.hasPermi('talk:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(TalkStudentRecord talkStudentRecord) {
        startPage();
        List<TalkStudentRecord> list = talkStudentRecordService.selectTalkStudentRecordList(talkStudentRecord);
        return getDataTable(list);
    }

    /**
     * 导出谈话记录管理列表
     */
    @PreAuthorize("@ss.hasPermi('talk:record:export')")
    @Log(title = "谈话记录管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TalkStudentRecord talkStudentRecord) {
        List<TalkStudentRecord> list = talkStudentRecordService.selectTalkStudentRecordList(talkStudentRecord);
        ExcelUtil<TalkStudentRecord> util = new ExcelUtil<TalkStudentRecord>(TalkStudentRecord.class);
        util.exportExcel(response, list, "谈话记录管理数据");
    }

    /**
     * 获取谈话记录管理详细信息（教师查看时标记反馈已读）
     */
    @PreAuthorize("@ss.hasPermi('talk:record:query')")
    @GetMapping(value = "/{recordId:\\d+}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId) {
        TalkStudentRecord record = talkStudentRecordService.selectTalkStudentRecordByRecordId(recordId);
        if (record != null && record.getTeacherNotified() != null && record.getTeacherNotified() == 0) {
            TalkStudentRecord update = new TalkStudentRecord();
            update.setRecordId(recordId);
            update.setTeacherNotified(1);
            talkStudentRecordService.updateTalkStudentRecord(update);
        }
        return success(record);
    }

    /**
     * 新增谈话记录管理
     */
    @PreAuthorize("@ss.hasPermi('talk:record:add')")
    @Log(title = "谈话记录管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TalkStudentRecord talkStudentRecord) {
        return toAjax(talkStudentRecordService.insertTalkStudentRecord(talkStudentRecord));
    }

    /**
     * 学生提交反馈
     */
    @Log(title = "谈话记录管理", businessType = BusinessType.UPDATE)
    @PutMapping("/submitFeedback")
    public AjaxResult submitFeedback(@RequestBody TalkStudentRecord talkStudentRecord) {
        String username = SecurityUtils.getUsername();
        TalkStudentRecord existing = talkStudentRecordService
                .selectTalkStudentRecordByRecordId(talkStudentRecord.getRecordId());
        if (existing == null || !username.equals(existing.getStudentCode())) {
            return error("无权限操作此记录");
        }
        TalkStudentRecord update = new TalkStudentRecord();
        update.setRecordId(talkStudentRecord.getRecordId());
        update.setStudentFeedback(talkStudentRecord.getStudentFeedback());
        update.setOriginalStudentFeedback(talkStudentRecord.getStudentFeedback());
        update.setTeacherNotified(0);
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = talkStudentRecordService.updateTalkStudentRecord(update);
        // 触发敏感词检测
        talkAlertService.checkStudentFeedbackForKeywords(existing.getStudentId(), talkStudentRecord.getStudentFeedback());
        return toAjax(rows);
    }

    /**
     * 修改谈话记录管理
     */
    @PreAuthorize("@ss.hasPermi('talk:record:edit')")
    @Log(title = "谈话记录管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TalkStudentRecord talkStudentRecord) {
        return toAjax(talkStudentRecordService.updateTalkStudentRecord(talkStudentRecord));
    }

    /**
     * 删除谈话记录管理
     */
    @PreAuthorize("@ss.hasPermi('talk:record:remove')")
    @Log(title = "谈话记录管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds:[\\d,]+}")
    public AjaxResult remove(@PathVariable Long[] recordIds) {
        return toAjax(talkStudentRecordService.deleteTalkStudentRecordByRecordIds(recordIds));
    }
}
