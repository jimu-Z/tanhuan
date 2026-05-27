package com.ruoyi.conversation.mapper;

import java.util.List;
import com.ruoyi.conversation.domain.BackupLog;

public interface BackupLogMapper {
    public List<BackupLog> selectBackupLogList(BackupLog backupLog);

    public BackupLog selectBackupLogById(Long backupId);

    public int insertBackupLog(BackupLog backupLog);

    public int deleteBackupLogById(Long backupId);

    public int deleteBackupLogByIds(Long[] backupIds);
}