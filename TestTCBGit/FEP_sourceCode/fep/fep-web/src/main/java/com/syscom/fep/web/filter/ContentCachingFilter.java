package com.syscom.fep.web.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@WebFilter(urlPatterns = "/*", asyncSupported = true)
public class ContentCachingFilter extends OncePerRequestFilter {
    /**
     * Same contract as for {@code doFilter}, but guaranteed to be
     * just invoked once per request within a single request thread.
     * See {@link #shouldNotFilterAsyncDispatch()} for details.
     * <p>Provides HttpServletRequest and HttpServletResponse arguments instead of the
     * default ServletRequest and ServletResponse ones.
     *
     * @param request
     * @param response
     * @param filterChain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 2025-01-17 Richard modified
        // 只需要將request包成ContentCachingRequestWrapper, 以便WebAuditInterceptor中可以提取到request的body
        // 因為下載檔案的情況下, 會用到response的OutputStream, 因此不包裝response
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        // ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);
        // Execution request chain
        // filterChain.doFilter(req, resp);
        // Finally remember to respond to the client with the cached data.
        // resp.copyBodyToResponse();
        filterChain.doFilter(req, response);
    }
}
