package com.ruoyi.common.utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class UserNameUtilsTest
{
    @Test void matchesAllBrowserTrimWhitespace()
    {
        String chars = "\t\n\u000b\f\r \u00a0\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u2028\u2029\u202f\u205f\u3000\ufeff";
        for (char c : chars.toCharArray())
        {
            assertThat(UserNameUtils.normalize(c + "qa_name" + c)).isEqualTo("qa_name");
        }
    }

    @Test void preservesInternalWhitespaceAndNonTrimCharacters()
    {
        assertThat(UserNameUtils.normalize("  qa name\u3000part  ")).isEqualTo("qa name\u3000part");
        assertThat(UserNameUtils.normalize("\u0000qa\u0085")).isEqualTo("\u0000qa\u0085");
        assertThat(UserNameUtils.normalize("\u180eqa\u200b")).isEqualTo("\u180eqa\u200b");
    }

    @Test void handlesMissingAndBlankNames()
    {
        assertThat(UserNameUtils.normalize(null)).isEmpty();
        assertThat(UserNameUtils.normalize("")).isEmpty();
        assertThat(UserNameUtils.normalize(" \u3000\ufeff ")).isEmpty();
    }
}
