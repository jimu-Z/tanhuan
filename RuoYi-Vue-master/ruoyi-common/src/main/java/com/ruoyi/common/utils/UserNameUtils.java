package com.ruoyi.common.utils;

import java.util.regex.Pattern;

/** Username normalization shared with the browser login action's String.trim(). */
public final class UserNameUtils
{
    private static final Pattern EDGE_WHITESPACE = Pattern.compile("^[\\s\\p{Z}\\uFEFF]+|[\\s\\p{Z}\\uFEFF]+$");

    private UserNameUtils()
    {
    }

    public static String normalize(String userName)
    {
        return userName == null ? "" : EDGE_WHITESPACE.matcher(userName).replaceAll("");
    }
}
