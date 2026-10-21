package com.ruoyi.framework.security.filter;

import java.io.IOException;
import java.util.Locale;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.ruoyi.common.constant.Constants;

/**
 * Prevents public uploads from being interpreted as executable same-origin web content.
 */
@Component
public class ProfileResourceSecurityFilter extends OncePerRequestFilter
{
    @org.springframework.beans.factory.annotation.Autowired
    private com.ruoyi.talk.service.ITalkAttachmentService attachments;

    private String applicationPath(HttpServletRequest request)
    {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        for (int i = 0; i < 4 && path.contains("%"); i++) {
            path = java.net.URLDecoder.decode(path.replace("+", "%2B"), java.nio.charset.StandardCharsets.UTF_8);
        }
        if (path.contains("%") || path.indexOf('\\') >= 0) throw new IllegalArgumentException("Invalid resource path");
        return java.nio.file.Path.of(path.replaceAll("/+", "/")).normalize().toString().replace('\\', '/');
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)
    {
        try {
            String path = applicationPath(request);
            return !path.startsWith(Constants.RESOURCE_PREFIX + "/")
                    && !path.equals("/common/download/resource") && !path.equals("/common/download");
        } catch (IllegalArgumentException error) { return false; }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        String path;
        try { path = applicationPath(request); }
        catch (IllegalArgumentException error) { response.setStatus(404); return; }
        String uri = path.toLowerCase(Locale.ROOT);
        response.setHeader("X-Content-Type-Options", "nosniff");
        if (uri.endsWith(".html") || uri.endsWith(".htm") || uri.endsWith(".xhtml")
                || uri.endsWith(".svg") || uri.endsWith(".js") || uri.endsWith(".mjs"))
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String resource = path;
        if (resource.equals("/common/download/resource")) resource = request.getParameter("resource");
        else if (resource.equals("/common/download")) resource = "/profile/download/" + request.getParameter("fileName");
        if (attachments.isRegisteredResource(resource)) {
            response.setHeader("Cache-Control", "no-store");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        chain.doFilter(request, response);
    }
}
