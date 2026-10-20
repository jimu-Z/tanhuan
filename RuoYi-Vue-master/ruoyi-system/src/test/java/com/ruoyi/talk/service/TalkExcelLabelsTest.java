package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.ByteArrayInputStream;
import java.util.List;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.talk.domain.*;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;

class TalkExcelLabelsTest {
    private <T> String exported(Class<T> type, T row) throws Exception {
        try (MockedStatic<DictUtils> dictionaries = Mockito.mockStatic(DictUtils.class)) {
            dictionaries.when(() -> DictUtils.getDictLabel("mental_health_status", "healthy")).thenReturn("身心健康");
            MockHttpServletResponse response = new MockHttpServletResponse();
            new ExcelUtil<>(type).exportExcel(response, List.of(row), "导出验证");
            try (Workbook book = WorkbookFactory.create(new ByteArrayInputStream(response.getContentAsByteArray()))) {
                StringBuilder text = new StringBuilder();
                DataFormatter formatter = new DataFormatter();
                for (Sheet sheet : book) for (Row cells : sheet) for (Cell cell : cells) text.append(formatter.formatCellValue(cell)).append('|');
                return text.toString();
            }
        }
    }

    @Test void studentExcelTranslatesCodesAndUsesConfiguredDictionary() throws Exception {
        TalkStudent student = new TalkStudent();
        student.setPoliticalStatus("party_member"); student.setEnrollmentStatus("active");
        student.setMentalHealthStatus("healthy"); student.setPovertyLevel("general");
        assertThat(exported(TalkStudent.class, student)).contains("中共党员", "在读", "身心健康", "家庭经济一般困难")
                .doesNotContain("party_member", "active", "healthy", "general");
        assertThat(student.getEnrollmentStatus()).isEqualTo("active");
    }

    @Test void teacherExcelTranslatesRole() throws Exception {
        TalkTeacher teacher = new TalkTeacher(); teacher.setRoleKey("school_admin");
        assertThat(exported(TalkTeacher.class, teacher)).contains("校管理员").doesNotContain("school_admin");
        assertThat(teacher.getRoleKey()).isEqualTo("school_admin");
    }

    @Test void recordExcelTranslatesFollowup() throws Exception {
        TalkStudentRecord record = new TalkStudentRecord(); record.setFollowupStatus("in_progress");
        assertThat(exported(TalkStudentRecord.class, record)).contains("跟进中").doesNotContain("in_progress");
    }

    @Test void sessionExcelShowsRichContentAsReadableTextWithoutChangingSavedContent() throws Exception {
        TalkSession session = new TalkSession();
        String html = "<p>中文 &amp; 学业&#x8BA1;划</p><p>第二行<br>第三行</p>";
        session.setTalkContent(html);
        assertThat(exported(TalkSession.class, session)).contains("中文 & 学业计划\n第二行\n第三行")
                .doesNotContain("<p>", "</p>", "<br>", "&amp;", "&#x8BA1;");
        assertThat(session.getTalkContent()).isEqualTo(html);
    }

    @Test void sessionExcelPreservesPlainTextAndHandlesEmptyContent() throws Exception {
        TalkSession session = new TalkSession();
        session.setTalkContent("成绩 2 < 3，进度 5 > 4\n原始第二行");
        assertThat(exported(TalkSession.class, session)).contains(session.getTalkContent());
        session.setTalkContent(null);
        assertThat(exported(TalkSession.class, session)).contains("谈话内容");
        assertThat(session.getTalkContent()).isNull();
    }
    @Test void studentExcelPreservesChineseGenderAndTranslatesCanonicalGenderCodes() throws Exception {
        for(String[] sample:new String[][]{{"0","男"},{"1","女"},{"2","未知"},{"男","男"},{"女","女"}}) {
            TalkStudent student=new TalkStudent();student.setGender(sample[0]);
            assertThat(exported(TalkStudent.class,student)).contains("|"+sample[1]+"|");
            assertThat(student.getGender()).isEqualTo(sample[0]);
        }
    }
    @Test void studentExcelShowsClassNameInsteadOfInternalDepartmentId() throws Exception {
        TalkStudent student=new TalkStudent();student.setDeptId(910003L);student.setDeptName("QA班级");
        assertThat(exported(TalkStudent.class,student)).contains("班级","QA班级").doesNotContain("部门ID(班级)","910003");
        assertThat(student.getDeptId()).isEqualTo(910003L);
    }

}
