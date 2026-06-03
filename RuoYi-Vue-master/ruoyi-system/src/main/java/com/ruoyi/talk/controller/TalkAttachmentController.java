package com.ruoyi.talk.controller;

import java.util.List;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.talk.domain.TalkAttachment;
import com.ruoyi.talk.service.ITalkAttachmentService;

@RestController
@RequestMapping("/ruoyi-system/talkattachment")
public class TalkAttachmentController extends BaseController
{
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    @Autowired
    private ITalkAttachmentService talkAttachmentService;

    @PreAuthorize("@ss.hasPermi('talk:session:add')")
    @GetMapping("/list/{sessionId}")
    public AjaxResult listBySession(@PathVariable Long sessionId)
    {
        List<TalkAttachment> list = talkAttachmentService.selectTalkAttachmentBySessionId(sessionId);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('talk:session:add')")
    @Log(title = "谈话附件", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file, @RequestParam("sessionId") Long sessionId) throws Exception
    {
        if (file == null || file.isEmpty())
        {
            return error("上传文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase()))
        {
            return error("不支持的文件类型，仅支持图片、PDF、Word和Excel文件");
        }

        if (file.getSize() > MAX_FILE_SIZE)
        {
            return error("文件大小超过限制（50MB）");
        }

        String filePath = FileUploadUtils.upload(file);
        TalkAttachment attachment = new TalkAttachment();
        attachment.setSessionId(sessionId);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFilePath(filePath);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(file.getContentType());
        int rows = talkAttachmentService.insertTalkAttachment(attachment);
        return rows > 0 ? success(attachment) : error("上传失败");
    }

    @PreAuthorize("@ss.hasPermi('talk:session:remove')")
    @Log(title = "谈话附件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{attachmentIds}")
    public AjaxResult remove(@PathVariable Long[] attachmentIds)
    {
        return toAjax(talkAttachmentService.deleteTalkAttachmentByIds(attachmentIds));
    }
}
