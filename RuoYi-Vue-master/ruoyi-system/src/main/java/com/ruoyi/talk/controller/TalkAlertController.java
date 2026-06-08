package com.ruoyi.talk.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.talk.domain.TalkAlert;
import com.ruoyi.talk.service.ITalkAlertService;
import com.ruoyi.common.core.page.TableDataInfo;

@RestController
@RequestMapping("/talk/alert")
public class TalkAlertController extends BaseController {
    @Autowired
    private ITalkAlertService talkAlertService;

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
}
