package com.ruoyi.talk.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.ruoyi.common.constant.Constants;

/** Resolves stored resource URLs within the configured upload root before deletion. */
public final class AttachmentPathUtils {
    private AttachmentPathUtils() { }

    /** Canonical file identity for denying aliases of registered private attachments. */
    public static Path resourceIdentity(String profile, String resourcePath) throws IOException {
        if (resourcePath == null || profile == null) throw new IOException("附件路径为空");
        String decoded = resourcePath;
        for (int i = 0; i < 4 && decoded.contains("%"); i++) {
            String next = java.net.URLDecoder.decode(decoded.replace("+", "%2B"), java.nio.charset.StandardCharsets.UTF_8);
            if (next.equals(decoded)) break;
            decoded = next;
        }
        if (decoded.contains("%") || decoded.indexOf('\\') >= 0 || decoded.indexOf('\0') >= 0) throw new IOException("附件路径无效");
        decoded = decoded.replaceAll("/+", "/");
        String prefix = Constants.RESOURCE_PREFIX + "/";
        if (!decoded.startsWith(prefix)) throw new IOException("附件路径不属于上传目录");
        Path root = Path.of(profile).toAbsolutePath().normalize();
        Path candidate = root.resolve(decoded.substring(prefix.length())).normalize();
        if (candidate.equals(root) || !candidate.startsWith(root)) throw new IOException("附件路径越界");
        if (Files.exists(candidate)) {
            candidate = candidate.toRealPath();
            if (!candidate.startsWith(root.toRealPath())) throw new IOException("附件实际路径越界");
        }
        return candidate;
    }

    public static void deleteWithinProfile(String profile, String resourcePath) throws IOException {
        Path file = resolveWithinProfile(profile, resourcePath);
        if (file != null) Files.delete(file);
    }

    public static Path resolveWithinProfile(String profile, String resourcePath) throws IOException {
        if (resourcePath == null) return null;
        Path file = resourceIdentity(profile, resourcePath);
        if (!Files.exists(file)) return null;
        if (!Files.isRegularFile(file)) throw new IOException("附件不是文件");
        return file;
    }
}
