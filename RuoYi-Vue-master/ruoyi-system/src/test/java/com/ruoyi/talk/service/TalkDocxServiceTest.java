package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TalkDocxServiceTest {

    @Test
    void richContentIsReadableAndLegacyRecordSectionIsStillExcluded() {
        assertThat(TalkDocxService.stripTalkRecord("<p>共享 &amp; 正文</p><p>第二段<br>第三行</p><p>【谈话记录】历史反馈</p>"))
                .isEqualTo("共享 & 正文\n第二段\n第三行");
    }

    @Test
    void actualFallbackDocxDoesNotPrintRichTextMarkupOrChangeSavedContent() throws Exception {
        TalkDocxService service = new TalkDocxService();
        com.ruoyi.talk.domain.TalkSession session = new com.ruoyi.talk.domain.TalkSession();
        String content = "<p>中文 &amp; 正文</p><p>下一段</p>";
        session.setTalkContent(content);
        byte[] output = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "fallback", session, null, java.util.List.of());
        try (org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument(new java.io.ByteArrayInputStream(output))) {
            String text = document.getParagraphs().stream().map(org.apache.poi.xwpf.usermodel.XWPFParagraph::getText)
                    .collect(java.util.stream.Collectors.joining("\n"));
            assertThat(text).contains("中文 & 正文", "下一段").doesNotContain("<p>", "</p>", "&amp;");
        }
        assertThat(session.getTalkContent()).isEqualTo(content);
    }

    @Test
    void fallbackDocumentTranslatesJsonTagCodes() throws Exception {
        TalkDocxService service = new TalkDocxService();
        com.ruoyi.talk.domain.TalkSession session = new com.ruoyi.talk.domain.TalkSession();
        session.setTalkType("individual");
        com.ruoyi.talk.domain.TalkSessionTag tag = new com.ruoyi.talk.domain.TalkSessionTag();
        tag.setTagValue("[\"mental_health\",\"study_style\"]");
        byte[] output = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "fallback", session, null, java.util.List.of(tag));
        try (org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument(new java.io.ByteArrayInputStream(output))) {
            String text = document.getParagraphs().stream().map(org.apache.poi.xwpf.usermodel.XWPFParagraph::getText)
                    .collect(java.util.stream.Collectors.joining("\n"));
            assertThat(text).contains("心理健康教育与咨询", "学风建设").doesNotContain("mental_health", "study_style");
        }
    }

    @Test
    void exportPlaceholdersTranslateStudentCodes() {
        TalkDocxService service = new TalkDocxService();
        com.ruoyi.system.mapper.SysDictDataMapper dictionaries = org.mockito.Mockito.mock(com.ruoyi.system.mapper.SysDictDataMapper.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "sysDictDataMapper", dictionaries);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "talkStudentRecordMapper",
                org.mockito.Mockito.mock(com.ruoyi.talk.mapper.TalkStudentRecordMapper.class));
        com.ruoyi.talk.domain.TalkStudent student = new com.ruoyi.talk.domain.TalkStudent();
        student.setPoliticalStatus("party_member"); student.setMentalHealthStatus("healthy");
        student.setEnrollmentStatus("active"); student.setPovertyLevel("general");
        com.ruoyi.talk.domain.TalkSession session = new com.ruoyi.talk.domain.TalkSession();
        session.setTalkType("individual");
        java.util.Map<String, String> values = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                service, "vals", session, student, null, java.util.List.of());
        assertThat(values).containsEntry("${political_status}", "中共党员")
                .containsEntry("${mental_health}", "健康").containsEntry("${enrollment_status}", "在读")
                .containsEntry("${poverty_level}", "家庭经济一般困难");
        org.mockito.Mockito.when(dictionaries.selectDictLabel("mental_health_status", "healthy")).thenReturn("身心健康");
        values = org.springframework.test.util.ReflectionTestUtils.invokeMethod(service, "vals", session, student, null, java.util.List.of());
        assertThat(values).containsEntry("${mental_health}", "身心健康");
    }

    @Test
    void stripTalkRecordSectionRemovesTalkRecordAndEverythingAfter() {
        String content = "【谈话目的】针对违纪行为进行教育，引导学生认识错误并改正。\n\n"
                + "【谈话要点】\n"
                + "1. 明确违纪事实，听取学生陈述\n"
                + "2. 解读相关校规校纪\n"
                + "3. 引导学生认识错误的性质和后果\n"
                + "4. 共同制定改正计划\n"
                + "5. 告知后续处理流程\n\n"
                + "【谈话记录】张三在2026年5月13日因考试作弊被处分，经过谈话后认识到错误。";

        String result = TalkDocxService.stripTalkRecord(content);

        assertThat(result)
                .contains("【谈话目的】")
                .contains("【谈话要点】")
                .doesNotContain("【谈话记录】")
                .doesNotContain("张三在2026年5月13日");
    }

    @Test
    void stripTalkRecordSectionTrimsTrailingWhitespace() {
        String content = "【谈话目的】了解学生学习困难。\n\n"
                + "【谈话要点】\n"
                + "1. 回顾学习情况\n"
                + "\n\n"
                + "【谈话记录】学生反馈内容。";

        String result = TalkDocxService.stripTalkRecord(content);

        assertThat(result).doesNotEndWith("\n").doesNotEndWith(" ");
    }

    @Test
    void stripTalkRecordSectionWithoutTalkRecordReturnsOriginal() {
        String content = "【谈话目的】了解学生学习困难。\n\n"
                + "【谈话要点】\n"
                + "1. 回顾学习情况";

        String result = TalkDocxService.stripTalkRecord(content);

        assertThat(result).isEqualTo(content);
    }

    @Test
    void stripTalkRecordSectionWithNullOrEmptyReturnsInput() {
        assertThat(TalkDocxService.stripTalkRecord(null)).isNull();
        assertThat(TalkDocxService.stripTalkRecord("")).isEmpty();
        assertThat(TalkDocxService.stripTalkRecord("   ")).isEqualTo("   ");
    }

    @Test
    void stripTalkRecordSectionPlainTextWithoutMarkersReturnsOriginal() {
        String content = "今天和学生谈了关于考试作弊的问题，学生态度良好。";

        String result = TalkDocxService.stripTalkRecord(content);

        assertThat(result).isEqualTo(content);
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({"group, 集体谈话", "individual, 个别谈话"})
    void groupSummaryUsesChineseTalkTypeWithoutChangingStoredCode(String code, String label) throws Exception {
        TalkDocxService service = new TalkDocxService();
        com.ruoyi.talk.mapper.TalkSessionMapper sessions = org.mockito.Mockito.mock(com.ruoyi.talk.mapper.TalkSessionMapper.class);
        com.ruoyi.talk.mapper.TalkStudentRecordMapper records = org.mockito.Mockito.mock(com.ruoyi.talk.mapper.TalkStudentRecordMapper.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "talkSessionMapper", sessions);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "talkStudentRecordMapper", records);
        com.ruoyi.talk.domain.TalkSession session = new com.ruoyi.talk.domain.TalkSession();
        session.setTalkType(code);
        org.mockito.Mockito.when(sessions.selectTalkSessionBySessionId(1L)).thenReturn(session);
        org.mockito.Mockito.when(records.selectTalkStudentRecordBySessionId(1L)).thenReturn(java.util.List.of());
        byte[] output = service.generateGroupSummaryDocx(1L);
        try (org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument(new java.io.ByteArrayInputStream(output))) {
            String text = document.getParagraphs().stream().map(org.apache.poi.xwpf.usermodel.XWPFParagraph::getText)
                    .collect(java.util.stream.Collectors.joining("\n"));
            assertThat(text).contains("谈话类型: " + label).doesNotContain("谈话类型: " + code);
        }
        assertThat(session.getTalkType()).isEqualTo(code);
    }
}
