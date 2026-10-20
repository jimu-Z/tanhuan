package com.ruoyi.web.controller.system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeService;
import org.junit.jupiter.api.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;

class NoticePublishedPreviewTest {
    SysNoticeController controller;
    ISysNoticeService service;
    @BeforeEach void setup() {
        controller = new SysNoticeController(); service = mock(ISysNoticeService.class);
        ReflectionTestUtils.setField(controller, "noticeService", service);
    }
    Method reader() {
        Method method = Arrays.stream(SysNoticeController.class.getDeclaredMethods())
            .filter(m -> m.getName().equals("getPublishedInfo")).findFirst().orElse(null);
        assertThat(method).as("authenticated published notice reader endpoint").isNotNull();
        return method;
    }
    AjaxResult read() throws Exception { return (AjaxResult) reader().invoke(controller, 3L); }
    @Test void normalNoticeIsReadableWithoutManagementPermission() throws Exception {
        SysNotice notice = new SysNotice(); notice.setStatus("0");notice.setNoticeContent("<p>published</p>");
        when(service.selectNoticeById(3L)).thenReturn(notice);
        assertThat(read().get("data")).isSameAs(notice);
        assertThat(reader().getAnnotation(PreAuthorize.class)).isNull();
        assertThat(reader().getAnnotation(GetMapping.class).value()).containsExactly("/published/{noticeId}");
        verifyNoMoreInteractions(ignoreStubs(service));
    }
    @Test void stoppedNoticeIsNotDisclosed() throws Exception {
        SysNotice notice = new SysNotice();notice.setStatus("1");notice.setNoticeContent("secret draft");
        when(service.selectNoticeById(3L)).thenReturn(notice);
        assertThat(read().get("code")).isEqualTo(500);assertThat(read()).doesNotContainKey("data");
    }
    @Test void missingNoticeIsNotDisclosed() throws Exception {
        assertThat(read().get("code")).isEqualTo(500);assertThat(read()).doesNotContainKey("data");
    }
    @Test void unknownStatusIsNotPublished() throws Exception {
        when(service.selectNoticeById(3L)).thenReturn(new SysNotice());
        assertThat(read().get("code")).isEqualTo(500);
    }
    @Test void existingManagementReadStillRequiresPermission() throws Exception {
        assertThat(SysNoticeController.class.getMethod("getInfo",Long.class).getAnnotation(PreAuthorize.class).value())
            .isEqualTo("@ss.hasPermi('system:notice:query')");
    }
}
