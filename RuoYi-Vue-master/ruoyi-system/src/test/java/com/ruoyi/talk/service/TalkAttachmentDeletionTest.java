package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.file.*;
import java.util.List;
import com.ruoyi.talk.domain.*;
import com.ruoyi.talk.mapper.*;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.talk.service.impl.*;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class TalkAttachmentDeletionTest {
    Path temp;
    TalkAttachmentServiceImpl attachments;TalkSessionServiceImpl sessions;
    TalkAttachmentMapper files;TalkSessionMapper talks;MockedStatic<TalkPermissionUtils> permissions;
    @BeforeEach void setup() throws Exception {
        temp=Files.createTempDirectory(Path.of("target").toAbsolutePath(),"qa-attachment-");
        attachments=new TalkAttachmentServiceImpl();sessions=new TalkSessionServiceImpl();files=mock(TalkAttachmentMapper.class);talks=mock(TalkSessionMapper.class);
        for(Object service:List.of(attachments,sessions)){
            ReflectionTestUtils.setField(service,"talkAttachmentMapper",files);
            ReflectionTestUtils.setField(service,"talkSessionMapper",talks);
            ReflectionTestUtils.setField(service,"profile",Files.createDirectories(temp.resolve("uploads")).toString());
        }
        ReflectionTestUtils.setField(sessions,"talkStudentRecordMapper",mock(TalkStudentRecordMapper.class));
        ReflectionTestUtils.setField(sessions,"talkSessionTagMapper",mock(TalkSessionTagMapper.class));
        permissions=mockStatic(TalkPermissionUtils.class);
        TalkSession own=new TalkSession();own.setSessionId(1L);own.setCollegeDeptId(100L);own.setCreateBy("own");when(talks.selectTalkSessionBySessionId(1L)).thenReturn(own);
        TalkSession foreign=new TalkSession();foreign.setSessionId(2L);foreign.setCollegeDeptId(200L);foreign.setCreateBy("other");when(talks.selectTalkSessionBySessionId(2L)).thenReturn(foreign);
        permissions.when(()->TalkPermissionUtils.requireOwnedWrite(200L,"other")).thenThrow(new ServiceException("无权访问"));
    }
    @AfterEach void close(){permissions.close();}
    TalkAttachment file(long id,long sid,String path){TalkAttachment a=new TalkAttachment();a.setAttachmentId(id);a.setSessionId(sid);a.setFilePath(path);when(files.selectTalkAttachmentById(id)).thenReturn(a);return a;}
    @Test void maliciousPathCannotDeleteOutsideUploadRoot() throws Exception {
        Path victim=Files.writeString(temp.resolve("victim.pdf"),"preserve");file(1,1,"/profile/../victim.pdf");
        attachments.deleteTalkAttachmentById(1L);assertThat(victim).exists();
    }
    @Test void validAttachmentStillDeletesPhysicalFile() throws Exception {
        Path target=Files.writeString(temp.resolve("uploads/valid.pdf"),"test");file(1,1,"/profile/valid.pdf");
        attachments.deleteTalkAttachmentById(1L);assertThat(target).doesNotExist();
    }
    @Test void unauthorizedAttachmentBatchPreservesEarlierFile() throws Exception {
        Path target=Files.writeString(temp.resolve("uploads/valid.pdf"),"test");file(1,1,"/profile/valid.pdf");file(2,2,"/profile/other.pdf");
        assertThatThrownBy(()->attachments.deleteTalkAttachmentByIds(new Long[]{1L,2L})).hasMessageContaining("无权");
        assertThat(target).exists();verify(files,never()).deleteTalkAttachmentById(1L);
    }
    @Test void unauthorizedSessionBatchPreservesEarlierFile() throws Exception {
        Path target=Files.writeString(temp.resolve("uploads/valid.pdf"),"test");TalkAttachment a=file(1,1,"/profile/valid.pdf");when(files.selectTalkAttachmentBySessionId(1L)).thenReturn(List.of(a));
        assertThatThrownBy(()->sessions.deleteTalkSessionBySessionIds(new Long[]{1L,2L})).hasMessageContaining("无权");
        assertThat(target).exists();verify(files,never()).deleteTalkAttachmentBySessionId(1L);
    }
}
