package com.ruoyi.conversation.mapper;

import java.util.List;
import com.ruoyi.conversation.domain.ExportTask;

public interface ExportTaskMapper {
    public List<ExportTask> selectExportTaskList(ExportTask exportTask);

    public ExportTask selectExportTaskById(Long taskId);

    public int insertExportTask(ExportTask exportTask);

    public int updateExportTask(ExportTask exportTask);

    public int deleteExportTaskById(Long taskId);

    public int deleteExportTaskByIds(Long[] taskIds);

    public List<ExportTask> selectPendingTasks();
}