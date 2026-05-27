package com.ruoyi.conversation.service;

import java.util.List;
import com.ruoyi.conversation.domain.BackupLog;

public interface IBackupLogService {
    public List<BackupLog> selectBackupLogList(BackupLog backupLog);

    public BackupLog selectBackupLogById(Long backupId);

    public int insertBackupLog(BackupLog backupLog);

    public int deleteBackupLogByIds(Long[] backupIds);

    public void executeBackup(String backupType);
}