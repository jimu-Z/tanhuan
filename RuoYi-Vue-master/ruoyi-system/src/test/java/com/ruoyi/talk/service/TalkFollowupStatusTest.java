package com.ruoyi.talk.service;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import com.ruoyi.talk.domain.*;
import com.ruoyi.talk.mapper.*;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.talk.service.impl.TalkStudentRecordServiceImpl;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.system.mapper.SysDeptMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class TalkFollowupStatusTest {
    TalkStudentRecordServiceImpl service;TalkStudentRecordMapper records;MockedStatic<TalkPermissionUtils> permissions;
    @BeforeEach void setup(){
        service=new TalkStudentRecordServiceImpl();records=mock(TalkStudentRecordMapper.class);ReflectionTestUtils.setField(service,"talkStudentRecordMapper",records);
        TalkSessionMapper sessions=mock(TalkSessionMapper.class);ReflectionTestUtils.setField(service,"talkSessionMapper",sessions);TalkSession session=new TalkSession();session.setSessionId(1L);session.setCollegeDeptId(100L);when(sessions.selectTalkSessionBySessionId(1L)).thenReturn(session);
        TalkStudentMapper students=mock(TalkStudentMapper.class);ReflectionTestUtils.setField(service,"talkStudentMapper",students);TalkStudent student=new TalkStudent();student.setStudentId(10L);student.setDeptId(100L);when(students.selectTalkStudentByStudentId(10L)).thenReturn(student);
        SysDeptMapper depts=mock(SysDeptMapper.class);ReflectionTestUtils.setField(service,"sysDeptMapper",depts);SysDept college=new SysDept();college.setDeptId(100L);college.setDeptType("college");when(depts.selectDeptById(100L)).thenReturn(college);
        TalkStudentRecord old=record(null);old.setFollowupStatus("legacy_code");when(records.selectTalkStudentRecordByRecordId(1L)).thenReturn(old);permissions=mockStatic(TalkPermissionUtils.class);
    }
    @AfterEach void close(){permissions.close();}
    TalkStudentRecord record(String status){TalkStudentRecord r=new TalkStudentRecord();r.setRecordId(1L);r.setSessionId(1L);r.setStudentId(10L);r.setFollowupStatus(status);return r;}
    @ParameterizedTest @ValueSource(strings={"unknown","","   "}) void unknownStatusIsRejectedOnInsertAndUpdate(String status){
        assertThatThrownBy(()->service.insertTalkStudentRecord(record(status))).hasMessageContaining("跟进状态");assertThatThrownBy(()->service.updateTalkStudentRecord(record(status))).hasMessageContaining("跟进状态");verify(records,never()).insertTalkStudentRecord(any());verify(records,never()).updateTalkStudentRecord(any());
    }
    @ParameterizedTest @ValueSource(strings={"none","pending","in_progress","completed"}) void knownStatusesRemainWritable(String status){service.insertTalkStudentRecord(record(status));service.updateTalkStudentRecord(record(status));verify(records).insertTalkStudentRecord(any());verify(records).updateTalkStudentRecord(any());}
    @Test void omittedStatusDoesNotRewriteLegacyValue(){TalkStudentRecord r=record(null);service.updateTalkStudentRecord(r);assertThat(r.getFollowupStatus()).isNull();verify(records).updateTalkStudentRecord(r);}
}
