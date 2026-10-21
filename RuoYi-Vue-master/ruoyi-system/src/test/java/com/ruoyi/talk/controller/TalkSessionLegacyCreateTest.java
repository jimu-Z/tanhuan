package com.ruoyi.talk.controller;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.ruoyi.talk.domain.TalkSession;
import com.ruoyi.talk.service.ITalkSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
class TalkSessionLegacyCreateTest {
 @Test void legacyCreateCannotCommitAnEmptySession(){
  var service=mock(ITalkSessionService.class);var controller=new TalkSessionController();
  ReflectionTestUtils.setField(controller,"talkSessionService",service);
  var session=new TalkSession();session.setCollegeDeptId(100L);session.setTalkType("individual");
  when(service.insertTalkSession(session)).thenReturn(1);
  assertThat(controller.add(session).get("code")).isNotEqualTo(200);
  verify(service,never()).insertTalkSession(any());
 }
}
