package com.ruoyi.talk.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/** 统一查询与统计分析 SQL 回归测试。 */
class TalkAnalyticsMapperXmlTest {

    @Test
    void unifiedQueryShouldUseServerSideParameterizedFilters() {
        String xml = load("/mapper/talk/TalkStudentRecordMapper.xml");
        assertThat(xml)
                .contains("#{keyword}")
                .contains("#{talkPerson}")
                .contains("#{beginTalkTime}")
                .contains("#{endTalkTime}")
                .contains("#{tagValue}")
                .contains("json_contains")
                .contains("order by ts.talk_time desc")
                .doesNotContain("${");
    }

    @Test
    void coverageShouldCountDistinctStudentsWithinScope() {
        assertThat(load("/mapper/talk/TalkStudentRecordMapper.xml"))
                .contains("count(distinct tsr.student_id)")
                .contains("ts.college_dept_id = #{collegeDeptId}");
    }

    @Test
    void monthlyTrendShouldSelectLatestTwelveMonths() {
        assertThat(load("/mapper/talk/TalkSessionMapper.xml"))
                .contains("order by month desc")
                .contains("limit 12")
                .contains("order by recent.month asc");
    }

    @Test
    void collegeStudentCountShouldIncludeDescendants() {
        assertThat(load("/mapper/talk/TalkStudentMapper.xml"))
                .contains("find_in_set(#{deptId}, ancestors)");
    }

    private String load(String path) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertThat(stream).isNotNull();
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("无法加载 Mapper XML: " + path, exception);
        }
    }
}
