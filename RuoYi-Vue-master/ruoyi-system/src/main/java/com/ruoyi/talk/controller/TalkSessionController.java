package com.ruoyi.talk.controller;

import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.domain.TalkSessionCreateRequest;
import com.ruoyi.talk.domain.TalkSessionTag;
import com.ruoyi.talk.domain.TalkStudentRecord;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;
import com.ruoyi.talk.service.ITalkSessionService;
import com.ruoyi.talk.service.TalkDocxService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 谈话会话管理Controller
 * 
 * @author admin
 * @date 2026-05-27
 */
@RestController
@RequestMapping("/ruoyi-system/talksession")
public class TalkSessionController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(TalkSessionController.class);

    @Autowired
    private ITalkSessionService talkSessionService;

    @Autowired
    private TalkDocxService talkDocxService;

    @Autowired
    private TalkStudentRecordMapper talkStudentRecordMapper;

    /**
     * 查询谈话会话管理列表
     */
    @PreAuthorize("@ss.hasPermi('talk:session:list')")
    @GetMapping("/list")
    public TableDataInfo list(TalkSession talkSession) {
        startPage();
        List<TalkSession> list = talkSessionService.selectTalkSessionList(talkSession);
        return getDataTable(list);
    }

    /**
     * 导出谈话会话管理列表
     */
    @PreAuthorize("@ss.hasPermi('talk:session:export')")
    @Log(title = "谈话会话管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TalkSession talkSession) {
        List<TalkSession> list = talkSessionService.selectTalkSessionList(talkSession);
        ExcelUtil<TalkSession> util = new ExcelUtil<TalkSession>(TalkSession.class);
        util.exportExcel(response, list, "谈话会话管理数据");
    }

    /**
     * 导出谈话记录为 .docx 文档
     */
    @PreAuthorize("@ss.hasPermi('talk:session:export')")
    @Log(title = "谈话会话管理", businessType = BusinessType.EXPORT)
    @GetMapping("/exportDocx/{sessionId}")
    public void exportDocx(@PathVariable Long sessionId, HttpServletResponse response) throws Exception {
        TalkSession session = talkSessionService.selectTalkSessionBySessionId(sessionId);
        if (session == null) {
            writeJsonError(response, "会话不存在");
            return;
        }
        byte[] docxBytes = talkDocxService.generateDocxBySession(sessionId);
        boolean isZip = "group".equals(session.getTalkType());
        String fileName = "谈话记录_" + (session != null ? session.getTalkPerson() : "未知") + (isZip ? ".zip" : ".docx");
        writeFileDownload(response, docxBytes, fileName, isZip);
    }

    /**
     * 导出单个学生的谈话记录为 .docx
     */
    @PreAuthorize("@ss.hasPermi('talk:session:export')")
    @Log(title = "谈话会话管理", businessType = BusinessType.EXPORT)
    @GetMapping("/exportDocx/{sessionId}/student/{studentId}")
    public void exportDocxForStudent(@PathVariable Long sessionId, @PathVariable Long studentId,
            HttpServletResponse response) throws Exception {
        TalkSession session = talkSessionService.selectTalkSessionBySessionId(sessionId);
        if (session == null) {
            writeJsonError(response, "会话不存在");
            return;
        }
        byte[] docxBytes = talkDocxService.generateDocxByStudent(studentId, sessionId);
        String fileName = "谈话记录_" + session.getTalkPerson() + ".docx";
        writeFileDownload(response, docxBytes, fileName, false);
    }

    /**
     * 发起谈话（集体/个体）— 创建 session + N条 record
     */
    @PreAuthorize("@ss.hasPermi('talk:session:add')")
    @Log(title = "谈话会话管理", businessType = BusinessType.INSERT)
    @RepeatSubmit(message = "请勿重复创建谈话")
    @PostMapping("/create")
    public AjaxResult createTalk(@RequestBody TalkSessionCreateRequest request) {
        TalkSession session = talkSessionService.createTalkWithRecords(request);
        return success(session);
    }

    /**
     * 批量导出谈话记录为 .docx 文档（打包zip）
     */
    @PreAuthorize("@ss.hasPermi('talk:session:export')")
    @Log(title = "谈话会话管理", businessType = BusinessType.EXPORT)
    @PostMapping("/exportDocx/batch")
    public void exportDocxBatch(@RequestBody Map<String, List<Long>> body, HttpServletResponse response) throws Exception {
        List<Long> sessionIds = body == null ? null : body.get("sessionIds");
        if (sessionIds == null || sessionIds.isEmpty()) {
            throw new ServiceException("sessionIds不能为空");
        }
        for (Long sessionId : sessionIds) {
            if (sessionId == null || talkSessionService.selectTalkSessionBySessionId(sessionId) == null) {
                throw new ServiceException("谈话会话不存在");
            }
        }
        ByteArrayOutputStream zip = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zip)) {
            for (Long sid : new java.util.LinkedHashSet<>(sessionIds)) {
                TalkSession session = talkSessionService.selectTalkSessionBySessionId(sid);
                byte[] docxBytes = talkDocxService.generateDocxBySession(sid);
                String name = com.ruoyi.talk.utils.ArchiveEntryNames.basename(session.getTalkPerson()) + "_" + sid
                        + ("group".equals(session.getTalkType()) ? ".zip" : ".docx");
                zos.putNextEntry(new ZipEntry(name));
                zos.write(docxBytes);
                zos.closeEntry();
                // 打包该会话的附件
                talkDocxService.writeAttachmentsToZip(zos, sid);
            }
        }
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode("谈话记录批量导出.zip", StandardCharsets.UTF_8));
        try (OutputStream os = response.getOutputStream()) {
            os.write(zip.toByteArray());
            os.flush();
        }
    }

    /**
     * 按选中的学生记录导出，不扩展到会话内其他学生。
     */
    @PreAuthorize("@ss.hasPermi('talk:session:export')")
    @Log(title = "学生谈话记录", businessType = BusinessType.EXPORT)
    @PostMapping("/exportDocx/records/batch")
    public void exportDocxRecordsBatch(@RequestBody Map<String, List<Long>> body,
            HttpServletResponse response) throws Exception {
        List<Long> recordIds = body == null ? null : body.get("recordIds");
        if (recordIds == null || recordIds.isEmpty()) {
            throw new ServiceException("请选择需要导出的学生谈话记录");
        }
        Map<Long, TalkStudentRecord> selectedRecords = new java.util.LinkedHashMap<>();
        for (Long recordId : new java.util.LinkedHashSet<>(recordIds)) {
            TalkStudentRecord record = recordId == null ? null
                    : talkStudentRecordMapper.selectTalkStudentRecordByRecordId(recordId);
            if (record == null) {
                throw new ServiceException("学生谈话记录不存在");
            }
            // 在生成任一文档之前校验全部会话的读取范围。
            if (talkSessionService.selectTalkSessionBySessionId(record.getSessionId()) == null
                    || record.getStudentId() == null) {
                throw new ServiceException("谈话记录关联的会话或学生不存在");
            }
            selectedRecords.put(recordId, record);
        }
        ByteArrayOutputStream zip = new ByteArrayOutputStream();
        try (ZipOutputStream output = new ZipOutputStream(zip)) {
            for (TalkStudentRecord record : selectedRecords.values()) {
                byte[] document = talkDocxService.generateDocxByRecord(record.getRecordId());
                String name = record.getRecordId() + "_"
                        + com.ruoyi.talk.utils.ArchiveEntryNames.basename(record.getStudentName()) + ".docx";
                output.putNextEntry(new ZipEntry(name));
                output.write(document);
                output.closeEntry();
            }
        }
        writeFileDownload(response, zip.toByteArray(), "学生谈话记录批量导出.zip", true);
    }

    @PreAuthorize("@ss.hasPermi('talk:session:export')")
    @Log(title = "学生谈话记录", businessType = BusinessType.EXPORT)
    @GetMapping("/exportDocx/record/{recordId}")
    public void exportDocxForRecord(@PathVariable Long recordId, HttpServletResponse response) throws Exception {
        byte[] document = talkDocxService.generateDocxByRecord(recordId);
        writeFileDownload(response, document, "学生谈话记录_" + recordId + ".docx", false);
    }

    /**
     * 导出集体谈话汇总表 (.docx)
     */
    @PreAuthorize("@ss.hasPermi('talk:session:export')")
    @Log(title = "谈话会话管理", businessType = BusinessType.EXPORT)
    @GetMapping("/exportGroupSummary/{sessionId}")
    public void exportGroupSummary(@PathVariable Long sessionId, HttpServletResponse response) throws Exception {
        TalkSession session = talkSessionService.selectTalkSessionBySessionId(sessionId);
        if (session == null) {
            writeJsonError(response, "会话不存在");
            return;
        }
        byte[] docxBytes = talkDocxService.generateGroupSummaryDocx(sessionId);
        String fileName = "集体谈话汇总表_" + session.getTalkPerson() + ".docx";
        writeFileDownload(response, docxBytes, fileName, false);
    }

    private void writeFileDownload(HttpServletResponse response, byte[] data, String fileName, boolean isZip)
            throws Exception {
        String contentType = isZip ? "application/zip"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        response.setContentType(contentType);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        try (OutputStream os = response.getOutputStream()) {
            os.write(data);
            os.flush();
        }
    }

    private void writeJsonError(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter()
                .write("{\"msg\":\"" + msg.replace("\\", "\\\\").replace("\"", "\\\"") + "\",\"code\":500}");
    }

    @PreAuthorize("@ss.hasPermi('talk:session:query')")
    @GetMapping("/detail/{sessionId:\\d+}")
    public AjaxResult getInfo(@PathVariable("sessionId") Long sessionId) {
        TalkSession session = talkSessionService.selectTalkSessionBySessionId(sessionId);
        return session == null ? error("谈话会话不存在") : success(session);
    }

    /**
     * 批量获取多个会话的标签（解决N+1查询问题）
     */
    @PreAuthorize("@ss.hasPermi('talk:session:query')")
    @GetMapping("/session-tags/batch")
    public AjaxResult getTagsBatch(@RequestParam String sessionIds) {
        return success(talkSessionService.selectTalkSessionTagsBatch(sessionIds));
    }

    @PreAuthorize("@ss.hasPermi('talk:session:query')")
    @GetMapping("/tags/{sessionId:\\d+}")
    public AjaxResult getTags(@PathVariable("sessionId") Long sessionId) {
        if (talkSessionService.selectTalkSessionBySessionId(sessionId) == null) {
            return error("谈话会话不存在");
        }
        return success(talkSessionService.selectTalkSessionTags(sessionId));
    }

    @PreAuthorize("@ss.hasPermi('talk:session:query')")
    @GetMapping("/participants/{sessionId:\\d+}")
    public AjaxResult getParticipants(@PathVariable("sessionId") Long sessionId) {
        if (talkSessionService.selectTalkSessionBySessionId(sessionId) == null) {
            return error("谈话会话不存在");
        }
        List<TalkStudentRecord> records = talkStudentRecordMapper.selectTalkStudentRecordBySessionId(sessionId);
        records.sort(Comparator.comparing(TalkStudentRecord::getStudentCode, Comparator.nullsLast(String::compareTo)));
        return success(records);
    }

    /**
     * 新增谈话会话管理
     */
    @PreAuthorize("@ss.hasPermi('talk:session:add')")
    @Log(title = "谈话会话管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TalkSession talkSession) {
        return toAjax(talkSessionService.insertTalkSession(talkSession));
    }

    /**
     * 修改谈话会话管理
     */
    @PreAuthorize("@ss.hasPermi('talk:session:edit')")
    @Log(title = "谈话会话管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TalkSession talkSession) {
        return toAjax(talkSessionService.updateTalkSession(talkSession));
    }

    /**
     * 删除谈话会话管理
     */
    @PreAuthorize("@ss.hasPermi('talk:session:remove')")
    @Log(title = "谈话会话管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sessionIds:[\\d,]+}")
    public AjaxResult remove(@PathVariable Long[] sessionIds) {
        return toAjax(talkSessionService.deleteTalkSessionBySessionIds(sessionIds));
    }
}
