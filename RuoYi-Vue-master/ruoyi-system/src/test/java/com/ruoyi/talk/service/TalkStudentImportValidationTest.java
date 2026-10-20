package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.talk.service.impl.TalkStudentServiceImpl;

class TalkStudentImportValidationTest {
    private TalkStudentServiceImpl service(TalkStudentMapper students, SysDeptMapper departments) {
        TalkStudentServiceImpl service = new TalkStudentServiceImpl();
        ReflectionTestUtils.setField(service, "talkStudentMapper", students);
        ReflectionTestUtils.setField(service, "sysDeptMapper", departments);
        return service;
    }

    @Test @SuppressWarnings("unchecked")
    void previewRejectsMissingRequiredStudentName() throws Exception {
        TalkStudentServiceImpl service = service(mock(TalkStudentMapper.class), mock(SysDeptMapper.class));
        byte[] bytes;
        try (InputStream input = Files.newInputStream(Path.of("..", "ruoyi-admin", "src", "main", "resources", "template", "student_import_template.xlsx"));
             Workbook workbook = WorkbookFactory.create(input); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.createRow(2);
            Map<String,String> values = Map.of("学号","QA001", "学院","测试学院", "年级","2026级", "班级","测试班");
            Row headers = sheet.getRow(1);
            for (int i=0; i<headers.getLastCellNum(); i++) {
                String value = values.get(headers.getCell(i).getStringCellValue());
                if (value != null) row.createCell(i).setCellValue(value);
            }
            workbook.write(output); bytes = output.toByteArray();
        }
        try (MockedStatic<TalkPermissionUtils> permissions = mockStatic(TalkPermissionUtils.class)) {
            permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(true);
            Map<String,Object> result = service.importPreview(new MockMultipartFile("file","students.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",bytes));
            List<Map<String,Object>> rows = (List<Map<String,Object>>) result.get("previewRows");
            assertThat(result.get("errorCount")).isEqualTo(1);
            assertThat(rows.get(0).get("status")).isEqualTo("error");
            assertThat(rows.get(0).get("message").toString()).contains("姓名");
        }
    }

    @Test @SuppressWarnings("unchecked")
    void existingStudentPreviewExplainsBothDuplicateModes() throws Exception {
        TalkStudentMapper students = mock(TalkStudentMapper.class);
        when(students.selectTalkStudentList(any(TalkStudent.class))).thenReturn(List.of(new TalkStudent()));
        TalkStudentServiceImpl service = service(students, mock(SysDeptMapper.class));
        byte[] bytes;
        try (Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(); Row headers=sheet.createRow(0); Row data=sheet.createRow(1);
            String[] labels={"学号","学生姓名","学院","年级","班级"};
            String[] values={"QA001","测试姓名","测试学院","2026级","测试班"};
            for(int i=0;i<labels.length;i++){headers.createCell(i).setCellValue(labels[i]);data.createCell(i).setCellValue(values[i]);}
            workbook.write(output); bytes=output.toByteArray();
        }
        try(MockedStatic<TalkPermissionUtils> permissions=mockStatic(TalkPermissionUtils.class)) {
            permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(true);
            Map<String,Object> result=service.importPreview(new MockMultipartFile("file","students.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",bytes));
            List<Map<String,Object>> rows=(List<Map<String,Object>>)result.get("previewRows");
            assertThat(rows.get(0).get("message").toString()).contains("跳过","覆盖","更新");
        }
    }
    @Test
    void updateModeSkipsLaterDuplicateAndPreservesFirstOccurrence() {
        TalkStudentMapper students = mock(TalkStudentMapper.class);
        SysDeptMapper departments = mock(SysDeptMapper.class);
        TalkStudentServiceImpl service = service(students, departments);
        TalkStudent existing = new TalkStudent(); existing.setStudentId(5L); existing.setStudentCode("QA001"); existing.setDeptId(203L);
        when(students.selectTalkStudentList(any(TalkStudent.class))).thenReturn(List.of(existing));
        when(departments.selectDeptList(any(SysDept.class))).thenAnswer(call -> {
            SysDept query = call.getArgument(0);
            SysDept match = new SysDept(); match.setDeptName(query.getDeptName()); match.setParentId(query.getParentId()); match.setDeptType("class");
            match.setDeptId("测试学院".equals(query.getDeptName()) ? 201L : "2026级".equals(query.getDeptName()) ? 202L : 203L);
            return List.of(match);
        });
        List<String> savedNames = new ArrayList<>();
        when(students.updateTalkStudent(any(TalkStudent.class))).thenAnswer(call -> { savedNames.add(((TalkStudent)call.getArgument(0)).getStudentName()); return 1; });
        Map<String,Object> first = new HashMap<>(Map.of("student_code","QA001","student_name","第一条","college","测试学院","grade","2026级","class","测试班"));
        Map<String,Object> duplicate = new HashMap<>(first); duplicate.put("student_name","重复行不应覆盖");
        try (MockedStatic<TalkPermissionUtils> permissions = mockStatic(TalkPermissionUtils.class)) {
            permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(true);
            Map<String,Object> result = service.importExecute(List.of(first,duplicate),"update");
            assertThat(result.get("successCount")).isEqualTo(1);
            assertThat(result.get("skipCount")).isEqualTo(1);
            assertThat(savedNames).containsExactly("第一条");
            assertThat(result.get("errors").toString()).contains("重复");
        }
    }
}