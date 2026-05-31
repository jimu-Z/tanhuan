package com.ruoyi.talk.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TalkDocxServiceTest {

    @Test
    void stripTalkRecordSectionRemovesTalkRecordAndEverythingAfter() {
        String content = "【谈话目的】针对违纪行为进行教育，引导学生认识错误并改正。\n\n"
                + "【谈话要点】\n"
                + "1. 明确违纪事实，听取学生陈述\n"
                + "2. 解读相关校规校纪\n"
                + "3. 引导学生认识错误的性质和后果\n"
                + "4. 共同制定改正计划\n"
                + "5. 告知后续处理流程\n\n"
                + "【谈话记录】张三在2026年5月13日因考试作弊被处分，经过谈话后认识到错误。";

        String result = TalkDocxService.stripTalkRecord(content);

        assertThat(result)
                .contains("【谈话目的】")
                .contains("【谈话要点】")
                .doesNotContain("【谈话记录】")
                .doesNotContain("张三在2026年5月13日");
    }

    @Test
    void stripTalkRecordSectionTrimsTrailingWhitespace() {
        String content = "【谈话目的】了解学生学习困难。\n\n"
                + "【谈话要点】\n"
                + "1. 回顾学习情况\n"
                + "\n\n"
                + "【谈话记录】学生反馈内容。";

        String result = TalkDocxService.stripTalkRecord(content);

        assertThat(result).doesNotEndWith("\n").doesNotEndWith(" ");
    }

    @Test
    void stripTalkRecordSectionWithoutTalkRecordReturnsOriginal() {
        String content = "【谈话目的】了解学生学习困难。\n\n"
                + "【谈话要点】\n"
                + "1. 回顾学习情况";

        String result = TalkDocxService.stripTalkRecord(content);

        assertThat(result).isEqualTo(content);
    }

    @Test
    void stripTalkRecordSectionWithNullOrEmptyReturnsInput() {
        assertThat(TalkDocxService.stripTalkRecord(null)).isNull();
        assertThat(TalkDocxService.stripTalkRecord("")).isEmpty();
        assertThat(TalkDocxService.stripTalkRecord("   ")).isEqualTo("   ");
    }

    @Test
    void stripTalkRecordSectionPlainTextWithoutMarkersReturnsOriginal() {
        String content = "今天和学生谈了关于考试作弊的问题，学生态度良好。";

        String result = TalkDocxService.stripTalkRecord(content);

        assertThat(result).isEqualTo(content);
    }
}