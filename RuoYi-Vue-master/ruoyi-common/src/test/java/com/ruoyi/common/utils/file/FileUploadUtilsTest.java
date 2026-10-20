package com.ruoyi.common.utils.file;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.ruoyi.common.config.RuoYiConfig;

class FileUploadUtilsTest
{
    @BeforeEach
    void setProfile()
    {
        RuoYiConfig config = new RuoYiConfig();
        config.setProfile("F:\\qa-runtime\\uploads");
    }

    @Test
    void returnsSingleProfileSeparatorForProfileRoot() throws Exception
    {
        assertEquals("/profile/2026/09/10/test.xlsx",
                FileUploadUtils.getPathFileName("F:\\qa-runtime\\uploads", "2026/09/10/test.xlsx"));
    }

    @Test
    void keepsNestedUploadDirectory() throws Exception
    {
        assertEquals("/profile/import/2026/09/10/test.xlsx",
                FileUploadUtils.getPathFileName("F:\\qa-runtime\\uploads\\import", "2026/09/10/test.xlsx"));
    }
}
