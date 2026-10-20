package com.ruoyi.talk.service;

import java.io.*;
import java.nio.file.Path;
import com.ruoyi.talk.utils.AttachmentPathUtils;
import com.ruoyi.talk.utils.ArchiveEntryNames;
import com.ruoyi.talk.util.TalkContentText;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.constant.TalkConstants;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.domain.TalkSessionTag;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.domain.TalkAttachment;
import com.ruoyi.talk.domain.TalkStudentRecord;
import com.ruoyi.talk.domain.TalkTag;
import com.ruoyi.talk.mapper.TalkAttachmentMapper;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.mapper.TalkSessionTagMapper;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;
import com.ruoyi.talk.mapper.TalkTagMapper;
import com.ruoyi.talk.security.TalkPermissionUtils;

@Service
public class TalkDocxService {

    private static final Logger log = LoggerFactory.getLogger(TalkDocxService.class);
    private static final com.fasterxml.jackson.databind.ObjectMapper TAG_JSON = new com.fasterxml.jackson.databind.ObjectMapper();
    private static final ThreadLocal<SimpleDateFormat> FMT = ThreadLocal
            .withInitial(() -> new SimpleDateFormat("yyyy年MM月dd日"));

    @Value("${talk.docx.template-path:templates/学生谈心谈话记录表.docx}")
    private String templatePath;

    @Autowired
    private TalkSessionMapper talkSessionMapper;
    @Autowired
    private TalkStudentMapper talkStudentMapper;
    @Autowired
    private TalkStudentRecordMapper talkStudentRecordMapper;
    @Autowired
    private TalkSessionTagMapper talkSessionTagMapper;
    @Autowired
    private TalkTagMapper talkTagMapper;
    @Autowired
    private TalkAttachmentMapper talkAttachmentMapper;
    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private com.ruoyi.system.mapper.SysDictDataMapper sysDictDataMapper;

    private String studentLabel(String dictType, String value, Map<String, String> fallback) {
        if (value == null || value.trim().isEmpty()) return "";
        String key = value.trim();
        String label = sysDictDataMapper.selectDictLabel(dictType, key);
        if (label != null && !label.trim().isEmpty()) return label;
        if (fallback.containsKey(key)) return fallback.get(key);
        return key.matches(".*[\\u3400-\\u9fff].*") ? key : "未知状态";
    }

    private List<String> tagValues(String value) {
        if (value == null || value.trim().isEmpty()) return Collections.emptyList();
        String raw = value.trim();
        if (raw.startsWith("[")) {
            try {
                return Arrays.asList(TAG_JSON.readValue(raw, String[].class));
            } catch (IOException ignored) { /* 兼容历史普通标签值 */ }
        }
        return Arrays.asList(raw.split(","));
    }

    private String tagDisplay(String value, Map<String, String> labels) {
        return tagValues(value).stream().filter(Objects::nonNull).map(String::trim).filter(key -> !key.isEmpty())
                .map(key -> labels.getOrDefault(key, TalkConstants.TAG_LABELS.getOrDefault(key,
                        key.matches(".*[\\u3400-\\u9fff].*") ? key : "未知标签")))
                .collect(Collectors.joining("、"));
    }

    private boolean templateOk() {
        try {
            ClassPathResource r = new ClassPathResource(templatePath);
            try (InputStream is = r.getInputStream()) {
                byte[] h = new byte[4];
                if (is.read(h) < 4)
                    return false;
                if (h[0] == (byte) 0xD0 && h[1] == (byte) 0xCF) {
                    log.warn("模板是旧版.doc格式，请用Word另存为.docx");
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("模板不可用: {}", e.getMessage());
            return false;
        }
    }

    public byte[] generateDocxBySession(Long sessionId) throws Exception {
        TalkSession session = talkSessionMapper.selectTalkSessionBySessionId(sessionId);
        if (session == null)
            throw new IllegalArgumentException("会话不存在");
        List<TalkStudentRecord> records = talkStudentRecordMapper.selectTalkStudentRecordBySessionId(sessionId);
        List<TalkSessionTag> tags = talkSessionTagMapper.selectTalkSessionTagBySessionId(sessionId);
        boolean useTemplate = templateOk();
        if (!useTemplate && !"group".equals(session.getTalkType()))
            return fallback(session, records, tags);

        if ("individual".equals(session.getTalkType())) {
            TalkStudentRecord rec = records.isEmpty() ? null : records.get(0);
            TalkStudent stu = rec != null ? talkStudentMapper.selectTalkStudentByStudentId(rec.getStudentId()) : null;
            return fill(session, stu, rec, tags);
        }
        ByteArrayOutputStream zip = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zip)) {
            int recordIndex = 0;
            for (TalkStudentRecord rec : records) {
                TalkStudent stu = talkStudentMapper.selectTalkStudentByStudentId(rec.getStudentId());
                if (stu == null)
                    continue;
                byte[] b = useTemplate ? fill(session, stu, rec, tags) : fallback(session, List.of(rec), tags);
                zos.putNextEntry(
                        new ZipEntry((++recordIndex) + "_" + stu.getStudentId() + "_" + ArchiveEntryNames.basename(stu.getStudentName()) + "_"
                                + (session.getTalkTime() != null ? FMT.get().format(session.getTalkTime()) : "未知")
                                + ".docx"));
                zos.write(b);
                zos.closeEntry();
            }
            // 打包附件
            writeAttachmentsToZip(zos, session.getSessionId());
        }
        return zip.toByteArray();
    }

    public byte[] generateDocxByStudent(Long studentId, Long sessionId) throws Exception {
        TalkSession session = talkSessionMapper.selectTalkSessionBySessionId(sessionId);
        if (session == null)
            throw new IllegalArgumentException("会话不存在");
        TalkPermissionUtils.requireCollegeAccess(session.getCollegeDeptId());
        List<TalkStudentRecord> records = talkStudentRecordMapper.selectTalkStudentRecordBySessionId(sessionId);
        TalkStudentRecord rec = records.stream().filter(r -> Objects.equals(r.getStudentId(), studentId)).findFirst()
                .orElse(null);
        if (rec == null) throw new IllegalArgumentException("当前会话没有该学生的谈话记录");
        return studentDocument(session, rec);
    }

    public byte[] generateDocxByRecord(Long recordId) throws Exception {
        TalkStudentRecord record = talkStudentRecordMapper.selectTalkStudentRecordByRecordId(recordId);
        if (record == null) throw new IllegalArgumentException("学生谈话记录不存在");
        TalkSession session = talkSessionMapper.selectTalkSessionBySessionId(record.getSessionId());
        if (session == null) throw new IllegalArgumentException("会话不存在");
        TalkPermissionUtils.requireCollegeAccess(session.getCollegeDeptId());
        return studentDocument(session, record);
    }

    private byte[] studentDocument(TalkSession session, TalkStudentRecord record) throws Exception {
        TalkStudent stu = talkStudentMapper.selectTalkStudentByStudentId(record.getStudentId());
        if (stu == null) throw new IllegalArgumentException("学生不存在");
        List<TalkSessionTag> tags = talkSessionTagMapper.selectTalkSessionTagBySessionId(session.getSessionId());
        if (!templateOk())
            return fallbackStudent(session, stu, record);
        return fill(session, stu, record, tags);
    }

    private byte[] fill(TalkSession session, TalkStudent stu, TalkStudentRecord rec, List<TalkSessionTag> tags)
            throws Exception {
        Map<String, String> vals = vals(session, stu, rec, tags);
        try (InputStream is = new ClassPathResource(templatePath).getInputStream();
                XWPFDocument doc = new XWPFDocument(is);
                ByteArrayOutputStream bos = new ByteArrayOutputStream()) {


            for (IBodyElement el : doc.getBodyElements())
                process(el, vals);
            for (XWPFHeader h : doc.getHeaderList())
                for (IBodyElement el : h.getBodyElements())
                    process(el, vals);
            for (XWPFFooter f : doc.getFooterList())
                for (IBodyElement el : f.getBodyElements())
                    process(el, vals);

            // 动态追加附件清单
            appendAttachmentList(doc, session.getSessionId());

            doc.write(bos);
            return bos.toByteArray();
        }
    }

    private void appendAttachmentList(XWPFDocument doc, Long sessionId) {
        List<TalkAttachment> attachments = talkAttachmentMapper.selectTalkAttachmentBySessionId(sessionId);
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        bl(doc);
        XWPFParagraph title = doc.createParagraph();
        XWPFRun tr = title.createRun();
        tr.setBold(true);
        tr.setFontSize(12);
        tr.setText("附件清单");
        for (TalkAttachment att : attachments) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun r = p.createRun();
            r.setFontSize(11);
            r.setText("• " + s(att.getFileName()) + " (" + formatFileSize(att.getFileSize()) + ")");
        }
    }

    private String formatFileSize(Long size) {
        if (size == null)
            return "未知大小";
        if (size < 1024)
            return size + " B";
        if (size < 1024 * 1024)
            return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }

    private void process(IBodyElement el, Map<String, String> vals) {
        if (el instanceof XWPFParagraph)
            tr((XWPFParagraph) el, vals);
        else if (el instanceof XWPFTable) {
            for (XWPFTableRow row : ((XWPFTable) el).getRows())
                for (XWPFTableCell cell : row.getTableCells())
                    for (IBodyElement cel : cell.getBodyElements())
                        process(cel, vals);
        }
    }

    private void tr(XWPFParagraph para, Map<String, String> vals) {
        List<XWPFRun> runs = para.getRuns();
        if (runs.isEmpty())
            return;
        String ff = runs.get(0).getFontFamily();
        int fs = runs.get(0).getFontSize();
        boolean bd = runs.get(0).isBold();
        String it = runs.get(0).getColor();

        StringBuilder sb = new StringBuilder();
        for (XWPFRun r : runs) {
            String t = r.getText(0);
            sb.append(t == null ? "" : t);
        }
        String txt = sb.toString();
        boolean hit = false;
        for (Map.Entry<String, String> e : vals.entrySet()) {
            if (txt.contains(e.getKey())) {
                txt = txt.replace(e.getKey(), e.getValue());
                hit = true;
            }
        }
        if (!hit)
            return;

        for (int i = runs.size() - 1; i >= 0; i--)
            para.removeRun(i);

        if (txt.contains("\n")) {
            String[] ls = txt.split("\n", -1);
            for (int i = 0; i < ls.length; i++) {
                if (i > 0) {
                    XWPFRun br = para.createRun();
                    br.addBreak();
                }
                XWPFRun r = para.createRun();
                r.setText(ls[i], 0);
                if (ff != null)
                    r.setFontFamily(ff);
                if (fs > 0)
                    r.setFontSize(fs);
                r.setBold(bd);
                if (it != null)
                    r.setColor(it);
            }
        } else {
            XWPFRun r = para.createRun();
            r.setText(txt, 0);
            if (ff != null)
                r.setFontFamily(ff);
            if (fs > 0)
                r.setFontSize(fs);
            r.setBold(bd);
            if (it != null)
                r.setColor(it);
        }
    }

    private Map<String, String> vals(TalkSession session, TalkStudent stu, TalkStudentRecord rec,
            List<TalkSessionTag> tags) {
        Map<String, String> m = new LinkedHashMap<>();

        if (stu != null) {
            m.put("${student_name}", s(stu.getStudentName()));
            m.put("${student_code}", s(stu.getStudentCode()));
            m.put("${gender}", "0".equals(stu.getGender()) ? "男" : "1".equals(stu.getGender()) ? "女" : "");
            m.put("${nation}", s(stu.getNation()));
            m.put("${phone}", s(stu.getPhone()));
            m.put("${political_status}", studentLabel("political_status", stu.getPoliticalStatus(), TalkConstants.POLITICAL_STATUS_LABELS));
            m.put("${dorm}", (s(stu.getDormBuilding()) + " " + s(stu.getDormRoom())).trim());
            if (stu.getDeptId() != null) {
                SysDept d = sysDeptMapper.selectDeptById(stu.getDeptId());
                m.put("${class_name}", d != null ? s(d.getDeptName()) : "");
            } else
                m.put("${class_name}", "");
        }

        m.put("${talk_type}", "group".equals(session.getTalkType()) ? "集体谈话" : "个别谈话");
        m.put("${talk_time}", session.getTalkTime() != null ? FMT.get().format(session.getTalkTime()) : "");
        m.put("${talk_person}", s(session.getTalkPerson()));
        m.put("${talk_content}", stripTalkRecord(s(session.getTalkContent())));

        // 标签：使用数据库驱动，hardcoded常量作为兜底
        Map<String, String> tagLabelMap = getTagLabelMap();
        Set<String> sel = tags != null ? tags.stream().flatMap(tag -> tagValues(tag.getTagValue()).stream()).filter(Objects::nonNull).map(String::trim).filter(key -> !key.isEmpty()).collect(Collectors.toSet())
                : Collections.emptySet();
        StringBuilder tb = new StringBuilder();
        for (Map.Entry<String, String> e : tagLabelMap.entrySet()) {
            if (tb.length() > 0)
                tb.append("\n");
            tb.append(sel.contains(e.getKey()) ? "☑ " : "☐ ").append(e.getValue());
        }
        m.put("${tags}", tb.toString());

        // 历次记录
        if (stu != null) {
            List<TalkStudentRecord> all = talkStudentRecordMapper
                    .selectTalkStudentRecordByStudentId(stu.getStudentId());
            StringBuilder hb = new StringBuilder();
            int n = 1;
            if (all != null)
                for (TalkStudentRecord r : all) {
                    TalkSession ts = talkSessionMapper.selectTalkSessionBySessionId(r.getSessionId());
                    if (ts == null)
                        continue;
                    if (hb.length() > 0)
                        hb.append("\n");
                    hb.append(n++).append(". ")
                            .append(ts.getTalkTime() != null ? FMT.get().format(ts.getTalkTime()) : "未知")
                            .append("  ")
                            .append("group".equals(ts.getTalkType()) ? "集体" : "个别").append(" | ")
                            .append(s(ts.getTalkPerson()));
                }
            m.put("${history}", hb.length() == 0 ? "无" : hb.toString());
        }

        if (rec != null) {
            m.put("${student_feedback}", s(rec.getStudentFeedback()));
            m.put("${followup_plan}", s(rec.getFollowupPlan()));
            m.put("${followup_status}",
                    studentLabel("followup_status", rec.getFollowupStatus(), TalkConstants.FOLLOWUP_STATUS_LABELS));
        }

        if (stu != null) {
            m.put("${enrollment_status}",
                    studentLabel("enrollment_status", stu.getEnrollmentStatus(), TalkConstants.ENROLLMENT_STATUS_LABELS));
            m.put("${mental_health}", studentLabel("mental_health_status", stu.getMentalHealthStatus(), TalkConstants.MENTAL_HEALTH_LABELS));
            m.put("${poverty_level}",
                    studentLabel("poverty_level", stu.getPovertyLevel(), TalkConstants.POVERTY_LEVEL_LABELS));
            m.put("${remark}", s(stu.getRemark()));
        }

        return m;
    }

    private Map<String, String> getTagLabelMap() {
        try {
            TalkTag query = new TalkTag();
            TalkPermissionUtils.applyCollegeScope(query);
            List<TalkTag> tags = talkTagMapper.selectActiveTags(query);
            if (tags != null && !tags.isEmpty()) {
                Map<String, String> m = new LinkedHashMap<>();
                for (TalkTag t : tags) {
                    m.put(t.getTagKey(), t.getTagName());
                }
                return m;
            }
        } catch (Exception e) {
            log.warn("从数据库读取标签失败，使用硬编码兜底: {}", e.getMessage());
        }
        return TalkConstants.TAG_LABELS;
    }

    public byte[] generateGroupSummaryDocx(Long sessionId) throws Exception {
        TalkSession session = talkSessionMapper.selectTalkSessionBySessionId(sessionId);
        if (session == null)
            throw new IllegalArgumentException("会话不存在");
        List<TalkStudentRecord> records = talkStudentRecordMapper.selectTalkStudentRecordBySessionId(sessionId);
        try (XWPFDocument d = new XWPFDocument(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            t(d, "集体谈话汇总表");
            bl(d);
            f(d, "谈话类型", "group".equals(session.getTalkType()) ? "集体谈话" : "个别谈话");
            f(d, "谈话时间", session.getTalkTime() != null ? FMT.get().format(session.getTalkTime()) : "");
            f(d, "谈话人", s(session.getTalkPerson()));
            f(d, "谈话地点", s(session.getTalkLocation()));
            bl(d);

            XWPFTable table = d.createTable(records.size() + 1, 7);
            String[] headers = { "序号", "姓名", "学号", "班级", "反馈", "跟进计划", "跟进状态" };
            for (int i = 0; i < headers.length; i++) {
                table.getRow(0).getCell(i).setText(headers[i]);
            }
            int idx = 1;
            for (TalkStudentRecord rec : records) {
                TalkStudent stu = talkStudentMapper.selectTalkStudentByStudentId(rec.getStudentId());
                XWPFTableRow row = table.getRow(idx);
                row.getCell(0).setText(String.valueOf(idx));
                row.getCell(1).setText(stu != null ? s(stu.getStudentName()) : "-");
                row.getCell(2).setText(stu != null ? s(stu.getStudentCode()) : "-");
                row.getCell(3).setText(stu != null && stu.getDeptId() != null
                        ? s(sysDeptMapper.selectDeptById(stu.getDeptId()).getDeptName())
                        : "-");
                row.getCell(4).setText(s(rec.getStudentFeedback()));
                row.getCell(5).setText(s(rec.getFollowupPlan()));
                row.getCell(6).setText(studentLabel("followup_status", rec.getFollowupStatus(), TalkConstants.FOLLOWUP_STATUS_LABELS));
                idx++;
            }
            bl(d);
            h(d, "谈话内容");
            c(d, stripTalkRecord(s(session.getTalkContent())));
            d.write(bos);
            return bos.toByteArray();
        }
    }

    private String s(String v) {
        return v == null ? "" : v;
    }

    static String stripTalkRecord(String content) {
        if (content == null) {
            return null;
        }
        content = TalkContentText.toPlainText(content);
        int idx = content.indexOf("【谈话记录】");
        if (idx < 0) {
            return content;
        }
        return content.substring(0, idx).replaceAll("\\s+$", "");
    }

    // ======== 程序化回退 ========

    private byte[] fallback(TalkSession session, List<TalkStudentRecord> records, List<TalkSessionTag> tags)
            throws Exception {
        try (XWPFDocument d = new XWPFDocument(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            t(d, "学生谈心谈话表");
            bl(d);
            f(d, "谈话类型", "group".equals(session.getTalkType()) ? "集体谈话" : "个别谈话");
            f(d, "谈话时间", session.getTalkTime() != null ? FMT.get().format(session.getTalkTime()) : "");
            f(d, "谈话人", s(session.getTalkPerson()));
            f(d, "标签",
                    tags != null
                            ? tags.stream()
                                    .map(t -> tagDisplay(t.getTagValue(), getTagLabelMap()))
                                    .collect(Collectors.joining("、"))
                            : "");
            h(d, "谈话内容");
            c(d, stripTalkRecord(s(session.getTalkContent())));
            bl(d);
            if (records != null)
                for (TalkStudentRecord r : records) {
                    TalkStudent stu = talkStudentMapper.selectTalkStudentByStudentId(r.getStudentId());
                    if (stu != null)
                        sb(d, stu, r);
                }
            d.write(bos);
            return bos.toByteArray();
        }
    }

    private byte[] fallbackStudent(TalkSession session, TalkStudent stu, TalkStudentRecord rec) throws Exception {
        try (XWPFDocument d = new XWPFDocument(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            t(d, "学生谈心谈话表");
            bl(d);
            f(d, "学号", s(stu.getStudentCode()));
            f(d, "姓名", s(stu.getStudentName()));
            f(d, "谈话类型", "group".equals(session.getTalkType()) ? "集体谈话" : "个别谈话");
            f(d, "谈话时间", session.getTalkTime() != null ? FMT.get().format(session.getTalkTime()) : "");
            f(d, "谈话人", s(session.getTalkPerson()));
            h(d, "谈话内容");
            c(d, stripTalkRecord(s(session.getTalkContent())));
            bl(d);
            if (rec != null) {
                h(d, "反馈");
                c(d, s(rec.getStudentFeedback()));
                h(d, "跟进计划");
                c(d, s(rec.getFollowupPlan()));
            }
            h(d, "备注");
            f(d, "学籍", studentLabel("enrollment_status", stu.getEnrollmentStatus(), TalkConstants.ENROLLMENT_STATUS_LABELS));
            f(d, "心理", studentLabel("mental_health_status", stu.getMentalHealthStatus(), TalkConstants.MENTAL_HEALTH_LABELS));
            f(d, "贫困",
                    studentLabel("poverty_level", stu.getPovertyLevel(), TalkConstants.POVERTY_LEVEL_LABELS));
            f(d, "备注", s(stu.getRemark()));
            d.write(bos);
            return bos.toByteArray();
        }
    }

    private void t(XWPFDocument d, String x) {
        XWPFParagraph p = d.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(18);
        r.setText(x);
    }

    private void h(XWPFDocument d, String x) {
        XWPFParagraph p = d.createParagraph();
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(14);
        r.setText(x);
    }

    private void f(XWPFDocument d, String l, String v) {
        XWPFParagraph p = d.createParagraph();
        XWPFRun lr = p.createRun();
        lr.setBold(true);
        lr.setFontSize(12);
        lr.setText(l + ": ");
        XWPFRun vr = p.createRun();
        vr.setFontSize(12);
        vr.setText(v);
    }

    private void c(XWPFDocument d, String x) {
        XWPFParagraph p = d.createParagraph();
        XWPFRun r = p.createRun();
        r.setFontSize(12);
        r.setText(x);
    }

    private void sb(XWPFDocument d, TalkStudent stu, TalkStudentRecord r) {
        bl(d);
        XWPFParagraph p = d.createParagraph();
        XWPFRun rn = p.createRun();
        rn.setBold(true);
        rn.setFontSize(12);
        rn.setText(stu.getStudentName() + " (" + stu.getStudentCode() + ")");
        f(d, "  反馈", s(r.getStudentFeedback()));
        f(d, "  计划", s(r.getFollowupPlan()));
    }

    private void bl(XWPFDocument d) {
        d.createParagraph();
    }

    public void writeAttachmentsToZip(ZipOutputStream zos, Long sessionId) throws IOException {
        List<TalkAttachment> attachments = talkAttachmentMapper.selectTalkAttachmentBySessionId(sessionId);
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        String profile = RuoYiConfig.getProfile();
        int attachmentIndex = 0;
        for (TalkAttachment att : attachments) {
            String filePath = att.getFilePath();
            if (filePath == null)
                continue;
            Path file;
            try {
                file = AttachmentPathUtils.resolveWithinProfile(profile, filePath);
            } catch (IOException | java.nio.file.InvalidPathException e) {
                log.warn("附件路径无效，跳过打包 {}: {}", att.getAttachmentId(), e.getMessage());
                continue;
            }
            if (file == null) continue;
            try (InputStream fis = java.nio.file.Files.newInputStream(file)) {
                zos.putNextEntry(new ZipEntry("attachments/" + sessionId + "_" + (++attachmentIndex) + "_"
                        + att.getAttachmentId() + "_" + ArchiveEntryNames.basename(att.getFileName())));
                byte[] buffer = new byte[8192];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
            } catch (IOException e) {
                log.warn("打包附件失败 {}: {}", att.getFileName(), e.getMessage());
            }
        }
    }
}
