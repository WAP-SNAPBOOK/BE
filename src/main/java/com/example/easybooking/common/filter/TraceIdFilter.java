package com.example.easybooking.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_KEY = "traceId";

    /**
     * Assigns a request-scoped trace identifier, exposes it on the response, and ensures it is removed from the MDC after processing.
     *
     * The method generates a UUID (without dashes), stores it in the Mapped Diagnostic Context under {@code TRACE_ID_KEY},
     * sets the HTTP response header {@code X-Trace-Id} with the same value, proceeds with the filter chain, and always
     * removes the trace id from the MDC in a finally block to prevent leakage across requests.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response where the {@code X-Trace-Id} header will be set
     * @param filterChain the filter chain to continue request processing
     * @throws ServletException if the downstream filter or servlet throws a ServletException
     * @throws IOException      if the downstream filter or servlet throws an IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString().replace("-", "");
        MDC.put(TRACE_ID_KEY, traceId);
        response.setHeader("X-Trace-Id", traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
