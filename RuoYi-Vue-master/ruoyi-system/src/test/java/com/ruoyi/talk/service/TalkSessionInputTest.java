package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.util.*;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.domain.*;
import com.ruoyi.talk.mapper.*;
import com.ruoyi.talk.service.impl.TalkSessionServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class TalkSessionInputTest {
    TalkSessionServiceImpl service;TalkSessionMapper sessions;MockedStatic<SecurityUtils> security;
    @BeforeEach void setup(){
        service=new TalkSessionServiceImpl();sessions=mock(TalkSessionMapper.class);
        TalkStudentMapper students=mock(TalkStudentMapper.class);SysDeptMapper depts=mock(SysDeptMapper.class);
        ReflectionTestUtils.setField(service,"talkSessionMapper",sessions);
        ReflectionTestUtils.setField(service,"talkStudentMapper",students);
        ReflectionTestUtils.setField(service,"sysDeptMapper",depts);
        ReflectionTestUtils.setField(service,"talkSessionTagMapper",mock(TalkSessionTagMapper.class));
        ReflectionTestUtils.setField(service,"talkStudentRecordMapper",mock(TalkStudentRecordMapper.class));
        TalkStudent student=new TalkStudent();student.setStudentId(10L);student.setDeptId(200L);
        when(students.selectTalkStudentByStudentId(anyLong())).thenReturn(student);
        SysDept dept=new SysDept();dept.setDeptId(200L);dept.setDeptType("college");when(depts.selectDeptById(200L)).thenReturn(dept);
        security=mockStatic(SecurityUtils.class);security.when(SecurityUtils::isAdmin).thenReturn(true);
        security.when(SecurityUtils::getUsername).thenReturn("qa-admin");
    }
    @AfterEach void close(){security.close();}
    TalkSessionCreateRequest valid(){TalkSessionCreateRequest r=new TalkSessionCreateRequest();r.setTalkType("individual");r.setTalkTime("2026-10-12 12:30:00");r.setTalkPerson("老师");r.setStudentIds(List.of(10L));return r;}
    void rejected(TalkSessionCreateRequest r,String message){assertThatThrownBy(()->service.createTalkWithRecords(r)).hasMessageContaining(message);verify(sessions,never()).insertTalkSession(any());}
    @Test void unknownTypeRejectedBeforeWrite(){TalkSessionCreateRequest r=valid();r.setTalkType("unexpected");rejected(r,"谈话类型");}
    @ParameterizedTest @ValueSource(strings={"2026-02-31","not-a-date","2026-10-12 25:00:00",""})
    void invalidDateRejectedBeforeWrite(String date){TalkSessionCreateRequest r=valid();r.setTalkTime(date);rejected(r,"谈话日期");}
    @Test void repeatedParticipantsRejectedBeforeWrite(){TalkSessionCreateRequest r=valid();r.setTalkType("group");r.setStudentIds(List.of(10L,10L));rejected(r,"重复");}
    @Test void individualRejectsMultipleStudents(){TalkSessionCreateRequest r=valid();r.setStudentIds(List.of(10L,11L));rejected(r,"个别谈话");}
    @Test void foreignFeedbackItemRejectedBeforeWrite(){TalkSessionCreateRequest r=valid();TalkSessionCreateRequest.StudentDataItem item=new TalkSessionCreateRequest.StudentDataItem();item.setStudentId(999L);r.setStudentDataList(List.of(item));rejected(r,"参与学生");}
    @Test void nullFeedbackItemRejectedBeforeWrite(){TalkSessionCreateRequest r=valid();r.setStudentDataList(Arrays.asList((TalkSessionCreateRequest.StudentDataItem)null));rejected(r,"学生记录");}
    @Test void validDateAndParticipantsStillCreate(){assertThat(service.createTalkWithRecords(valid()).getTalkTime()).isNotNull();verify(sessions).insertTalkSession(any());}
    @Test void unknownFeedbackStatusRejectedBeforeWrite(){TalkSessionCreateRequest r=valid();TalkSessionCreateRequest.StudentDataItem item=new TalkSessionCreateRequest.StudentDataItem();item.setStudentId(10L);item.setFollowupStatus("unknown");r.setStudentDataList(List.of(item));rejected(r,"跟进状态");}
}
