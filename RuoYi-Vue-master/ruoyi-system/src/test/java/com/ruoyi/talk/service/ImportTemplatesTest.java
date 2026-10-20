package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.domain.TalkTeacher;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.talk.service.impl.TalkStudentServiceImpl;

/** Verifies that the downloadable forms can be read by the actual importers. */
class ImportTemplatesTest
{
    @Test
    void teacherTemplatePreservesAccountIdentifiersAndRecognizedFields() throws Exception
    {
        byte[] data = filledTemplate("teacher_import_template.xlsx", Map.of(
                "工号", "000123", "姓名", "测试教师", "角色", "院管理员", "所属学院", "测试学院",
                "岗位", "辅导员", "手机号码", "13800138000", "备注", "导入验证"));
        List<TalkTeacher> teachers = new ExcelUtil<>(TalkTeacher.class)
                .importExcel(new ByteArrayInputStream(data), 1);

        assertThat(teachers).hasSize(1);
        TalkTeacher teacher = teachers.get(0);
        assertThat(teacher.getTeacherCode()).isEqualTo("000123");
        assertThat(teacher.getTeacherName()).isEqualTo("测试教师");
        assertThat(teacher.getRoleKey()).isEqualTo("院管理员");
        assertThat(teacher.getDeptName()).isEqualTo("测试学院");
        assertThat(teacher.getPosition()).isEqualTo("辅导员");
        assertThat(teacher.getPhone()).isEqualTo("13800138000");
        assertThat(teacher.getRemark()).isEqualTo("导入验证");
    }

    @Test
    @SuppressWarnings("unchecked")
    void studentTemplateMapsGenderAndThreeDistinctPhoneColumns() throws Exception
    {
        Map<String, String> values = Map.ofEntries(
                Map.entry("学院", "测试学院"), Map.entry("年级", "2026级"),
                Map.entry("班级", "测试班"), Map.entry("学生姓名", "测试学生"),
                Map.entry("学号", "00001234"), Map.entry("性别", "1"),
                Map.entry("本人联系电话", "13800138000"),
                Map.entry("父亲联系电话", "13900139000"),
                Map.entry("母亲联系电话", "13700137000"),
                Map.entry("身份证号", "00000000000000000X"),
                Map.entry("政治面貌", "league_member"), Map.entry("学籍状态", "active"),
                Map.entry("心理健康状态", "mild_warning"), Map.entry("贫困等级认定", "general"));
        byte[] data = filledTemplate("student_import_template.xlsx", values);
        TalkStudentServiceImpl service = new TalkStudentServiceImpl();
        ReflectionTestUtils.setField(service, "talkStudentMapper", mock(TalkStudentMapper.class));
        SysDeptMapper departments = mock(SysDeptMapper.class);
        SysDept college = new SysDept();
        college.setDeptId(200L);
        college.setDeptName("测试学院");
        college.setDeptType("college");
        when(departments.selectDeptList(any(SysDept.class))).thenReturn(List.of(college));
        ReflectionTestUtils.setField(service, "sysDeptMapper", departments);

        try (MockedStatic<TalkPermissionUtils> permissions = mockStatic(TalkPermissionUtils.class))
        {
            permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(true);
            Map<String, Object> preview = service.importPreview(new MockMultipartFile(
                    "file", "students.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", data));
            List<Map<String, Object>> rows = (List<Map<String, Object>>) preview.get("previewRows");
            assertThat(rows).hasSize(1);
            Map<String, String> row = (Map<String, String>) rows.get(0).get("data");
            assertThat(row.get("gender")).isEqualTo("1");
            assertThat(row.get("student_code")).isEqualTo("00001234");
            assertThat(row.get("phone")).isEqualTo("13800138000");
            assertThat(row.get("father_phone")).isEqualTo("13900139000");
            assertThat(row.get("mother_phone")).isEqualTo("13700137000");
            assertThat(row.get("id_card")).isEqualTo("00000000000000000X");
            assertThat(row.get("political_status")).isEqualTo("league_member");
            assertThat(row.get("enrollment_status")).isEqualTo("active");
            assertThat(row.get("mental_health_status")).isEqualTo("mild_warning");
            assertThat(row.get("poverty_level")).isEqualTo("general");
            assertThat(preview.get("errorCount")).isEqualTo(0);
            assertThat(preview.get("totalRows")).isEqualTo(1);
        }
    }

    private byte[] filledTemplate(String name, Map<String, String> values) throws Exception
    {
        Path source = Path.of("..", "ruoyi-admin", "src", "main", "resources", "template", name);
        try (InputStream input = Files.newInputStream(source);
             Workbook workbook = WorkbookFactory.create(input);
             ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(1);
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
            Row headers = sheet.getRow(1);
            if (name.startsWith("student"))
            {
                assertThat(headers.getCell(0).getStringCellValue()).isEqualTo("学号");
                assertThat(headers.getCell(1).getStringCellValue()).isEqualTo("学生姓名");
            }
            Row data = sheet.createRow(2);
            for (int i = 0; i < headers.getLastCellNum(); i++)
            {
                String header = headers.getCell(i) == null ? "" : headers.getCell(i).getStringCellValue();
                String value = values.get(header);
                if (value != null)
                {
                    assertThat(sheet.getColumnStyle(i).getDataFormatString()).isEqualTo("@");
                    data.createCell(i).setCellValue(value);
                }
            }
            workbook.write(output);
            return output.toByteArray();
        }
    }
}
