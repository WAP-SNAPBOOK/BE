package com.example.easybooking.auth;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.easybooking.auth.domain.AuthPrincipal;
import com.example.easybooking.auth.domain.AuthenticatedUser;
import com.example.easybooking.auth.domain.TempUser;
import com.example.easybooking.auth.util.JwtUtil;
import com.example.easybooking.common.filter.TraceIdFilter;
import com.example.easybooking.errors.errorcode.AuthErrorCode;
import com.example.easybooking.errors.exception.AuthException;
import com.example.easybooking.errors.response.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /**
     * Authenticates the incoming HTTP request using a JWT from the Authorization header and,
     * when valid, sets the resulting Authentication into the SecurityContext; if no token is
     * present the request proceeds unchanged, and on authentication failure a JSON error
     * response is written.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response used for error responses when authentication fails
     * @param filterChain the remaining filter chain to invoke when processing continues
     * @throws ServletException if an error occurs during request processing
     * @throws IOException      if an I/O error occurs while writing the error response
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String traceId = currentTraceId();
        Long userId = null;

        try {
            String token = resolveToken(request);
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            jwtUtil.validateToken(token);

            String role = jwtUtil.getRoleFromToken(token);
            AuthPrincipal principal;
            GrantedAuthority authority;

            // TEMP 토큰인지 확인
            if ("TEMP".equals(role)) {
                String providerId = jwtUtil.getSubjectFromToken(token);
                principal = new TempUser(providerId);
                authority = new SimpleGrantedAuthority("ROLE_TEMP");
                log.info("[{}][{} {}] TEMP 토큰 인증 성공 providerId={}",
                        traceId, request.getMethod(), request.getRequestURI(), providerId);
            } else {
                userId = jwtUtil.getUserIdFromToken(token);
                principal = new AuthenticatedUser(userId, role);
                authority = new SimpleGrantedAuthority("ROLE_" + role);
                log.info("[{}][{} {}] 정식 토큰 인증 성공 userId={} role={}",
                        traceId, request.getMethod(), request.getRequestURI(), userId, role);
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            Collections.singletonList(authority)
                    );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (AuthException ex) {
            log.warn("[{}][{} {}] JWT 인증 실패 code={} msg={} userId={}",
                    traceId,
                    request.getMethod(),
                    request.getRequestURI(),
                    ex.getErrorCode().name(),
                    ex.getMessage(),
                    userId,
                    ex);
            handleAuthException(response, ex);
        } catch (RuntimeException ex) {
            log.error("[{}][{} {}] JWT 인증 중 예상치 못한 오류 userId={}",
                    traceId,
                    request.getMethod(),
                    request.getRequestURI(),
                    userId,
                    ex);
            handleAuthException(response, new AuthException(AuthErrorCode.INTERNAL_SEVERVER_ERROR));
        }
    }

    /**
     * Writes a JSON error response for an authentication failure to the provided HTTP response.
     *
     * Sets the response status from the exception's error code and writes a JSON body containing
     * the error code, exception message, traceId, and timestamp.
     *
     * @param response the HTTP response to populate
     * @param ex the authentication exception whose error information will be used
     * @throws IOException if writing the response body fails
     */
    private void handleAuthException(HttpServletResponse response, AuthException ex) throws IOException {
        AuthErrorCode errorCode = (AuthErrorCode) ex.getErrorCode();
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.name(),
                ex.getMessage(),
                null,
                currentTraceId(),
                Instant.now(),
                null
        );

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    /**
     * Extracts a Bearer token from the HTTP Authorization header.
     *
     * @param request the HTTP request to read the Authorization header from
     * @return the token string after the "Bearer " prefix if present; `null` otherwise
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Retrieve the current trace identifier from the MDC, falling back to a default when absent.
     *
     * @return the trace id stored under TraceIdFilter.TRACE_ID_KEY, or {@code "no-trace"} if none is present
     */
    private String currentTraceId() {
        String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        return traceId != null ? traceId : "no-trace";
    }

}