package com.ruoyi.conversation.service;

import java.util.List;
import com.ruoyi.conversation.domain.ExportTask;

public interface IExportTaskService {
    public List<ExportTask> selectExportTaskList(ExportTask exportTask);

    public ExportTask selectExportTaskById(Long taskId);

    public int insertExportTask(ExportTask exportTask);

    public int updateExportTask(ExportTask exportTask);

    public int deleteExportTaskByIds(Long[] taskIds);

    public Long createExportTask(String taskName, String exportType, String exportParams);

    public List<ExportTask> selectPendingTasks();
}