package com.example.easybooking.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";
    private static final String REQUEST_PATH = "requestPath";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        MDC.put(TRACE_ID, traceId);
        MDC.put(REQUEST_PATH, request.getRequestURI());
        response.setHeader("X-Trace-Id", traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
            MDC.remove(REQUEST_PATH);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (StringUtils.hasText(traceId)) {
            return traceId;
        }
        return UUID.randomUUID().toString();
    }
}
