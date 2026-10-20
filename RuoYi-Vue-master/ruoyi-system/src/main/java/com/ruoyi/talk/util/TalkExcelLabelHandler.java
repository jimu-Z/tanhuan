package com.ruoyi.talk.util;

import java.util.Map;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.poi.ExcelHandlerAdapter;
import com.ruoyi.talk.constant.TalkConstants;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

/** 仅导出时翻译编码；ExcelUtil 导入调用的 cell 为 null，必须保留原始值。 */
public class TalkExcelLabelHandler implements ExcelHandlerAdapter {
    private static final Map<String, Map<String, String>> LABELS = Map.of(
            "gender", Map.of("0", "男", "1", "女", "2", "未知"),
            "political_status", TalkConstants.POLITICAL_STATUS_LABELS,
            "enrollment_status", TalkConstants.ENROLLMENT_STATUS_LABELS,
            "mental_health_status", TalkConstants.MENTAL_HEALTH_LABELS,
            "poverty_level", TalkConstants.POVERTY_LEVEL_LABELS,
            "followup_status", TalkConstants.FOLLOWUP_STATUS_LABELS,
            "teacher_role", Map.of("talk_teacher", "老师", "college_admin", "院管理员", "school_admin", "校管理员", "admin", "管理员"));

    @Override
    public Object format(Object value, String[] args, Cell cell, Workbook workbook) {
        if (cell == null) return value;
        if (value == null || value.toString().trim().isEmpty()) return "";
        String code = value.toString().trim();
        String type = args[0];
        if (!"teacher_role".equals(type) && !"gender".equals(type)) {
            try {
                String label = DictUtils.getDictLabel(type, code);
                if (label != null && !label.trim().isEmpty()) return label;
            } catch (RuntimeException ignored) { /* 字典缓存暂不可用时仍输出系统定义的中文 */ }
        }
        Map<String, String> labels = LABELS.get(type);
        if (labels != null && labels.containsKey(code)) return labels.get(code);
        return code.matches(".*[\\u3400-\\u9fff].*") ? code : "未知状态";
    }
}
