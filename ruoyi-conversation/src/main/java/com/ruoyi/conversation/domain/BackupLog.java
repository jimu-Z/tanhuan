package com.ruoyi.conversation.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class BackupLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long backupId;

    private String backupType;

    private String fileName;

    private String filePath;

    private Long fileSize;

    private String status;

    private String errorMsg;

    private Long duration;

    public Long getBackupId() {
        return backupId;
    }

    public void setBackupId(Long backupId) {
        this.backupId = backupId;
    }

    public String getBackupType() {
        return backupType;
    }

    public void setBackupType(String backupType) {
        this.backupType = backupType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Long getDuration()
    {
        return duration;
    }

public void setDuration(Long duration)
    {
        this.duration = duration;
    }
}