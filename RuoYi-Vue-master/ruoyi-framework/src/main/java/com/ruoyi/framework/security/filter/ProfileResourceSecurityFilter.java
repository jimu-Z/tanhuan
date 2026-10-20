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
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)
    {
        return !request.getRequestURI().startsWith(Constants.RESOURCE_PREFIX + "/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        String uri = request.getRequestURI().toLowerCase(Locale.ROOT);
        response.setHeader("X-Content-Type-Options", "nosniff");
        if (uri.endsWith(".html") || uri.endsWith(".htm") || uri.endsWith(".xhtml")
                || uri.endsWith(".svg") || uri.endsWith(".js") || uri.endsWith(".mjs"))
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        chain.doFilter(request, response);
    }
}
