package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.io.ByteArrayOutputStream;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.talk.domain.TalkStudent;
import com.ruoyi.talk.mapper.TalkStudentMapper;
import com.ruoyi.talk.security.TalkPermissionUtils;
import com.ruoyi.talk.service.impl.TalkStudentServiceImpl;

class TalkStudentImportEnumsTest {
    private TalkStudentServiceImpl service(TalkStudentMapper students,SysDeptMapper departments){
        TalkStudentServiceImpl s=new TalkStudentServiceImpl();
        ReflectionTestUtils.setField(s,"talkStudentMapper",students);
        ReflectionTestUtils.setField(s,"sysDeptMapper",departments);
        ReflectionTestUtils.setField(s,"talkAlertService",mock(ITalkAlertService.class));return s;
    }
    private void dictionary(MockedStatic<DictUtils> dictionaries){
        dictionaries.when(()->DictUtils.getDictValue("political_status","群众")).thenReturn("people");
        dictionaries.when(()->DictUtils.getDictValue("enrollment_status","休学")).thenReturn("suspended");
        dictionaries.when(()->DictUtils.getDictValue("mental_health_status","轻度干预")).thenReturn("mild_warning");
        // 实际字典标签与固定fallback不同：必须以当前系统字典为准。
        dictionaries.when(()->DictUtils.getDictValue("poverty_level","C.家庭经济一般困难")).thenReturn("severe");
    }
    @Test @SuppressWarnings("unchecked")
    void chineseTemplatePreviewNormalizesAllFiveEnumsUsingConfiguredDictionary() throws Exception {
        TalkStudentServiceImpl s=service(mock(TalkStudentMapper.class),mock(SysDeptMapper.class));
        byte[] bytes;
        try(Workbook workbook=new XSSFWorkbook();ByteArrayOutputStream output=new ByteArrayOutputStream()){
            Sheet sheet=workbook.createSheet();Row h=sheet.createRow(0),r=sheet.createRow(1);
            String[] labels={"学号","学生姓名","学院","年级","班级","性别","政治面貌","学籍状态","心理健康状态","贫困等级认定"};
            String[] values={"QA001","测试学生","测试学院","2026级","测试班","女","群众","休学","轻度干预","C.家庭经济一般困难"};
            for(int i=0;i<labels.length;i++){h.createCell(i).setCellValue(labels[i]);r.createCell(i).setCellValue(values[i]);}
            workbook.write(output);bytes=output.toByteArray();
        }
        try(MockedStatic<TalkPermissionUtils> permissions=mockStatic(TalkPermissionUtils.class);MockedStatic<DictUtils> dictionaries=mockStatic(DictUtils.class)){
            permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(true);dictionary(dictionaries);
            Map<String,Object> result=s.importPreview(new MockMultipartFile("file","students.xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",bytes));
            List<Map<String,Object>> rows=(List<Map<String,Object>>)result.get("previewRows");
            Map<String,String> data=(Map<String,String>)rows.get(0).get("data");
            assertThat(data).containsEntry("gender","1").containsEntry("political_status","people").containsEntry("enrollment_status","suspended").containsEntry("mental_health_status","mild_warning").containsEntry("poverty_level","severe");
        }
    }
    @Test
    void executeNormalizesChinesePayloadIndependentlyOfPreview(){
        TalkStudentMapper students=mock(TalkStudentMapper.class);SysDeptMapper departments=mock(SysDeptMapper.class);TalkStudentServiceImpl s=service(students,departments);
        TalkStudent existing=new TalkStudent();existing.setStudentId(5L);existing.setDeptId(203L);
        when(students.selectTalkStudentList(any(TalkStudent.class))).thenReturn(List.of(existing));
        when(departments.selectDeptList(any(SysDept.class))).thenAnswer(call->{SysDept q=call.getArgument(0),d=new SysDept();d.setDeptName(q.getDeptName());d.setParentId(q.getParentId());d.setDeptType("class");d.setDeptId("测试学院".equals(q.getDeptName())?201L:"2026级".equals(q.getDeptName())?202L:203L);return List.of(d);});
        Map<String,Object> data=new HashMap<>();data.put("student_code","QA001");data.put("student_name","测试学生");data.put("college","测试学院");data.put("grade","2026级");data.put("class","测试班");data.put("gender","女");data.put("political_status","群众");data.put("enrollment_status","休学");data.put("mental_health_status","轻度干预");data.put("poverty_level","C.家庭经济一般困难");
        try(MockedStatic<TalkPermissionUtils> permissions=mockStatic(TalkPermissionUtils.class);MockedStatic<DictUtils> dictionaries=mockStatic(DictUtils.class)){
            permissions.when(TalkPermissionUtils::isGlobalAdmin).thenReturn(true);dictionary(dictionaries);
            assertThat(s.importExecute(List.of(data),"update").get("successCount")).isEqualTo(1);
            ArgumentCaptor<TalkStudent> saved=ArgumentCaptor.forClass(TalkStudent.class);verify(students).updateTalkStudent(saved.capture());
            TalkStudent student=saved.getValue();assertThat(student.getGender()).isEqualTo("1");assertThat(student.getPoliticalStatus()).isEqualTo("people");assertThat(student.getEnrollmentStatus()).isEqualTo("suspended");assertThat(student.getMentalHealthStatus()).isEqualTo("mild_warning");assertThat(student.getPovertyLevel()).isEqualTo("severe");
        }
    }
}