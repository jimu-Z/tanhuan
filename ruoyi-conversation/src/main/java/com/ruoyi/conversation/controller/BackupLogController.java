package com.ruoyi.conversation.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.conversation.domain.BackupLog;
import com.ruoyi.conversation.service.IBackupLogService;

@RestController
@RequestMapping("/conversation/backup")
public class BackupLogController extends BaseController {
    @Autowired
    private IBackupLogService backupLogService;

    @GetMapping("/list")
    public TableDataInfo list(BackupLog backupLog) {
        startPage();
        List<BackupLog> list = backupLogService.selectBackupLogList(backupLog);
        return getDataTable(list);
    }

    @Log(title = "数据备份", businessType = BusinessType.OTHER)
    @PostMapping("/manual")
    public AjaxResult manualBackup() {
        backupLogService.executeBackup("0");
        return success();
    }

    @Log(title = "数据备份", businessType = BusinessType.DELETE)
    @DeleteMapping("/{backupIds}")
    public AjaxResult remove(@PathVariable Long[] backupIds) {
        return toAjax(backupLogService.deleteBackupLogByIds(backupIds));
    }

    @GetMapping("/download/{backupId}")
    public void download(@PathVariable Long backupId, HttpServletResponse response) {
    }
}