package com.ruoyi.talk.util;

import org.springframework.web.util.HtmlUtils;

/** 将保存的富文本转换为导出用正文，不修改数据库中的格式。 */
public final class TalkContentText {
    private TalkContentText() {}

    public static String toPlainText(String content) {
        if (content == null) return null;
        String text = content.replaceAll("(?is)<!--.*?-->|<(script|style)\\b[^>]*>.*?</\\1\\s*>", "");
        text = text.replaceAll("(?i)<br\\s*/?>|</(?:p|div|li|h[1-6]|tr)\\s*>", "\n");
        text = text.replaceAll("(?s)</?[a-zA-Z][^>]*>", "");
        String decoded = HtmlUtils.htmlUnescape(text).replace('\u00a0', ' ');
        return text.equals(content) ? decoded : decoded.stripTrailing();
    }
}
