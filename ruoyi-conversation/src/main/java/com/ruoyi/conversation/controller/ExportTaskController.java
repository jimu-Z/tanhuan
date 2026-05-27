package com.ruoyi.conversation.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.conversation.domain.ExportTask;
import com.ruoyi.conversation.service.IExportTaskService;

@RestController
@RequestMapping("/conversation/export")
public class ExportTaskController extends BaseController {
    @Autowired
    private IExportTaskService exportTaskService;

    @GetMapping("/list")
    public TableDataInfo list(ExportTask exportTask) {
        startPage();
        List<ExportTask> list = exportTaskService.selectExportTaskList(exportTask);
        return getDataTable(list);
    }

    @PostMapping
    public AjaxResult submit(@RequestBody ExportTask exportTask) {
        exportTask.setCreateBy(getUsername());
        exportTaskService.insertExportTask(exportTask);
        return success(exportTask.getTaskId());
    }

    @GetMapping("/{taskId}")
    public AjaxResult getStatus(@PathVariable Long taskId) {
        return success(exportTaskService.selectExportTaskById(taskId));
    }

    @GetMapping("/download/{taskId}")
    public void download(@PathVariable Long taskId, HttpServletResponse response) {
    }

    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds) {
        return toAjax(exportTaskService.deleteExportTaskByIds(taskIds));
    }
}