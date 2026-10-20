package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import java.util.List;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.talk.domain.*;
import com.ruoyi.talk.mapper.*;
import com.ruoyi.talk.service.impl.TalkSessionServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

class TalkTagBatchTest {
    TalkSessionServiceImpl service;TalkSessionMapper sessions;TalkSessionTagMapper tags;MockedStatic<SecurityUtils> security;
    @BeforeEach void setup(){service=new TalkSessionServiceImpl();sessions=mock(TalkSessionMapper.class);tags=mock(TalkSessionTagMapper.class);ReflectionTestUtils.setField(service,"talkSessionMapper",sessions);ReflectionTestUtils.setField(service,"talkSessionTagMapper",tags);security=mockStatic(SecurityUtils.class);security.when(SecurityUtils::isAdmin).thenReturn(true);}
    @AfterEach void close(){security.close();}
    @Test void batchUsesOneScopeQueryAndDeduplicatesIds(){
        TalkSession a=new TalkSession();a.setSessionId(1L);a.setCollegeDeptId(100L);TalkSession b=new TalkSession();b.setSessionId(2L);b.setCollegeDeptId(100L);
        when(sessions.selectTalkSessionBySessionIds(any())).thenReturn(List.of(a,b));
        when(tags.selectTagsBySessionIds(any())).thenReturn(List.of());
        service.selectTalkSessionTagsBatch("1,2,1");
        ArgumentCaptor<Long[]> ids=ArgumentCaptor.forClass(Long[].class);verify(sessions).selectTalkSessionBySessionIds(ids.capture());assertThat(ids.getValue()).containsExactly(1L,2L);
        verify(sessions,never()).selectTalkSessionBySessionId(any());verify(tags).selectTagsBySessionIds(List.of(1L,2L));
    }
    @Test void missingSessionCannotExposeOrphanTags(){when(sessions.selectTalkSessionBySessionIds(any())).thenReturn(List.of());assertThatThrownBy(()->service.selectTalkSessionTagsBatch("999")).hasMessageContaining("不存在");verifyNoInteractions(tags);}
    @Test void singleMissingSessionCannotExposeOrphanTags(){assertThatThrownBy(()->service.selectTalkSessionTags(999L)).hasMessageContaining("不存在");verifyNoInteractions(tags);}
    @ParameterizedTest @ValueSource(strings={"abc","0","-1","1,abc",","})
    void malformedIdsRejectedBeforeQuery(String value){assertThatThrownBy(()->service.selectTalkSessionTagsBatch(value)).hasMessageContaining("编号");verifyNoInteractions(tags);}
    @Test void emptyInputReturnsEmptyMapWithoutQueries(){assertThat(service.selectTalkSessionTagsBatch("")).isEmpty();verifyNoInteractions(sessions,tags);}
}
