package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.talk.domain.TalkTemplate;
import com.ruoyi.talk.mapper.TalkTemplateMapper;
import com.ruoyi.talk.service.impl.TalkTemplateServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

class TalkTemplateTagsTest {
    TalkTemplateServiceImpl service; TalkTemplateMapper templates; MockedStatic<SecurityUtils> security;
    @BeforeEach void setup() { service=new TalkTemplateServiceImpl();templates=mock(TalkTemplateMapper.class);ReflectionTestUtils.setField(service,"talkTemplateMapper",templates);security=mockStatic(SecurityUtils.class);security.when(SecurityUtils::isAdmin).thenReturn(true);security.when(SecurityUtils::getUsername).thenReturn("qa"); }
    @AfterEach void close() { security.close(); }
    @Test void commaTagsEscapeQuotesAndBackslashes() throws Exception {
        TalkTemplate t=new TalkTemplate();t.setTemplateTags("说\"明,路径\\文件");service.insertTalkTemplate(t);
        assertThat(new ObjectMapper().readTree(t.getTemplateTags()).get(0).asText()).isEqualTo("说\"明");
        assertThat(new ObjectMapper().readTree(t.getTemplateTags()).get(1).asText()).isEqualTo("路径\\文件");
    }
    @Test void malformedArrayRejectedBeforeWrite() { TalkTemplate t=new TalkTemplate();t.setTemplateTags("[坏格式");assertThatThrownBy(()->service.insertTalkTemplate(t)).isInstanceOf(ServiceException.class).hasMessageContaining("标签格式");verify(templates,never()).insertTalkTemplate(any()); }
    @Test void nonStringArrayRejectedBeforeWrite() { TalkTemplate t=new TalkTemplate();t.setTemplateTags("[\"学习\",42]");assertThatThrownBy(()->service.insertTalkTemplate(t)).isInstanceOf(ServiceException.class).hasMessageContaining("标签格式");verify(templates,never()).insertTalkTemplate(any()); }
    @Test void validArrayAndEmptyTagsRemainUsable() throws Exception { TalkTemplate t=new TalkTemplate();t.setTemplateTags("[\"学习\",\"健康\"]");service.insertTalkTemplate(t);assertThat(new ObjectMapper().readTree(t.getTemplateTags())).hasSize(2);t.setTemplateTags("");service.insertTalkTemplate(t);assertThat(t.getTemplateTags()).isEmpty(); }
}
