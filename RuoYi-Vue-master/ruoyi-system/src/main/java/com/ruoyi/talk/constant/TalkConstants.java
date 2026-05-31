package com.ruoyi.talk.constant;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TalkConstants {

    public static final Map<String, String> TAG_LABELS;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("thought_education", "思想理论教育和价值引领");
        m.put("party_class", "党团和班级建设");
        m.put("study_style", "学风建设");
        m.put("daily_affairs", "日常事务");
        m.put("mental_health", "心理健康教育与咨询");
        m.put("crisis_response", "危机事件应对");
        m.put("career_guidance", "职业规划与就业创业指导");
        TAG_LABELS = Collections.unmodifiableMap(m);
    }

    public static final Map<String, String> FOLLOWUP_STATUS_LABELS;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("pending", "待跟进");
        m.put("in_progress", "跟进中");
        m.put("completed", "已完成");
        m.put("none", "无需跟进");
        FOLLOWUP_STATUS_LABELS = Collections.unmodifiableMap(m);
    }

    public static final Map<String, String> ENROLLMENT_STATUS_LABELS;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("active", "在读");
        m.put("suspended", "休学");
        m.put("withdrawn", "退学");
        m.put("graduated", "毕业");
        ENROLLMENT_STATUS_LABELS = Collections.unmodifiableMap(m);
    }

    public static final Map<String, String> MENTAL_HEALTH_LABELS;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("normal", "正常");
        m.put("weekly_track", "周跟踪");
        m.put("monthly_track", "月跟踪");
        MENTAL_HEALTH_LABELS = Collections.unmodifiableMap(m);
    }

    public static final Map<String, String> POVERTY_LEVEL_LABELS;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("none", "无");
        m.put("general", "一般困难");
        m.put("difficult", "困难");
        m.put("severe", "特别困难");
        POVERTY_LEVEL_LABELS = Collections.unmodifiableMap(m);
    }

    public static final String DEFAULT_FEEDBACK = "无";
    public static final String DEFAULT_FOLLOWUP_PLAN = "待填写";
    public static final String DEFAULT_FOLLOWUP_STATUS = "pending";

    private TalkConstants() {}
}