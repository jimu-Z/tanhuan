package com.ruoyi.talk.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.ruoyi.common.constant.Constants;

/** Resolves stored resource URLs within the configured upload root before deletion. */
public final class AttachmentPathUtils {
    private AttachmentPathUtils() { }

    public static void deleteWithinProfile(String profile, String resourcePath) throws IOException {
        Path file = resolveWithinProfile(profile, resourcePath);
        if (file != null) Files.delete(file);
    }

    public static Path resolveWithinProfile(String profile, String resourcePath) throws IOException {
        if (resourcePath == null) return null;
        String prefix = Constants.RESOURCE_PREFIX + "/";
        if (profile == null || resourcePath.indexOf('\\') >= 0 || !resourcePath.startsWith(prefix)) {
            throw new IOException("附件路径不属于上传目录");
        }
        Path root = Path.of(profile).toAbsolutePath().normalize();
        Path file = root.resolve(resourcePath.substring(prefix.length())).normalize();
        if (file.equals(root) || !file.startsWith(root)) {
            throw new IOException("附件路径超出上传目录");
        }
        if (!Files.exists(file)) return null;
        Path realFile = file.toRealPath();
        if (!realFile.startsWith(root.toRealPath()) || !Files.isRegularFile(realFile)) {
            throw new IOException("附件实际路径超出上传目录或不是文件");
        }
        return file;
    }
}
