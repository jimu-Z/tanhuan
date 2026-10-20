package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.talk.domain.*;
import com.ruoyi.talk.mapper.*;
import com.ruoyi.talk.security.TalkPermissionUtils;

class TalkDocxSelectedRecordTest {
    TalkDocxService service;
    TalkStudentMapper students;
    TalkStudentRecordMapper records;
    MockedStatic<TalkPermissionUtils> permissions;
    @BeforeEach void setup() {
        service = new TalkDocxService();
        students = mock(TalkStudentMapper.class); records = mock(TalkStudentRecordMapper.class);
        TalkSessionMapper sessions = mock(TalkSessionMapper.class);
        TalkSessionTagMapper tags = mock(TalkSessionTagMapper.class);
        ReflectionTestUtils.setField(service, "templatePath", "qa-missing.docx");
        ReflectionTestUtils.setField(service, "talkSessionMapper", sessions);
        ReflectionTestUtils.setField(service, "talkStudentMapper", students);
        ReflectionTestUtils.setField(service, "talkStudentRecordMapper", records);
        ReflectionTestUtils.setField(service, "talkSessionTagMapper", tags);
        TalkSession session = new TalkSession(); session.setSessionId(10L); session.setTalkType("group"); session.setCollegeDeptId(100L);
        when(sessions.selectTalkSessionBySessionId(10L)).thenReturn(session);
        TalkStudent student = new TalkStudent(); student.setStudentId(101L); student.setStudentName("同一学生");
        when(students.selectTalkStudentByStudentId(101L)).thenReturn(student);
        TalkStudentRecord first = new TalkStudentRecord(); first.setRecordId(1L); first.setStudentId(101L); first.setSessionId(10L); first.setStudentFeedback("第一条反馈");
        TalkStudentRecord second = new TalkStudentRecord(); second.setRecordId(2L); second.setStudentId(101L); second.setSessionId(10L); second.setStudentFeedback("第二条反馈");
        when(records.selectTalkStudentRecordByRecordId(2L)).thenReturn(second);
        when(records.selectTalkStudentRecordBySessionId(10L)).thenReturn(List.of(first, second));
        when(tags.selectTalkSessionTagBySessionId(10L)).thenReturn(List.of());
        permissions = mockStatic(TalkPermissionUtils.class);
    }
    @AfterEach void close() { permissions.close(); }
    @Test void usesExactSelectedRecordEvenForRepeatedStudent() throws Exception {
        byte[] output = ReflectionTestUtils.invokeMethod(service, "generateDocxByRecord", 2L);
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(output))) {
            String text = document.getParagraphs().stream().map(p -> p.getText()).collect(java.util.stream.Collectors.joining("\n"));
            assertThat(text).contains("第二条反馈").doesNotContain("第一条反馈");
        }
        permissions.verify(() -> TalkPermissionUtils.requireCollegeAccess(100L));
    }
    @Test void rejectsNonParticipantWithoutReadingTheirStudentDetails() {
        assertThatThrownBy(() -> service.generateDocxByStudent(999L, 10L)).isInstanceOf(IllegalArgumentException.class);
        verify(students, never()).selectTalkStudentByStudentId(999L);
    }
    @Test void defaultTemplateDoesNotAppendFeedbackPlanOrStatus() throws Exception {
        ReflectionTestUtils.setField(service, "templatePath", "templates/学生谈心谈话记录表.docx");
        ReflectionTestUtils.setField(service, "talkAttachmentMapper", mock(TalkAttachmentMapper.class));
        ReflectionTestUtils.setField(service, "talkTagMapper", mock(TalkTagMapper.class));
        ReflectionTestUtils.setField(service, "sysDictDataMapper", mock(com.ruoyi.system.mapper.SysDictDataMapper.class));
        TalkStudentRecord record = records.selectTalkStudentRecordByRecordId(2L);
        record.setFollowupPlan("第二条计划"); record.setFollowupStatus("pending");
        byte[] output = service.generateDocxByRecord(2L);
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(output))) {
            String text = new org.apache.poi.xwpf.extractor.XWPFWordExtractor(document).getText();
            assertThat(text).doesNotContain("第二条反馈", "第二条计划", "待跟进", "第一条反馈", "学生反馈与跟进");
        }
    }
}
