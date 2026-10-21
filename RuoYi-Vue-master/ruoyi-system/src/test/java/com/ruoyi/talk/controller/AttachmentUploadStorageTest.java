package com.ruoyi.talk.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.nio.file.*;
import java.io.*;
import java.util.zip.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.talk.service.ITalkAttachmentService;
import com.ruoyi.talk.domain.TalkAttachment;

class AttachmentUploadStorageTest {
    Path root;String oldProfile,oldBase;
    TalkAttachmentController controller;ITalkAttachmentService attachments;
    @BeforeEach void setup() throws Exception {
        Files.createDirectories(Path.of("target"));root=Files.createTempDirectory(Path.of("target").toAbsolutePath(),"attachment-upload-");
        oldProfile=RuoYiConfig.getProfile();oldBase=FileUploadUtils.getDefaultBaseDir();
        new RuoYiConfig().setProfile(root.toString());FileUploadUtils.setDefaultBaseDir(root.toString());
        attachments=mock(ITalkAttachmentService.class);controller=new TalkAttachmentController();ReflectionTestUtils.setField(controller,"talkAttachmentService",attachments);
    }
    @AfterEach void restore() { new RuoYiConfig().setProfile(oldProfile);FileUploadUtils.setDefaultBaseDir(oldBase); }
    MockMultipartFile image() {return new MockMultipartFile("file","微信图片_20260813123124.jpg","image/jpeg",new byte[]{(byte)0xff,(byte)0xd8,(byte)0xff,1,2,3});}
    long files() throws Exception {try(var files=Files.walk(root)){return files.filter(Files::isRegularFile).count();}}

    @Test void metadataFailureRemovesSavedFile() throws Exception {
        assertThat(controller.upload(image(),1021L).get("code")).isEqualTo(500);
        assertThat(files()).isZero();
    }
    @Test void databaseExceptionAlsoRemovesSavedFile() throws Exception {
        when(attachments.insertTalkAttachment(any())).thenThrow(new IllegalStateException("DB unavailable"));
        assertThatThrownBy(()->controller.upload(image(),1021L)).isInstanceOf(IllegalStateException.class);
        assertThat(files()).isZero();
    }
    @Test void chineseImageNameIsPreservedWhileStorageFilenameUsesUuid() throws Exception {
        when(attachments.insertTalkAttachment(any())).thenReturn(1);
        var result=controller.upload(image(),1021L);assertThat(result.get("code")).isEqualTo(200);
        TalkAttachment saved=(TalkAttachment)result.get("data");
        assertThat(saved.getFileName()).isEqualTo("微信图片_20260813123124.jpg");
        assertThat(saved.getFilePath()).matches("/profile/upload/[0-9]{4}/[0-9]{2}/[0-9]{2}/[a-f0-9]{32}\\.jpg");
        assertThat(Files.readAllBytes(root.resolve(saved.getFilePath().substring("/profile/".length())))).isEqualTo(image().getBytes());
    }
    @Test void deniedSessionDoesNotWriteAnyFile() throws Exception {
        doThrow(new ServiceException("无权修改该谈话")).when(attachments).checkWriteAccess(1021L);
        assertThatThrownBy(()->controller.upload(image(),1021L)).isInstanceOf(ServiceException.class);
        assertThat(files()).isZero();verify(attachments,never()).insertTalkAttachment(any());
    }
    @Test void chineseWordDocumentUploadsAndHasExactBytes() throws Exception {
        ByteArrayOutputStream buffer=new ByteArrayOutputStream();
        try(ZipOutputStream zip=new ZipOutputStream(buffer)) {
            for(String name:new String[]{"[Content_Types].xml","word/document.xml"}) {zip.putNextEntry(new ZipEntry(name));zip.write("<xml/>".getBytes());zip.closeEntry();}
        }
        byte[] bytes=buffer.toByteArray();
        var document=new MockMultipartFile("file","商业计划书(2).docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document",bytes);
        when(attachments.insertTalkAttachment(any())).thenReturn(1);
        var result=controller.upload(document,1022L);assertThat(result.get("code")).isEqualTo(200);
        TalkAttachment saved=(TalkAttachment)result.get("data");
        assertThat(Files.readAllBytes(root.resolve(saved.getFilePath().substring("/profile/".length())))).isEqualTo(bytes);
    }
    @Test void blockedProfileReturnsClearErrorAndDoesNotSaveMetadata() throws Exception {
        Path blocked=Files.writeString(root.resolve("blocked"),"preserve");new RuoYiConfig().setProfile(blocked.toString());
        var result=controller.upload(image(),1021L);
        assertThat(result.get("code")).isEqualTo(500);assertThat(result.get("msg").toString()).contains("上传目录","权限");
        assertThat(Files.readString(blocked)).isEqualTo("preserve");verify(attachments,never()).insertTalkAttachment(any());
    }
    @Test void multipartHttpRequestCanUploadPngWithLongChineseName() throws Exception {
        var mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller).build();
        byte[] png=java.util.Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jA1cAAAAASUVORK5CYII=");
        MockMultipartFile file=new MockMultipartFile("file","图".repeat(80)+".png","image/png",png);
        when(attachments.insertTalkAttachment(any())).thenReturn(1);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/ruoyi-system/talkattachment/upload").file(file).param("sessionId","1023"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value(200));
        var saved=org.mockito.ArgumentCaptor.forClass(TalkAttachment.class);verify(attachments).insertTalkAttachment(saved.capture());
        assertThat(saved.getValue().getFileName()).isEqualTo(file.getOriginalFilename());
        assertThat(Files.readAllBytes(root.resolve(saved.getValue().getFilePath().substring("/profile/".length())))).isEqualTo(png);
    }
}
