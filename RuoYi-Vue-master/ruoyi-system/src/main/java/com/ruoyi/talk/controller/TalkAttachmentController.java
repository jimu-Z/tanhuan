package com.ruoyi.talk.controller;

import java.util.List;
import java.util.Set;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.talk.domain.TalkAttachment;
import com.ruoyi.talk.service.ITalkAttachmentService;
import com.ruoyi.talk.utils.AttachmentFileValidator;
import com.ruoyi.talk.utils.AttachmentPathUtils;

@RestController
@RequestMapping("/ruoyi-system/talkattachment")
public class TalkAttachmentController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(TalkAttachmentController.class);
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

    @PreAuthorize("@ss.hasPermi('talk:session:list')")
    @GetMapping("/download/{attachmentId}")
    public void download(@PathVariable Long attachmentId, jakarta.servlet.http.HttpServletResponse response) throws IOException
    {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        TalkAttachment attachment = talkAttachmentService.selectTalkAttachmentById(attachmentId);
        if (attachment == null) { response.setStatus(404); return; }
        java.nio.file.Path file;
        try { file = AttachmentPathUtils.resolveWithinProfile(RuoYiConfig.getProfile(), attachment.getFilePath()); }
        catch (IOException | IllegalArgumentException error) { response.setStatus(404); return; }
        if (file == null) { response.setStatus(404); return; }
        response.setContentType("application/octet-stream");
        String name = attachment.getFileName() == null ? "附件" : attachment.getFileName();
        name = name.replaceAll("[\\r\\n\\\\/]", "_");
        com.ruoyi.common.utils.file.FileUtils.setAttachmentResponseHeader(response, name);
        try (var input = java.nio.file.Files.newInputStream(file)) {
            input.transferTo(response.getOutputStream());
        }
    }

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

        if (!AttachmentFileValidator.hasExpectedContent(file, contentType))
        {
            return error("文件内容与声明类型不匹配");
        }

        talkAttachmentService.checkWriteAccess(sessionId);
        String filePath;
        try
        {
            // UUID avoids filesystem byte-length/character restrictions while metadata keeps the original name.
            filePath = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file,
                    com.ruoyi.common.utils.file.MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION, true);
        }
        catch (IOException error)
        {
            log.error("谈话附件保存失败，请检查 ruoyi.profile 上传目录及运行账号权限", error);
            return error(error.getMessage());
        }
        TalkAttachment attachment = new TalkAttachment();
        attachment.setSessionId(sessionId);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFilePath(filePath);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(file.getContentType());
        try
        {
            int rows = talkAttachmentService.insertTalkAttachment(attachment);
            if (rows > 0) return success(attachment);
            cleanupFile(filePath);
            return error("附件信息保存失败，请重试");
        }
        catch (Exception error)
        {
            cleanupFile(filePath);
            throw error;
        }
    }

    private void cleanupFile(String filePath)
    {
        try { AttachmentPathUtils.deleteWithinProfile(RuoYiConfig.getProfile(), filePath); }
        catch (IOException error) { log.warn("清理未保存的附件失败: {}", filePath, error); }
    }

    @PreAuthorize("@ss.hasPermi('talk:session:remove')")
    @Log(title = "谈话附件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{attachmentIds}")
    public AjaxResult remove(@PathVariable Long[] attachmentIds)
    {
        return toAjax(talkAttachmentService.deleteTalkAttachmentByIds(attachmentIds));
    }
}
