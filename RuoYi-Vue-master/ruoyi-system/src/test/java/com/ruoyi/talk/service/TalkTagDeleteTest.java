package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import com.ruoyi.talk.domain.TalkTag;
import com.ruoyi.talk.mapper.TalkTagMapper;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.talk.service.impl.TalkTagServiceImpl;
import com.ruoyi.common.utils.SecurityUtils;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class TalkTagDeleteTest {
    TalkTagServiceImpl service;TalkTagMapper tags;MockedStatic<TalkPermissionUtils> permissions;MockedStatic<SecurityUtils> security;
    @BeforeEach void setup(){service=new TalkTagServiceImpl();tags=mock(TalkTagMapper.class);ReflectionTestUtils.setField(service,"talkTagMapper",tags);permissions=mockStatic(TalkPermissionUtils.class);security=mockStatic(SecurityUtils.class);security.when(SecurityUtils::getUsername).thenReturn("qa");TalkTag own=new TalkTag();own.setTagId(1L);own.setScopeType("college");own.setCollegeDeptId(100L);when(tags.selectTalkTagById(1L)).thenReturn(own);TalkTag system=new TalkTag();system.setTagId(2L);system.setScopeType("system");when(tags.selectTalkTagById(2L)).thenReturn(system);}
    @AfterEach void close(){security.close();permissions.close();}
    @Test void rejectedBatchDoesNotDeleteEarlierAllowedTag(){assertThatThrownBy(()->service.deleteTalkTagByIds(new Long[]{1L,2L})).hasMessageContaining("全校管理员");verify(tags,never()).updateTalkTag(any());}
    @Test void repeatedIdsAreDeletedOnce(){when(tags.updateTalkTag(any())).thenReturn(1);assertThat(service.deleteTalkTagByIds(new Long[]{1L,1L})).isEqualTo(1);verify(tags).updateTalkTag(any());}
}
