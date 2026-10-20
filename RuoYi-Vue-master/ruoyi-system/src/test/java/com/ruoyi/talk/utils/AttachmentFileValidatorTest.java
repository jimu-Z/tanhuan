package com.ruoyi.talk.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class AttachmentFileValidatorTest
{
    @Test
    void acceptsGif89aImage() throws Exception
    {
        byte[] image = Base64.getDecoder().decode(
                "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");
        MockMultipartFile file = new MockMultipartFile("file", "pixel.gif", "image/gif", image);

        assertThat(AttachmentFileValidator.hasExpectedContent(file, file.getContentType())).isTrue();
    }

    @Test
    void acceptsGif87aImage() throws Exception
    {
        byte[] image = Base64.getDecoder().decode(
                "R0lGODdhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUQAOw==");
        MockMultipartFile file = new MockMultipartFile("file", "pixel.gif", "image/gif", image);

        assertThat(AttachmentFileValidator.hasExpectedContent(file, file.getContentType())).isTrue();
    }

    @Test
    void rejectsGifHeaderAfterUnrelatedBytes() throws Exception
    {
        MockMultipartFile file = new MockMultipartFile("file", "fake.gif", "image/gif",
                new byte[] {'B', 'A', 'D', '1', '2', '3', 'G', 'I', 'F', '8', '9', 'a'});

        assertThat(AttachmentFileValidator.hasExpectedContent(file, file.getContentType())).isFalse();
    }

    @Test
    void rejectsTruncatedGifHeader() throws Exception
    {
        MockMultipartFile file = new MockMultipartFile("file", "short.gif", "image/gif",
                new byte[] {'G', 'I', 'F', '8', '9'});

        assertThat(AttachmentFileValidator.hasExpectedContent(file, file.getContentType())).isFalse();
    }

    @Test
    void rejectsHtmlPretendingToBeAnXlsx() throws Exception
    {
        MockMultipartFile file = new MockMultipartFile("file", "probe.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "<html><body>not an xlsx</body></html>".getBytes());

        assertThat(AttachmentFileValidator.hasExpectedContent(file, file.getContentType())).isFalse();
    }

    @Test
    void acceptsOpenXmlWorkbookStructure() throws Exception
    {
        MockMultipartFile file = new MockMultipartFile("file", "valid.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", workbookBytes());

        assertThat(AttachmentFileValidator.hasExpectedContent(file, file.getContentType())).isTrue();
    }

    private byte[] workbookBytes() throws Exception
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output))
        {
            zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zip.write("<Types/>".getBytes());
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("xl/workbook.xml"));
            zip.write("<workbook/>".getBytes());
            zip.closeEntry();
        }
        return output.toByteArray();
    }
}
