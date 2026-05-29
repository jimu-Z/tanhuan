package com.ruoyi.talk.service;

import java.io.*;
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

import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.domain.TalkSessionTag;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.domain.TalkStudentRecord;
import com.ruoyi.talk.mapper.TalkSessionMapper;
import com.ruoyi.talk.mapper.TalkSessionTagMapper;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;

@Service
public class TalkDocxService {

    private static final Logger log = LoggerFactory.getLogger(TalkDocxService.class);
    private static final SimpleDateFormat FMT = new SimpleDateFormat("yyyy年MM月dd日");

    private static final Map<String, String> TAG_MAP = new LinkedHashMap<>();
    static {
        TAG_MAP.put("thought_education", "思想理论教育和价值引领");
        TAG_MAP.put("party_class", "党团和班级建设");
        TAG_MAP.put("study_style", "学风建设");
        TAG_MAP.put("daily_affairs", "日常事务");
        TAG_MAP.put("mental_health", "心理健康教育与咨询");
        TAG_MAP.put("crisis_response", "危机事件应对");
        TAG_MAP.put("career_guidance", "职业规划与就业创业指导");
    }

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
    private SysDeptMapper sysDeptMapper;

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
            log.info("模板不可用: {}", e.getMessage());
            return false;
        }
    }

    public byte[] generateDocxBySession(Long sessionId) throws Exception {
        TalkSession session = talkSessionMapper.selectTalkSessionBySessionId(sessionId);
        if (session == null)
            throw new IllegalArgumentException("会话不存在");
        List<TalkStudentRecord> records = talkStudentRecordMapper.selectTalkStudentRecordBySessionId(sessionId);
        List<TalkSessionTag> tags = talkSessionTagMapper.selectTalkSessionTagBySessionId(sessionId);
        if (!templateOk())
            return fallback(session, records, tags);

        if ("individual".equals(session.getTalkType()) || records.size() <= 1) {
            TalkStudentRecord rec = records.isEmpty() ? null : records.get(0);
            TalkStudent stu = rec != null ? talkStudentMapper.selectTalkStudentByStudentId(rec.getStudentId()) : null;
            return fill(session, stu, rec, tags);
        }
        ByteArrayOutputStream zip = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zip)) {
            for (TalkStudentRecord rec : records) {
                TalkStudent stu = talkStudentMapper.selectTalkStudentByStudentId(rec.getStudentId());
                if (stu == null)
                    continue;
                byte[] b = fill(session, stu, rec, tags);
                zos.putNextEntry(
                        new ZipEntry(stu.getStudentName() + "_" + FMT.format(session.getTalkTime()) + ".docx"));
                zos.write(b);
                zos.closeEntry();
            }
        }
        return zip.toByteArray();
    }

    public byte[] generateDocxByStudent(Long studentId, Long sessionId) throws Exception {
        TalkSession session = talkSessionMapper.selectTalkSessionBySessionId(sessionId);
        if (session == null)
            throw new IllegalArgumentException("会话不存在");
        TalkStudent stu = talkStudentMapper.selectTalkStudentByStudentId(studentId);
        if (stu == null)
            throw new IllegalArgumentException("学生不存在");
        List<TalkStudentRecord> records = talkStudentRecordMapper.selectTalkStudentRecordBySessionId(sessionId);
        TalkStudentRecord rec = records.stream().filter(r -> r.getStudentId().equals(studentId)).findFirst()
                .orElse(null);
        List<TalkSessionTag> tags = talkSessionTagMapper.selectTalkSessionTagBySessionId(sessionId);
        if (!templateOk())
            return fallbackStudent(session, stu, rec);
        return fill(session, stu, rec, tags);
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
            doc.write(bos);
            return bos.toByteArray();
        }
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
            m.put("${political_status}", s(stu.getPoliticalStatus()));
            m.put("${dorm}", (s(stu.getDormBuilding()) + " " + s(stu.getDormRoom())).trim());
            if (stu.getDeptId() != null) {
                SysDept d = sysDeptMapper.selectDeptById(stu.getDeptId());
                m.put("${class_name}", d != null ? s(d.getDeptName()) : "");
            } else
                m.put("${class_name}", "");
        }

        m.put("${talk_type}", "group".equals(session.getTalkType()) ? "集体谈话" : "个别谈话");
        m.put("${talk_time}", session.getTalkTime() != null ? FMT.format(session.getTalkTime()) : "");
        m.put("${talk_person}", s(session.getTalkPerson()));
        m.put("${talk_content}", s(session.getTalkContent()));

        // 标签：列出全部7项，勾选已选
        Set<String> sel = tags != null ? tags.stream().map(TalkSessionTag::getTagValue).collect(Collectors.toSet())
                : Collections.emptySet();
        StringBuilder tb = new StringBuilder();
        for (Map.Entry<String, String> e : TAG_MAP.entrySet()) {
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
                    hb.append(n++).append(". ").append(ts.getTalkTime() != null ? FMT.format(ts.getTalkTime()) : "未知")
                            .append("  ")
                            .append("group".equals(ts.getTalkType()) ? "集体" : "个别").append(" | ")
                            .append(s(ts.getTalkPerson()))
                            .append(" | ").append(trunc(s(ts.getTalkContent()), 50));
                }
            m.put("${history}", hb.length() == 0 ? "无" : hb.toString());
        }

        if (rec != null) {
            m.put("${student_feedback}", s(rec.getStudentFeedback()));
            m.put("${followup_plan}", s(rec.getFollowupPlan()));
            m.put("${followup_status}",
                    Map.of("none", "无需跟进", "pending", "待跟进", "in_progress", "跟进中", "completed", "已完成")
                            .getOrDefault(rec.getFollowupStatus(), ""));
        }

        if (stu != null) {
            m.put("${enrollment_status}", lb(stu.getEnrollmentStatus(),
                    Map.of("active", "在读", "suspended", "休学", "withdrawn", "退学", "graduated", "毕业")));
            m.put("${mental_health}", lb(stu.getMentalHealthStatus(),
                    Map.of("normal", "正常", "weekly_track", "周跟踪", "monthly_track", "月跟踪")));
            m.put("${poverty_level}", lb(stu.getPovertyLevel(),
                    Map.of("none", "无", "general", "一般困难", "difficult", "困难", "severe", "特别困难")));
            m.put("${remark}", s(stu.getRemark()));
        }

        return m;
    }

    private String s(String v) {
        return v == null ? "" : v;
    }

    private String lb(String v, Map<String, String> map) {
        return v == null ? "" : map.getOrDefault(v, v);
    }

    private String trunc(String v, int max) {
        return v.length() > max ? v.substring(0, max) + "..." : v;
    }

    // ======== 程序化回退 ========

    private byte[] fallback(TalkSession session, List<TalkStudentRecord> records, List<TalkSessionTag> tags)
            throws Exception {
        try (XWPFDocument d = new XWPFDocument(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            t(d, "学生谈心谈话记录表");
            bl(d);
            f(d, "谈话类型", "group".equals(session.getTalkType()) ? "集体谈话" : "个别谈话");
            f(d, "谈话时间", FMT.format(session.getTalkTime()));
            f(d, "谈话人", s(session.getTalkPerson()));
            f(d, "标签", tags != null ? tags.stream().map(t -> TAG_MAP.getOrDefault(t.getTagValue(), t.getTagValue()))
                    .collect(Collectors.joining("、")) : "");
            h(d, "谈话内容");
            c(d, s(session.getTalkContent()));
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
            t(d, "学生谈心谈话记录表");
            bl(d);
            f(d, "学号", s(stu.getStudentCode()));
            f(d, "姓名", s(stu.getStudentName()));
            f(d, "谈话类型", "group".equals(session.getTalkType()) ? "集体谈话" : "个别谈话");
            f(d, "谈话时间", FMT.format(session.getTalkTime()));
            f(d, "谈话人", s(session.getTalkPerson()));
            h(d, "谈话内容");
            c(d, s(session.getTalkContent()));
            bl(d);
            if (rec != null) {
                h(d, "反馈");
                c(d, s(rec.getStudentFeedback()));
                h(d, "跟进计划");
                c(d, s(rec.getFollowupPlan()));
            }
            h(d, "备注");
            f(d, "学籍", s(stu.getEnrollmentStatus()));
            f(d, "心理", s(stu.getMentalHealthStatus()));
            f(d, "贫困", s(stu.getPovertyLevel()));
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
}
