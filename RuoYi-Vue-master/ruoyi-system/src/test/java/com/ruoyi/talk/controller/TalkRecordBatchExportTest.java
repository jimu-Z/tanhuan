package com.ruoyi.talk.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;
import java.io.ByteArrayInputStream;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.talk.domain.*;
import com.ruoyi.talk.mapper.TalkStudentRecordMapper;
import com.ruoyi.talk.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

class TalkRecordBatchExportTest {
    TalkSessionController controller;
    ITalkSessionService sessions;
    TalkDocxService documents;
    TalkStudentRecordMapper records;
    @BeforeEach void setup() throws Exception {
        controller = new TalkSessionController();
        sessions = mock(ITalkSessionService.class);
        documents = mock(TalkDocxService.class);
        records = mock(TalkStudentRecordMapper.class);
        ReflectionTestUtils.setField(controller, "talkSessionService", sessions);
        ReflectionTestUtils.setField(controller, "talkDocxService", documents);
        ReflectionTestUtils.setField(controller, "talkStudentRecordMapper", records);
        TalkSession session = new TalkSession(); session.setSessionId(10L); session.setTalkType("group");
        when(sessions.selectTalkSessionBySessionId(10L)).thenReturn(session);
        for (long id = 1; id <= 2; id++) {
            TalkStudentRecord record = new TalkStudentRecord(); record.setRecordId(id);
            record.setSessionId(10L); record.setStudentId(100L + id); record.setStudentName("学生" + id);
            when(records.selectTalkStudentRecordByRecordId(id)).thenReturn(record);
            when(documents.generateDocxByRecord(id)).thenReturn(("DOCX" + id).getBytes(StandardCharsets.UTF_8));
        }
    }
    void export(List<Long> ids, MockHttpServletResponse response) throws Exception {
        try {
            TalkSessionController.class.getMethod("exportDocxRecordsBatch", Map.class, HttpServletResponse.class)
                    .invoke(controller, Map.of("recordIds", ids), response);
        } catch (InvocationTargetException error) {
            if (error.getCause() instanceof Exception cause) throw cause;
            throw error;
        }
    }
    List<String> entries(MockHttpServletResponse response) throws Exception {
        List<String> contents = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                assertThat(entry.getName()).endsWith(".docx").doesNotContain("../", "\\");
                contents.add(new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return contents;
    }
    @Test void exportsOnlySelectedStudent() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse(); export(List.of(1L), response);
        assertThat(entries(response)).containsExactly("DOCX1");
        verify(documents, never()).generateDocxByRecord(2L);
        verify(documents, never()).generateDocxBySession(anyLong());
        assertThat(response.getContentType()).isEqualTo("application/zip");
    }
    @Test void preservesTwoStudentsInSameSessionAndDeduplicatesRecord() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse(); export(List.of(1L, 2L, 1L), response);
        assertThat(entries(response)).containsExactly("DOCX1", "DOCX2");
        verify(documents, times(1)).generateDocxByRecord(1L);
    }
    @Test void rejectsForeignSessionBeforeGeneratingAnyDocuments() throws Exception {
        TalkStudentRecord foreign = new TalkStudentRecord(); foreign.setRecordId(3L); foreign.setSessionId(99L);
        when(records.selectTalkStudentRecordByRecordId(3L)).thenReturn(foreign);
        when(sessions.selectTalkSessionBySessionId(99L)).thenThrow(new ServiceException("无权访问其他学院"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThatThrownBy(() -> export(List.of(1L, 3L), response)).hasMessageContaining("无权访问");
        verifyNoInteractions(documents); assertThat(response.getContentAsByteArray()).isEmpty();
    }
    @Test void rejectsMissingRecordWithoutPartialDownload() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThatThrownBy(() -> export(List.of(1L, 9L), response)).isInstanceOf(ServiceException.class);
        verifyNoInteractions(documents); assertThat(response.getContentAsByteArray()).isEmpty();
    }
    @Test void rejectsEmptySelection() {
        assertThatThrownBy(() -> export(List.of(), new MockHttpServletResponse())).isInstanceOf(ServiceException.class);
    }
    @Test void rejectsNullRecordId() {
        assertThatThrownBy(() -> export(Arrays.asList(1L, null), new MockHttpServletResponse())).isInstanceOf(ServiceException.class);
        verifyNoInteractions(documents);
    }
}
