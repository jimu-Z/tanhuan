package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.talk.domain.TalkAttachment;
import com.ruoyi.talk.mapper.TalkAttachmentMapper;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

class TalkDocxAttachmentsTest {
    TalkDocxService service; TalkAttachmentMapper attachments; Path root,profile; String oldProfile;
    @BeforeEach void setup() throws Exception {
        root=Files.createTempDirectory(Path.of("target").toAbsolutePath(),"qa-docx-");profile=Files.createDirectory(root.resolve("uploads"));
        oldProfile=RuoYiConfig.getProfile();new RuoYiConfig().setProfile(profile.toString());
        service=new TalkDocxService();attachments=mock(TalkAttachmentMapper.class);ReflectionTestUtils.setField(service,"talkAttachmentMapper",attachments);
    }
    @AfterEach void restore(){new RuoYiConfig().setProfile(oldProfile);}
    TalkAttachment file(long id,String path,String name){TalkAttachment a=new TalkAttachment();a.setAttachmentId(id);a.setFilePath(path);a.setFileName(name);return a;}
    Map<String,String> export(List<TalkAttachment> files) throws Exception {
        when(attachments.selectTalkAttachmentBySessionId(1L)).thenReturn(files);ByteArrayOutputStream data=new ByteArrayOutputStream();
        try(ZipOutputStream zip=new ZipOutputStream(data)){service.writeAttachmentsToZip(zip,1L);}
        Map<String,String> result=new LinkedHashMap<>();try(ZipInputStream zip=new ZipInputStream(new ByteArrayInputStream(data.toByteArray()))){ZipEntry e;while((e=zip.getNextEntry())!=null){result.put(e.getName(),new String(zip.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8));}}return result;
    }
    @Test void exportSkipsOutsideUploadDirectoryAndKeepsValidFile() throws Exception {
        Files.writeString(root.resolve("outside.txt"),"PRIVATE");Files.writeString(profile.resolve("valid.txt"),"VALID");
        Map<String,String> result=export(List.of(file(1,"/profile/../outside.txt","秘密.txt"),file(2,"/profile/valid.txt","正常.txt")));
        assertThat(result).hasSize(1);assertThat(result.values()).containsExactly("VALID");assertThat(Files.readString(root.resolve("outside.txt"))).isEqualTo("PRIVATE");
    }
    @Test void missingAttachmentDoesNotBreakRemainingExport() throws Exception {
        Files.writeString(profile.resolve("valid.txt"),"VALID");assertThat(export(List.of(file(1,"/profile/missing.txt","丢失.txt"),file(2,"/profile/valid.txt","正常.txt"))).values()).containsExactly("VALID");
    }
    @Test void duplicateAttachmentNamesKeepBothFiles() throws Exception {
        Files.writeString(profile.resolve("one.txt"),"ONE");Files.writeString(profile.resolve("two.txt"),"TWO");
        Map<String,String> result=export(List.of(file(1,"/profile/one.txt","记录.txt"),file(2,"/profile/two.txt","记录.txt")));
        assertThat(result).hasSize(2);assertThat(result.values()).containsExactlyInAnyOrder("ONE","TWO");assertThat(result.keySet()).allMatch(n->n.contains("记录.txt"));
    }
    @Test void attachmentArchiveNamesCannotContainDirectories() throws Exception {
        Files.writeString(profile.resolve("one.txt"),"ONE");Files.writeString(profile.resolve("two.txt"),"TWO");
        Map<String,String> result=export(List.of(file(1,"/profile/one.txt","../../秘密.txt"),file(2,"/profile/two.txt","..\\目录\\记录.txt")));
        assertThat(result).hasSize(2);assertThat(result.keySet()).allMatch(n->n.startsWith("attachments/")&&!n.substring("attachments/".length()).contains("/")&&!n.contains("\\")&&!n.contains(".."));
    }
}
