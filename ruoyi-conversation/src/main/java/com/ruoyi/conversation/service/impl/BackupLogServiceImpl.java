package com.ruoyi.conversation.service.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.conversation.domain.BackupLog;
import com.ruoyi.conversation.mapper.BackupLogMapper;
import com.ruoyi.conversation.service.IBackupLogService;

@Service
public class BackupLogServiceImpl implements IBackupLogService {
    private static final Logger log = LoggerFactory.getLogger(BackupLogServiceImpl.class);

    @Autowired
    private BackupLogMapper backupLogMapper;

    @Override
    public List<BackupLog> selectBackupLogList(BackupLog backupLog) {
        return backupLogMapper.selectBackupLogList(backupLog);
    }

    @Override
    public BackupLog selectBackupLogById(Long backupId) {
        return backupLogMapper.selectBackupLogById(backupId);
    }

    @Override
    @Transactional
    public int insertBackupLog(BackupLog backupLog) {
        backupLog.setCreateBy(SecurityUtils.getUsername());
        return backupLogMapper.insertBackupLog(backupLog);
    }

    @Override
    @Transactional
    public int deleteBackupLogByIds(Long[] backupIds) {
        return backupLogMapper.deleteBackupLogByIds(backupIds);
    }

    @Override
    public void executeBackup(String backupType) {
        long startTime = System.currentTimeMillis();
        BackupLog backupLog = new BackupLog();
        backupLog.setBackupType(backupType);
        backupLog.setStatus("1");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String dateStr = sdf.format(new Date());
            String fileName = "backup_" + backupType + "_" + dateStr + ".sql";
            String backupDir = System.getProperty("user.dir") + File.separator + "backup";
            File dir = new File(backupDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filePath = backupDir + File.separator + fileName;
            String cmd = "mysqldump --single-transaction --routines --triggers --add-drop-database"
                    + " -h${MYSQL_HOST} -P${MYSQL_PORT} -u${MYSQL_USER} -p${MYSQL_PASSWORD}"
                    + " --databases ruoyi_vue > " + filePath;
            Process process = Runtime.getRuntime().exec(new String[] { "cmd", "/c", cmd });
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                backupLog.setStatus("0");
                backupLog.setFileName(fileName);
                backupLog.setFilePath(filePath);
                File backupFile = new File(filePath);
                backupLog.setFileSize(backupFile.length());
                long endTime = System.currentTimeMillis();
                backupLog.setDuration(endTime - startTime);
            } else {
                backupLog.setStatus("2");
                backupLog.setErrorMsg("备份命令执行失败，退出码：" + exitCode);
            }
        } catch (Exception e) {
            log.error("备份执行失败", e);
            backupLog.setStatus("2");
            backupLog.setErrorMsg(e.getMessage());
        }
        backupLog.setCreateBy(SecurityUtils.getUsername());
        backupLog.setCreateTime(new Date());
        backupLogMapper.insertBackupLog(backupLog);
    }
}