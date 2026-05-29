package com.ruoyi.talk.controller;

import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.domain.TalkSessionCreateRequest;
import com.ruoyi.talk.service.ITalkSessionService;
import com.ruoyi.talk.service.TalkDocxService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 谈话会话管理Controller
 * 
 * @author admin
 * @date 2026-05-27
 */
@RestController
@RequestMapping("/ruoyi-system/talksession")
public class TalkSessionController extends BaseController {
    @Autowired
    private ITalkSessionService talkSessionService;

    @Autowired
    private TalkDocxService talkDocxService;

    @Autowired
    private com.ruoyi.talk.mapper.TalkSessionTagMapper talkSessionTagMapper;

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
        byte[] docxBytes = talkDocxService.generateDocxBySession(sessionId);
        TalkSession session = talkSessionService.selectTalkSessionBySessionId(sessionId);
        boolean isZip = docxBytes.length >= 2 && docxBytes[0] == 'P' && docxBytes[1] == 'K'
                && !(docxBytes.length >= 4 && docxBytes[2] == 0x03 && docxBytes[3] == 0x04);
        String ext = isZip ? ".zip" : ".docx";
        String contentType = isZip ? "application/zip"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        String fileName = "谈话记录_" + (session != null ? session.getTalkPerson() : sessionId) + ext;
        response.setContentType(contentType);
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        try (OutputStream os = response.getOutputStream()) {
            os.write(docxBytes);
            os.flush();
        }
    }

    /**
     * 导出单个学生的谈话记录为 .docx
     */
    @PreAuthorize("@ss.hasPermi('talk:session:export')")
    @Log(title = "谈话会话管理", businessType = BusinessType.EXPORT)
    @GetMapping("/exportDocx/{sessionId}/student/{studentId}")
    public void exportDocxForStudent(@PathVariable Long sessionId, @PathVariable Long studentId,
            HttpServletResponse response) throws Exception {
        byte[] docxBytes = talkDocxService.generateDocxByStudent(studentId, sessionId);
        TalkSession session = talkSessionService.selectTalkSessionBySessionId(sessionId);
        String fileName = "谈话记录_" + (session != null ? session.getTalkPerson() : sessionId) + ".docx";
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        try (OutputStream os = response.getOutputStream()) {
            os.write(docxBytes);
            os.flush();
        }
    }

    /**
     * 发起谈话（集体/个体）— 创建 session + N条 record
     */
    @PreAuthorize("@ss.hasPermi('talk:session:add')")
    @Log(title = "谈话会话管理", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public AjaxResult createTalk(@RequestBody TalkSessionCreateRequest request) {
        TalkSession session = talkSessionService.createTalkWithRecords(request);
        return success(session);
    }

    /**
     * 获取谈话会话管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('talk:session:export')")
    @Log(title = "谈话会话管理", businessType = BusinessType.EXPORT)
    @PostMapping("/exportDocx/batch")
    public void exportDocxBatch(@RequestBody List<Long> sessionIds, HttpServletResponse response) throws Exception {
        ByteArrayOutputStream zip = new ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(zip)) {
            for (Long sid : sessionIds) {
                try {
                    byte[] b = talkDocxService.generateDocxBySession(sid);
                    TalkSession s = talkSessionService.selectTalkSessionBySessionId(sid);
                    String name = (s != null ? s.getTalkPerson() : sid) + "_" + sid + ".docx";
                    zos.putNextEntry(new java.util.zip.ZipEntry(name));
                    zos.write(b);
                    zos.closeEntry();
                } catch (Exception e) {
                    log.warn("跳过导出失败的会话{}: {}", sid, e.getMessage());
                }
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

    @PreAuthorize("@ss.hasPermi('talk:session:query')")
    @GetMapping(value = "/{sessionId}")
    public AjaxResult getInfo(@PathVariable("sessionId") Long sessionId) {
        return success(talkSessionService.selectTalkSessionBySessionId(sessionId));
    }

    @PreAuthorize("@ss.hasPermi('talk:session:query')")
    @GetMapping("/tags/{sessionId}")
    public AjaxResult getTags(@PathVariable("sessionId") Long sessionId) {
        return success(talkSessionTagMapper.selectTalkSessionTagBySessionId(sessionId));
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
    @DeleteMapping("/{sessionIds}")
    public AjaxResult remove(@PathVariable Long[] sessionIds) {
        return toAjax(talkSessionService.deleteTalkSessionBySessionIds(sessionIds));
    }
}
