package com.ruoyi.conversation.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.conversation.domain.ExportTask;
import com.ruoyi.conversation.mapper.ExportTaskMapper;
import com.ruoyi.conversation.service.IExportTaskService;

@Service
public class ExportTaskServiceImpl implements IExportTaskService {
    @Autowired
    private ExportTaskMapper exportTaskMapper;

    @Override
    public List<ExportTask> selectExportTaskList(ExportTask exportTask) {
        return exportTaskMapper.selectExportTaskList(exportTask);
    }

    @Override
    public ExportTask selectExportTaskById(Long taskId) {
        return exportTaskMapper.selectExportTaskById(taskId);
    }

    @Override
    @Transactional
    public int insertExportTask(ExportTask exportTask) {
        exportTask.setCreateBy(SecurityUtils.getUsername());
        return exportTaskMapper.insertExportTask(exportTask);
    }

    @Override
    @Transactional
    public int updateExportTask(ExportTask exportTask) {
        exportTask.setUpdateBy(SecurityUtils.getUsername());
        return exportTaskMapper.updateExportTask(exportTask);
    }

    @Override
    @Transactional
    public Long createExportTask(String taskName, String exportType, String exportParams) {
        ExportTask task = new ExportTask();
        task.setTaskName(taskName);
        task.setExportType(exportType);
        task.setExportParams(exportParams);
        task.setStatus("0");
        task.setCreateBy(SecurityUtils.getUsername());
        task.setCreateTime(new Date());
        exportTaskMapper.insertExportTask(task);
        return task.getTaskId();
    }

    @Override
    public List<ExportTask> selectPendingTasks() {
        return exportTaskMapper.selectPendingTasks();
    }

    @Override
    @Transactional
    public int deleteExportTaskByIds(Long[] taskIds) {
        return exportTaskMapper.deleteExportTaskByIds(taskIds);
    }
}