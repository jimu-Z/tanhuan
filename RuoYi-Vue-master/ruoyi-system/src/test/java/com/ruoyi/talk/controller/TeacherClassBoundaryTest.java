package com.ruoyi.talk.controller;

import com.ruoyi.talk.domain.TalkTeacher;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.talk.service.ITalkTeacherClassService;
import com.ruoyi.talk.service.ITalkTeacherService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TeacherClassBoundaryTest {
    TalkTeacherController controller;
    ITalkTeacherService teachers;
    ITalkTeacherClassService classes;
    MockedStatic<TalkPermissionUtils> permissions;

    @BeforeEach void setup() {
        controller = new TalkTeacherController();
        teachers = mock(ITalkTeacherService.class);
        classes = mock(ITalkTeacherClassService.class);
        ReflectionTestUtils.setField(controller, "teacherService", teachers);
        ReflectionTestUtils.setField(controller, "teacherClassService", classes);
        TalkTeacher teacher = new TalkTeacher();
        teacher.setTeacherId(10L); teacher.setTeacherCode("qa_teacher"); teacher.setDeptId(100L);
        when(teachers.selectTalkTeacherById(10L)).thenReturn(teacher);
        when(teachers.selectClassNamesByCollegeDept(100L)).thenReturn(List.of("本院班级", "空班级"));
        permissions = mockStatic(TalkPermissionUtils.class);
    }
    @AfterEach void close() { permissions.close(); }

    @Test void foreignClassIsRejectedBeforeAnyAssociationWrite() {
        assertThat(controller.saveTeacherClasses(10L, List.of("外院班级")).get("code")).isEqualTo(500);
        verifyNoInteractions(classes);
    }
    @Test void mixedOwnAndForeignSelectionDoesNotPartiallyReplaceAssignments() {
        assertThat(controller.saveTeacherClasses(10L, List.of("本院班级", "外院班级")).get("code")).isEqualTo(500);
        verifyNoInteractions(classes);
    }
    @Test void nullEntryIsRejectedBeforeReplacingExistingAssignments() {
        assertThat(controller.saveTeacherClasses(10L, Arrays.asList("本院班级", null)).get("code")).isEqualTo(500);
        verifyNoInteractions(classes);
    }
    @Test void sameCollegeSelectionAllowsEmptyClassesAndDeduplicatesNames() {
        assertThat(controller.saveTeacherClasses(10L, List.of("本院班级", "空班级", "本院班级")).get("code")).isEqualTo(200);
        verify(classes).saveTeacherClasses("qa_teacher", List.of("本院班级", "空班级"));
    }
    @Test void explicitEmptySelectionAndLegacyNullClearAssignments() {
        assertThat(controller.saveTeacherClasses(10L, List.of()).get("code")).isEqualTo(200);
        assertThat(controller.saveTeacherClasses(10L, null).get("code")).isEqualTo(200);
        verify(classes).saveTeacherClasses("qa_teacher", List.of());
        verify(classes).saveTeacherClasses("qa_teacher", null);
    }
    @Test void missingTeacherCannotChangeAnyAssignments() {
        assertThat(controller.saveTeacherClasses(99L, List.of("本院班级")).get("code")).isEqualTo(500);
        verifyNoInteractions(classes);
    }
}
