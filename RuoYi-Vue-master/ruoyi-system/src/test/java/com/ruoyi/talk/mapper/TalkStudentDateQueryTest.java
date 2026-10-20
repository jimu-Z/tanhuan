package com.ruoyi.talk.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.InputStream;
import java.util.List;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import com.ruoyi.talk.domain.TalkStudent;

class TalkStudentDateQueryTest {
    private Configuration configuration() throws Exception {
        Configuration config=new Configuration();
        config.getTypeAliasRegistry().registerAlias("TalkStudent",TalkStudent.class);
        try(InputStream xml=getClass().getResourceAsStream("/mapper/talk/TalkStudentMapper.xml")) {
            new XMLMapperBuilder(xml,config,"TalkStudentMapper.xml",config.getSqlFragments()).parse();
        }
        return config;
    }
    @Test
    void listAndExportKeepAllFiltersWhenFindingStudentsUntalkedDuringInclusiveDates() throws Exception {
        Configuration config=configuration();
        TalkStudent query=new TalkStudent();query.setStudentCode("QA910101");query.setGender("0");
        query.getParams().put("collegeDeptId",910001L);query.getParams().put("beginTime","2026-10-01");query.getParams().put("endTime","2026-10-31");
        for(String name:List.of("selectTalkStudentList","selectTalkStudentListWithLastTalk")) {
            BoundSql bound=config.getMappedStatement("com.ruoyi.talk.mapper.TalkStudentMapper."+name).getBoundSql(query);
            String sql=bound.getSql().toLowerCase().replaceAll("\\s+"," ");
            assertThat(sql).contains("not exists", "talk_student_record", "date_add(?, interval 1 day)", "s.student_code = ?", "s.gender = ?", "find_in_set");
            assertThat(bound.getParameterMappings()).extracting(m->m.getProperty()).contains("params.beginTime","params.endTime","params.collegeDeptId","studentCode","gender");
        }
    }
    @Test
    void emptyDateRangeDoesNotHidePreviouslyTalkedStudents() throws Exception {
        Configuration config=configuration();TalkStudent query=new TalkStudent();query.getParams().put("collegeDeptId",910001L);
        for(String name:List.of("selectTalkStudentList","selectTalkStudentListWithLastTalk")) {
            BoundSql bound=config.getMappedStatement("com.ruoyi.talk.mapper.TalkStudentMapper."+name).getBoundSql(query);
            assertThat(bound.getSql().toLowerCase()).doesNotContain("not exists");
            assertThat(bound.getParameterMappings()).extracting(m->m.getProperty()).doesNotContain("params.beginTime","params.endTime");
        }
    }
    @Test
    void selectedCollegeOrGradeIncludesDescendantClassesWithoutDroppingCollegeScope() throws Exception {
        Configuration config=configuration();TalkStudent query=new TalkStudent();query.setDeptId(910001L);query.getParams().put("collegeDeptId",910001L);
        for(String name:List.of("selectTalkStudentList","selectTalkStudentListWithLastTalk")) {
            BoundSql bound=config.getMappedStatement("com.ruoyi.talk.mapper.TalkStudentMapper."+name).getBoundSql(query);
            assertThat(bound.getParameterMappings().stream().filter(m->"deptId".equals(m.getProperty())).count()).isGreaterThanOrEqualTo(2);
            assertThat(bound.getParameterMappings()).extracting(m->m.getProperty()).contains("params.collegeDeptId");
        }
    }

    @Test void historicalChineseAliasesAreBoundAlongsideCodesForListAndExport() throws Exception {
        Configuration config=configuration();TalkStudent q=new TalkStudent();q.setGender("0");q.setPoliticalStatus("people");q.setEnrollmentStatus("active");q.setMentalHealthStatus("healthy");q.setPovertyLevel("severe");
        for(String key:List.of("genderLabel","politicalStatusLabel","enrollmentStatusLabel","mentalHealthStatusLabel","povertyLevelLabel"))q.getParams().put(key,"历史中文标签");
        for(String name:List.of("selectTalkStudentList","selectTalkStudentListWithLastTalk")) {
            BoundSql bound=config.getMappedStatement("com.ruoyi.talk.mapper.TalkStudentMapper."+name).getBoundSql(q);
            assertThat(bound.getParameterMappings()).extracting(m->m.getProperty()).contains("params.genderLabel","params.politicalStatusLabel","params.enrollmentStatusLabel","params.mentalHealthStatusLabel","params.povertyLevelLabel");
        }
    }

}