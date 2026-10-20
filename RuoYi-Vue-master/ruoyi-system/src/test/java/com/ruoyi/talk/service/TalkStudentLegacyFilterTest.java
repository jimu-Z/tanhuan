package com.ruoyi.talk.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.talk.service.impl.TalkStudentServiceImpl;
class TalkStudentLegacyFilterTest {
    @Test void canonicalFiltersAlsoMatchHistoricalChineseValuesWithoutRewritingRows(){
        TalkStudentMapper mapper=mock(TalkStudentMapper.class);TalkStudentServiceImpl service=new TalkStudentServiceImpl();ReflectionTestUtils.setField(service,"talkStudentMapper",mapper);
        TalkStudent q=new TalkStudent();q.setGender("0");q.setPoliticalStatus("people");q.setEnrollmentStatus("active");q.setMentalHealthStatus("healthy");q.setPovertyLevel("severe");
        try(MockedStatic<TalkPermissionUtils> permissions=mockStatic(TalkPermissionUtils.class);MockedStatic<DictUtils> dictionaries=mockStatic(DictUtils.class)){
            permissions.when(()->TalkPermissionUtils.applyCollegeScope(q)).thenAnswer(call->{q.getParams().put("collegeDeptId",910001L);return null;});
            dictionaries.when(()->DictUtils.getDictLabel("political_status","people")).thenReturn("群众");dictionaries.when(()->DictUtils.getDictLabel("enrollment_status","active")).thenReturn("在读");dictionaries.when(()->DictUtils.getDictLabel("mental_health_status","healthy")).thenReturn("健康");dictionaries.when(()->DictUtils.getDictLabel("poverty_level","severe")).thenReturn("C.家庭经济一般困难");
            service.selectTalkStudentListWithLastTalk(q);
            assertThat(q.getParams()).containsEntry("genderLabel","男").containsEntry("politicalStatusLabel","群众").containsEntry("enrollmentStatusLabel","在读").containsEntry("mentalHealthStatusLabel","健康").containsEntry("povertyLevelLabel","C.家庭经济一般困难").containsEntry("collegeDeptId",910001L);
            assertThat(q.getGender()).isEqualTo("0");verify(mapper).selectTalkStudentListWithLastTalk(q);
        }
    }
    @Test void aliasesAreServerDerivedAndClearedWhenFiltersAreEmpty(){
        TalkStudentMapper mapper=mock(TalkStudentMapper.class);TalkStudentServiceImpl service=new TalkStudentServiceImpl();ReflectionTestUtils.setField(service,"talkStudentMapper",mapper);TalkStudent q=new TalkStudent();
        for(String key:List.of("genderLabel","politicalStatusLabel","enrollmentStatusLabel","mentalHealthStatusLabel","povertyLevelLabel"))q.getParams().put(key,"untrusted label");
        try(MockedStatic<TalkPermissionUtils> permissions=mockStatic(TalkPermissionUtils.class)){
            service.selectTalkStudentList(q);
            assertThat(q.getParams()).doesNotContainKeys("genderLabel","politicalStatusLabel","enrollmentStatusLabel","mentalHealthStatusLabel","povertyLevelLabel");
        }
    }
}