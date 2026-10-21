package com.ruoyi.talk.service;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.file.*;
import java.util.*;
import com.ruoyi.talk.domain.*;
import com.ruoyi.talk.mapper.*;
import com.ruoyi.talk.service.impl.TalkAttachmentServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
class AttachmentResourceIdentityTest {
 TalkAttachmentServiceImpl service;Path root;
 @BeforeEach void setup() throws Exception{root=Files.createTempDirectory(Path.of("target").toAbsolutePath(),"identity-");Files.createDirectories(root.resolve("upload"));Files.writeString(root.resolve("upload/private.pdf"),"QA");service=new TalkAttachmentServiceImpl();var mapper=mock(TalkAttachmentMapper.class);ReflectionTestUtils.setField(service,"talkAttachmentMapper",mapper);ReflectionTestUtils.setField(service,"profile",root.toString());TalkAttachment a=new TalkAttachment();a.setFilePath("/profile//upload/private.pdf");when(mapper.selectTalkAttachmentList(any())).thenReturn(List.of(a));}
 @ParameterizedTest @ValueSource(strings={"/profile/upload/private.pdf","/profile//upload/private.pdf","/profile/upload/%70rivate.pdf","/profile/upload/%2570rivate.pdf","/profile/upload/../upload/private.pdf"})
 void canonicalAliasesArePrivate(String path){assertThat(service.isRegisteredResource(path)).isTrue();}
 @ParameterizedTest @ValueSource(strings={"/profile/../../outside.pdf","/profile/upload/%2e%2e/%2e%2e/escape.pdf","/profile/upload/%255c..%255coutside","/profile/upload/%00.pdf"})
 void invalidPathsFailClosed(String path){assertThat(service.isRegisteredResource(path)).isTrue();}
 @Test void unrelatedAvatarIsNotClassifiedAsTalkAttachment(){assertThat(service.isRegisteredResource("/profile/avatar/face.png")).isFalse();}
 @Test void absentPhysicalFileStillBlocksRegisteredUrl() throws Exception{Files.delete(root.resolve("upload/private.pdf"));assertThat(service.isRegisteredResource("/profile/upload/private.pdf")).isTrue();}
}
