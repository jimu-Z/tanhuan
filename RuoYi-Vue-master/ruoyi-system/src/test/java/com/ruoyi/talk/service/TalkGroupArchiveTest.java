package com.ruoyi.talk.service;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.ruoyi.talk.domain.*;
import com.ruoyi.talk.mapper.*;
import com.ruoyi.talk.controller.TalkSessionController;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

class TalkGroupArchiveTest {
    @Test void missingTemplateStillExportsSeparateStudentsAsGroupZip() throws Exception {
        TalkDocxService service=new TalkDocxService();ReflectionTestUtils.setField(service,"templatePath","qa-missing-template.docx");
        TalkSessionMapper sessions=mock(TalkSessionMapper.class);ReflectionTestUtils.setField(service,"talkSessionMapper",sessions);TalkSession session=new TalkSession();session.setSessionId(1L);session.setTalkType("group");when(sessions.selectTalkSessionBySessionId(1L)).thenReturn(session);
        TalkStudentRecordMapper records=mock(TalkStudentRecordMapper.class);ReflectionTestUtils.setField(service,"talkStudentRecordMapper",records);
        TalkStudentRecord r1=new TalkStudentRecord();r1.setStudentId(10L);TalkStudentRecord r2=new TalkStudentRecord();r2.setStudentId(11L);when(records.selectTalkStudentRecordBySessionId(1L)).thenReturn(List.of(r1,r2));
        TalkStudentMapper students=mock(TalkStudentMapper.class);ReflectionTestUtils.setField(service,"talkStudentMapper",students);
        for(long id:new long[]{10,11}){TalkStudent student=new TalkStudent();student.setStudentId(id);student.setStudentName("../../张三");when(students.selectTalkStudentByStudentId(id)).thenReturn(student);}
        TalkSessionTagMapper tags=mock(TalkSessionTagMapper.class);ReflectionTestUtils.setField(service,"talkSessionTagMapper",tags);TalkSessionTag coded=new TalkSessionTag();coded.setTagValue("[\"mental_health\",\"study_style\"]");TalkSessionTag chinese=new TalkSessionTag();chinese.setTagValue("学习辅导");when(tags.selectTalkSessionTagBySessionId(1L)).thenReturn(List.of(coded,chinese));
        ReflectionTestUtils.setField(service,"talkAttachmentMapper",mock(TalkAttachmentMapper.class));
        List<String> names=new ArrayList<>();try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(service.generateDocxBySession(1L)))){ZipEntry e;while((e=zip.getNextEntry())!=null){names.add(e.getName());byte[] data=zip.readAllBytes();assertThat(data).startsWith((byte)'P',(byte)'K');try(org.apache.poi.xwpf.usermodel.XWPFDocument document=new org.apache.poi.xwpf.usermodel.XWPFDocument(new ByteArrayInputStream(data))){String text=document.getParagraphs().stream().map(org.apache.poi.xwpf.usermodel.XWPFParagraph::getText).collect(java.util.stream.Collectors.joining("\n"));assertThat(text).contains("心理健康教育与咨询","学风建设","学习辅导").doesNotContain("mental_health","study_style");}}}
        assertThat(names).hasSize(2).doesNotHaveDuplicates().allMatch(n->n.endsWith(".docx")&&n.contains("张三")&&!n.contains("/")&&!n.contains("\\"));
    }
    @Test void batchGroupDocumentHasZipExtensionAndSafeName() throws Exception {
        TalkSessionController controller=new TalkSessionController();ITalkSessionService sessions=mock(ITalkSessionService.class);TalkDocxService docs=mock(TalkDocxService.class);ReflectionTestUtils.setField(controller,"talkSessionService",sessions);ReflectionTestUtils.setField(controller,"talkDocxService",docs);
        TalkSession session=new TalkSession();session.setSessionId(1L);session.setTalkType("group");session.setTalkPerson("../../老师");when(sessions.selectTalkSessionBySessionId(1L)).thenReturn(session);when(docs.generateDocxBySession(1L)).thenReturn(new byte[]{'P','K'});
        MockHttpServletResponse response=new MockHttpServletResponse();controller.exportDocxBatch(Map.of("sessionIds",List.of(1L,1L)),response);
        try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(response.getContentAsByteArray()))){assertThat(zip.getNextEntry().getName()).isEqualTo("老师_1.zip");assertThat(zip.getNextEntry()).isNull();}
    }
}
