package com.ruoyi.talk.util;

import com.ruoyi.common.utils.poi.ExcelHandlerAdapter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

/** 富文本正文仅在 Excel 导出时转换为可阅读文本，导入保留原值。 */
public class TalkExcelContentHandler implements ExcelHandlerAdapter {
    @Override
    public Object format(Object value, String[] args, Cell cell, Workbook workbook) {
        if (cell == null) return value;
        if (value == null) return "";
        return TalkContentText.toPlainText(value.toString());
    }
}
