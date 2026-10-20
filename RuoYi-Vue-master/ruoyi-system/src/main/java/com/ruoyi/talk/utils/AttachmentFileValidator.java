package com.ruoyi.talk.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.web.multipart.MultipartFile;

/** Validates the actual bytes of files accepted by the talk attachment API. */
public final class AttachmentFileValidator
{
    private AttachmentFileValidator()
    {
    }

    public static boolean hasExpectedContent(MultipartFile file, String contentType) throws IOException
    {
        if (contentType == null)
        {
            return false;
        }
        String normalizedType = contentType.toLowerCase(Locale.ROOT);
        try (InputStream input = file.getInputStream())
        {
            return switch (normalizedType)
            {
                case "image/jpeg" -> hasPrefix(input, 0xFF, 0xD8, 0xFF);
                case "image/png" -> hasPrefix(input, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
                case "image/gif" -> isGif(input);
                case "image/webp" -> isWebp(input);
                case "application/pdf" -> hasPrefix(input, "%PDF-".getBytes());
                case "application/msword", "application/vnd.ms-excel" -> hasPrefix(input,
                        0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1);
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                    isOpenXml(input, "word/");
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ->
                    isOpenXml(input, "xl/");
                default -> false;
            };
        }
    }

    private static boolean hasPrefix(InputStream input, int... expected) throws IOException
    {
        byte[] bytes = input.readNBytes(expected.length);
        if (bytes.length != expected.length)
        {
            return false;
        }
        for (int index = 0; index < expected.length; index++)
        {
            if ((bytes[index] & 0xFF) != expected[index])
            {
                return false;
            }
        }
        return true;
    }

    private static boolean hasPrefix(InputStream input, byte[] expected) throws IOException
    {
        byte[] bytes = input.readNBytes(expected.length);
        return java.util.Arrays.equals(bytes, expected);
    }

    private static boolean isGif(InputStream input) throws IOException
    {
        byte[] bytes = input.readNBytes(6);
        return bytes.length == 6 && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F'
                && bytes[3] == '8' && (bytes[4] == '7' || bytes[4] == '9') && bytes[5] == 'a';
    }

    private static boolean isWebp(InputStream input) throws IOException
    {
        byte[] bytes = input.readNBytes(12);
        return bytes.length == 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
    }

    private static boolean isOpenXml(InputStream input, String requiredDirectory) throws IOException
    {
        try (ZipInputStream zip = new ZipInputStream(input))
        {
            boolean hasContentTypes = false;
            boolean hasRequiredDirectory = false;
            int entryCount = 0;
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null && entryCount++ < 1000)
            {
                String name = entry.getName();
                hasContentTypes |= "[Content_Types].xml".equals(name);
                hasRequiredDirectory |= name.startsWith(requiredDirectory);
                if (hasContentTypes && hasRequiredDirectory)
                {
                    return true;
                }
            }
            return false;
        }
    }
}
