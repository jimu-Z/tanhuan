package com.ruoyi.framework.security;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.springframework.mock.web.*;
import com.ruoyi.framework.security.filter.ProfileResourceSecurityFilter;
import com.ruoyi.talk.service.ITalkAttachmentService;
class AttachmentResourceGuardTest {
 @Mock ITalkAttachmentService attachments; @InjectMocks ProfileResourceSecurityFilter filter; AutoCloseable mocks;
 @BeforeEach void setup(){mocks=MockitoAnnotations.openMocks(this);when(attachments.isRegisteredResource(anyString())).thenAnswer(c->c.getArgument(0,String.class) .contains("avatar") == false);}
 @AfterEach void close() throws Exception{mocks.close();}
 @ParameterizedTest @ValueSource(strings={"/profile/upload/private.pdf","/profile//upload/private.pdf","/profile/upload/%70rivate.pdf","/%70rofile/upload/private.pdf","/unrelated/../profile/upload/private.pdf"})
 void privateStaticRouteNeverReachesFileHandler(String uri) throws Exception{MockHttpServletRequest req=new MockHttpServletRequest("GET",uri);MockHttpServletResponse res=new MockHttpServletResponse();boolean[] reached={false};filter.doFilter(req,res,(a,b)->reached[0]=true);assertThat(reached[0]).isFalse();assertThat(res.getStatus()).isEqualTo(404);assertThat(res.getContentAsByteArray()).isEmpty();assertThat(res.getHeader("Cache-Control")).isEqualTo("no-store");}
 @ParameterizedTest @ValueSource(strings={"/common/download/resource","/%63ommon/download/resource"}) void commonDownloadCannotBypassGuard(String path) throws Exception{MockHttpServletRequest req=new MockHttpServletRequest("GET",path);req.setParameter("resource","/profile/upload/private.pdf");MockHttpServletResponse res=new MockHttpServletResponse();filter.doFilter(req,res,(a,b)->{throw new AssertionError("file handler reached");});assertThat(res.getStatus()).isEqualTo(404);assertThat(res.getContentAsByteArray()).isEmpty();}
 @Test void avatarRemainsAccessible() throws Exception{MockHttpServletResponse res=new MockHttpServletResponse();boolean[] reached={false};filter.doFilter(new MockHttpServletRequest("GET","/profile/avatar/face.png"),res,(a,b)->reached[0]=true);assertThat(reached[0]).isTrue();assertThat(res.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");}
}
