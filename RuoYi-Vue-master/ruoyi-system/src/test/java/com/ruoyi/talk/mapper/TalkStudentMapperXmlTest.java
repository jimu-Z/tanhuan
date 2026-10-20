package com.ruoyi.talk.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

/** Mapper SQL 安全性与学院数据边界回归测试。 */
class TalkStudentMapperXmlTest {
    private static final String MAPPER_XML_PATH = "/mapper/talk/TalkStudentMapper.xml";

    @Test
    void mapperShouldNotContainStringInjectedDataScope() {
        assertThat(loadMapperXml()).doesNotContain("${params.dataScope}");
    }

    @Test
    void studentQueriesShouldUseParameterizedCollegeScope() {
        String xml = loadMapperXml();
        assertThat(xml)
                .contains("params.collegeDeptId")
                .contains("#{params.collegeDeptId}")
                .doesNotContain("params.counselorCode")
                .doesNotContain("params.secretaryDeptId");
    }

    @Test
    void statisticsQueriesShouldUseCollegeScope() {
        assertThat(loadMapperXml())
                .contains("#{collegeDeptId}")
                .doesNotContain("counselorUsername")
                .doesNotContain("secretaryDeptId");
    }

    private String loadMapperXml() {
        try (InputStream is = getClass().getResourceAsStream(MAPPER_XML_PATH)) {
            assertThat(is).isNotNull();
            Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
            String content = scanner.useDelimiter("\\A").next();
            scanner.close();
            return content;
        } catch (Exception e) {
            throw new RuntimeException("无法加载 Mapper XML: " + MAPPER_XML_PATH, e);
        }
    }
}
