package com.ruoyi.talk.utils;

/** Keep user-visible Chinese names while removing archive directory components. */
public final class ArchiveEntryNames {
    private ArchiveEntryNames() { }
    public static String basename(String name) {
        String value = name == null ? "" : name.replace('\\', '/');
        value = value.substring(value.lastIndexOf('/') + 1).trim();
        value = value.replaceAll("[\\x00-\\x1f\\x7f<>:\"|?*]", "_");
        return value.isEmpty() || ".".equals(value) || "..".equals(value) ? "未命名" : value;
    }
}
