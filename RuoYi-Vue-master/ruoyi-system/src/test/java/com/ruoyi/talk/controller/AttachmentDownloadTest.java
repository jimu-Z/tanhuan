package com.ruoyi.talk.controller;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.nio.file.*;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.talk.domain.TalkAttachment;
import com.ruoyi.talk.service.ITalkAttachmentService;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
class AttachmentDownloadTest {
 MockMvc mvc;Path root;String previous;ITalkAttachmentService service=mock(ITalkAttachmentService.class);
 @BeforeEach void setup() throws Exception{root=Files.createTempDirectory(Path.of("target").toAbsolutePath(),"download-");previous=RuoYiConfig.getProfile();new RuoYiConfig().setProfile(root.toString());Files.writeString(root.resolve("private.pdf"),"QA-private-bytes");TalkAttachment a=new TalkAttachment();a.setAttachmentId(5L);a.setSessionId(1L);a.setFileName("中文附件.pdf");a.setFilePath("/profile/private.pdf");a.setFileType("application/pdf");when(service.selectTalkAttachmentById(5L)).thenReturn(a);TalkAttachmentController c=new TalkAttachmentController();ReflectionTestUtils.setField(c,"talkAttachmentService",service);mvc=MockMvcBuilders.standaloneSetup(c).build();}
 @AfterEach void restore(){new RuoYiConfig().setProfile(previous);}
 @Test void authorizedDownloadHasOriginalNameAndNoCache() throws Exception{var r=mvc.perform(get("/ruoyi-system/talkattachment/download/5")).andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store")).andExpect(content().string("QA-private-bytes")).andReturn().getResponse();assertThat(r.getHeader("Content-Disposition")).contains("attachment","filename");verify(service).selectTalkAttachmentById(5L);}
 @Test void missingFileHasNoBytes() throws Exception{Files.delete(root.resolve("private.pdf"));mvc.perform(get("/ruoyi-system/talkattachment/download/5")).andExpect(status().isNotFound()).andExpect(content().string(""));}
}
