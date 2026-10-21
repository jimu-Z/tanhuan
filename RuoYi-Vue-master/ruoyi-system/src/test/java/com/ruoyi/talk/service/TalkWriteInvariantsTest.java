package com.ruoyi.talk.service;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.util.*;
import com.ruoyi.talk.domain.*;
import com.ruoyi.talk.mapper.*;
import com.ruoyi.talk.service.impl.*;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
class TalkWriteInvariantsTest {
 TalkSessionServiceImpl sessions=new TalkSessionServiceImpl(); TalkStudentRecordServiceImpl records=new TalkStudentRecordServiceImpl();
 TalkSessionMapper talks=mock(TalkSessionMapper.class); TalkStudentRecordMapper rows=mock(TalkStudentRecordMapper.class);
 TalkSession existing; MockedStatic<TalkPermissionUtils> permission; MockedStatic<SecurityUtils> security;
 @BeforeEach void setup(){
  for(Object s:List.of(sessions,records)){ReflectionTestUtils.setField(s,"talkSessionMapper",talks);ReflectionTestUtils.setField(s,"talkStudentRecordMapper",rows);}
  existing=new TalkSession();existing.setSessionId(1L);existing.setCollegeDeptId(100L);existing.setCreateBy("teacher");existing.setTalkType("group");existing.setTalkPerson("老师");existing.setTalkTime(new Date());
  when(talks.selectTalkSessionBySessionId(1L)).thenReturn(existing);when(rows.selectRecordsForUpdate(1L)).thenReturn(List.of(row(10,100),row(11,101)));
  permission=mockStatic(TalkPermissionUtils.class);security=mockStatic(SecurityUtils.class);security.when(SecurityUtils::getUsername).thenReturn("teacher");
 }
 @AfterEach void close(){permission.close();security.close();}
 TalkStudentRecord row(long id,long student){TalkStudentRecord r=new TalkStudentRecord();r.setRecordId(id);r.setStudentId(student);r.setSessionId(1L);return r;}
 TalkSession change(String type){TalkSession r=new TalkSession();r.setSessionId(1L);r.setTalkType(type);return r;}
 @Test void rejectsUnknownType(){assertThatThrownBy(()->sessions.updateTalkSession(change("bad"))).hasMessageContaining("类型");verify(talks,never()).updateTalkSession(any());}
 @Test void rejectsIndividualWithTwoStudents(){assertThatThrownBy(()->sessions.updateTalkSession(change("individual"))).hasMessageContaining("一名");verify(talks,never()).updateTalkSession(any());}
 @Test void rejectsBlankPerson(){TalkSession r=change(null);r.setTalkPerson("  ");assertThatThrownBy(()->sessions.updateTalkSession(r)).hasMessageContaining("谈话人");}
 @Test void rejectsDuplicateStudents(){when(rows.selectRecordsForUpdate(1L)).thenReturn(List.of(row(10,100),row(11,100)));assertThatThrownBy(()->sessions.updateTalkSession(change(null))).hasMessageContaining("重复");}
 @Test void partialUpdatePreservesFields(){TalkSession r=change(null);r.setTalkContent("新正文");sessions.updateTalkSession(r);assertThat(r.getTalkType()).isEqualTo("group");assertThat(r.getTalkTime()).isEqualTo(existing.getTalkTime());assertThat(r.getTalkPerson()).isEqualTo("老师");assertThat(r.getCollegeDeptId()).isNull();}
 @Test void rejectsDeletingAllBeforeAnyWrite(){when(rows.selectTalkStudentRecordByRecordId(10L)).thenReturn(row(10,100));when(rows.selectTalkStudentRecordByRecordId(11L)).thenReturn(row(11,101));assertThatThrownBy(()->records.deleteTalkStudentRecordByRecordIds(new Long[]{10L,11L})).hasMessageContaining("至少");verify(rows,never()).deleteTalkStudentRecordByRecordIds(any());}
 @Test void deduplicatesDeletionIds(){when(rows.selectTalkStudentRecordByRecordId(10L)).thenReturn(row(10,100));records.deleteTalkStudentRecordByRecordIds(new Long[]{10L,10L});verify(rows).deleteTalkStudentRecordByRecordIds(new Long[]{10L});}
 @Test void rejectsLastRecordDeletion(){when(rows.selectTalkStudentRecordByRecordId(10L)).thenReturn(row(10,100));when(rows.selectRecordsForUpdate(1L)).thenReturn(List.of(row(10,100)));assertThatThrownBy(()->records.deleteTalkStudentRecordByRecordId(10L)).hasMessageContaining("至少");verify(rows,never()).deleteTalkStudentRecordByRecordId(anyLong());}
 @Test void rejectsDuplicateRecordInsert(){TalkStudentRecord r=row(0,100);assertThatThrownBy(()->records.insertTalkStudentRecord(r)).hasMessageContaining("重复");verify(rows,never()).insertTalkStudentRecord(any());}
 @Test void individualRejectsSecondRecord(){existing.setTalkType("individual");TalkStudentRecord r=row(0,999);assertThatThrownBy(()->records.insertTalkStudentRecord(r)).hasMessageContaining("一名");verify(rows,never()).insertTalkStudentRecord(any());}
}
