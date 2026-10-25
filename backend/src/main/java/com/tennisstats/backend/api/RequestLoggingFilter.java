package com.tennisstats.backend.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Logs every API request when it comes in and when it is answered. Only the path is logged, not the
 * query string, so player names typed into the search don't end up in the log.
 */
@Component
@ConditionalOnWebApplication
class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String method = request.getMethod();
        String path = request.getRequestURI();
        log.info("Handling {} {}", method, path);
        long start = System.nanoTime();
        chain.doFilter(request, response);
        log.info("Handled {} {} -> {} in {} ms", method, path, response.getStatus(), (System.nanoTime() - start) / 1_000_000);
    }
}
